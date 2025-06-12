package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String email;
    private String password;

    private String id;

    @Column(name = "thai_name")
    private String thaiName;

    @Column(name = "english_name")
    private String englishName;

    private String pin;
    private String role;

    // Default constructor required by JPA
    public User() {
    }

    // Constructor with fields
    public User(String email, String password, String id, String thaiName, String englishName, String pin,
            String role) {
        this.email = email;
        this.password = password;
        this.id = id;
        this.thaiName = thaiName;
        this.englishName = englishName;
        this.pin = pin;
        this.role = role;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }

    public String getThaiName() {
        return thaiName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getPin() {
        return pin;
    }

    public String getRole() {
        return role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setThaiName(String thaiName) {
        this.thaiName = thaiName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setRole(String role) {
        this.role = role;
    }

}