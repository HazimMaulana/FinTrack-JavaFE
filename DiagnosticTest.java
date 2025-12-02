import utils.SocketClient;
import utils.SessionManager;

/**
 * Diagnostic test to check session flow step by step
 */
public class DiagnosticTest {
    public static void main(String[] args) {
        System.out.println("=== Diagnostic Test ===\n");
        
        String testUser = "diagtest_" + System.currentTimeMillis();
        String testPass = "test123";
        
        try {
            // Step 1: Connect
            System.out.println("Step 1: Connecting to backend...");
            SocketClient client = SocketClient.getInstance();
            client.connect();
            System.out.println("✓ Connected\n");
            
            // Step 2: Register
            System.out.println("Step 2: Registering user...");
            String regCmd = client.formatCommand("REGISTER", testUser, testPass);
            System.out.println("Command: " + regCmd);
            String regResp = client.sendCommand(regCmd);
            System.out.println("Response: " + regResp);
            System.out.println();
            
            // Step 3: Login
            System.out.println("Step 3: Logging in...");
            String loginCmd = client.formatCommand("LOGIN", testUser, testPass);
            System.out.println("Command: " + loginCmd);
            String loginResp = client.sendCommand(loginCmd);
            System.out.println("Response: " + loginResp);
            
            String[] parts = client.parseResponse(loginResp);
            if (parts.length >= 2 && parts[0].equals("OK")) {
                String token = parts[1];
                System.out.println("✓ Got session token: " + token.substring(0, Math.min(8, token.length())) + "...");
                SessionManager.getInstance().setSession(token, testUser);
                System.out.println();
                
                // Step 4: Validate session
                System.out.println("Step 4: Validating session...");
                String valCmd = client.formatCommand("VALIDATE_SESSION", token);
                System.out.println("Command: " + valCmd);
                String valResp = client.sendCommand(valCmd);
                System.out.println("Response: " + valResp);
                System.out.println();
                
                // Step 5: Try GET_ALL
                System.out.println("Step 5: Testing GET_ALL...");
                String getAllCmd = client.formatCommand("GET_ALL", token);
                System.out.println("Command: " + getAllCmd);
                String getAllResp = client.sendCommand(getAllCmd);
                System.out.println("Response: " + getAllResp);
                System.out.println();
                
                // Step 6: Try GET_ACCOUNTS
                System.out.println("Step 6: Testing GET_ACCOUNTS...");
                String getAccCmd = client.formatCommand("GET_ACCOUNTS", token);
                System.out.println("Command: " + getAccCmd);
                String getAccResp = client.sendCommand(getAccCmd);
                System.out.println("Response: " + getAccResp);
                System.out.println();
                
                // Step 7: Logout
                System.out.println("Step 7: Logging out...");
                String logoutCmd = client.formatCommand("LOGOUT", token);
                System.out.println("Command: " + logoutCmd);
                String logoutResp = client.sendCommand(logoutCmd);
                System.out.println("Response: " + logoutResp);
                
            } else {
                System.out.println("✗ Login failed");
            }
            
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
