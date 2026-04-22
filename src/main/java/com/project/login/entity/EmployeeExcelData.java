package com.project.login.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee_excel_data")
public class EmployeeExcelData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employeeNo;
    private String employeeName;

    private String presentDay;
    private String paidHoliday;
    private String leaveCount;
    private String absent;
    private String totalPayableDays;

    private String grossSalary;
    private String basic;
    private String hra;
    private String occupationalAllowance;
    private String conveyanceAllowance;
    private String medicalAllowance;

    private String tea;
    private String shoes;
    private String washing;
    private String incentive;
    private String dailyProduction;
    private String responsible;
    private String performance;
    private String attendance;

    private String previousMonth;
    private String currentMonth;
    private String rejection;

    private String esicGross;
    private String grossWages;

    private String labourFund;
    private String salaryAdvance;
    private String pf;
    private String esi;
    private String pt;
    private String tds;
    private String fine;

    private String totalDeduction;
    private String netPayable;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEmployeeNo() {
		return employeeNo;
	}
	public void setEmployeeNo(String employeeNo) {
		this.employeeNo = employeeNo;
	}
	public String getEmployeeName() {
		return employeeName;
	}
	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}
	public String getPresentDay() {
		return presentDay;
	}
	public void setPresentDay(String presentDay) {
		this.presentDay = presentDay;
	}
	public String getPaidHoliday() {
		return paidHoliday;
	}
	public void setPaidHoliday(String paidHoliday) {
		this.paidHoliday = paidHoliday;
	}
	public String getLeaveCount() {
		return leaveCount;
	}
	public void setLeaveCount(String leaveCount) {
		this.leaveCount = leaveCount;
	}
	public String getAbsent() {
		return absent;
	}
	public void setAbsent(String absent) {
		this.absent = absent;
	}
	public String getTotalPayableDays() {
		return totalPayableDays;
	}
	public void setTotalPayableDays(String totalPayableDays) {
		this.totalPayableDays = totalPayableDays;
	}
	public String getGrossSalary() {
		return grossSalary;
	}
	public void setGrossSalary(String grossSalary) {
		this.grossSalary = grossSalary;
	}
	public String getBasic() {
		return basic;
	}
	public void setBasic(String basic) {
		this.basic = basic;
	}
	public String getHra() {
		return hra;
	}
	public void setHra(String hra) {
		this.hra = hra;
	}
	public String getOccupationalAllowance() {
		return occupationalAllowance;
	}
	public void setOccupationalAllowance(String occupationalAllowance) {
		this.occupationalAllowance = occupationalAllowance;
	}
	public String getConveyanceAllowance() {
		return conveyanceAllowance;
	}
	public void setConveyanceAllowance(String conveyanceAllowance) {
		this.conveyanceAllowance = conveyanceAllowance;
	}
	public String getMedicalAllowance() {
		return medicalAllowance;
	}
	public void setMedicalAllowance(String medicalAllowance) {
		this.medicalAllowance = medicalAllowance;
	}
	public String getTea() {
		return tea;
	}
	public void setTea(String tea) {
		this.tea = tea;
	}
	public String getShoes() {
		return shoes;
	}
	public void setShoes(String shoes) {
		this.shoes = shoes;
	}
	public String getWashing() {
		return washing;
	}
	public void setWashing(String washing) {
		this.washing = washing;
	}
	public String getIncentive() {
		return incentive;
	}
	public void setIncentive(String incentive) {
		this.incentive = incentive;
	}
	public String getDailyProduction() {
		return dailyProduction;
	}
	public void setDailyProduction(String dailyProduction) {
		this.dailyProduction = dailyProduction;
	}
	public String getResponsible() {
		return responsible;
	}
	public void setResponsible(String responsible) {
		this.responsible = responsible;
	}
	public String getPerformance() {
		return performance;
	}
	public void setPerformance(String performance) {
		this.performance = performance;
	}
	public String getAttendance() {
		return attendance;
	}
	public void setAttendance(String attendance) {
		this.attendance = attendance;
	}
	public String getPreviousMonth() {
		return previousMonth;
	}
	public void setPreviousMonth(String previousMonth) {
		this.previousMonth = previousMonth;
	}
	public String getCurrentMonth() {
		return currentMonth;
	}
	public void setCurrentMonth(String currentMonth) {
		this.currentMonth = currentMonth;
	}
	public String getRejection() {
		return rejection;
	}
	public void setRejection(String rejection) {
		this.rejection = rejection;
	}
	public String getEsicGross() {
		return esicGross;
	}
	public void setEsicGross(String esicGross) {
		this.esicGross = esicGross;
	}
	public String getGrossWages() {
		return grossWages;
	}
	public void setGrossWages(String grossWages) {
		this.grossWages = grossWages;
	}
	public String getLabourFund() {
		return labourFund;
	}
	public void setLabourFund(String labourFund) {
		this.labourFund = labourFund;
	}
	public String getSalaryAdvance() {
		return salaryAdvance;
	}
	public void setSalaryAdvance(String salaryAdvance) {
		this.salaryAdvance = salaryAdvance;
	}
	public String getPf() {
		return pf;
	}
	public void setPf(String pf) {
		this.pf = pf;
	}
	public String getEsi() {
		return esi;
	}
	public void setEsi(String esi) {
		this.esi = esi;
	}
	public String getPt() {
		return pt;
	}
	public void setPt(String pt) {
		this.pt = pt;
	}
	public String getTds() {
		return tds;
	}
	public void setTds(String tds) {
		this.tds = tds;
	}
	public String getFine() {
		return fine;
	}
	public void setFine(String fine) {
		this.fine = fine;
	}
	public String getTotalDeduction() {
		return totalDeduction;
	}
	public void setTotalDeduction(String totalDeduction) {
		this.totalDeduction = totalDeduction;
	}
	public String getNetPayable() {
		return netPayable;
	}
	public void setNetPayable(String netPayable) {
		this.netPayable = netPayable;
	}

    // getters & setters
    
}