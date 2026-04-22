package com.project.login.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
	@Table(name = "uploaded_files")
	public class UploadedFiles {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String fileName;
	    private Long size;
	    private LocalDateTime uploadDate;
	    private Integer headerCount;
	    private Integer trailerCount;
	    @jakarta.persistence.Column(name = "file_content")
	    private byte[] fileContent;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public Long getSize() {
			return size;
		}
		public void setSize(Long size) {
			this.size = size;
		}
		public LocalDateTime getUploadDate() {
			return uploadDate;
		}
		public void setUploadDate(LocalDateTime uploadDate) {
			this.uploadDate = uploadDate;
		}
		public Integer getHeaderCount() {
			return headerCount;
		}
		public void setHeaderCount(Integer headerCount) {
			this.headerCount = headerCount;
		}
		public Integer getTrailerCount() {
			return trailerCount;
		}
		public void setTrailerCount(Integer trailerCount) {
			this.trailerCount = trailerCount;
		}
		public byte[] getFileContent() {
			return fileContent;
		}
		public void setFileContent(byte[] fileContent) {
			this.fileContent = fileContent;
		}
	    
	}


