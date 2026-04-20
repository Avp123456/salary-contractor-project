package com.project.login.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.login.entity.Contractor;
import com.project.login.entity.Employee;
import com.project.login.repository.ContractorRepository;

@Service
public class ContractorService {

    @Autowired
    private ContractorRepository repo;

    public Contractor login(String email, String password) {
        return repo.findByEmailAndPassword(email, password);
    }
    
}