package com.easyshm.service.impl;

import com.easyshm.dao.TdengineStatsDao;
import com.easyshm.entity.MonitorPoint;
import com.easyshm.repository.MonitorPointRepository;
import com.easyshm.service.HistoryDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HistoryDataServiceImpl implements HistoryDataService {

    private static final Set<String> VALID_PERIODS = Set.of("1s", "1min", "10min", "1h");

    @Autowired
    private TdengineStatsDao tdengineStatsDao;

    @Autowired
    private MonitorPointRepository monitorPointRepository;

    @Override
    public List<Map<String, Object>> queryStatsData(Long structureId, Long valueTypeId,
                                                     String period, String startTime, String endTime) {
        if (!VALID_PERIODS.contains(period)) {
            throw new RuntimeException("无效的统计粒度：" + period + "，可选值：1s, 1min, 10min, 1h");
        }

        List<MonitorPoint> points = monitorPointRepository.findByStructureIdAndValueTypeId(structureId, valueTypeId);
        if (points.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (MonitorPoint point : points) {
            String tableName = "stats_data.stats_data_" + point.getId() + "_" + period;
            List<Map<String, Object>> records = tdengineStatsDao.queryPointStats(tableName, startTime, endTime);

            Map<String, Object> pointData = new LinkedHashMap<>();
            pointData.put("pointId", point.getId());
            pointData.put("pointCode", point.getCode());
            pointData.put("pointLocation", point.getLocation());
            pointData.put("records", records);
            result.add(pointData);
        }
        return result;
    }
}
