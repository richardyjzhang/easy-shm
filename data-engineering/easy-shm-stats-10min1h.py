"""定时聚合脚本 —— 由 crontab 每小时执行一次。

从 origin_data 库的原始数据，
对 [now-2h, now-1h) 窗口执行 10min / 1h 两种粒度聚合，
将结果写入 stats_data 库的对应子表中。
"""

import os
from datetime import datetime, timedelta

import taosrest
from dotenv import load_dotenv

load_dotenv()

TAOS_URL = os.getenv("TAOS_URL", "http://localhost:6041")
TAOS_USER = os.getenv("TAOS_USER", "root")
TAOS_PASSWORD = os.getenv("TAOS_PASSWORD", "taosdata")

ORIGIN_DB = "origin_data"
STATS_DB = "stats_data"

PERIODS = [
    ("10min", "10m"),
    ("1h", "1h"),
]

conn = taosrest.connect(url=TAOS_URL, user=TAOS_USER, password=TAOS_PASSWORD)

created_tables: set[str] = set()


def exec_sql(sql: str):
    try:
        return conn.execute(sql)
    except Exception as e:
        print(f"[taos] ERROR: {e}\n  SQL: {sql}")
        return None


def query_sql(sql: str) -> list[list]:
    try:
        result = conn.query(sql)
        return result.data if result and result.data else []
    except Exception as e:
        print(f"[taos] QUERY ERROR: {e}\n  SQL: {sql}")
        return []


def get_point_ids() -> list[int]:
    """查询 origin_data 超级表下所有子表的 point_id tag。"""
    rows = query_sql(
        f"SELECT DISTINCT point_id FROM {ORIGIN_DB}.origin_data"
    )
    return [int(row[0]) for row in rows]


def ensure_stats_table(point_id: int, period: str):
    table_name = f"stats_data_{point_id}_{period}"
    if table_name in created_tables:
        return
    exec_sql(
        f"CREATE TABLE IF NOT EXISTS {STATS_DB}.{table_name} "
        f"USING {STATS_DB}.stats_data TAGS ({point_id}, '{period}')"
    )
    created_tables.add(table_name)


def aggregate_one_period(
    point_id: int, period: str, interval: str,
    win_start: str, win_end: str,
):
    """从原始数据直接聚合并写入。"""
    ensure_stats_table(point_id, period)
    target_table = f"{STATS_DB}.stats_data_{point_id}_{period}"
    source_table = f"{ORIGIN_DB}.origin_data_{point_id}"

    rows = query_sql(
        f"SELECT _wstart, FIRST(val), AVG(val), MAX(val), MIN(val), "
        f"STDDEV(val), COUNT(val) "
        f"FROM {source_table} "
        f"WHERE ts >= '{win_start}' AND ts < '{win_end}' "
        f"INTERVAL({interval})"
    )

    if not rows:
        return 0

    values_parts = []
    for row in rows:
        ts = row[0]
        first_val = row[1]
        avg_val = row[2]
        max_val = row[3]
        min_val = row[4]
        std_val = row[5]
        count_val = int(row[6])
        if isinstance(ts, datetime):
            ts = ts.strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]
        values_parts.append(
            f"('{ts}', {first_val}, {avg_val}, {max_val}, {min_val}, "
            f"{std_val}, {count_val})"
        )

    sql = f"INSERT INTO {target_table} VALUES {' '.join(values_parts)}"
    exec_sql(sql)

    return len(values_parts)


def main():
    now = datetime.now()
    win_end = now.replace(minute=0, second=0, microsecond=0) - timedelta(hours=1)
    win_start = win_end - timedelta(hours=1)

    win_start_str = win_start.strftime("%Y-%m-%d %H:%M:%S.000")
    win_end_str = win_end.strftime("%Y-%m-%d %H:%M:%S.000")

    point_ids = get_point_ids()
    if not point_ids:
        print(f"[aggregate] ERROR: no child tables found. window=[{win_start_str}, {win_end_str})")
        return

    for point_id in point_ids:
        for period, interval in PERIODS:
            aggregate_one_period(
                point_id, period, interval, win_start_str, win_end_str,
            )


if __name__ == "__main__":
    main()
