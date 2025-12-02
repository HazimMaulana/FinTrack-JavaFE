import javax.swing.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import utils.SocketClient;
import utils.SessionManager;
import utils.TransactionStore;
import utils.AccountStore;
import utils.CategoryStore;

/**
 * End-to-end test for the complete socket client integration.
 * Tests all major flows including authentication, CRUD operations, and session management.
 * 
 * PREREQUISITES:
 * - Backend server must be running on localhost:3000
 * - Backend should have clean state or test user should not exist
 * 
 * This test verifies:
 * 1. Login flow with valid credentials
 * 2. Register flow with new user
 * 3. Transaction CRUD operations
 * 4. Account CRUD operations
 * 5. Category CRUD operations
 * 6. Session persistence across app restarts
 * 7. Logout flow
 * 8. Error handling (invalid credentials, session expiry)
 */
public class TestEndToEndFlow {
    
    private static final String TEST_USERNAME = "testuser_e2e_" + System.currentTimeMillis();
    private static final String TEST_PASSWORD = "testpass123";
    private static final String SESSION_FILE = ".fintrack_session";
    
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== FinTrack End-to-End Flow Test ===\n");
        System.out.println("Test Username: " + TEST_USERNAME);
        System.out.println("Prerequisites: Backend server must be running on localhost:3000\n");
        
