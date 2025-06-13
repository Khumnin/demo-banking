package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Version;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "account_number", unique = true, length = 7)
    private String accountNumber;
    @Column(name = "id")
    private String id;
    @Column(name = "thai_name")
    private String thaiName;
    @Column(name = "english_name")
    private String englishName;
    @Column(name = "balance")
    private double balance;
    @Version
    private Long version;

    // Default constructor required by JPA
    public Account() {
    }

    // Constructor with fields
    public Account(String id, String thaiName, String englishName) {
        this.id = id;
        this.thaiName = thaiName;
        this.englishName = englishName;
        this.balance = 0.0;
    }

    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThaiName() {
        return thaiName;
    }

    public void setThaiName(String thaiName) {
        this.thaiName = thaiName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}