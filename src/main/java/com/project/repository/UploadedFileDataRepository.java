package com.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.entity.UploadedFileData;

import java.util.List;

@Repository
public interface UploadedFileDataRepository extends JpaRepository<UploadedFileData, Long> {
    List<UploadedFileData> findByFileId(Long fileId);
    List<UploadedFileData> findByConfigId(Long configId);
    List<UploadedFileData> findByContractorId(Long contractorId);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByFileId(Long fileId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByConfigId(Long configId);
}
