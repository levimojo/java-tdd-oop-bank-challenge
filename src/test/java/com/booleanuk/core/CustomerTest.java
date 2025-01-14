package com.booleanuk.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class CustomerTest {

    Customer customer;
    Manager manager;

    @BeforeEach
    void setUp() {
        customer = new Customer("customer");
        customer.createCurrent(123);
        customer.createSavings(321);
        manager = new Manager("manager");
    }

    @Test
    void shouldCreateCurrentAccount() {
        customer.setCurrent(null);
        customer.createCurrent(123);
        Assertions.assertEquals(123, customer.getCurrent().getNumber());
    }

    @Test
    void shouldCreateSavingsAccount() {
        customer.setSavings(null);
        customer.createSavings(321);
        Assertions.assertEquals(321, customer.getSavings().getNumber());
    }

    @Test
    void shouldCheckIfNumberValidAndReturnAccount() {
        Assertions.assertEquals(customer.getCurrent(), customer.accountCheck(123));
        Assertions.assertEquals(customer.getSavings(), customer.accountCheck(321));
        Assertions.assertEquals(null, customer.accountCheck(3212));
    }

    @Test
    void shouldDepositFundsOnTheAccount() {
        customer.depositFunds(123, BigDecimal.valueOf(500));
        Assertions.assertEquals(BigDecimal.valueOf(500), customer.getCurrent().getBalance());

        customer.depositFunds(321, BigDecimal.valueOf(125));
        Assertions.assertEquals(BigDecimal.valueOf(125), customer.getSavings().getBalance());

        customer.depositFunds(111, BigDecimal.valueOf(125));
        Assertions.assertEquals(BigDecimal.valueOf(500), customer.getCurrent().getBalance());
        Assertions.assertEquals(BigDecimal.valueOf(125), customer.getSavings().getBalance());
    }

    @Test
    void shouldWithdrawFundsFromTheAccount() {
        customer.depositFunds(123, BigDecimal.valueOf(500));
        customer.depositFunds(321, BigDecimal.valueOf(125));

        customer.withdrawFunds(111, BigDecimal.valueOf(100));
        Assertions.assertEquals(BigDecimal.valueOf(500), customer.getCurrent().getBalance());
        Assertions.assertEquals(BigDecimal.valueOf(125), customer.getSavings().getBalance());

        customer.withdrawFunds(123, BigDecimal.valueOf(1000));
        Assertions.assertEquals(BigDecimal.valueOf(500), customer.getCurrent().getBalance());

        customer.withdrawFunds(123, BigDecimal.valueOf(100));
        Assertions.assertEquals(BigDecimal.valueOf(400), customer.getCurrent().getBalance());

        customer.withdrawFunds(321, BigDecimal.valueOf(100));
        Assertions.assertEquals(BigDecimal.valueOf(25), customer.getSavings().getBalance());

        customer.setOverCheck(true);
        customer.requestOverdraft(BigDecimal.valueOf(100));
        Assertions.assertEquals(true, customer.isOverCheck());
        Assertions.assertEquals(BigDecimal.valueOf(100), customer.getOverAmount());
        customer.withdrawFunds(123, BigDecimal.valueOf(410));
        Assertions.assertEquals(BigDecimal.valueOf(90), customer.getOverAmount());
        Assertions.assertEquals(BigDecimal.valueOf(-10), customer.getCurrent().getBalance());
        customer.withdrawFunds(123, BigDecimal.valueOf(100));
        Assertions.assertEquals(BigDecimal.valueOf(-10), customer.getCurrent().getBalance());
        customer.withdrawFunds(123, BigDecimal.valueOf(80));
        Assertions.assertEquals(BigDecimal.valueOf(10), customer.getOverAmount());
        Assertions.assertEquals(BigDecimal.valueOf(-90), customer.getCurrent().getBalance());
    }

    @Test
    void shouldSetOverdraftAmount() {
        Assertions.assertEquals(BigDecimal.ZERO, customer.getOverAmount());

        manager.overdraftRequest(customer);
        customer.requestOverdraft(BigDecimal.valueOf(500));
        Assertions.assertEquals(BigDecimal.ZERO, customer.getOverAmount());

        customer.depositFunds(123, BigDecimal.valueOf(500.00));
        customer.withdrawFunds(123, BigDecimal.valueOf(100.00));
        customer.depositFunds(123, BigDecimal.valueOf(250.00));
        customer.depositFunds(123, BigDecimal.valueOf(500.00));
        customer.withdrawFunds(123, BigDecimal.valueOf(100.00));
        customer.depositFunds(123, BigDecimal.valueOf(250.00));

        manager.overdraftRequest(customer);
        customer.requestOverdraft(BigDecimal.valueOf(500));
        Assertions.assertEquals(BigDecimal.valueOf(500), customer.getOverAmount());
    }

    @Test
    void souldReturnBankStatement() {
        customer.depositFunds(123, BigDecimal.valueOf(500.00));
        customer.withdrawFunds(123, BigDecimal.valueOf(100.00));
        customer.depositFunds(123, BigDecimal.valueOf(250.00));

        String expected1 = "date       || credit || debit  || balance\n" +
                "18/07/2023 || 500.0  ||        || 500.0\n" +
                "18/07/2023 ||        || 100.0  || 400.0\n" +
                "18/07/2023 || 250.0  ||        || 650.0\n";
        Assertions.assertEquals(expected1, customer.generateBankStatement(123));

        customer.depositFunds(321, BigDecimal.valueOf(225.00));
        customer.withdrawFunds(321, BigDecimal.valueOf(100.00));
        customer.withdrawFunds(321, BigDecimal.valueOf(110.00));

        String expected2 = "date       || credit || debit  || balance\n" +
                "18/07/2023 || 225.0  ||        || 225.0\n" +
                "18/07/2023 ||        || 100.0  || 125.0\n" +
                "18/07/2023 ||        || 110.0  || 15.0\n";
        Assertions.assertEquals(expected2, customer.generateBankStatement(321));
    }
}
