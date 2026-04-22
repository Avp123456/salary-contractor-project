package com.project.login.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.project.login.entity.UploadedFileColumns;

@Repository
public interface UploadedFileColumnsRepository extends JpaRepository<UploadedFileColumns, Long> {

    List<UploadedFileColumns> findByFileId(Long fileId);

    @Modifying
    @Transactional
    void deleteByFileId(Long fileId);
}
