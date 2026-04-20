package com.project.login.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.login.entity.Contractor;
import com.project.login.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByEmailAndPassword(String email, String password);
    List<Employee> findByContractor(Contractor contractor);
}
