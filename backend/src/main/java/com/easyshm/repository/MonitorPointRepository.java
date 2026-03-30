package com.easyshm.repository;

import com.easyshm.entity.MonitorPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitorPointRepository extends JpaRepository<MonitorPoint, Long> {

    List<MonitorPoint> findByStructureId(Long structureId);

    List<MonitorPoint> findByStructureIdAndValueTypeId(Long structureId, Long valueTypeId);

    boolean existsByStructureIdAndCode(Long structureId, String code);

    boolean existsByStructureIdAndCodeAndIdNot(Long structureId, String code, Long id);

    boolean existsByDeviceId(Long deviceId);

    void deleteByStructureId(Long structureId);
}
