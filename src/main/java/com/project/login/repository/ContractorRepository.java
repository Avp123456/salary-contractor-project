package com.project.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.login.entity.Contractor;

public interface ContractorRepository extends JpaRepository<Contractor, Long> {
	    Contractor findByEmailAndPassword(String email, String password);
	}


