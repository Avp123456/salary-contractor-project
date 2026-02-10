// commit test for cut length feature

package com.project.login.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_contract")
public class gen_bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "contract_no", nullable = false)
    private Integer contractNo;


    @Column(name = "sr_no")
    private int srNo;

    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;

    @Column(name = "weaver_name", nullable = false)
    private String weaverName;

    @Column(name = "trader_name")
    private String traderName;

    @Column(name = "broker_name", nullable = false)
    private String brokerName;

    private String quality;

    @Column(name = "quantity_meters")
    private Integer quantityMeters;

    private Integer beams;

    @Column(name = "job_rate")
    private Double jobRate;

    @Column(name = "payment_days")
    private Integer paymentDays;

    @Column(name = "production_schedule")
    private String productionSchedule;

    @Column(name = "no_of_machines")
    private Integer noOfMachines;

    private String remark;
    @Column(name = "cut_length")
    private String cutLength;

    @Column(name = "minimum_delivery")
    private String minimumDelivery;

    @Column(name = "rolling_folding")
    private String rollingFolding;


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getContractNo() {
        return contractNo;
    }

    public void setContractNo(Integer contractNo) {
        this.contractNo = contractNo;
    }


    public int getSrNo() {
        return srNo;
    }

    public void setSrNo(int srNo) {
        this.srNo = srNo;
    }

    public LocalDate getContractDate() {
        return contractDate;
    }

    public void setContractDate(LocalDate contractDate) {
        this.contractDate = contractDate;
    }

    public String getWeaverName() {
        return weaverName;
    }

    public void setWeaverName(String weaverName) {
        this.weaverName = weaverName;
    }

    public String getTraderName() {
        return traderName;
    }

    public void setTraderName(String traderName) {
        this.traderName = traderName;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public Integer getQuantityMeters() {
        return quantityMeters;
    }

    public void setQuantityMeters(Integer quantityMeters) {
        this.quantityMeters = quantityMeters;
    }

    public Integer getBeams() {
        return beams;
    }

    public void setBeams(Integer beams) {
        this.beams = beams;
    }

    public Double getJobRate() {
        return jobRate;
    }

    public void setJobRate(Double jobRate) {
        this.jobRate = jobRate;
    }

    public Integer getPaymentDays() {
        return paymentDays;
    }

    public void setPaymentDays(Integer paymentDays) {
        this.paymentDays = paymentDays;
    }

    public String getProductionSchedule() {
        return productionSchedule;
    }

    public void setProductionSchedule(String productionSchedule) {
        this.productionSchedule = productionSchedule;
    }

    public Integer getNoOfMachines() {
        return noOfMachines;
    }

    public void setNoOfMachines(Integer noOfMachines) {
        this.noOfMachines = noOfMachines;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public String getCutLength() {
        return cutLength;
    }

    public void setCutLength(String cutLength) {
        this.cutLength = cutLength;
    }

    public String getMinimumDelivery() {
        return minimumDelivery;
    }

    public void setMinimumDelivery(String minimumDelivery) {
        this.minimumDelivery = minimumDelivery;
    }

    public String getRollingFolding() {
        return rollingFolding;
    }

    public void setRollingFolding(String rollingFolding) {
        this.rollingFolding = rollingFolding;
    }

}
