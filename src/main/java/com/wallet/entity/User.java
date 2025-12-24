package com.wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
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

    public User(String name, String email) {
        this.emailId = email;
        this.name = name;
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
}
