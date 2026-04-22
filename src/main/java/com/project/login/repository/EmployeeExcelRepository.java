package com.project.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.login.entity.EmployeeExcelData;

@Repository
public interface EmployeeExcelRepository 
        extends JpaRepository<EmployeeExcelData, Long> {
}