package com.project.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.login.entity.UploadedFiles;

@Repository
	public interface UploadedFileRepository
	        extends JpaRepository<UploadedFiles, Long> {
	}