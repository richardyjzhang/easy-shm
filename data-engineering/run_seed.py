"""
Easy-SHM 模拟数据灌入脚本 (纯 Python)
所有数据归属 "联邦监测中心" (department id=1)

使用方式:
    conda activate eash-shm
    python run_seed.py
    python run_seed.py --host 127.0.0.1 --port 3306 --user root --password secret
"""

import argparse
import os
import random
import sys
from datetime import date, timedelta
from pathlib import Path

try:
    import pymysql
except ImportError:
    sys.exit("pymysql 未安装，请执行: pip install pymysql")

from dotenv import load_dotenv

SCRIPT_DIR = Path(__file__).resolve().parent
DEPT_ID = 1
SENSORS_PER_CONFIG = 5


def parse_args():
    load_dotenv(SCRIPT_DIR / ".env")
    p = argparse.ArgumentParser(description="灌入模拟数据")
    p.add_argument("--host", default=os.getenv("MYSQL_HOST", "127.0.0.1"))
    p.add_argument("--port", default=int(os.getenv("MYSQL_PORT", "3306")), type=int)
    p.add_argument("--user", default=os.getenv("MYSQL_USER", "root"))
    p.add_argument("--password", default=os.getenv("MYSQL_PASSWORD", ""))
    p.add_argument("--database", default=os.getenv("MYSQL_DATABASE", "easy_shm"))
    return p.parse_args()


def insert(cursor, table, data: dict) -> int:
    cols = ", ".join(f"`{k}`" for k in data)
    placeholders = ", ".join(["%s"] * len(data))
    cursor.execute(
        f"INSERT INTO `{table}` ({cols}) VALUES ({placeholders})",
        tuple(data.values()),
    )
    return cursor.lastrowid


# ── 数据定义 ──────────────────────────────────────────────

MONITOR_INDEXES = [
    # (type, name, code)
    (1, "温度",     "ENV-TEMP"),
    (1, "风",       "ENV-WIND"),
    (2, "车辆荷载", "ACT-VEH"),
    (3, "应变",     "RSP-STRAIN"),
    (3, "振动",     "RSP-VIB"),
    (3, "挠度",     "RSP-DEFL"),
    (4, "裂缝",     "CHG-CRACK"),
    (4, "沉降",     "CHG-SETTL"),
]

VALUE_TYPES = [
    # (所属监测项name, 监测内容name, unit)
    ("温度",     "结构表面温度", "℃"),
    ("温度",     "环境温度",     "℃"),
    ("风",       "风速",         "m/s"),
    ("风",       "风向",         "°"),
    ("车辆荷载", "车辆总重",     "kN"),
    ("应变",     "静态应变",     "με"),
    ("振动",     "加速度",       "m/s²"),
    ("挠度",     "竖向挠度",     "mm"),
    ("裂缝",     "裂缝宽度",     "mm"),
    ("沉降",     "不均匀沉降",   "mm"),
]

STRUCTURES = [
    # (name, type, lon, lat, remark)
    ("金门大桥",           "桥梁", -122.4786,  37.8199, "Golden Gate Bridge, San Francisco, CA"),
    ("布鲁克林大桥",       "桥梁",  -73.9969,  40.7061, "Brooklyn Bridge, New York, NY"),
    ("胡佛水坝",           "其他", -114.7377,  36.0160, "Hoover Dam, NV/AZ border"),
    ("帝国大厦",           "民建",  -73.9857,  40.7484, "Empire State Building, New York, NY"),
    ("五角大楼",           "工建",  -77.0558,  38.8719, "The Pentagon, Arlington, VA"),
    ("切萨皮克湾大桥隧道", "桥梁",  -76.0834,  37.0350, "Chesapeake Bay Bridge-Tunnel, VA"),
    ("威利斯大厦",         "民建",  -87.6359,  41.8789, "Willis Tower (Sears Tower), Chicago, IL"),
    ("诺福克海军基地码头", "工建",  -76.3316,  36.9470, "Naval Station Norfolk, VA"),
    ("自由女神像基座",     "民建",  -74.0445,  40.6892, "Statue of Liberty, New York, NY"),
    ("科罗拉多河大桥",     "桥梁", -114.7381,  36.0112, "Mike O'Callaghan-Pat Tillman Memorial Bridge, NV/AZ"),
]

