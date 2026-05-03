package com.project.repository;

import com.project.entity.ReportConfigurationColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ReportConfigurationColumnRepository extends JpaRepository<ReportConfigurationColumn, Long> {
    List<ReportConfigurationColumn> findByConfigId(Long configId);
    
    @Transactional
    void deleteByConfigId(Long configId);
}
