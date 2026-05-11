package com.project.entity;

import javax.persistence.*;

@Entity
@Table(name = "report_configuration_files")
public class ReportConfigurationFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long configId;
    private String fileName;
    private int headerSkip;
    private int trailerSkip;
    private Integer totalPayableColumn;
    private Integer overtimeTotalAmountColumn;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public int getHeaderSkip() { return headerSkip; }
    public void setHeaderSkip(int headerSkip) { this.headerSkip = headerSkip; }
    public int getTrailerSkip() { return trailerSkip; }
    public void setTrailerSkip(int trailerSkip) { this.trailerSkip = trailerSkip; }
    public Integer getTotalPayableColumn() { return totalPayableColumn; }
    public void setTotalPayableColumn(Integer totalPayableColumn) { this.totalPayableColumn = totalPayableColumn; }
    public Integer getOvertimeTotalAmountColumn() { return overtimeTotalAmountColumn; }
    public void setOvertimeTotalAmountColumn(Integer overtimeTotalAmountColumn) { this.overtimeTotalAmountColumn = overtimeTotalAmountColumn; }
}
