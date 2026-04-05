"""模拟边缘侧设备，每秒向 MQTT 上报一条监测数据。

每分钟从 Open-Meteo 获取华盛顿 DC 的实时温湿度作为基准，
每秒在基准值上叠加 ±1℃ / ±1% 的随机浮动后发布。
"""

import json
import os
import random
import ssl
import threading
import time
from datetime import datetime

import paho.mqtt.client as mqtt
import requests
from dotenv import load_dotenv

load_dotenv()

BROKER = os.getenv("MQTT_BROKER", "localhost")
PORT = int(os.getenv("MQTT_PORT", "8883"))
USERNAME = os.getenv("MQTT_USERNAME", "")
PASSWORD = os.getenv("MQTT_PASSWORD", "")
CLIENT_ID = f"edge_mock_{random.randint(1000, 9999)}"
TOPIC = os.getenv("MQTT_TOPIC", "easy-shm/2")

LATITUDE = float(os.getenv("WEATHER_LATITUDE", "38.8951"))
LONGITUDE = float(os.getenv("WEATHER_LONGITUDE", "-77.0364"))
WEATHER_URL = (
    f"https://api.open-meteo.com/v1/forecast"
    f"?latitude={LATITUDE}&longitude={LONGITUDE}"
    f"&current=temperature_2m,relative_humidity_2m"
)

DRIFT = float(os.getenv("DRIFT", "1.0"))
FETCH_INTERVAL = int(os.getenv("WEATHER_FETCH_INTERVAL", "60"))

base_temp: float = 25.0
base_humi: float = 60.0
lock = threading.Lock()


def fetch_weather():
    """从 Open-Meteo 获取 DC 当前温湿度，更新基准值。"""
    global base_temp, base_humi
    while True:
        try:
            resp = requests.get(WEATHER_URL, timeout=10)
            data = resp.json().get("current", {})
            with lock:
                base_temp = data.get("temperature_2m", base_temp)
                base_humi = data.get("relative_humidity_2m", base_humi)
            # print(f"[weather] DC temp={base_temp}℃  humi={base_humi}%")
        except Exception as e:
            print(f"[weather] fetch failed: {e}")
        time.sleep(FETCH_INTERVAL)


def main():
    # 先拉一次天气数据再开始发送
    threading.Thread(target=fetch_weather, daemon=True).start()
    time.sleep(2)

    client = mqtt.Client(client_id=CLIENT_ID, protocol=mqtt.MQTTv5)
    client.tls_set(cert_reqs=ssl.CERT_NONE)
    client.tls_insecure_set(True)
    if USERNAME:
        client.username_pw_set(USERNAME, PASSWORD)
    client.connect(BROKER, PORT)
    client.loop_start()

    print(f"Connected to {BROKER}:{PORT}, publishing to {TOPIC}")

    try:
        while True:
            with lock:
                temp = base_temp
                humi = base_humi

            now = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]
            payload = [
                {"ts": now, "point_id": pid, "val": round(base + random.uniform(-DRIFT, DRIFT), 2)}
                for pid, base in [(1, temp), (2, humi)]
            ]
            client.publish(TOPIC, json.dumps(payload), qos=1)
            # print(f"  {payload}")

            time.sleep(1)
    except KeyboardInterrupt:
        print("\nStopped.")
    finally:
        client.loop_stop()
        client.disconnect()


if __name__ == "__main__":
    main()
