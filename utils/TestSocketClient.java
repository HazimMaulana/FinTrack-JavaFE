package utils;

/**
 * Simple test class to verify SocketClient basic functionality.
 * This is not a comprehensive test suite, just basic verification.
 */
public class TestSocketClient {
    
    public static void main(String[] args) {
        System.out.println("=== SocketClient Basic Tests ===\n");
        
        testFormatCommand();
        testParseResponse();
        testIsErrorResponse();
        testGetErrorMessage();
        
        System.out.println("\n=== All basic tests passed ===");
    }
    
    private static void testFormatCommand() {
        SocketClient client = SocketClient.getInstance();
        
        // Test single part
        String result1 = client.formatCommand("LOGIN");
        assert result1.equals("LOGIN") : "Single part failed";
        
        // Test multiple parts
        String result2 = client.formatCommand("LOGIN", "user123", "pass456");
        assert result2.equals("LOGIN|user123|pass456") : "Multiple parts failed";
        
        // Test empty
        String result3 = client.formatCommand();
        assert result3.equals("") : "Empty parts failed";
        
        System.out.println("✓ formatCommand tests passed");
    }
    
    private static void testParseResponse() {
        SocketClient client = SocketClient.getInstance();
        
        // Test normal response
        String[] parts1 = client.parseResponse("OK|token123");
        assert parts1.length == 2 : "Parse length failed";
        assert parts1[0].equals("OK") : "Parse first part failed";
        assert parts1[1].equals("token123") : "Parse second part failed";
        
        // Test single part
        String[] parts2 = client.parseResponse("OK");
        assert parts2.length == 1 : "Single part length failed";
        assert parts2[0].equals("OK") : "Single part value failed";
        
        // Test empty
        String[] parts3 = client.parseResponse("");
        assert parts3.length == 0 : "Empty parse failed";
        
        // Test null
        String[] parts4 = client.parseResponse(null);
        assert parts4.length == 0 : "Null parse failed";
        
        System.out.println("✓ parseResponse tests passed");
    }
    
    private static void testIsErrorResponse() {
        SocketClient client = SocketClient.getInstance();
        
        // Test error response
        assert client.isErrorResponse("ERROR|INVALID_CREDENTIALS") : "Error detection failed";
        assert client.isErrorResponse("ERROR|SESSION_INVALID|Session expired") : "Error with description failed";
        
        // Test non-error response
        assert !client.isErrorResponse("OK|token123") : "OK false positive";
        assert !client.isErrorResponse("DATA_ALL|0") : "DATA false positive";
        assert !client.isErrorResponse(null) : "Null false positive";
        assert !client.isErrorResponse("") : "Empty false positive";
        
        System.out.println("✓ isErrorResponse tests passed");
    }
    
    private static void testGetErrorMessage() {
        SocketClient client = SocketClient.getInstance();
        
        // Test full error format
        String msg1 = client.getErrorMessage("ERROR|SESSION_INVALID|Session has expired");
        assert msg1.equals("Session has expired") : "Full format failed: " + msg1;
        
        // Test two-part error format
        String msg2 = client.getErrorMessage("ERROR|Invalid credentials");
        assert msg2.equals("Invalid credentials") : "Two-part format failed: " + msg2;
        
        // Test single part (edge case)
        String msg3 = client.getErrorMessage("ERROR");
        assert msg3.equals("ERROR") : "Single part failed: " + msg3;
        
        // Test non-error
        String msg4 = client.getErrorMessage("OK|token123");
        assert msg4.equals("OK|token123") : "Non-error failed: " + msg4;
        
        System.out.println("✓ getErrorMessage tests passed");
    }
}
