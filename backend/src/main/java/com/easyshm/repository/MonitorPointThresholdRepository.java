package com.easyshm.repository;

import com.easyshm.entity.MonitorPointThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitorPointThresholdRepository extends JpaRepository<MonitorPointThreshold, Long> {

    Optional<MonitorPointThreshold> findByPointId(Long pointId);

    List<MonitorPointThreshold> findByPointIdIn(List<Long> pointIds);

    void deleteByPointId(Long pointId);
}
