package utils;

import java.io.IOException;

/**
 * Test class for CategoryStore backend integration.
 * Tests async operations with SocketClient.
 */
public class TestCategoryStore {
    
    public static void main(String[] args) {
        System.out.println("=== CategoryStore Backend Integration Test ===\n");
        
        try {
            // Connect to backend
            System.out.println("Connecting to backend...");
            SocketClient.getInstance().connect();
            System.out.println("✓ Connected to backend\n");
            
            // Login to get session token
            System.out.println("Logging in...");
            String loginCommand = SocketClient.getInstance().formatCommand("LOGIN", "testuser", "testpass");
            String loginResponse = SocketClient.getInstance().sendCommand(loginCommand);
            
            if (loginResponse.startsWith("LOGIN_FAIL")) {
                // Try to register if login fails
                System.out.println("User doesn't exist, registering...");
                String registerCommand = SocketClient.getInstance().formatCommand("REGISTER", "testuser", "testpass");
                String registerResponse = SocketClient.getInstance().sendCommand(registerCommand);
                
                if (registerResponse.startsWith("ERROR")) {
                    System.err.println("✗ Failed to register: " + SocketClient.getInstance().getErrorMessage(registerResponse));
                    System.exit(1);
                }
                
                System.out.println("✓ Registered successfully");
                
                // Login again after registration
                loginResponse = SocketClient.getInstance().sendCommand(loginCommand);
            }
            
            // Parse session token
            String[] parts = SocketClient.getInstance().parseResponse(loginResponse);
            if (parts.length >= 2 && parts[0].equals("OK")) {
                String sessionToken = parts[1];
                SessionManager.getInstance().setSession(sessionToken, "testuser");
                System.out.println("✓ Logged in successfully\n");
                
                // Test 1: Load categories from backend
                testLoadCategories();
            } else {
                System.err.println("✗ Failed to login: " + loginResponse);
                System.exit(1);
            }
            
        } catch (IOException e) {
            System.err.println("✗ Connection error: " + e.getMessage());
            System.exit(1);
        }
        
        // Keep main thread alive for async operations
        try {
            Thread.sleep(10000); // Wait 10 seconds for all operations
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private static void testLoadCategories() {
        System.out.println("Test 1: Loading categories from backend...");
        CategoryStore.loadFromBackend(
            () -> {
                System.out.println("✓ Categories loaded successfully");
                CategoryStore.Snapshot snapshot = CategoryStore.snapshot();
                System.out.println("  Expense categories: " + snapshot.expenses());
                System.out.println("  Income categories: " + snapshot.incomes());
                
                // Test 2: Add new category
                testAddCategory();
            },
            error -> {
                System.err.println("✗ Failed to load categories: " + error);
                System.exit(1);
            }
        );
    }
    
    private static void testAddCategory() {
        System.out.println("\nTest 2: Adding new category...");
        CategoryStore.addCategory(
            CategoryStore.EXPENSE,
            "Test Category",
            () -> {
                System.out.println("✓ Category added successfully");
                
                // Test 3: Remove category
                testRemoveCategory();
            },
            error -> {
                System.err.println("✗ Failed to add category: " + error);
            }
        );
    }
    
    private static void testRemoveCategory() {
        System.out.println("\nTest 3: Removing category...");
        CategoryStore.removeCategory(
            CategoryStore.EXPENSE,
            "Test Category",
            () -> {
                System.out.println("✓ Category removed successfully");
                
                // Test 4: Verify final state
                testFinalState();
            },
            error -> {
                System.err.println("✗ Failed to remove category: " + error);
            }
        );
    }
    
    private static void testFinalState() {
        System.out.println("\nTest 4: Verifying final state...");
        CategoryStore.loadFromBackend(
            () -> {
                System.out.println("✓ Final state loaded successfully");
                CategoryStore.Snapshot snapshot = CategoryStore.snapshot();
                System.out.println("  Expense categories: " + snapshot.expenses());
                System.out.println("  Income categories: " + snapshot.incomes());
                
                System.out.println("\n=== All tests completed successfully! ===");
            },
            error -> {
                System.err.println("✗ Failed to load final state: " + error);
            }
        );
    }
}
