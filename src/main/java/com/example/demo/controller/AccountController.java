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

@Controller
@RequestMapping("/api/account")
public class AccountController {

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
    public String createAccount(@RequestParam String id,
            @RequestParam String thaiName,
            @RequestParam String englishName) {
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
        // Validate PIN
        Account account = accountService.getAccountByNumber(accountNumber);
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
}
