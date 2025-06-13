package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionChannel;
import com.example.demo.model.User;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ParallelTransactionTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private static final double INITIAL_BALANCE = 10000.0;
    private static final int NUM_THREADS = 10;
    private static final int NUM_TRANSACTIONS = 100;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Generate unique IDs for this test run
        String sourceUserId = "testUser1_" + UUID.randomUUID().toString().substring(0, 8);
        String destUserId = "testUser2_" + UUID.randomUUID().toString().substring(0, 8);
        String sourceAccNum = "" + UUID.randomUUID().toString().substring(0, 7);
        String destAccNum = "" + UUID.randomUUID().toString().substring(0, 7);

        // Create test accounts
        Account sourceAccount = new Account();
        sourceAccount.setAccountNumber(sourceAccNum);
        sourceAccount.setBalance(INITIAL_BALANCE);
        sourceAccount.setId(sourceUserId);
        accountRepository.save(sourceAccount);

        Account destAccount = new Account();
        destAccount.setAccountNumber(destAccNum);
        destAccount.setBalance(0.0);
        destAccount.setId(destUserId);
        accountRepository.save(destAccount);

        // Create test users with PINs
        User sourceUser = new User();
        sourceUser.setId(sourceUserId);
        sourceUser.setPin("{noop}1234");
        userRepository.save(sourceUser);

        User destUser = new User();
        destUser.setId(destUserId);
        destUser.setPin("{noop}5678");
        userRepository.save(destUser);

        sourceAccountNumber = sourceAccount.getAccountNumber();
        destinationAccountNumber = destAccount.getAccountNumber();
    }

    @Test
    void testParallelDeposits() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_TRANSACTIONS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < NUM_TRANSACTIONS; i++) {
            executor.submit(() -> {
                try {
                    transactionService.deposit(sourceAccountNumber, 100.0,
                            TransactionChannel.OTC, "Parallel deposit test");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify final balance
        Account finalAccount = accountRepository.findById(sourceAccountNumber).orElseThrow();
        double expectedBalance = INITIAL_BALANCE + (successCount.get() * 100.0);

        assertEquals(expectedBalance, finalAccount.getBalance(), 0.001);
        assertEquals(NUM_TRANSACTIONS, successCount.get() + failureCount.get());

        // Verify no duplicate transactions
        List<Transaction> transactions = transactionRepository
                .findByAccountNumberOrderByDateAscTimeAsc(sourceAccountNumber);
        assertEquals(successCount.get(), transactions.size());
    }

    @Test
    void testParallelTransfers() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_TRANSACTIONS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < NUM_TRANSACTIONS; i++) {
            executor.submit(() -> {
                try {
                    transactionService.transfer(sourceAccountNumber, destinationAccountNumber,
                            100.0, "1234");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify final balances
        Account sourceAccount = accountRepository.findById(sourceAccountNumber).orElseThrow();
        Account destAccount = accountRepository.findById(destinationAccountNumber).orElseThrow();

        double expectedSourceBalance = INITIAL_BALANCE - (successCount.get() * 100.0);
        double expectedDestBalance = successCount.get() * 100.0;

        assertEquals(expectedSourceBalance, sourceAccount.getBalance(), 0.001);
        assertEquals(expectedDestBalance, destAccount.getBalance(), 0.001);
        assertEquals(NUM_TRANSACTIONS, successCount.get() + failureCount.get());

        // Verify no duplicate transactions
        List<Transaction> sourceTransactions = transactionRepository
                .findByAccountNumberOrderByDateAscTimeAsc(sourceAccountNumber);
        List<Transaction> destTransactions = transactionRepository
                .findByAccountNumberOrderByDateAscTimeAsc(destinationAccountNumber);

        System.out.println("sourceTransactions: " + sourceTransactions.size());
        System.out.println("destTransactions: " + destTransactions.size());
        System.out.println("successCount: " + successCount.get());
        System.out.println("failureCount: " + failureCount.get());
        assertEquals(successCount.get(), sourceTransactions.size());
        assertEquals(successCount.get(), destTransactions.size());
    }

    @Test
    void testMixedParallelOperations() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_TRANSACTIONS * 2);
        AtomicInteger transferSuccessCount = new AtomicInteger(0);
        AtomicInteger depositSuccessCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Submit transfer operations
        for (int i = 0; i < NUM_TRANSACTIONS; i++) {
            executor.submit(() -> {
                try {
                    transactionService.transfer(sourceAccountNumber, destinationAccountNumber,
                            100.0, "1234");
                    transferSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Submit deposit operations
        for (int i = 0; i < NUM_TRANSACTIONS; i++) {
            executor.submit(() -> {
                try {
                    transactionService.deposit(destinationAccountNumber, 50.0,
                            TransactionChannel.OTC, "Mixed parallel test");
                    depositSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify final balances
        Account sourceAccount = accountRepository.findById(sourceAccountNumber).orElseThrow();
        Account destAccount = accountRepository.findById(destinationAccountNumber).orElseThrow();

        double expectedSourceBalance = INITIAL_BALANCE - (transferSuccessCount.get() * 100.0);
        double expectedDestBalance = (transferSuccessCount.get() * 100.0) + (depositSuccessCount.get() * 50.0);

        assertEquals(expectedSourceBalance, sourceAccount.getBalance(), 0.001);
        assertEquals(expectedDestBalance, destAccount.getBalance(), 0.001);
        assertEquals(NUM_TRANSACTIONS * 2, transferSuccessCount.get() + depositSuccessCount.get() + failureCount.get());

        // Verify no duplicate transactions
        List<Transaction> sourceTransactions = transactionRepository
                .findByAccountNumberOrderByDateAscTimeAsc(sourceAccountNumber);
        List<Transaction> destTransactions = transactionRepository
                .findByAccountNumberOrderByDateAscTimeAsc(destinationAccountNumber);
        System.out.println("sourceTransactions: " + sourceTransactions.size());
        System.out.println("destTransactions: " + destTransactions.size());
        System.out.println("transferSuccessCount: " + transferSuccessCount.get());
        System.out.println("depositSuccessCount: " + depositSuccessCount.get());
        System.out.println("failureCount: " + failureCount.get());
        assertEquals(transferSuccessCount.get(), sourceTransactions.size());
        assertEquals(transferSuccessCount.get() + depositSuccessCount.get(), destTransactions.size());
    }
}