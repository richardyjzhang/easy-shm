-- ============================================================
-- TDengine DDL — 监测数据时序存储
-- 两个 Database: origin_data / stats_data
-- ============================================================

-- ============================================================
-- 1. origin_data — 原始采集数据
-- ============================================================
CREATE DATABASE IF NOT EXISTS origin_data
  PRECISION 'ms'
  KEEP 60;

USE origin_data;

-- 超级表：原始采样值
-- 每条记录 = 某测点在某时刻的一次采集
CREATE STABLE origin_data (
  ts          TIMESTAMP,
  val         DOUBLE
) TAGS (
  point_id      BIGINT
);

-- 子表命名规则: origin_data_{point_id}
-- 示例:
-- CREATE TABLE origin_data_1001 USING origin_data TAGS (1001);


-- ============================================================
-- 2. stats_data — 统计数据
--    保留 3 年，存储各时间粒度的聚合结果
-- ============================================================
CREATE DATABASE IF NOT EXISTS stats_data
  PRECISION 'ms'
  KEEP 1095;

USE stats_data;

-- 超级表：统计聚合值
-- 由后端定时任务或 TDengine 流计算写入
CREATE STABLE stats_data (
  ts          TIMESTAMP,
  first_val   DOUBLE,
  avg_val     DOUBLE,
  max_val     DOUBLE,
  min_val     DOUBLE,
  std_val     DOUBLE,
  count_val   INT
) TAGS (
  point_id      BIGINT,
  point_type    VARCHAR(10)
);

-- 子表命名规则: stats_data_{point_id}_{period}
-- 示例:
-- CREATE TABLE stats_data_1001_1h USING stats_data TAGS (1001, '1h');



-- ============================================================
-- 流计算（可选）— 自动从原始数据生成小时统计
-- 需要 TDengine 3.x+
-- ============================================================

-- CREATE STREAM stats_hourly_stream
--   TRIGGER AT_ONCE
--   INTO ts_stats.stats_hourly AS
-- SELECT
--   _wstart AS ts,
--   '1h' AS period,
--   AVG(val) AS avg_val,
--   MAX(val) AS max_val,
--   MIN(val) AS min_val,
--   STDDEV(val) AS std_val,
--   COUNT(*) AS count_val,
--   tbname
-- FROM ts_raw.origin_data
-- WHERE quality = 0
-- INTERVAL(1h);
