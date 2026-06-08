package com.project.repository;

import com.project.entity.ReportConfigurationFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportConfigurationFileRepository extends JpaRepository<ReportConfigurationFile, Long> {
    List<ReportConfigurationFile> findByConfigId(Long configId);
    void deleteByConfigId(Long configId);
}
