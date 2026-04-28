package com.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.entity.UploadedFileColumns;

import java.util.List;

@Repository
public interface UploadedFileColumnsRepository extends JpaRepository<UploadedFileColumns, Long> {
    List<UploadedFileColumns> findByFileId(Long fileId);
    List<UploadedFileColumns> findByFileIdAndContractorId(Long fileId, Long contractorId);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByFileId(Long fileId);
}
