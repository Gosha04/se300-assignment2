package com.se300.ledger.complete;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TransferQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        // probably want to set variables and create blocks, accounts, etc.
        testLedger = Ledger.getInstance("test ledger",
         "test-Ledger", "test-ledger");
        
        testLedger.createAccount("test-account-A");
        testLedger.getUncommittedBlock().getAccount("test-account-A").setBalance(1000);
        testLedger.getUncommittedBlock().getAccount("test-account-A").clone(); // think this lets us have 2 accounts
        testLedger.getUncommittedBlock().getAccount("test-account-A").setAddress("test-account-B");
         
        Transaction testTransaction = new Transaction();
        

        


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

    class NestedTestClass {
        @Test
        void nestedTest() {
            // TODO: Complete this test to demonstrate nested test classes
        }
    }

    @Test
    void basicAssertionsTest() {
        // TODO: Complete this test to demonstrate basic assertions (assertEquals, assertTrue, assertFalse, etc.)
        // TODO: At least 5 different basic assertions
        assertNotNull(testLedger);
        
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
