package com.easyshm.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TdengineStatsDao {

    private static final Logger log = LoggerFactory.getLogger(TdengineStatsDao.class);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Autowired
    @Qualifier("tdengineJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> queryPointStats(String tableName, String startTime, String endTime) {
        String sql = "SELECT ts, first_val, avg_val, max_val, min_val, std_val, count_val FROM " + tableName
                + " WHERE ts >= ? AND ts <= ? ORDER BY ts ASC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("ts", rs.getTimestamp("ts").toLocalDateTime().format(TS_FMT));
                row.put("firstVal", rs.getDouble("first_val"));
                row.put("avgVal", rs.getDouble("avg_val"));
                row.put("maxVal", rs.getDouble("max_val"));
                row.put("minVal", rs.getDouble("min_val"));
                row.put("stdVal", rs.getDouble("std_val"));
                row.put("countVal", rs.getInt("count_val"));
                return row;
            }, startTime, endTime);
        } catch (Exception e) {
            log.warn("查询子表 {} 失败: {}", tableName, e.getMessage());
            return List.of();
        }
    }
}
