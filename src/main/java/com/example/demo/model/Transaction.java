package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "account_number")
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "code")
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionChannel channel;

    @Column(nullable = false, name = "debit/credit")
    private double amount;

    @Column(nullable = false, name = "balance")
    private double balance;

    @Column(nullable = false, name = "date")
    private LocalDate date;

    @Column(nullable = false, name = "time")
    private LocalTime time;

    @Column(nullable = false, name = "remark")
    private String description;

    // Default constructor required by JPA
    public Transaction() {
    }

    // Constructor for deposit
    public Transaction(String accountNumber, double amount, double balance, TransactionChannel channel,
            String description) {
        this.accountNumber = accountNumber;
        this.type = TransactionType.DEPOSIT;
        this.channel = channel;
        this.amount = amount;
        this.balance = balance;
        this.date = LocalDate.now();
        this.time = LocalTime.now();
        this.description = description;
    }

    // Constructor for transfer from source account
    public Transaction(String accountNumber, String destinationAccount, double amount, double balance,
            TransactionChannel channel,
            String description) {
        this.accountNumber = accountNumber;
        this.type = TransactionType.TRANSFER;
        this.channel = channel;
        this.amount = amount;
        this.balance = balance;
        this.date = LocalDate.now();
        this.time = LocalTime.now();
        this.description = String.format("Transfer to %s: %s", destinationAccount, description);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionChannel getChannel() {
        return channel;
    }

    public void setChannel(TransactionChannel channel) {
        this.channel = channel;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}