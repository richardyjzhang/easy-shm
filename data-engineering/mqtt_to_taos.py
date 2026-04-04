"""订阅 MQTT 消息，将原始监测数据通过 REST API 写入 TDengine。"""

import json
import os
import random
import ssl

import paho.mqtt.client as mqtt
import taosrest
from dotenv import load_dotenv

load_dotenv()

MQTT_BROKER = os.getenv("MQTT_BROKER", "localhost")
MQTT_PORT = int(os.getenv("MQTT_PORT", "8883"))
MQTT_USERNAME = os.getenv("MQTT_USERNAME", "")
MQTT_PASSWORD = os.getenv("MQTT_PASSWORD", "")
MQTT_CLIENT_ID = f"taos_consumer_{random.randint(1000, 9999)}"
MQTT_SUBSCRIBE = os.getenv("MQTT_SUBSCRIBE", "easy-shm/+")

TAOS_URL = os.getenv("TAOS_URL", "http://localhost:6041")
TAOS_USER = os.getenv("TAOS_USER", "root")
TAOS_PASSWORD = os.getenv("TAOS_PASSWORD", "taosdata")
TAOS_DB = os.getenv("TAOS_DB", "origin_data")

conn = taosrest.connect(url=TAOS_URL, user=TAOS_USER, password=TAOS_PASSWORD)

created_tables: set[int] = set()


def exec_sql(sql: str):
    try:
        conn.execute(sql)
    except Exception as e:
        print(f"[taos] ERROR: {e}  SQL: {sql}")


def ensure_table(point_id: int):
    if point_id in created_tables:
        return
    sql = (
        f"CREATE TABLE IF NOT EXISTS {TAOS_DB}.origin_data_{point_id} "
        f"USING {TAOS_DB}.origin_data TAGS ({point_id})"
    )
    exec_sql(sql)
    created_tables.add(point_id)


def on_connect(client, userdata, flags, reason_code, properties):
    print(f"Connected to MQTT, subscribing to {MQTT_SUBSCRIBE}")
    client.subscribe(MQTT_SUBSCRIBE, qos=1)


def on_message(client, userdata, msg):
    try:
        records = json.loads(msg.payload)
    except json.JSONDecodeError as e:
        print(f"[mqtt] bad payload: {e}")
        return

    structure_id = msg.topic.split("/")[-1]

    for r in records:
        ensure_table(r["point_id"])
        sql = (
            f"INSERT INTO {TAOS_DB}.origin_data_{r['point_id']} "
            f"VALUES ({r['ts']}, {r['val']})"
        )
        exec_sql(sql)

    point_ids = [r["point_id"] for r in records]
    print(f"  [{structure_id}] wrote {len(records)} record(s) for points {point_ids}")


def main():
    client = mqtt.Client(client_id=MQTT_CLIENT_ID, protocol=mqtt.MQTTv5)
    client.tls_set(cert_reqs=ssl.CERT_NONE)
    client.tls_insecure_set(True)
    if MQTT_USERNAME:
        client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)

    client.on_connect = on_connect
    client.on_message = on_message

    client.connect(MQTT_BROKER, MQTT_PORT)

    print(f"Connecting to {MQTT_BROKER}:{MQTT_PORT} ...")

    try:
        client.loop_forever()
    except KeyboardInterrupt:
        print("\nStopped.")
    finally:
        client.disconnect()


if __name__ == "__main__":
    main()
