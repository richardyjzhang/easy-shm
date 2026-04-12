"""从 MySQL 导出测点配置到 sensor_config.json，供 mqtt_mock.py 离线使用。

使用方式:
    conda activate eash-shm
    python export_points.py
    python export_points.py --host 127.0.0.1 --port 3306 --user root --password 123456
"""

import argparse
import json
import os
import sys
from pathlib import Path

try:
    import pymysql
except ImportError:
    sys.exit("pymysql 未安装，请执行: pip install pymysql")

from dotenv import load_dotenv

SCRIPT_DIR = Path(__file__).resolve().parent


def parse_args():
    load_dotenv(SCRIPT_DIR / ".env")
    p = argparse.ArgumentParser(description="导出测点配置")
    p.add_argument("--host", default=os.getenv("MYSQL_HOST", "127.0.0.1"))
    p.add_argument("--port", default=int(os.getenv("MYSQL_PORT", "3306")), type=int)
    p.add_argument("--user", default=os.getenv("MYSQL_USER", "root"))
    p.add_argument("--password", default=os.getenv("MYSQL_PASSWORD", ""))
    p.add_argument("--database", default=os.getenv("MYSQL_DATABASE", "easy_shm"))
    p.add_argument("-o", "--output", default=str(SCRIPT_DIR / "sensor_config.json"))
    return p.parse_args()


SENSOR_PROFILES = {
    "结构表面温度": {"unit": "℃",    "baseline": 20, "drift": 8,    "noise": 0.3,  "min": -10, "max": 40},
    "环境温度":     {"unit": "℃",    "baseline": 18, "drift": 8,    "noise": 0.3,  "min": -15, "max": 35},
    "风速":         {"unit": "m/s",  "baseline": 5,  "drift": 3,    "noise": 0.5,  "min": 0,   "max": 15},
    "风向":         {"unit": "°",    "baseline": 180,"drift": 30,   "noise": 5,    "min": 0,   "max": 360},
    "车辆总重":     {"unit": "kN",   "baseline": 80, "drift": 40,   "noise": 5,    "min": 0,   "max": 200},
    "静态应变":     {"unit": "με",   "baseline": 0,  "drift": 50,   "noise": 5,    "min": -500,"max": 500},
    "加速度":       {"unit": "m/s²", "baseline": 0,  "drift": 0.3,  "noise": 0.2,  "min": -2,  "max": 2},
    "竖向挠度":     {"unit": "mm",   "baseline": 0,  "drift": 3,    "noise": 0.5,  "min": -20, "max": 20},
    "裂缝宽度":     {"unit": "mm",   "baseline": 0.1,"drift": 0.02, "noise": 0.005,"min": 0,   "max": 0.2},
    "不均匀沉降":   {"unit": "mm",   "baseline": 0,  "drift": 1,    "noise": 0.2,  "min": -10, "max": 10},
}


def main():
    args = parse_args()
    conn = pymysql.connect(
        host=args.host, port=args.port,
        user=args.user, password=args.password,
        database=args.database, charset="utf8mb4",
    )
    cursor = conn.cursor()

    cursor.execute("""
        SELECT
            mp.id          AS point_id,
            s.id           AS structure_id,
            s.name         AS structure_name,
            mvt.name       AS value_type_name
        FROM monitor_point mp
        JOIN structure           s   ON mp.structure_id  = s.id
        JOIN monitor_value_type  mvt ON mp.value_type_id = mvt.id
        ORDER BY s.id, mvt.id, mp.id
    """)

    rows = cursor.fetchall()
    conn.close()

    structures: dict = {}
    for point_id, structure_id, structure_name, value_type_name in rows:
        sid = str(int(structure_id))
        if sid not in structures:
            structures[sid] = {"name": structure_name, "points": []}
        structures[sid]["points"].append({
            "point_id": int(point_id),
            "value_type": value_type_name,
        })

    config = {
        "sensor_profiles": SENSOR_PROFILES,
        "structures": structures,
    }

    with open(args.output, "w", encoding="utf-8") as f:
        json.dump(config, f, ensure_ascii=False, indent=2)

    total = sum(len(s["points"]) for s in structures.values())
    print(f"导出完成: {len(structures)} 个结构物, {total} 个测点 -> {args.output}")


if __name__ == "__main__":
    main()