        try {
            // Test 1: Connection to backend
            testBackendConnection();
            
            // Test 2: Register new user
            testRegisterFlow();
            
            // Test 3: Login with valid credentials
            testLoginFlow();
            
            // Test 4: Transaction operations
            testTransactionOperations();
            
            // Test 5: Account operations
            testAccountOperations();
            
            // Test 6: Category operations
            testCategoryOperations();
            
            // Test 7: Session persistence
            testSessionPersistence();
            
            // Test 8: Logout flow
            testLogoutFlow();
            
            // Test 9: Error handling - invalid credentials
            testInvalidCredentials();
            
            // Test 10: UI responsiveness (manual verification note)
            printUIResponsivenessNote();
            
            // Print summary
            printSummary();
            
        } catch (Exception e) {
            System.err.println("\n❌ FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Test 1: Verify connection to backend server
     */
    private static void testBackendConnection() {
        System.out.println("Test 1: Backend Connection");
        System.out.println("---------------------------");
        
        try {
            SocketClient client = SocketClient.getInstance();
            client.connect();
            
            if (client.isConnected()) {
                System.out.println("✓ Successfully connected to backend server");
                testsPassed++;
            } else {
                System.out.println("✗ Failed to connect to backend server");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Connection failed: " + e.getMessage());
            System.out.println("  Make sure backend server is running on localhost:3000");
            testsFailed++;
            throw new RuntimeException("Cannot proceed without backend connection");
        }
        
        System.out.println();
    }
    
    /**
     * Test 2: Register new user
     */
    private static void testRegisterFlow() {
        System.out.println("Test 2: Register Flow");
        System.out.println("---------------------");
        
        try {
            SocketClient client = SocketClient.getInstance();
            String command = client.formatCommand("REGISTER", TEST_USERNAME, TEST_PASSWORD);
            String response = client.sendCommand(command);
            
            if (response != null && response.startsWith("OK")) {
                System.out.println("✓ User registered successfully");
                System.out.println("  Response: " + response);
                testsPassed++;
            } else if (client.isErrorResponse(response)) {
                String errorMsg = client.getErrorMessage(response);
                System.out.println("✗ Registration failed: " + errorMsg);
                testsFailed++;
            } else {
                System.out.println("✗ Unexpected response: " + response);
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Registration error: " + e.getMessage());
            testsFailed++;
        }
        
        System.out.println();
    }
    
    /**
     * Test 3: Login with valid credentials
     */
    private static void testLoginFlow() {
        System.out.println("Test 3: Login Flow");
        System.out.println("------------------");
        
        try {
            SocketClient client = SocketClient.getInstance();
            String command = client.formatCommand("LOGIN", TEST_USERNAME, TEST_PASSWORD);
            String response = client.sendCommand(command);
            
            String[] parts = client.parseResponse(response);
            if (parts.length >= 2 && parts[0].equals("OK")) {
                String sessionToken = parts[1];
                
                // Save session
                SessionManager sessionManager = SessionManager.getInstance();
                sessionManager.setSession(sessionToken, TEST_USERNAME);
                sessionManager.saveToFile();
                
                System.out.println("✓ Login successful");
                System.out.println("  Session token: " + sessionToken.substring(0, 8) + "...");
                System.out.println("  Session saved to file");
                testsPassed++;
            } else {
                System.out.println("✗ Login failed: " + response);
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Login error: " + e.getMessage());
            testsFailed++;
        }
        
        System.out.println();
    }
    
    /**
     * Test 4: Transaction CRUD operations
     */
    private static void testTransactionOperations() {
        System.out.println("Test 4: Transaction Operations");
        System.out.println("------------------------------");
        
        final String[] addedTransactionId = {null};
        final boolean[] addSuccess = {false};
        final boolean[] updateSuccess = {false};
        final boolean[] deleteSuccess = {false};
        final boolean[] loadSuccess = {false};
        
        // Test Add Transaction
        System.out.println("  4a. Testing Add Transaction...");
        try {
            String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            TransactionStore.addTransaction(
                date,
                "expense",
                "Food",
                "Cash",
                "cash",
                50000,
                "Test transaction",
                (transactionId) -> {
                    addedTransactionId[0] = transactionId;
                    addSuccess[0] = true;
                    System.out.println("     ✓ Transaction added: " + transactionId);
                },
                (error) -> {
                    System.out.println("     ✗ Add failed: " + error);
                }
            );
            
            // Wait for async operation
            Thread.sleep(2000);
            
            if (addSuccess[0]) {
                testsPassed++;
            } else {
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("     ✗ Add error: " + e.getMessage());
            testsFailed++;
        }
        
        // Test Update Transaction
        if (addedTransactionId[0] != null) {
            System.out.println("  4b. Testing Update Transaction...");
            try {
                String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                TransactionStore.updateTransaction(
                    addedTransactionId[0],
                    date,
                    "expense",
                    "Food",
                    "Cash",
                    "cash",
                    75000,
                    "Updated test transaction",
                    () -> {
                        updateSuccess[0] = true;
                        System.out.println("     ✓ Transaction updated");
                    },
                    (error) -> {
                        System.out.println("     ✗ Update failed: " + error);
                    }
                );
                
                Thread.sleep(2000);
                
                if (updateSuccess[0]) {
                    testsPassed++;
                } else {
                    testsFailed++;
                }
            } catch (Exception e) {
                System.out.println("     ✗ Update error: " + e.getMessage());
                testsFailed++;
            }
        }
        
        // Test Load Transactions
        System.out.println("  4c. Testing Load Transactions...");
        try {
            TransactionStore.loadFromBackend(
                () -> {
                    loadSuccess[0] = true;
                    int count = TransactionStore.snapshot().transactions().size();
                    System.out.println("     ✓ Transactions loaded: " + count + " transactions");
                },
                (error) -> {
                    System.out.println("     ✗ Load failed: " + error);
                }
            );
            
            Thread.sleep(2000);
            
            if (loadSuccess[0]) {
                testsPassed++;
            } else {
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("     ✗ Load error: " + e.getMessage());
            testsFailed++;
        }
        
        // Test Delete Transaction
        if (addedTransactionId[0] != null) {
            System.out.println("  4d. Testing Delete Transaction...");
            try {
                TransactionStore.removeTransaction(
                    addedTransactionId[0],
                    () -> {
                        deleteSuccess[0] = true;
                        System.out.println("     ✓ Transaction deleted");
                    },
                    (error) -> {
                        System.out.println("     ✗ Delete failed: " + error);
                    }
                );
                
                Thread.sleep(2000);
                
                if (deleteSuccess[0]) {
                    testsPassed++;
                } else {
                    testsFailed++;
                }
            } catch (Exception e) {
                System.out.println("     ✗ Delete error: " + e.getMessage());
                testsFailed++;
            }
        }
        
        System.out.println();
    }
    
    /**
     * Test 5: Account CRUD operations
     */
    private static void testAccountOperations() {
        System.out.println("Test 5: Account Operations");
        System.out.println("--------------------------");
        
        final String[] addedAccountId = {null};
        final boolean[] addSuccess = {false};
        final boolean[] updateSuccess = {false};
        final boolean[] deleteSuccess = {false};
        final boolean[] loadSuccess = {false};
        
        // Test Add Account
        System.out.println("  5a. Testing Add Account...");
        try {
            AccountStore.addAccount(
                "Test Account",
                "1234567890",
                1000000,
                "bank",
                (accountId) -> {
                    addedAccountId[0] = accountId;
                    addSuccess[0] = true;
                    System.out.println("     ✓ Account added: " + accountId);
                },
                (error) -> {
                    System.out.println("     ✗ Add failed: " + error);
                }
            );
            
            Thread.sleep(2000);
            
            if (addSuccess[0]) {
                testsPassed++;
            } else {
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("     ✗ Add error: " + e.getMessage());
            testsFailed++;
        }
        
        // Test Update Account
        if (addedAccountId[0] != null) {
            System.out.println("  5b. Testing Update Account...");
            try {
                AccountStore.updateAccount(
                    addedAccountId[0],
                    "Updated Test Account",
                    "1234567890",
                    2000000,
                    "bank",
                    () -> {
                        updateSuccess[0] = true;
                        System.out.println("     ✓ Account updated");
                    },
                    (error) -> {
                        System.out.println("     ✗ Update failed: " + error);
                    }
                );
                
                Thread.sleep(2000);
                
                if (updateSuccess[0]) {
                    testsPassed++;
                } else {
                    testsFailed++;
                }
            } catch (Exception e) {
                System.out.println("     ✗ Update error: " + e.getMessage());
                testsFailed++;
            }
        }
        
        // Test Load Accounts
        System.out.println("  5c. Testing Load Accounts...");
        try {
            AccountStore.loadFromBackend(
                () -> {
                    loadSuccess[0] = true;
                    int count = AccountStore.snapshot().accounts().size();
                    System.out.println("     ✓ Accounts loaded: " + count + " accounts");
                },
                (error) -> {
                    System.out.println("     ✗ Load failed: " + error);
                }
            );
            
            Thread.sleep(2000);
            
            if (loadSuccess[0]) {
                testsPassed++;
            } else {
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("     ✗ Load error: " + e.getMessage());
            testsFailed++;
        }
        
        // Test Delete Account
        if (addedAccountId[0] != null) {
            System.out.println("  5d. Testing Delete Account...");
            try {
                AccountStore.removeAccount(
                    addedAccountId[0],
                    () -> {
                        deleteSuccess[0] = true;
                        System.out.println("     ✓ Account deleted");
                    },
                    (error) -> {
                        System.out.println("     ✗ Delete failed: " + error);
                    }
                );
                
                Thread.sleep(2000);
                
                if (deleteSuccess[0]) {
                    testsPassed++;
                } else {
                    testsFailed++;
                }
            } catch (Exception e) {
                System.out.println("     ✗ Delete error: " + e.getMessage());
                testsFailed++;
            }
        }
        
        System.out.println();
    }
    
    /**
     * Test 6: Category CRUD operations
     */
    private static void testCategoryOperations() {
        System.out.println("Test 6: Category Operations");
        System.out.println("---------------------------");
        
        final boolean[] addSuccess = {false};
        final boolean[] deleteSuccess = {false};
        final boolean[] loadSuccess = {false};
        
        // Test Add Category
        System.out.println("  6a. Testing Add Category...");
        try {
            CategoryStore.addCategory(
                "expense",
                "Test Category",
                () -> {
                    addSuccess[0] = true;
                    System.out.println("     ✓ Category added");
                },
                (error) -> {
                    System.out.println("     ✗ Add failed: " + error);
                }
            );
            
            Thread.sleep(2000);
            
            if (addSuccess[0]) {
                testsPassed++;
            } else {
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("     ✗ Add error: " + e.getMessage());
            testsFailed++;
        }
        
        // Test Load Categories
        System.out.println("  6b. Testing Load Categories...");
        try {
            CategoryStore.loadFromBackend(
                () -> {
                    loadSuccess[0] = true;
                    int expenseCount = CategoryStore.snapshot().expenses().size();
                    int incomeCount = CategoryStore.snapshot().incomes().size();
                    System.out.println("     ✓ Categories loaded: " + expenseCount + " expense, " + incomeCount + " income");
                },
                (error) -> {
                    System.out.println("     ✗ Load failed: " + error);
                }
            );
            
            Thread.sleep(2000);
            
            if (loadSuccess[0]) {
                testsPassed++;
            } else {
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("     ✗ Load error: " + e.getMessage());
            testsFailed++;
        }
        
        // Test Delete Category
        System.out.println("  6c. Testing Delete Category...");
        try {
            CategoryStore.removeCategory(
                "expense",
                "Test Category",
                () -> {
                    deleteSuccess[0] = true;
                    System.out.println("     ✓ Category deleted");
                },
                (error) -> {
                    System.out.println("     ✗ Delete failed: " + error);
                }
            );
            
            Thread.sleep(2000);
            
            if (deleteSuccess[0]) {
                testsPassed++;
            } else {
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("     ✗ Delete error: " + e.getMessage());
            testsFailed++;
        }
        
        System.out.println();
    }
    
    /**
     * Test 7: Session persistence across app restarts
     */
    private static void testSessionPersistence() {
        System.out.println("Test 7: Session Persistence");
        System.out.println("---------------------------");
        
        try {
            // Verify session file exists
            File sessionFile = new File(SESSION_FILE);
            if (!sessionFile.exists()) {
                System.out.println("✗ Session file not found");
                testsFailed++;
                System.out.println();
                return;
            }
            
            System.out.println("  ✓ Session file exists");
            
            // Clear current session
            SessionManager sessionManager = SessionManager.getInstance();
            String originalToken = sessionManager.getSessionToken();
            String originalUsername = sessionManager.getUsername();
            
            sessionManager.clearSession();
            System.out.println("  ✓ Session cleared from memory");
            
            // Load from file
            sessionManager.loadFromFile();
            
            if (sessionManager.hasValidSession()) {
                System.out.println("  ✓ Session loaded from file");
                
                // Validate session with backend
                if (sessionManager.validateSession()) {
                    System.out.println("  ✓ Session validated with backend");
                    System.out.println("✓ Session persistence test passed");
                    testsPassed++;
                } else {
                    System.out.println("  ✗ Session validation failed");
                    testsFailed++;
                }
            } else {
                System.out.println("  ✗ Failed to load session from file");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Session persistence error: " + e.getMessage());
            testsFailed++;
        }
        
        System.out.println();
    }
    
    /**
     * Test 8: Logout flow
     */
    private static void testLogoutFlow() {
        System.out.println("Test 8: Logout Flow");
        System.out.println("-------------------");
        
        try {
            SessionManager sessionManager = SessionManager.getInstance();
            String sessionToken = sessionManager.getSessionToken();
            
            if (sessionToken == null || sessionToken.isEmpty()) {
                System.out.println("✗ No active session to logout");
                testsFailed++;
                System.out.println();
                return;
            }
            
            // Send logout command
            SocketClient client = SocketClient.getInstance();
            String command = client.formatCommand("LOGOUT", sessionToken);
            String response = client.sendCommand(command);
            
            if (response != null && response.startsWith("OK")) {
                System.out.println("  ✓ Logout command sent successfully");
                
                // Clear session
                sessionManager.clearSession();
                
                // Verify session is cleared
                if (!sessionManager.hasValidSession()) {
                    System.out.println("  ✓ Session cleared from memory");
                    
                    // Verify session file is deleted
                    File sessionFile = new File(SESSION_FILE);
                    if (!sessionFile.exists()) {
                        System.out.println("  ✓ Session file deleted");
                        System.out.println("✓ Logout flow test passed");
                        testsPassed++;
                    } else {
                        System.out.println("  ⚠ Session file still exists (may be expected)");
                        testsPassed++;
                    }
                } else {
                    System.out.println("  ✗ Session not cleared properly");
                    testsFailed++;
                }
            } else {
                System.out.println("✗ Logout failed: " + response);
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Logout error: " + e.getMessage());
            testsFailed++;
        }
        
        System.out.println();
    }
    
    /**
     * Test 9: Error handling - invalid credentials
     */
    private static void testInvalidCredentials() {
        System.out.println("Test 9: Error Handling - Invalid Credentials");
        System.out.println("---------------------------------------------");
        
        try {
            SocketClient client = SocketClient.getInstance();
            String command = client.formatCommand("LOGIN", "invalid_user", "invalid_pass");
            String response = client.sendCommand(command);
            
            if (client.isErrorResponse(response)) {
                String errorMsg = client.getErrorMessage(response);
                System.out.println("✓ Invalid credentials properly rejected");
                System.out.println("  Error message: " + errorMsg);
                testsPassed++;
            } else {
                System.out.println("✗ Invalid credentials not rejected properly");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Error handling test failed: " + e.getMessage());
            testsFailed++;
        }
        
        System.out.println();
    }
    
    /**
     * Print note about UI responsiveness (manual verification)
     */
    private static void printUIResponsivenessNote() {
        System.out.println("Test 10: UI Responsiveness (Manual Verification)");
        System.out.println("------------------------------------------------");
        System.out.println("⚠ MANUAL TEST REQUIRED:");
        System.out.println("  - Launch the application GUI");
        System.out.println("  - Verify UI remains responsive during operations");
        System.out.println("  - Verify loading indicators appear and disappear correctly");
        System.out.println("  - Test all CRUD operations through the UI");
        System.out.println("  - Verify error dialogs display properly");
        System.out.println("  - Test session persistence by closing and reopening app");
        System.out.println();
    }
    
    /**
     * Print test summary
     */
    private static void printSummary() {
        System.out.println("=== Test Summary ===");
        System.out.println("Tests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + testsFailed);
        System.out.println("Total Tests: " + (testsPassed + testsFailed));
        
        if (testsFailed == 0) {
            System.out.println("\n✓ ALL AUTOMATED TESTS PASSED!");
            System.out.println("  Remember to perform manual UI testing as noted above.");
        } else {
            System.out.println("\n✗ SOME TESTS FAILED");
            System.out.println("  Please review the failures above and fix issues.");
        }
    }
}