DEVICE_MODELS = [
    # (manufacturer, model, range, principle, sensitivity, accuracy, remark)
    ("Campbell Scientific", "CS-T100",     "-40~80℃",   "热电偶",     "0.01℃",   "±0.1℃",   "结构表面/环境温度传感器"),
    ("Gill Instruments",    "WindSonic-2D","0~60m/s",    "超声波",     "0.01m/s",  "±2%",     "二维超声风速风向传感器"),
    ("Kistler",             "Lineas-WIM",  "0~500kN",    "石英压电",   "0.1kN",    "±5%",     "动态称重传感器"),
    ("Tokyo Measuring",     "TML-FGSS",    "±3000με",    "应变片",     "1με",      "±0.5%FS", "静态应变计"),
    ("PCB Piezotronics",    "PCB-393B04",  "±5g",        "IEPE压电",   "1000mV/g", "±1%",     "低频加速度传感器"),
    ("Keyence",             "IL-S065",     "0~150mm",    "激光位移",   "0.01mm",   "±0.05mm", "竖向挠度位移传感器"),
    ("Epsilon Technology",  "EPS-CG50",    "0~50mm",     "LVDT",       "0.001mm",  "±0.02mm", "裂缝宽度监测仪"),
    ("Leica Geosystems",    "Leica-NA2",   "±100mm",     "液压静力水准","0.01mm",  "±0.1mm",  "沉降监测传感器"),
]

# 监测内容 → 设备型号 model 名称
VTYPE_TO_DEVICE_MODEL = {
    "结构表面温度": "CS-T100",
    "环境温度":     "CS-T100",
    "风速":         "WindSonic-2D",
    "风向":         "WindSonic-2D",
    "车辆总重":     "Lineas-WIM",
    "静态应变":     "TML-FGSS",
    "加速度":       "PCB-393B04",
    "竖向挠度":     "IL-S065",
    "裂缝宽度":     "EPS-CG50",
    "不均匀沉降":   "Leica-NA2",
}

# 每个结构物选 5 项监测内容 (用监测内容 name 引用)
STRUCTURE_CONFIGS = {
    "金门大桥":           ["结构表面温度", "风速", "风向", "加速度", "竖向挠度"],
    "布鲁克林大桥":       ["环境温度", "静态应变", "加速度", "裂缝宽度", "竖向挠度"],
    "胡佛水坝":           ["结构表面温度", "环境温度", "静态应变", "裂缝宽度", "不均匀沉降"],
    "帝国大厦":           ["环境温度", "风速", "加速度", "裂缝宽度", "不均匀沉降"],
    "五角大楼":           ["结构表面温度", "环境温度", "静态应变", "裂缝宽度", "不均匀沉降"],
    "切萨皮克湾大桥隧道": ["风速", "风向", "车辆总重", "加速度", "竖向挠度"],
    "威利斯大厦":         ["环境温度", "风速", "风向", "加速度", "不均匀沉降"],
    "诺福克海军基地码头": ["结构表面温度", "静态应变", "裂缝宽度", "不均匀沉降", "加速度"],
    "自由女神像基座":     ["环境温度", "风速", "加速度", "裂缝宽度", "不均匀沉降"],
    "科罗拉多河大桥":     ["结构表面温度", "风速", "车辆总重", "静态应变", "竖向挠度"],
}

# 蓝/黄/红 阈值 (lower, upper) × 3 级
THRESHOLD_MAP = {
    "结构表面温度": (-10, 40, -20, 55, -30, 70),
    "环境温度":     (-15, 35, -25, 45, -35, 55),
    "风速":         (0, 15, 15, 30, 30, 45),
    "风向":         (0, 360, 0, 360, 0, 360),
    "车辆总重":     (0, 200, 200, 350, 350, 500),
    "静态应变":     (-500, 500, -1500, 1500, -2500, 2500),
    "加速度":       (-2, 2, -3.5, 3.5, -5, 5),
    "竖向挠度":     (-20, 20, -40, 40, -60, 60),
    "裂缝宽度":     (0, 0.2, 0.2, 0.5, 0.5, 1.0),
    "不均匀沉降":   (-10, 10, -20, 20, -30, 30),
}


# ── 主逻辑 ──────────────────────────────────────────────

