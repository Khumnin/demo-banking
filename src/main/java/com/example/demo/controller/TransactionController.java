package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.TransactionService;
import com.example.demo.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.model.TransactionChannel;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.demo.service.AccountService;
import com.example.demo.model.Account;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @PreAuthorize("hasRole('TELLER')")
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequest request) {
        try {
            logger.info("Processing deposit request for account: {}, amount: {}", request.accountNumber(),
                    request.amount());
            Transaction transaction = transactionService.deposit(
                    request.accountNumber(),
                    request.amount(),
                    TransactionChannel.OTC,
                    request.description());
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid deposit request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing deposit: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing deposit: " + e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/verify-transfer")
    public ResponseEntity<?> verifyTransfer(@RequestBody TransferVerifyRequest request) {
        try {
            Account sourceAccount = accountService.getAccountByNumber(request.sourceAccountNumber());
            Account destinationAccount = accountService.getAccountByNumber(request.destinationAccountNumber());

            TransferVerifyResponse response = new TransferVerifyResponse(
                    sourceAccount.getThaiName(),
                    sourceAccount.getAccountNumber(),
                    destinationAccount.getThaiName(),
                    destinationAccount.getAccountNumber(),
                    request.amount());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error verifying transfer: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        try {
            logger.info("Processing transfer request from account: {} to account: {}, amount: {}",
                    request.sourceAccountNumber(), request.destinationAccountNumber(), request.amount());

            Transaction transaction = transactionService.transfer(
                    request.sourceAccountNumber(),
                    request.destinationAccountNumber(),
                    request.amount(),
                    request.pin());

            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid transfer request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing transfer: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing transfer: " + e.getMessage());
        }
    }

    public record DepositRequest(
            String accountNumber,
            double amount,
            String description) {
    }

    public record TransferRequest(
            String sourceAccountNumber,
            String destinationAccountNumber,
            double amount,
            String pin,
            String description) {
    }

    public record TransferVerifyRequest(
            String sourceAccountNumber,
            String destinationAccountNumber,
            double amount) {
    }

    public record TransferVerifyResponse(
            String sourceAccountName,
            String sourceAccountNumber,
            String destinationAccountName,
            String destinationAccountNumber,
            double amount) {
    }
}
