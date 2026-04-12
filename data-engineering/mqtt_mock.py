"""模拟边缘侧设备，为每个测点以 1 Hz 向 MQTT 上报监测数据。

读取 sensor_config.json（由 export_points.py 从 MySQL 导出），
根据传感器类型生成物理上合理的模拟值，每个结构物发往独立 topic。
"""

import json
import math
import os
import random
import ssl
import time
from datetime import datetime
from pathlib import Path

import paho.mqtt.client as mqtt
from dotenv import load_dotenv

load_dotenv()

BROKER = os.getenv("MQTT_BROKER", "localhost")
PORT = int(os.getenv("MQTT_PORT", "8883"))
USERNAME = os.getenv("MQTT_USERNAME", "")
PASSWORD = os.getenv("MQTT_PASSWORD", "")
CLIENT_ID = f"edge_mock_{random.randint(1000, 9999)}"
TOPIC_PREFIX = os.getenv("MQTT_TOPIC_PREFIX", "easy-shm")

CONFIG_PATH = Path(__file__).resolve().parent / "sensor_config.json"

# ---------------------------------------------------------------------------
# 数据生成器
# ---------------------------------------------------------------------------

def clamp(val: float, lo: float, hi: float) -> float:
    return max(lo, min(hi, val))


def hour_of_day() -> float:
    """当前时刻在一天中的小数小时 (0~24)。"""
    now = datetime.now()
    return now.hour + now.minute / 60 + now.second / 3600


def diurnal_offset(amplitude: float) -> float:
    """日周期正弦偏移，14 时最高、02 时最低。"""
    h = hour_of_day()
    return amplitude * math.sin(2 * math.pi * (h - 8) / 24)


class SensorState:
    """单个测点的运行状态，每次 tick 生成下一个值。"""

    __slots__ = ("value_type", "profile", "current")

    def __init__(self, value_type: str, profile: dict):
        self.value_type = value_type
        self.profile = profile
        lo, hi = profile["min"], profile["max"]
        mid = profile["baseline"]
        self.current = mid + random.uniform(-profile["noise"] * 3, profile["noise"] * 3)
        self.current = clamp(self.current, lo, hi)

    def tick(self) -> float:
        p = self.profile
        lo, hi = p["min"], p["max"]
        vt = self.value_type

        if vt in ("结构表面温度", "环境温度"):
            target = p["baseline"] + diurnal_offset(p["drift"])
            self.current += (target - self.current) * 0.01 + random.gauss(0, p["noise"])

        elif vt == "风速":
            # 缓慢随机游走 + 偶发阵风
            self.current += random.gauss(0, p["noise"])
            if random.random() < 0.02:
                self.current += random.uniform(2, 5)
            self.current += (p["baseline"] - self.current) * 0.005

        elif vt == "风向":
            self.current += random.gauss(0, p["noise"])
            if self.current < 0:
                self.current += 360
            elif self.current >= 360:
                self.current -= 360
            return round(self.current, 1)

        elif vt == "车辆总重":
            # 脉冲式：大部分时间基线附近，偶尔有车辆过桥
            if random.random() < 0.05:
                self.current = random.uniform(30, 180)
            else:
                self.current += (p["baseline"] - self.current) * 0.1 + random.gauss(0, p["noise"])

        elif vt == "静态应变":
            self.current += random.gauss(0, p["noise"])
            self.current += (p["baseline"] - self.current) * 0.002

        elif vt == "加速度":
            # 振动类：以零为中心的白噪声
            self.current = random.gauss(0, p["noise"])

        elif vt == "竖向挠度":
            self.current += random.gauss(0, p["noise"])
            self.current += (p["baseline"] - self.current) * 0.003

        elif vt == "裂缝宽度":
            self.current += random.gauss(0, p["noise"])
            self.current += (p["baseline"] - self.current) * 0.001

        elif vt == "不均匀沉降":
            self.current += random.gauss(0, p["noise"])
            self.current += (p["baseline"] - self.current) * 0.002

        else:
            self.current += random.gauss(0, p["noise"])

        self.current = clamp(self.current, lo, hi)
        return round(self.current, 3)


# ---------------------------------------------------------------------------
# 主逻辑
# ---------------------------------------------------------------------------

def load_config() -> dict:
    with open(CONFIG_PATH, encoding="utf-8") as f:
        return json.load(f)


def main():
    config = load_config()
    profiles = config["sensor_profiles"]
    structures = config["structures"]

    # 为每个测点创建状态实例
    states: dict[int, SensorState] = {}
    for _sid, sinfo in structures.items():
        for pt in sinfo["points"]:
            pid = pt["point_id"]
            vt = pt["value_type"]
            states[pid] = SensorState(vt, profiles[vt])

    total_points = len(states)
    total_structures = len(structures)
    print(f"Loaded {total_points} points across {total_structures} structures")

    client = mqtt.Client(client_id=CLIENT_ID, protocol=mqtt.MQTTv5)
    client.tls_set(cert_reqs=ssl.CERT_NONE)
    client.tls_insecure_set(True)
    if USERNAME:
        client.username_pw_set(USERNAME, PASSWORD)
    client.connect(BROKER, PORT)
    client.loop_start()

    print(f"Connected to {BROKER}:{PORT}, topic prefix: {TOPIC_PREFIX}")

    try:
        while True:
            now = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]

            for sid, sinfo in structures.items():
                payload = []
                for pt in sinfo["points"]:
                    pid = pt["point_id"]
                    val = states[pid].tick()
                    payload.append({"ts": now, "point_id": pid, "val": val})

                topic = f"{TOPIC_PREFIX}/{sid}"
                client.publish(topic, json.dumps(payload), qos=1)

            time.sleep(1)
    except KeyboardInterrupt:
        print("\nStopped.")
    finally:
        client.loop_stop()
        client.disconnect()


if __name__ == "__main__":
    main()
