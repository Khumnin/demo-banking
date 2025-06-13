package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.demo.service.TransactionService;
import com.example.demo.model.Transaction;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import com.example.demo.model.Account;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/api/account")
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountController(AccountService accountService, TransactionService transactionService,
            UserRepository userRepository) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userRepository = userRepository;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @PreAuthorize("hasRole('TELLER')")
    @PostMapping("/create")
    public String createAccount(@RequestBody Account account) {
        String id = account.getId();
        String thaiName = account.getThaiName();
        String englishName = account.getEnglishName();
        System.out.println(
                "Creating account with id: " + id + ", thaiName: " + thaiName + ", englishName: " + englishName);
        accountService.createAccount(id, thaiName, englishName);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/statement/{accountNumber}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAccountStatement(@PathVariable String accountNumber,
            @RequestBody Map<String, String> body) {
        String pin = body.get("pin");
        if (pin == null || pin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get the authenticated user's information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserId = authentication.getName();

        // Get the requested account
        Account account = accountService.getAccountByNumber(accountNumber);
        if (account == null) {
            logger.warn("Account not found: {}", accountNumber);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Get user information for the account
        User accountUser = userRepository.findByid(account.getId())
                .orElseThrow(() -> new RuntimeException("User not found for account"));

        // Access control check
        if (!accountUser.getEmail().equals(authenticatedUserId)) {
            logger.warn("Unauthorized access attempt to account {} by user {}", accountNumber, authenticatedUserId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Validate PIN
        User user = userRepository.findByid(account.getId()).orElse(null);
        if (user == null || !passwordEncoder.matches(pin, user.getPin())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        List<Map<String, Object>> statement = transactions.stream().map(tx -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("date", tx.getDate().toString());
            map.put("time", tx.getTime().toString().substring(0, 5));
            map.put("code", tx.getType().getCode());
            map.put("channel", tx.getChannel().toString());
            map.put("amount", tx.getAmount());
            map.put("balance", tx.getBalance());
            map.put("remark", tx.getDescription());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(statement);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info/{accountNumber}")
    public ResponseEntity<?> getAccountInfo(@PathVariable String accountNumber) {
        try {
            // Get the authenticated user's information
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUserId = authentication.getName();

            // Get the requested account
            Account account = accountService.getAccountByNumber(accountNumber);
            if (account == null) {
                logger.warn("Account not found: {}", accountNumber);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
            }

            // Get user information for the account
            User accountUser = userRepository.findByid(account.getId())
                    .orElseThrow(() -> new RuntimeException("User not found for account"));

            // Access control check
            if (!accountUser.getEmail().equals(authenticatedUserId)) {
                logger.warn("Unauthorized access attempt to account {} by user {}", accountNumber, authenticatedUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            // Create a response object with only necessary information
            Map<String, Object> accountInfo = Map.of(
                    "accountNumber", account.getAccountNumber(),
                    "thaiName", account.getThaiName(),
                    "englishName", account.getEnglishName(),
                    "balance", account.getBalance(),
                    "id", account.getId(),
                    "email", accountUser.getEmail());

            logger.info("Account info retrieved successfully for account: {} by user: {}",
                    accountNumber, authenticatedUserId);
            return ResponseEntity.ok(accountInfo);
        } catch (Exception e) {
            logger.error("Error retrieving account info for account: {} - Error: {}",
                    accountNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving account information");
        }
    }
}
