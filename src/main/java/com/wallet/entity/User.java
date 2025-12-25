package com.wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;


@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "emailId"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String emailId;

    @Column(nullable = false)
    private String password;

    @Column
    private String role = "USER";

    public User(String name, String email, String password) {
        this.emailId = email;
        this.name = name;
        this.password = password;
    }
    public User(){
        
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(emailId, user.emailId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(emailId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
