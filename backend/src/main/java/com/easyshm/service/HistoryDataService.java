package com.easyshm.service;

import java.util.List;
import java.util.Map;

public interface HistoryDataService {

    List<Map<String, Object>> queryStatsData(Long structureId, Long valueTypeId,
                                              String period, String startTime, String endTime);
}
