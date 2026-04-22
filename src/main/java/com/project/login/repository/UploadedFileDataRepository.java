package com.project.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.login.entity.UploadedFileData;

@Repository
public interface UploadedFileDataRepository
        extends JpaRepository<UploadedFileData, Long> {
}
