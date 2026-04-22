package com.project.login.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.login.entity.EmployeeExcelData;
import com.project.login.repository.EmployeeExcelRepository;

@Service
public class EmployeeExcelService {

    @Autowired
    private EmployeeExcelRepository repo;

    public void saveAll(List<String> headers, List<List<String>> data) {

        for (List<String> row : data) {

            try {

                // ❌ Skip TOTAL rows
                boolean isTotalRow = false;
                for (String cell : row) {
                    if (cell != null && cell.toLowerCase().contains("total")) {
                        isTotalRow = true;
                        break;
                    }
                }
                if (isTotalRow) continue;

                // ✅ Get values dynamically
                String empNo = getValue(headers, row, "employee no");
                String empName = getValue(headers, row, "name");

                if ((empNo == null || empNo.trim().isEmpty()) &&
                    (empName == null || empName.trim().isEmpty())) {
                    continue;
                }

                EmployeeExcelData e = new EmployeeExcelData();

                e.setEmployeeNo(empNo);
                e.setEmployeeName(empName);

                e.setPresentDay(getValue(headers, row, "present"));
                e.setPaidHoliday(getValue(headers, row, "holiday"));
                e.setLeaveCount(getValue(headers, row, "leave"));
                e.setAbsent(getValue(headers, row, "absent"));
                e.setTotalPayableDays(getValue(headers, row, "payable"));

                e.setGrossSalary(getValue(headers, row, "gross salary"));
                e.setBasic(getValue(headers, row, "basic"));
                e.setHra(getValue(headers, row, "hra"));
                e.setOccupationalAllowance(getValue(headers, row, "occupational"));
                e.setConveyanceAllowance(getValue(headers, row, "conveyance"));
                e.setMedicalAllowance(getValue(headers, row, "medical"));

                e.setTea(getValue(headers, row, "tea"));
                e.setShoes(getValue(headers, row, "shoes"));
                e.setWashing(getValue(headers, row, "washing"));
                e.setIncentive(getValue(headers, row, "incentive"));
                e.setDailyProduction(getValue(headers, row, "production"));
                e.setResponsible(getValue(headers, row, "responsible"));
                e.setPerformance(getValue(headers, row, "performance"));
                e.setAttendance(getValue(headers, row, "attendance"));

                e.setPreviousMonth(getValue(headers, row, "previous"));
                e.setCurrentMonth(getValue(headers, row, "current"));
                e.setRejection(getValue(headers, row, "rejection"));

                e.setEsicGross(getValue(headers, row, "esic"));
                e.setGrossWages(getValue(headers, row, "gross wages"));

                e.setLabourFund(getValue(headers, row, "labour"));
                e.setSalaryAdvance(getValue(headers, row, "advance"));
                e.setPf(getValue(headers, row, "pf"));
                e.setEsi(getValue(headers, row, "esi"));
                e.setPt(getValue(headers, row, "pt"));
                e.setTds(getValue(headers, row, "tds"));
                e.setFine(getValue(headers, row, "fine"));

                e.setTotalDeduction(getValue(headers, row, "deduction"));
                e.setNetPayable(getValue(headers, row, "net"));

                repo.save(e);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // 🔍 Helper methods
    private String getValue(List<String> headers, List<String> row, String keyword) {
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).toLowerCase().contains(keyword)) {
                return i < row.size() ? row.get(i) : "";
            }
        }
        return "";
    }
}