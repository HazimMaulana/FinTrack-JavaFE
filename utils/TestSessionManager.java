package utils;

import java.io.File;

/**
 * Test class for SessionManager functionality.
 */
public class TestSessionManager {
    
    public static void main(String[] args) {
        System.out.println("=== Testing SessionManager ===\n");
        
        // Clean up any existing session file
        File sessionFile = new File(".fintrack_session");
        if (sessionFile.exists()) {
            sessionFile.delete();
        }
        
        SessionManager manager = SessionManager.getInstance();
        
        // Test 1: Initial state
        System.out.println("Test 1: Initial state");
        System.out.println("Has valid session: " + manager.hasValidSession());
        System.out.println("Token: " + manager.getSessionToken());
        System.out.println("Username: " + manager.getUsername());
        assert !manager.hasValidSession() : "Should not have valid session initially";
        System.out.println("✓ PASSED\n");
        
        // Test 2: Set session
        System.out.println("Test 2: Set session");
        manager.setSession("test-token-123", "testuser");
        System.out.println("Has valid session: " + manager.hasValidSession());
        System.out.println("Token: " + manager.getSessionToken());
        System.out.println("Username: " + manager.getUsername());
        assert manager.hasValidSession() : "Should have valid session after setting";
        assert "test-token-123".equals(manager.getSessionToken()) : "Token should match";
        assert "testuser".equals(manager.getUsername()) : "Username should match";
        System.out.println("✓ PASSED\n");
        
        // Test 3: Save to file
        System.out.println("Test 3: Save to file");
        manager.saveToFile();
        assert sessionFile.exists() : "Session file should exist after save";
        System.out.println("Session file created: " + sessionFile.exists());
        System.out.println("✓ PASSED\n");
        
        // Test 4: Clear session
        System.out.println("Test 4: Clear session");
        manager.clearSession();
        System.out.println("Has valid session: " + manager.hasValidSession());
        System.out.println("Token: " + manager.getSessionToken());
        System.out.println("Username: " + manager.getUsername());
        assert !manager.hasValidSession() : "Should not have valid session after clearing";
        assert manager.getSessionToken() == null : "Token should be null";
        assert manager.getUsername() == null : "Username should be null";
        System.out.println("✓ PASSED\n");
        
        // Test 5: Load from file
        System.out.println("Test 5: Load from file");
        manager.loadFromFile();
        System.out.println("Has valid session: " + manager.hasValidSession());
        System.out.println("Token: " + manager.getSessionToken());
        System.out.println("Username: " + manager.getUsername());
        assert manager.hasValidSession() : "Should have valid session after loading";
        assert "test-token-123".equals(manager.getSessionToken()) : "Token should match loaded value";
        assert "testuser".equals(manager.getUsername()) : "Username should match loaded value";
        System.out.println("✓ PASSED\n");
        
        // Test 6: Singleton pattern
        System.out.println("Test 6: Singleton pattern");
        SessionManager manager2 = SessionManager.getInstance();
        assert manager == manager2 : "Should return same instance";
        assert manager2.hasValidSession() : "Second instance should have same session";
        System.out.println("Same instance: " + (manager == manager2));
        System.out.println("✓ PASSED\n");
        
        // Test 7: Load from non-existent file
        System.out.println("Test 7: Load from non-existent file (graceful handling)");
        manager.clearSession();
        sessionFile.delete();
        manager.loadFromFile(); // Should not throw exception
        System.out.println("Has valid session: " + manager.hasValidSession());
        assert !manager.hasValidSession() : "Should not have session when file doesn't exist";
        System.out.println("✓ PASSED\n");
        
        // Clean up
        if (sessionFile.exists()) {
            sessionFile.delete();
        }
        
        System.out.println("=== All SessionManager tests PASSED ===");
    }
}
