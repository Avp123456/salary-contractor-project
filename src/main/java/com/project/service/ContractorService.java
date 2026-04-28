package com.project.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.entity.Contractor;
import com.project.entity.Employee;
import com.project.repository.ContractorRepository;

@Service
public class ContractorService {

    @Autowired
    private ContractorRepository repo;

    public Contractor login(String email, String password) {
        return repo.findByEmailAndPassword(email, password);
    }
    
}