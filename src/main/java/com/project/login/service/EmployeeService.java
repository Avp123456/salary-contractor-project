package com.project.login.service;

import com.project.login.entity.Contractor;
import com.project.login.entity.Employee;
import com.project.login.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repo;

    // ✅ ADD THIS (MISSING METHOD)
    public Employee login(String email, String password) {
        return repo.findByEmailAndPassword(email, password);
    }

    // SAVE
    public void save(Employee employee) {
        repo.save(employee);
    }

    // GET ALL
    public List<Employee> getAll() {
        return repo.findAll();
    }
    
    
    public List<Employee> getByContractor(Contractor contractor) {
        return repo.findByContractor(contractor);
    }
}