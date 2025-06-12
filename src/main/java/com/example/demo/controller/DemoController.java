package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.demo.service.AccountService;
import com.example.demo.service.UserService;
import com.example.demo.model.Account;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class DemoController {
    private final AccountService accountService;
    private final UserService userService;

    @Autowired
    public DemoController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("");
        // String email = auth.getName();

        model.addAttribute("userRole", role);
        return "menu";
    }

    @GetMapping("/account/create")
    public String showCreateAccountForm() {
        return "account/create";
    }

    @GetMapping("/transaction/deposit")
    public String showDepositPage() {
        return "deposit";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/transaction/transfer")
    public String showTransferPage(Model model) {
        // Get user's account number
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        System.out.println("email: " + email);
        // Get user's account number
        String accountNumber = "";
        try {
            Account account = accountService.getAccountByUserId(userService.getUserIdByEmail(email));
            accountNumber = account.getAccountNumber();
            System.out.println("accountNumber: " + accountNumber);
        } catch (Exception e) {
            // Handle case where user doesn't have an account yet
            System.out.println("Error: " + e.getMessage());
            accountNumber = "";
        }
        model.addAttribute("accountNumber", accountNumber);
        return "transfer";
    }

    @GetMapping("/account/info")
    public String showAccountInfoPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        try {
            String userId = userService.getUserIdByEmail(email);
            Account account = accountService.getAccountByUserId(userId);
            model.addAttribute("accountNumber", account.getAccountNumber());

        } catch (Exception e) {
            System.out.println("Error getting account info: " + e.getMessage());
            model.addAttribute("accountNumber", "");
        }
        return "account/info";
    }

    @GetMapping("/account/statement")
    public String showAccountStatementPage(Model model) throws Exception {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            String accountNumber = accountService.getAccountByUserId(userService.getUserIdByEmail(email))
                    .getAccountNumber();
            model.addAttribute("accountNumber", accountNumber);
        } catch (Exception e) {
            System.out.println("Error getting account statement: " + e.getMessage());
            model.addAttribute("accountNumber", "");
        }
        return "account/statement";
    }
}
