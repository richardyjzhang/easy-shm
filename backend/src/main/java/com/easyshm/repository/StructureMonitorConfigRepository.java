package com.easyshm.repository;

import com.easyshm.entity.StructureMonitorConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StructureMonitorConfigRepository extends JpaRepository<StructureMonitorConfig, Long> {

    List<StructureMonitorConfig> findByStructureId(Long structureId);

    Optional<StructureMonitorConfig> findByStructureIdAndValueTypeId(Long structureId, Long valueTypeId);

    void deleteByStructureIdAndValueTypeId(Long structureId, Long valueTypeId);
}
