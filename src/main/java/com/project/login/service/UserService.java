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
    	System.out.println("Before Encode: " + user.getPassword());
    	String password = passwordEncoder.encode(user.getPassword());
    	System.out.println("After Encode: " + password);
    	user.setPassword(password);
    	repo.save(user);
    }

    // ❌ DO NOT use manual authentication with Spring Security
    // ✅ Keep this method only if needed elsewhere (fixed & safe)
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }
}
