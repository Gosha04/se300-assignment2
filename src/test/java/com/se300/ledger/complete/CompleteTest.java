package com.se300.ledger.complete;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.NavigableMap;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.se300.ledger.Account;
import com.se300.ledger.Block;
import com.se300.ledger.Ledger;
import com.se300.ledger.LedgerException;
import com.se300.ledger.MerkleTrees;
import com.se300.ledger.Transaction;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CompleteTest {

    // shared ledger instance for test methods
    private Ledger testLedger;

    /* TODO: The following
     * 1. Achieve 100% Test Coverage
     * 2. Produce/Print Identical Results to Command Line DriverTest
     * 3. Produce Quality Report
     */

    @ParameterizedTest(name = "Creating account with name: {0}")
    @ValueSource(strings = {"mary", "bob", "bill", "frank", "jane"})
    @Tag("Param")
    void parameterizedValueSourcesTest(String value) throws LedgerException {
    System.out.println("\n=== Running parameterizedValueSourcesTest: creating account '" + value + "' ===\n");
        Account newAccount = testLedger.createAccount(value);
        System.out.println("Creating Account: " + newAccount.getAddress());
        
        assertNotNull(newAccount, "Account should be created");
        assertEquals(value, newAccount.getAddress(), "Account address should match");
    }

    @Test // FINISH REST OF GETTERS SETTERS HERE
    @Tag("Param")
    void parameterizedComplexSourcesTest() {
    System.out.println("\n=== Running parameterizedComplexSourcesTest ===\n");
        // TODO: Complete this test to demonstrate parameterized testing with complex sources like CSV, method sources, etc.
    }


    @RepeatedTest(10)
    @DisplayName("ProcessTransactionLoadTest")
    void repeatedTest(RepetitionInfo repetitionInfo) throws LedgerException {

    System.out.println("\n=== Running repeatedTest (ProcessTransactionLoadTest) repetition "
         + repetitionInfo.getCurrentRepetition() + " ===\n");

    long startTime = System.currentTimeMillis();

    Integer rep = repetitionInfo.getCurrentRepetition();
    int totalRep = repetitionInfo.getTotalRepetitions();
    System.out.println("Running Repetition " + rep + " of " + totalRep);

    assertEquals(0, testLedger.getNumberOfBlocks(), "No committed blocks at start");

    Account a = testLedger.getUncommittedBlock().getAccount("test-account-A");
    Account b = testLedger.getUncommittedBlock().getAccount("test-account-B");

    for (int i = 1; i <= 9; i++) {
        String txId = rep + "-" + i; // unique ID per repetition
    Transaction tx = new Transaction(txId,0,10,"transaction " + i,a,b);
    System.out.println("Processing Transaction: " + tx.getTransactionId() + " " + tx.getAmount() + " " + tx.getFee() + " " + tx.getNote() + " " + tx.getPayer().getAddress() + " " + tx.getReceiver().getAddress());
    testLedger.processTransaction(tx);
    }

    // still uncommitted until 10th tx happens elsewhere
    assertEquals(1, testLedger.getNumberOfBlocks(), "10 commited blocks");

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    System.out.println("Repetition " + rep + " completed in " + duration + " ms");    
}

    @BeforeEach
    void setUp() throws LedgerException{
        // initialize and reset ledger to a clean state
        System.out.println("Setting up...");
        System.out.println("Creating Ledger: test ledger test-Ledger test-ledger");
        testLedger = Ledger.getInstance("test ledger", "test-Ledger", "test-ledger");
        testLedger.reset();

        // create account A and set balance
        Account a = testLedger.createAccount("test-account-A");
        System.out.println("Creating Account: " + a.getAddress());
        a.setBalance(1000);

        // create account B by cloning A 
        Account b = (Account) a.clone();
        b.setAddress("test-account-B");
        testLedger.getUncommittedBlock().addAccount(b.getAddress(), b);
        System.out.println("Creating Account: " + b.getAddress());

        // create a transaction between A and B + process
        String txId = "init-transaction";
        Transaction testTransaction = new Transaction(txId, 100, 10, "Initial", a, b);
        System.out.println("Processing Transaction: " + testTransaction.getTransactionId() + 
            " " + testTransaction.getAmount() + " " + testTransaction.getFee() + " " 
            + testTransaction.getNote() + " " + testTransaction.getPayer().getAddress() 
            + " " + testTransaction.getReceiver().getAddress());
        testLedger.processTransaction(testTransaction);

        // System.out.println("TEST SET UP");
    }


    @AfterEach
    void tearDown() {
        // TODO: Complete this teardown method for lifecycle demonstration
    System.out.println("--- tearDown: cleaning up test ledger ---");
        testLedger = null;
        // System.out.println("Test Ledger is torn down");
    }


    @Test
    void lifeCycleTest() {
    System.out.println("\n=== Running lifeCycleTest ===\n");
        // TODO: Complete this test to demonstrate test lifecycle with BeforeEach, AfterEach, BeforeAll, AfterAll
    }

    @Test
    void conditionalTest() { // you can copy over the last of the assert all things I did, I commented it
    System.out.println("\n=== Running conditionalTest ===\n");
        // TODO: Complete this test to demonstrate conditional test execution based on condition
    }

    @Test
    void taggedTest() { // Done needs print
    System.out.println("\n=== Running taggedTest ===\n");
        // TODO: Complete this test to demonstrate test tagging for selective execution
    }

    @Nested
    @DisplayName("Validation Nested Tests")
    class NestedTestClass {
    private Ledger nestedLedger = null;

        @BeforeEach
        void nestedSetUp() throws LedgerException {
            // Reset the singleton so each test starts clean
            nestedLedger = Ledger.getInstance("nested ledger", "nested-Ledger", "nested-ledger");
            nestedLedger.reset();

            // Create zero-balance accounts
            Account a = nestedLedger.createAccount("test-account-A");
            Account b = nestedLedger.createAccount("test-account-B");

            // Master pays all fees so totalBalances + fees == Integer.MAX_VALUE
            Account master = nestedLedger.getUncommittedBlock().getAccount("master");

            // Commit exactly 10 valid transactions -> 1 committed block
            for (int i = 1; i <= 10; i++) {
                String txId = "tx-" + i;
                Transaction tx = new Transaction(txId, 0, 10, "transaction " + i, master, a);
                nestedLedger.processTransaction(tx);
            }

            assertEquals(1, nestedLedger.getNumberOfBlocks(), "Should have 1 committed block");
        }

        @Test
        @DisplayName("validate(): happy path passes after 1 good commit")
        void validate_ok() {
            assertDoesNotThrow(() -> nestedLedger.validate());
        }

        @Test
        @DisplayName("validate(): throws when no block has been committed")
        void validate_noCommittedBlocks() {
            nestedLedger.reset();  // blockMap becomes empty
            LedgerException ex = assertThrows(LedgerException.class, () -> nestedLedger.validate());
            assertNotNull(ex); // don't inspect message; it may be null
        }

        @Test
        @DisplayName("validate(): throws if a committed block has txn count != 10")
        void validate_badTxnCount() throws LedgerException {
            // From the committed state (1 block), remove one txn from block #1
            com.se300.ledger.Block b1 = nestedLedger.getBlock(1);
            assertNotNull(b1);
            assertFalse(b1.getTransactionList().isEmpty(), "Expected at least 1 committed txn to remove");
            b1.getTransactionList().remove(0);

            LedgerException ex = assertThrows(LedgerException.class, () -> nestedLedger.validate());
            assertNotNull(ex);
        }

        @Test
        @DisplayName("validate(): throws on hash inconsistency between block.prevHash and prevBlock.hash")
        void validate_badHashLink() throws LedgerException {
            // Create a 2nd committed block (20 total tx) with master paying fees
            Account master = nestedLedger.getUncommittedBlock().getAccount("master");
            Account a = nestedLedger.getUncommittedBlock().getAccount("test-account-A");
            for (int i = 11; i <= 20; i++) {
                String txId = "tx-" + i;
                nestedLedger.processTransaction(new Transaction(txId, 0, 10, "transaction " + i, master, a));
            }
            assertEquals(2, nestedLedger.getNumberOfBlocks(), "Should have 2 committed blocks");

            // Corrupt block #1's hash so block #2's previousHash won't match
            com.se300.ledger.Block block1 = nestedLedger.getBlock(1);
            assertNotNull(block1);
            block1.setHash("BROKEN-HASH");

            LedgerException ex = assertThrows(LedgerException.class, () -> nestedLedger.validate());
            assertNotNull(ex);
        }

        @Test
        @DisplayName("validate(): throws when balances + fees != Integer.MAX_VALUE")
        void validate_badAdjustedBalance() throws LedgerException {
            // Nudge a committed account's balance to break the sum invariant
            com.se300.ledger.Block block1 = nestedLedger.getBlock(1);
            assertNotNull(block1);
            Account tamper = block1.getAccount("test-account-B");
            assertNotNull(tamper, "Committed block should contain account test-account-B");
            tamper.setBalance(tamper.getBalance() + 1);

            LedgerException ex = assertThrows(LedgerException.class, () -> nestedLedger.validate());
            assertNotNull(ex);
        }

        @AfterEach
        void nestedTearDown() {
            nestedLedger = null;
        }
    }

    @Test
    @Order(1)
    @Tag("Assertion")
    void basicAssertionsTest() throws LedgerException {
    System.out.println("\n=== Running basicAssertionsTest ===\n");
        // TODO: Complete this test to demonstrate basic assertions (assertEquals, assertTrue, assertFalse, etc.)
        // TODO: At least 5 different basic assertions

        MerkleTrees mt = new MerkleTrees(List.of("seed")); 
        String out = mt.getSHA2HexValue(null);
        assertEquals("", out); // catch path returns empty string

        assertNotNull(testLedger);
        
        Account testEquals = new Account("Horatio", 0);
        Account clone = (Account) testEquals.clone();
        assertEquals(testEquals.getBalance(), clone.getBalance());
        assertEquals(testEquals.getAddress(), clone.getAddress());

        assertNotEquals(testEquals, clone);
        assertNotEquals(testLedger.getUncommittedBlock().getAccount("test-account-A"),
        testLedger.getUncommittedBlock().getAccount("test-account-B"));

        assertEquals(1100, testLedger.getUncommittedBlock().getAccount("test-account-B").getBalance());

        assertFalse(testLedger.getUncommittedBlock().getAccount("test-account-B").getBalance() == 890); // I think wrong
    }

    @Test
    @Order(2)
    @Tag("Assertion")
    void advancedAssertionsTest() {
    System.out.println("\n=== Running advancedAssertionsTest ===\n");
        // TODO: Complete this test to demonstrate advanced assertions (assertAll, assertThrows, assertTimeout, etc.)
        // TODO: At least 5 different advanced assertions
        // assert that querying committed balances throws when no block is committed

        // example: assertThrows for a LedgerException path (fee < 10)
        Account payer = testLedger.getUncommittedBlock().getAccount("test-account-A");
        System.out.println("Processing: createAccount for address: " + payer.getAddress());

        assertThrows(LedgerException.class, () -> testLedger.createAccount(payer.getAddress()));
        System.out.println("Throwing Exception: Account already exists for address: " + payer.getAddress());


        Account receiver = testLedger.getUncommittedBlock().getAccount("test-account-B");
        Transaction badFeeTx = new Transaction("badfee-" + UUID.randomUUID().toString(), 10, 5, "bad fee", payer, receiver);
        assertThrows(LedgerException.class, () -> testLedger.processTransaction(badFeeTx));

        // prepare a duplicate transaction id to exercise the duplicate-id path
    
        assertDoesNotThrow(() -> {
            Transaction initialDup = new Transaction("duplicate", 1, 10, "dup-init", payer, receiver);
            testLedger.processTransaction(initialDup);
        });

        // group assertions that each LedgerException path is thrown as expected
        assertAll("ledger exception paths",
            () -> assertThrows(LedgerException.class, () -> testLedger.getBlock(0)),
            () -> assertThrows(LedgerException.class, () -> testLedger.getAccountBalance("no-such")),
            () -> assertThrows(LedgerException.class, () -> {
                Transaction neg = new Transaction("negative", -1, 10, "neg", payer, receiver);
                testLedger.processTransaction(neg);
            }),
            () -> assertThrows(LedgerException.class, () -> {
                Transaction lowFee = new Transaction("fee", 10, 5, "low fee", payer, receiver);
                testLedger.processTransaction(lowFee);
            }),
            () -> assertThrows(LedgerException.class, () -> {
                String longNote = new String(new char[1025]).replace('\0', 'x');
                Transaction longNoteTx = new Transaction("note", 10, 10, longNote, payer, receiver);
                testLedger.processTransaction(longNoteTx);
            }),
            () -> assertThrows(LedgerException.class, () -> {
                Transaction dup = new Transaction("duplicate", 1, 10, "dup-again", payer, receiver);
                testLedger.processTransaction(dup);
            }),
            () -> assertThrows(LedgerException.class, () -> {
                Transaction insuf = new Transaction("insuf", 1000000, 10, "no money", payer, receiver);
                testLedger.processTransaction(insuf);
            }),
            () -> assertThrows(LedgerException.class, () -> {
                Account maxPayer = new Account("maximumPay", Integer.MAX_VALUE+1);
                Account maxReciever = new Account("maxReciever", 0);
                Transaction maxedOut = new Transaction("maximum value", Integer.MAX_VALUE+1,
                     10, "max int", maxPayer, maxReciever);
                System.out.println("Processing Transaction: " + maxedOut.getTransactionId() +
                    " " + maxedOut.getAmount() + " " + maxedOut.getFee() + " " 
                    + maxedOut.getNote() + " " + maxedOut.getPayer().getAddress() + " "
                    + maxedOut.getReceiver().getAddress());
                testLedger.processTransaction(maxedOut);
            }),
            () -> assertThrows(LedgerException.class, () -> { // this could get moved to conditional probably
                Account a = testLedger.getUncommittedBlock().getAccount("test-account-A");
                Account b = testLedger.getUncommittedBlock().getAccount("test-account-B");
                
                for (int i = 1; i <= 9; i++) {
                String txId =  "Assumption -" + i; // unique ID per repetition
                    Transaction tx = new Transaction(txId,0,10,"transaction " + i, a, b);
                    System.out.println("Processing Transaction: " + tx.getTransactionId() + " " + tx.getAmount() 
                    + " " + tx.getFee() + " " + tx.getNote() + " " + tx.getPayer().getAddress() + " " 
                    + tx.getReceiver().getAddress());
                    testLedger.processTransaction(tx);
                }
                testLedger.getAccountBalance("no acc");
            })
        );

        assertTimeout(Duration.ofSeconds(20), () -> {
            // after loop block should be committed
            assertNotNull(testLedger.getBlock(1));
            assertEquals(1, testLedger.getNumberOfBlocks());
            assertNotNull(testLedger.getAccountBalance("test-account-A"));
        });

        // assertDoesNotThrow for processing a valid transaction
        Transaction validTx = new Transaction("valid", 1, 10, "ok", payer, receiver);
        assertDoesNotThrow(() -> testLedger.processTransaction(validTx));

        // assertIterableEquals: verify transaction IDs list is consistent
        java.util.List<String> expectedIds = new java.util.ArrayList<>();
        for (Transaction t : testLedger.getUncommittedBlock().getTransactionList()) {
            expectedIds.add(t.getTransactionId());
        }
        java.util.List<String> actualIds = new java.util.ArrayList<>(expectedIds);
        assertIterableEquals(expectedIds, actualIds);
    }

    @Test // Doesn't affect coverage
    @Tag("Mock")
    void mockBehaviorTest() throws LedgerException {
    System.out.println("\n=== Running mockBehaviorTest ===\n");
        // TODO: Complete this test to demonstrate configuring mock behavior (when/then, doReturn/when, etc.)
        // TODO: At least 3 different behaviors
        Ledger mockLedger = mock(Ledger.class);
        
        when(mockLedger.getAccountBalance(eq("alice"))).thenReturn(100);

        when(mockLedger.getAccountBalance(eq("missing")))
            .thenThrow(new LedgerException("Get Account Balance", "Account Does Not Exist"));
        
        when(mockLedger.getAccountBalance(isNull()))
            .thenThrow(new LedgerException("Get Account Balance", "Account Does Not Exist"));
            
        NavigableMap <Integer,Block> mockMap = null;
        doReturn(mockMap).when(mockLedger).getAccountBalances();

        given(mockLedger.getAccountBalance("alice")).willReturn(100);
    }

    @Test
    @Order(3)
    void assumptionsTest() throws LedgerException {
    System.out.println("\n=== Running assumptionsTest ===\n");
        Account a = testLedger.getUncommittedBlock().getAccount("test-account-A");
        Account b = testLedger.getUncommittedBlock().getAccount("test-account-B");
        // TODO: Complete this test to demonstrate using assumptions (assumeTrue, assumeFalse, assumingThat, etc.)
        // TODO: At least 3 different assumptions

        System.out.println("Assuming only uncommited block, getAccountBalances should return null");
        assumeTrue(testLedger.getNumberOfBlocks() == 0, "only uncommmited block");
        assertNull(testLedger.getAccountBalances());


        assumingThat(testLedger.getNumberOfBlocks() == 0,
        () -> {
            for (int i = 1; i <= 9; i++) {
                String txId =  "Assumption -" + i; // unique ID per repetition
                    Transaction tx = new Transaction(txId,0,10,"transaction " + i, a, b);
                    System.out.println("Processing Transaction: " + tx.getTransactionId() + " " + tx.getAmount() 
                    + " " + tx.getFee() + " " + tx.getNote() + " " + tx.getPayer().getAddress() + " " 
                    + tx.getReceiver().getAddress());
                    testLedger.processTransaction(tx);
            }
            assertTrue(testLedger.getAccountBalances() != null);
        });

        assumeFalse(testLedger.getTransaction("init-transaction") == null);
        assertNotNull(testLedger.getTransaction("init-transaction"));
    }


    @Test
    @Tag("Mock")
    void mockVerificationTest() throws LedgerException{
    System.out.println("\n=== Running mockVerificationTest ===\n");
        // TODO: Complete this test to demonstrate verifying mock interactions (verify, times, never, etc.)
        // TODO: At least 3 different interactions
        Ledger mockLedger = mock(Ledger.class);

        mockLedger.getAccountBalance("alice");

        verify(mockLedger).getAccountBalance("alice");
        verify(mockLedger, times(1)).getAccountBalance("alice");

        verify(mockLedger, never()).getAccountBalance("bob");

        verify(mockLedger, atLeast(1)).getAccountBalance(anyString());
        verifyNoMoreInteractions(mockLedger);   
    }

    @Test
    @Tag("Mock")
    void mockArgumentMatchersTest() throws LedgerException{
    System.out.println("\n=== Running mockArgumentMatchersTest ===\n");
        // TODO: Complete this test to demonstrate using argument matchers with mocks (any(), eq(), etc.)
        // TODO: At least 3 different argument matchers

    }

    @Test
    void methodOrderTest() { //Done needs print
    System.out.println("\n=== Running methodOrderTest ===\n");
        // TODO: Complete this test to demonstrate test method ordering using @TestMethodOrder and @Order annotations
    }
}