def main():
    args = parse_args()
    conn = pymysql.connect(
        host=args.host, port=args.port,
        user=args.user, password=args.password,
        database=args.database, charset="utf8mb4", autocommit=True,
    )
    cursor = conn.cursor()
    today = date.today()

    try:
        # 1. 监测项
        print("[1/7] 插入监测项...")
        mi_ids = {}  # name → id
        for mi_type, mi_name, mi_code in MONITOR_INDEXES:
            mi_ids[mi_name] = insert(cursor, "monitor_index", {
                "department_id": DEPT_ID, "type": mi_type, "name": mi_name, "code": mi_code,
            })
        print(f"  完成: {len(mi_ids)} 条")

        # 2. 监测内容
        print("[2/7] 插入监测内容...")
        vt_ids = {}  # vt_name → id
        for mi_name, vt_name, unit in VALUE_TYPES:
            vt_ids[vt_name] = insert(cursor, "monitor_value_type", {
                "monitor_index_id": mi_ids[mi_name], "name": vt_name, "unit": unit,
            })
        print(f"  完成: {len(vt_ids)} 条")

        # 3. 结构物
        print("[3/7] 插入结构物...")
        struct_ids = {}  # name → id
        for s_name, s_type, lon, lat, remark in STRUCTURES:
            struct_ids[s_name] = insert(cursor, "structure", {
                "name": s_name, "type": s_type, "department_id": DEPT_ID,
                "longitude": lon, "latitude": lat, "remark": remark,
            })
        print(f"  完成: {len(struct_ids)} 条")

        # 4. 设备型号
        print("[4/7] 插入设备型号...")
        dm_ids = {}  # model_name → id
        for mfr, model, rng, principle, sens, acc, rmk in DEVICE_MODELS:
            dm_ids[model] = insert(cursor, "device_model", {
                "department_id": DEPT_ID, "manufacturer": mfr, "device_type": "传感器",
                "model": model, "range": rng, "principle_type": principle,
                "sensitivity": sens, "accuracy": acc, "remark": rmk,
            })
        print(f"  完成: {len(dm_ids)} 条")

        # 5. 结构物-监测内容关联
        print("[5/7] 插入结构物-监测配置...")
        config_count = 0
        for s_name, vt_names in STRUCTURE_CONFIGS.items():
            for vt_name in vt_names:
                insert(cursor, "structure_monitor_config", {
                    "structure_id": struct_ids[s_name], "value_type_id": vt_ids[vt_name],
                })
                config_count += 1
        print(f"  完成: {config_count} 条")

        # 6. 设备实例 + 测点
        print("[6/7] 插入设备实例 & 测点...")
        device_count = 0
        point_thresholds = []  # (point_id, vt_name)

        for s_name, vt_names in STRUCTURE_CONFIGS.items():
            s_id = struct_ids[s_name]
            for vt_name in vt_names:
                vt_id = vt_ids[vt_name]
                dm_model_name = VTYPE_TO_DEVICE_MODEL[vt_name]
                dm_id = dm_ids[dm_model_name]

                for idx in range(1, SENSORS_PER_CONFIG + 1):
                    prod_date = today - timedelta(days=random.randint(0, 730))
                    device_id = insert(cursor, "monitor_device", {
                        "department_id": DEPT_ID, "device_model_id": dm_id,
                        "sn": f"FMC-{s_id}-{vt_id}-{idx:02d}",
                        "production_date": prod_date,
                        "remark": f"{s_name} / {vt_name} / #{idx}",
                    })

                    point_id = insert(cursor, "monitor_point", {
                        "structure_id": s_id, "value_type_id": vt_id,
                        "code": f"P-{s_id}-{vt_id}-{idx:02d}",
                        "location": f"Section-{chr(64 + idx)} / Zone-{idx}",
                        "device_id": device_id,
                        "channel": f"CH-{idx:02d}",
                    })

                    point_thresholds.append((point_id, vt_name))
                    device_count += 1

        print(f"  完成: {device_count} 台设备 + {device_count} 个测点")

        # 7. 测点阈值
        print("[7/7] 插入测点阈值...")
        for point_id, vt_name in point_thresholds:
            bl, bu, yl, yu, rl, ru = THRESHOLD_MAP[vt_name]
            insert(cursor, "monitor_point_threshold", {
                "point_id": point_id, "enabled": 1,
                "blue_lower": bl, "blue_upper": bu,
                "yellow_lower": yl, "yellow_upper": yu,
                "red_lower": rl, "red_upper": ru,
            })
        print(f"  完成: {len(point_thresholds)} 条")

        # 汇总
        print("\n=== 数据汇总 ===")
        print(f"  监测项:       {len(mi_ids)}")
        print(f"  监测内容:     {len(vt_ids)}")
        print(f"  结构物:       {len(struct_ids)}")
        print(f"  设备型号:     {len(dm_ids)}")
        print(f"  结构物配置:   {config_count}")
        print(f"  设备实例:     {device_count}")
        print(f"  测点:         {device_count}")
        print(f"  测点阈值:     {len(point_thresholds)}")
        print("\n✓ 模拟数据灌入完成！")

    except Exception:
        conn.close()
        raise

    conn.close()


if __name__ == "__main__":
    main()
