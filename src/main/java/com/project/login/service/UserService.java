package com.project.login.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.login.entity.User;
import com.project.login.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void saveUser(User user) {
    	System.out.println("saveuser method hit");
    	String password = passwordEncoder.encode(user.getPassword());
    	user.setPassword(password);
    	repo.save(user);
    }
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }
}
