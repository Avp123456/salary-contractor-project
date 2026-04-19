package com.project.login.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    // ✅ ADD THIS
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /* ========= GETTERS & SETTERS ========= */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email.trim().toLowerCase();
    }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        this.password = password;
    }

    // ✅ NEW
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}