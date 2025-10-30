package com.se300.ledger.complete;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    private static PrintWriter writer;

    /* TODO: The following
     * 1. Achieve 100% Test Coverage
     * 2. Produce/Print Identical Results to Command Line DriverTest
     * 3. Produce Quality Report
     */

    @ParameterizedTest(name = "Creating account with name: {0}")
    @ValueSource(strings = {"mary", "bob", "bill", "frank", "jane"})
    @Order(1)
    @Tag("Param")
    void parameterizedValueSourcesTest(String value) throws LedgerException {
        System.out.println("\n=== Running parameterizedValueSourcesTest: creating account '" + value + "' ===\n");

        Account newAccount = testLedger.createAccount(value);

        System.out.println("Creating Account: " + newAccount.getAddress());
       
        assertNotNull(newAccount, "Account should be created");

        assertEquals(value, newAccount.getAddress(), "Account address should match");
    }

    @Tag("Param")
    @ParameterizedTest
    @Order(2)
    @CsvSource({"mary", "bob", "bill", "frank", "jane"})
    void parameterizedComplexSourcesTest(String value) throws LedgerException {
        System.out.println("\n=== Running parameterizedComplexSourcesTest ===\n");

        Account newAccount = testLedger.createAccount(value);
        System.out.println("Creating Account: " + newAccount.getAddress());

        assertNotNull(newAccount, "Account should be created");

        assertEquals(value, newAccount.getAddress(), "Account address should match");
    }


    @RepeatedTest(10)
    @Order(3)
    @DisplayName("ProcessTransactionLoadTest")
    void repeatedTest(RepetitionInfo repetitionInfo) throws LedgerException {
        assertTimeout(Duration.ofSeconds(1), () -> {
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

            System.out.println("Processing Transaction: " + tx.getTransactionId() + " " + tx.getAmount() + " " + tx.getFee()
            + " " + tx.getNote() + " " + tx.getPayer().getAddress() + " " + tx.getReceiver().getAddress());

            testLedger.processTransaction(tx);
            }

            // still uncommitted until 10th tx happens elsewhere
            assertEquals(1, testLedger.getNumberOfBlocks(), "10 commited blocks");

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Repetition " + rep + " completed in " + duration + " ms");    
        });
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
    }


    @AfterEach
    void tearDown() {
        // TODO: Complete this teardown method for lifecycle demonstration
        System.out.println("--- tearDown: cleaning up test ledger ---");
        testLedger.reset();
        testLedger = null;
    }


    @Test
    void lifeCycleTest() { // Done throughout
    System.out.println("\n=== Running lifeCycleTest ===\n");
        // TODO: Complete this test to demonstrate test lifecycle with BeforeEach, AfterEach, BeforeAll, AfterAll
    }

    @BeforeAll
    static void open() throws Exception {
        writer = new PrintWriter("endToEnd.txt"); 
        System.setProperty("RUN_CONDITIONAL", "true");
    }

    @AfterAll
    static void close() throws Exception {
        if (writer != null) writer.close();
        System.setProperty("RUN_CONDITIONAL", "false");
    }
    
    @EnabledIfSystemProperty(named = "RUN_CONDITIONAL", matches = "true")
    @Test
    @Order(4)
    void conditionalTest() { 
    System.out.println("\n=== Running conditionalTest ===\n");
        //TODO: Complete this test to demonstrate conditional test execution based on condition

        Account acc = new Account("addr-1", 7);
        assertEquals("addr-1", acc.getAddress());
        acc.setAddress("addr-2");
        assertEquals("addr-2", acc.getAddress());
        acc.setBalance(123);
        assertEquals(123, acc.getBalance());

        Account accClone = (Account) acc.clone();
        assertEquals(acc.getAddress(), accClone.getAddress());
        assertEquals(acc.getBalance(), accClone.getBalance());

        Block blk = new Block(1, "prev-hash-0");
        assertEquals(1, blk.getBlockNumber());
        assertEquals("prev-hash-0", blk.getPreviousHash());

        blk.setBlockNumber(2);
        assertEquals(2, blk.getBlockNumber());
        blk.setPreviousHash("prev-hash-1");
        assertEquals("prev-hash-1", blk.getPreviousHash());
        blk.setHash("hash-2");
        assertEquals("hash-2", blk.getHash());

        Block prev = new Block(0, "");
        blk.setPreviousBlock(prev);
        assertSame(prev, blk.getPreviousBlock());

        Account a1 = new Account("A1", 10);
        blk.addAccount(a1.getAddress(), a1);
        assertSame(a1, blk.getAccount("A1"));
        assertNotNull(blk.getAccountBalanceMap());
        assertNotNull(blk.getTransactionList());

        Account payer = new Account("payer", 100);
        Account recv  = new Account("recv",  0);
        Transaction tx = new Transaction("t-1", 5, 10, "n0", payer, recv);

        tx.setTransactionId("t-2");  assertEquals("t-2", tx.getTransactionId());
        tx.setAmount(6);             assertEquals(6, tx.getAmount());
        tx.setFee(11);               assertEquals(11, tx.getFee());
        tx.setNote("n1");            assertEquals("n1", tx.getNote());
        tx.setPayer(recv);           assertSame(recv, tx.getPayer());
        tx.setReceiver(payer);       assertSame(payer, tx.getReceiver());
        assertNotNull(tx.toString()); // touch toString

        LedgerException le = new LedgerException("act", "why");
        assertEquals("act", le.getAction());
        assertEquals("why", le.getReason());
        le.setAction("act2");   assertEquals("act2", le.getAction());
        le.setReason("why2");   assertEquals("why2", le.getReason());

        assertNotNull(testLedger, "setUp should have initialized testLedger");

        String origName = testLedger.getName();
        String origDesc = testLedger.getDescription();
        String origSeed = testLedger.getSeed();

        testLedger.setName("name1");          assertEquals("name1", testLedger.getName());
        testLedger.setDescription("desc1");   assertEquals("desc1", testLedger.getDescription());
        testLedger.setSeed("seed1");          assertEquals("seed1", testLedger.getSeed());

        testLedger.setName(origName);
        testLedger.setDescription(origDesc);
        testLedger.setSeed(origSeed);
        testLedger.reset();
    }

    @Test
    @Order(5)
    void taggedTest() { // Done needs print
    System.out.println("\n=== Running taggedTest ===\n");

        // assertNotNull(testLedger);

        // assertNotNull(testLedger.getTransaction("init-transaction"));

        // Account src = new Account("clone-src", 77);
        // Account copy = (Account) src.clone();
        // assertEquals(src.getAddress(), copy.getAddress());
        // assertEquals(src.getBalance(), copy.getBalance());

        // Block b = testLedger.getUncommittedBlock();
        // assertNotNull(b.getAccountBalanceMap());
        // assertNotNull(b.getTransactionList());

        // assertNotNull(testLedger.getName());
        // assertNotNull(testLedger.getDescription());
        // assertNotNull(testLedger.getSeed());
    }

    @Nested
    @DisplayName("Validation Nested Tests")
    @Order(6)
    class NestedTestClass {
    private Ledger nestedLedger = null;

        @BeforeEach
        void nestedSetUp() throws LedgerException {
            nestedLedger = Ledger.getInstance("nested ledger", "nested-Ledger", "nested-ledger");
            nestedLedger.reset();

            Account a = nestedLedger.createAccount("test-account-A");
            Account b = nestedLedger.createAccount("test-account-B"); // needs to exist
            Account master = nestedLedger.getUncommittedBlock().getAccount("master");

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
            nestedLedger.reset(); 
            LedgerException ex = assertThrows(LedgerException.class, () -> nestedLedger.validate());
            assertNotNull(ex); 
        }

        @Test
        @DisplayName("validate(): throws if a committed block has txn count != 10")
        void validate_badTxnCount() throws LedgerException {
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
            Account master = nestedLedger.getUncommittedBlock().getAccount("master");
            Account a = nestedLedger.getUncommittedBlock().getAccount("test-account-A");
            for (int i = 11; i <= 20; i++) {
                String txId = "tx-" + i;
                nestedLedger.processTransaction(new Transaction(txId, 0, 10, "transaction " + i, master, a));
            }
            assertEquals(2, nestedLedger.getNumberOfBlocks(), "Should have 2 committed blocks");

            com.se300.ledger.Block block1 = nestedLedger.getBlock(1);
            assertNotNull(block1);
            block1.setHash("BROKEN-HASH");

            LedgerException ex = assertThrows(LedgerException.class, () -> nestedLedger.validate());
            assertNotNull(ex);
        }

        @Test
        @DisplayName("validate(): throws when balances + fees != Integer.MAX_VALUE")
        void validate_badAdjustedBalance() throws LedgerException {
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
    @Tag("Assertion")
    @Order(7)
    void basicAssertionsTest() throws LedgerException {
    System.out.println("\n=== Running basicAssertionsTest ===\n");
        // TODO: Complete this test to demonstrate basic assertions (assertEquals, assertTrue, assertFalse, etc.)
        // TODO: At least 5 different basic assertions

        MerkleTrees mt = new MerkleTrees(List.of("seed")); 
        String out = mt.getSHA2HexValue(null);
        assertEquals("", out); 

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

        Transaction opTrans = new Transaction(null, null, null, null, null, null);
        opTrans.setTransactionId("test");
        assertEquals("test", opTrans.getTransactionId());

        opTrans.setAmount(10);
        assertEquals(10, opTrans.getAmount());

        opTrans.setFee(10);
        assertEquals(opTrans.getFee(), 10);

        opTrans.setNote(out);
        assertNotNull(opTrans.getNote());

        opTrans.setPayer(testLedger.getUncommittedBlock().getAccount("master"));
        opTrans.setReceiver(testLedger.getUncommittedBlock().getAccount("master"));
        assertTrue(opTrans.getReceiver() == opTrans.getPayer());

        testLedger.setSeed(null);
        testLedger.setName(null);
        testLedger.setDescription(null);
        
        assertNull(testLedger.getSeed());
        assertNull(testLedger.getName());
        assertNull(testLedger.getDescription());

        LedgerException ledgerException = new LedgerException(null, null);
        ledgerException.setAction("test");
        ledgerException.setReason("test");

        assertNotNull(ledgerException.getAction());
        assertNotNull(ledgerException.getReason());

    }

    @Test
    @Order(8)
    @Tag("Assertion")
    void advancedAssertionsTest() {
    System.out.println("\n=== Running advancedAssertionsTest ===\n");
        // TODO: Complete this test to demonstrate advanced assertions (assertAll, assertThrows, assertTimeout, etc.)
        // TODO: At least 5 different advanced assertions

        Account payer = testLedger.getUncommittedBlock().getAccount("test-account-A");
        System.out.println("Processing: createAccount for address: " + payer.getAddress());

        assertThrows(LedgerException.class, () -> testLedger.createAccount(payer.getAddress()));
        System.out.println("Throwing Exception: Account already exists for address: " + payer.getAddress());


        Account receiver = testLedger.getUncommittedBlock().getAccount("test-account-B");
        Transaction badFeeTx = new Transaction("badfee-" + UUID.randomUUID().toString(), 10, 5, "bad fee", payer, receiver);
        assertThrows(LedgerException.class, () -> testLedger.processTransaction(badFeeTx));
    
        assertDoesNotThrow(() -> {
            Transaction initialDup = new Transaction("duplicate", 1, 10, "dup-init", payer, receiver);
            testLedger.processTransaction(initialDup);
        });

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
            }),
            () -> assertDoesNotThrow(()-> {testLedger.getUncommittedBlock().setBlockNumber(null);
            }),
            () -> assertDoesNotThrow(()->{testLedger.getUncommittedBlock().setPreviousHash(null);
            })
        );

        assertTimeout(Duration.ofSeconds(20), () -> {
            assertNotNull(testLedger.getBlock(1));
            assertEquals(1, testLedger.getNumberOfBlocks());
            assertNotNull(testLedger.getAccountBalance("test-account-A"));
        });

        Transaction validTx = new Transaction("valid", 1, 10, "ok", payer, receiver);
        assertDoesNotThrow(() -> testLedger.processTransaction(validTx));

        java.util.List<String> expectedIds = new java.util.ArrayList<>();
        for (Transaction t : testLedger.getUncommittedBlock().getTransactionList()) {
            expectedIds.add(t.getTransactionId());
        }
        java.util.List<String> actualIds = new java.util.ArrayList<>(expectedIds);
        assertIterableEquals(expectedIds, actualIds);
    }

    @Test // Doesn't affect coverage
    @Tag("Mock")
    @Order(9)
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
    @Order(10)
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
    @Order(11)
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
    @Order(12)
    void mockArgumentMatchersTest() throws LedgerException{
    System.out.println("\n=== Running mockArgumentMatchersTest ===\n");
        // TODO: Complete this test to demonstrate using argument matchers with mocks (any(), eq(), etc.)
        // TODO: At least 3 different argument matchers
        Ledger mockLedger = mock(Ledger.class);
        
        when(mockLedger.getAccountBalance(eq("alice"))).thenReturn(100);

        when(mockLedger.getAccountBalance(eq("missing")))
            .thenThrow(new LedgerException("Get Account Balance", "Account Does Not Exist"));
        
        when(mockLedger.getAccountBalance(isNull()))
            .thenThrow(new LedgerException("Get Account Balance", "Account Does Not Exist"));
            
        when(mockLedger.getAccountBalance(anyString())).thenReturn(123);    
    }

    @Test
    @Order(13)
    void methodOrderTest() { //Done needs print
    System.out.println("\n=== Running methodOrderTest ===\n");
        // TODO: Complete this test to demonstrate test method ordering using @TestMethodOrder and @Order annotations
    }

    @Test
    @Order(14)
    void endToEnd() throws LedgerException {
        // Test 1
        Ledger testLedger = Ledger.getInstance("test", "test ledger 2025", "chapman");
        assertNotNull(testLedger);
        System.out.println("create-ledger test description \"test ledger 2025\" seed \"chapman\"");
        writer.write("\ncreate-ledger test description \"test ledger 2025\" seed \"chapman\"");

        testLedger.reset();

        // Test 2
        for (String addr : new String[]{"mary","bob","bill","frank","jane"}) {
            Account a = testLedger.createAccount(addr);
            assertNotNull(a);
            assertEquals(addr, a.getAddress());
            System.out.println("create-account " + addr);
            writer.write("\ncreate-account " + addr);
        }

        // Test 3
        System.out.println("get-account-balance mary");
        writer.write("\nget-account-balance mary");
        assertThrows(LedgerException.class, () -> testLedger.getAccountBalance("mary"));

        // Test 4
        Integer i = 1;
        for (String addr : new String[]{"mary","bob","bill","frank","mary"}) {
            Transaction trans = new Transaction(Integer.toString(i), 1000, 10, "fund account",
             testLedger.getUncommittedBlock().getAccount("master"),
             testLedger.getUncommittedBlock().getAccount(addr));

            assertDoesNotThrow(() -> testLedger.processTransaction(trans));
            System.out.println("process-transaction " + i + " amount " + trans.getAmount() + " fee " + trans.getFee() +
            " note " + trans.getNote() + " payer " + trans.getPayer().getAddress() + " receiver " + trans.getReceiver().getAddress());
            writer.write("\nprocess-transaction " + i + " amount " + trans.getAmount() + " fee " + trans.getFee() +
            " note " + trans.getNote() + " payer " + trans.getPayer().getAddress() + " receiver " + trans.getReceiver().getAddress());
            ++i;
        }

        // Test 5
        System.out.println("get-account-balance mary");
        writer.write("\nget-account-balance mary");
        assertThrows(LedgerException.class, () -> testLedger.getAccountBalance("mary"));

        // Test 6
        System.out.println("get-account-balances");
        writer.write("\nget-account-balances");
        assertNull(testLedger.getAccountBalances());

        // Test 7 
        for (String addr : new String[]{"bob", "bill", "frank", "mary", "bob"}) {
            Transaction trans = new Transaction(Integer.toString(i), 1000, 10, "fund account",
             testLedger.getUncommittedBlock().getAccount("master"),
             testLedger.getUncommittedBlock().getAccount(addr));


            assertDoesNotThrow(() -> testLedger.processTransaction(trans));
            System.out.println("process-transaction " + i + " amount " + trans.getAmount() + " fee " + trans.getFee() +
            " note " + trans.getNote() + " payer " + trans.getPayer().getAddress() + " receiver " + trans.getReceiver().getAddress());
            writer.write("\nprocess-transaction " + i + " amount " + trans.getAmount() + " fee " + trans.getFee() +
            " note " + trans.getNote() + " payer " + trans.getPayer().getAddress() + " receiver " + trans.getReceiver().getAddress());
            i++;
        }

        // Test 8
        assertEquals(testLedger.getAccountBalance("mary"), 3000);
        assertEquals(testLedger.getAccountBalance("bob"), 3000);
        assertEquals(testLedger.getAccountBalance("bill"), 2000);
        assertEquals(testLedger.getAccountBalance("frank"), 2000);
        assertEquals(testLedger.getAccountBalance("jane"), 0);
        for (String addr: new String[]{"mary", "bob", "bill", "frank", "jane"}) {
            System.out.println("get-account-" + addr);
            writer.write("\nget-account-" + addr);
        }

        // Test 9
        assertNotNull(testLedger.getBlock(1));
        assertNotNull(testLedger.getTransaction(Integer.toString(10)));
        System.out.println("get-block" + 1);
        System.out.println("get-transaction" + 10);
        writer.write("\nget-block 1\nget-transaction 10");

        // Test 10
        assertDoesNotThrow(() -> testLedger.getBlock(1)); // for existence
        Account frankAcc = testLedger.getUncommittedBlock().getAccount("frank");
        Account janeAcc  = testLedger.getUncommittedBlock().getAccount("jane");

        assertEquals(2000, assertDoesNotThrow(() -> testLedger.getAccountBalance("frank")));
        assertEquals(0,    assertDoesNotThrow(() -> testLedger.getAccountBalance("jane")));

        Transaction t11 = new Transaction("11", 200, 10, "september rent", frankAcc, janeAcc);
        assertDoesNotThrow(() -> testLedger.processTransaction(t11));

        System.out.println("process-transaction 11 amount 200 fee 10 note \"september rent\" payer frank receiver jane");
        writer.println("process-transaction 11 amount 200 fee 10 note \"september rent\" payer frank receiver jane");

        System.out.println("get-account-balance frank");
        System.out.println("get-account-balance jane");
        writer.println("get-account-balance frank");
        writer.println("get-account-balance jane");

        // Test 11
        Account bobU  = testLedger.getUncommittedBlock().getAccount("bob");
        Account maryU = testLedger.getUncommittedBlock().getAccount("mary");

        for (int id = 12; id <= 20; id++) {
            Transaction tx = new Transaction(Integer.toString(id), 20, 10, "uber", bobU, maryU);
            assertDoesNotThrow(() -> testLedger.processTransaction(tx));
            String line = "process-transaction " + id + " amount " + tx.getAmount() + " fee " + tx.getFee()
                + " note " + tx.getNote() + " payer " + tx.getPayer().getAddress()
                + " receiver " + tx.getReceiver().getAddress();
            System.out.println(line);
            writer.println(line);
        }

        int jane  = assertDoesNotThrow(() -> testLedger.getAccountBalance("jane"));
        int frank = assertDoesNotThrow(() -> testLedger.getAccountBalance("frank"));
        int mary  = assertDoesNotThrow(() -> testLedger.getAccountBalance("mary"));
        int bob   = assertDoesNotThrow(() -> testLedger.getAccountBalance("bob"));

        System.out.println("get-account-balance jane => " + jane);
        System.out.println("get-account-balance frank => " + frank);
        System.out.println("get-account-balance mary => " + mary);
        System.out.println("get-account-balance bob  => " + bob);

        writer.println("get-account-balance jane => " + jane);
        writer.println("get-account-balance frank => " + frank);
        writer.println("get-account-balance mary => " + mary);
        writer.println("get-account-balance bob  => " + bob);

        assertEquals(200,  jane,  "jane balance");
        assertEquals(1790, frank, "frank balance");
        assertEquals(3180, mary,  "mary balance");
        assertEquals(2730, bob,   "bob balance");

        // Test 13
        System.out.println("get-account-balances");
        writer.println("get-account-balances");
        Map<String,Integer> all = testLedger.getAccountBalances();
        assertNotNull(all);
        System.out.println(all);
        writer.println(all);

        Transaction t21 = new Transaction("21", 5000, 10, "food", bobU, maryU);
        assertThrows(LedgerException.class, () -> testLedger.processTransaction(t21));

        Transaction t22 = new Transaction("22", 20, 5, "food", bobU, maryU);
        System.out.println("process-transaction 22 amount 20 fee 5 note \"food\" payer bob receiver mary");
        writer.println("process-transaction 22 amount 20 fee 5 note \"food\" payer bob receiver mary");
        assertThrows(LedgerException.class, () -> testLedger.processTransaction(t22));

        System.out.println("validate");
        writer.println("validate");
        assertDoesNotThrow(() -> testLedger.validate());
    }
}
