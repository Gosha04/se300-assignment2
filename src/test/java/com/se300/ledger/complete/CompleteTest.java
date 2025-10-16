package com.se300.ledger.complete;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.se300.ledger.Account;
import com.se300.ledger.Ledger;
import com.se300.ledger.LedgerException;
import com.se300.ledger.Transaction;

public class CompleteTest {

    // shared ledger instance for test methods
    private Ledger testLedger;

    /* TODO: The following
     * 1. Achieve 100% Test Coverage
     * 2. Produce/Print Identical Results to Command Line DriverTest
     * 3. Produce Quality Report
     */

    @ParameterizedTest
    @ValueSource(strings = {"mary", "bob", "bill", "frank", "jane"})
    void parameterizedValueSourcesTest(String value) throws LedgerException {
        
    }

    @Test
    void parameterizedComplexSourcesTest() {
        // TODO: Complete this test to demonstrate parameterized testing with complex sources like CSV, method sources, etc.
    }


    @RepeatedTest(5)
    @DisplayName("ProcessTransactionLoadTest")
    void repeatedTest(RepetitionInfo repetitionInfo) {
        
        int rep = repetitionInfo.getCurrentRepetition();
        int totalRep = repetitionInfo.getTotalRepetitions();
        System.out.println("Running Repetition " + rep + " of " + totalRep);
    }

    @BeforeEach
    void setUp() throws LedgerException{
        // initialize and reset ledger to a clean state
        testLedger = Ledger.getInstance("test ledger", "test-Ledger", "test-ledger");
        testLedger.reset();

        // create account A and set balance
        Account a = testLedger.createAccount("test-account-A");
        a.setBalance(1000);

        // create account B by cloning A 
        Account b = (Account) a.clone();
        b.setAddress("test-account-B");
        testLedger.getUncommittedBlock().addAccount(b.getAddress(), b);

        // create a transaction between A and B + process
        String txId = "init-transaction-" + UUID.randomUUID().toString();
        Transaction testTransaction = new Transaction(txId, 100, 10, "Initial", a, b);
        testLedger.processTransaction(testTransaction);

        System.out.println("Test Ledger is set up");
    }


    @AfterEach
    void tearDown() {
        // TODO: Complete this teardown method for lifecycle demonstration
        testLedger = null;
        System.out.println("Test Ledger is torn down");
    }


    @Test
    void lifeCycleTest() {
        // TODO: Complete this test to demonstrate test lifecycle with BeforeEach, AfterEach, BeforeAll, AfterAll
    }

    @Test
    void conditionalTest() {
        // TODO: Complete this test to demonstrate conditional test execution based on condition
    }

    @Test
    void taggedTest() {
        // TODO: Complete this test to demonstrate test tagging for selective execution
    }

    @Nested
    class NestedTestClass {
        @Test
        void nestedTest() {
            // TODO: Complete this test to demonstrate nested test classes
        }
    }

    @Test
    void basicAssertionsTest() throws LedgerException {
        // TODO: Complete this test to demonstrate basic assertions (assertEquals, assertTrue, assertFalse, etc.)
        // TODO: At least 5 different basic assertions
        assertNotNull(testLedger);
        
        Account testEquals = new Account("Horatio", 0);
        Account clone = (Account) testEquals.clone();
        assertEquals(testEquals, clone);

        assertNotEquals(testLedger.getBlock(1).getAccount("test-account-A"),
        testLedger.getBlock(1).getAccount("test-account-B"));

        assertTrue(null);
        
    }

    @Test
    void advancedAssertionsTest() {
        // TODO: Complete this test to demonstrate advanced assertions (assertAll, assertThrows, assertTimeout, etc.)
        // TODO: At least 5 different advanced assertions
    }

    @Test
    void mockBehaviorTest() {
        // TODO: Complete this test to demonstrate configuring mock behavior (when/then, doReturn/when, etc.)
        // TODO: At least 3 different behaviors
    }

    @Test
    void assumptionsTest() {
        // TODO: Complete this test to demonstrate using assumptions (assumeTrue, assumeFalse, assumingThat, etc.)
        // TODO: At least 3 different assumptions
    }


    @Test
    void mockVerificationTest() {
        // TODO: Complete this test to demonstrate verifying mock interactions (verify, times, never, etc.)
        // TODO: At least 3 different interactions
    }

    @Test
    void mockArgumentMatchersTest() {
        // TODO: Complete this test to demonstrate using argument matchers with mocks (any(), eq(), etc.)
        // TODO: At least 3 different argument matchers
    }

    @Test
    void methodOrderTest() {
        // TODO: Complete this test to demonstrate test method ordering using @TestMethodOrder and @Order annotations
    }
}
