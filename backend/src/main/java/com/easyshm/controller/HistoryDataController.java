package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.service.HistoryDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history-data")
public class HistoryDataController {

    @Autowired
    private HistoryDataService historyDataService;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> query(
            @RequestParam Long structureId,
            @RequestParam Long valueTypeId,
            @RequestParam String period,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        return ApiResponse.ok(historyDataService.queryStatsData(structureId, valueTypeId, period, startTime, endTime));
    }
}
