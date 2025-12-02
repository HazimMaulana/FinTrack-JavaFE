import utils.SocketClient;
import utils.SessionManager;

/**
 * Quick smoke test to verify basic connectivity and core functionality.
 * This test performs minimal validation to ensure the system is operational.
 */
public class SmokeTest {
    
    private static final String TEST_USERNAME = "smoketest_" + System.currentTimeMillis();
    private static final String TEST_PASSWORD = "test123";
    
    public static void main(String[] args) {
        System.out.println("=== FinTrack Smoke Test ===\n");
        
        boolean allPassed = true;
        
        // Test 1: Connection
        System.out.print("1. Testing backend connection... ");
        try {
            SocketClient client = SocketClient.getInstance();
            client.connect();
            if (client.isConnected()) {
                System.out.println("✓ PASS");
            } else {
                System.out.println("✗ FAIL - Not connected");
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL - " + e.getMessage());
            System.out.println("\nERROR: Cannot connect to backend. Make sure server is running on localhost:3000");
            System.exit(1);
        }
        
        // Test 2: Register
        System.out.print("2. Testing user registration... ");
        try {
            SocketClient client = SocketClient.getInstance();
            String command = client.formatCommand("REGISTER", TEST_USERNAME, TEST_PASSWORD);
            String response = client.sendCommand(command);
            
            if (response != null && response.startsWith("REGISTER_OK")) {
                System.out.println("✓ PASS");
            } else if (response != null && response.contains("USER_EXISTS")) {
                System.out.println("⚠ SKIP - User already exists");
            } else {
                System.out.println("✗ FAIL - Unexpected response: " + response);
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL - " + e.getMessage());
            allPassed = false;
        }
        
        // Test 3: Login
        System.out.print("3. Testing user login... ");
        String sessionToken = null;
        try {
            SocketClient client = SocketClient.getInstance();
            String command = client.formatCommand("LOGIN", TEST_USERNAME, TEST_PASSWORD);
            String response = client.sendCommand(command);
            
            String[] parts = client.parseResponse(response);
            if (parts.length >= 2 && parts[0].equals("OK")) {
                sessionToken = parts[1];
                SessionManager.getInstance().setSession(sessionToken, TEST_USERNAME);
                System.out.println("✓ PASS");
            } else {
                System.out.println("✗ FAIL - Response: " + response);
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL - " + e.getMessage());
            allPassed = false;
        }
        
        // Test 4: Session Validation
        if (sessionToken != null) {
            System.out.print("4. Testing session validation... ");
            try {
                SocketClient client = SocketClient.getInstance();
                String command = client.formatCommand("VALIDATE_SESSION", sessionToken);
                String response = client.sendCommand(command);
                
                if (response != null && response.startsWith("OK")) {
                    System.out.println("✓ PASS");
                } else {
                    System.out.println("✗ FAIL - Response: " + response);
                    allPassed = false;
                }
            } catch (Exception e) {
                System.out.println("✗ FAIL - " + e.getMessage());
                allPassed = false;
            }
        } else {
            System.out.println("4. Testing session validation... ⊘ SKIP - No session token");
        }
        
        // Test 5: Get Accounts
        if (sessionToken != null) {
            System.out.print("5. Testing GET_ACCOUNTS command... ");
            try {
                SocketClient client = SocketClient.getInstance();
                String command = client.formatCommand("GET_ACCOUNTS", sessionToken);
                String response = client.sendCommand(command);
                
                if (response != null && response.startsWith("DATA_ACCOUNTS")) {
                    System.out.println("✓ PASS");
                } else {
                    System.out.println("✗ FAIL - Response: " + response);
                    allPassed = false;
                }
            } catch (Exception e) {
                System.out.println("✗ FAIL - " + e.getMessage());
                allPassed = false;
            }
        } else {
            System.out.println("5. Testing GET_ACCOUNTS command... ⊘ SKIP - No session token");
        }
        
        // Test 6: Get Categories
        if (sessionToken != null) {
            System.out.print("6. Testing GET_CATEGORIES command... ");
            try {
                SocketClient client = SocketClient.getInstance();
                String command = client.formatCommand("GET_CATEGORIES", sessionToken);
                String response = client.sendCommand(command);
                
                if (response != null && response.startsWith("DATA_CATEGORIES")) {
                    System.out.println("✓ PASS");
                } else {
                    System.out.println("✗ FAIL - Response: " + response);
                    allPassed = false;
                }
            } catch (Exception e) {
                System.out.println("✗ FAIL - " + e.getMessage());
                allPassed = false;
            }
        } else {
            System.out.println("6. Testing GET_CATEGORIES command... ⊘ SKIP - No session token");
        }
        
        // Test 7: Get Transactions
        if (sessionToken != null) {
            System.out.print("7. Testing GET_ALL command... ");
            try {
                SocketClient client = SocketClient.getInstance();
                String command = client.formatCommand("GET_ALL", sessionToken);
                String response = client.sendCommand(command);
                
                if (response != null && response.startsWith("DATA_ALL")) {
                    System.out.println("✓ PASS");
                } else {
                    System.out.println("✗ FAIL - Response: " + response);
                    allPassed = false;
                }
            } catch (Exception e) {
                System.out.println("✗ FAIL - " + e.getMessage());
                allPassed = false;
            }
        } else {
            System.out.println("7. Testing GET_ALL command... ⊘ SKIP - No session token");
        }
        
        // Test 8: Logout
        if (sessionToken != null) {
            System.out.print("8. Testing logout... ");
            try {
                SocketClient client = SocketClient.getInstance();
                String command = client.formatCommand("LOGOUT", sessionToken);
                String response = client.sendCommand(command);
                
                if (response != null && response.startsWith("OK")) {
                    SessionManager.getInstance().clearSession();
                    System.out.println("✓ PASS");
                } else {
                    System.out.println("✗ FAIL - Response: " + response);
                    allPassed = false;
                }
            } catch (Exception e) {
                System.out.println("✗ FAIL - " + e.getMessage());
                allPassed = false;
            }
        } else {
            System.out.println("8. Testing logout... ⊘ SKIP - No session token");
        }
        
        // Summary
        System.out.println("\n=== Smoke Test Complete ===");
        if (allPassed) {
            System.out.println("✓ ALL TESTS PASSED");
            System.out.println("\nThe system is operational. Proceed with manual end-to-end testing.");
            System.out.println("See END_TO_END_TEST_GUIDE.md for detailed manual test procedures.");
        } else {
            System.out.println("✗ SOME TESTS FAILED");
            System.out.println("\nPlease fix the failing tests before proceeding with manual testing.");
        }
    }
}
