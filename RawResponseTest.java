import utils.SocketClient;
import utils.SessionManager;

/**
 * Test to see raw responses from backend
 */
public class RawResponseTest {
    public static void main(String[] args) {
        try {
            SocketClient client = SocketClient.getInstance();
            client.connect();
            
            // Register and login
            String testUser = "rawtest_" + System.currentTimeMillis();
            client.sendCommand(client.formatCommand("REGISTER", testUser, "test123"));
            
            String loginResp = client.sendCommand(client.formatCommand("LOGIN", testUser, "test123"));
            String[] parts = client.parseResponse(loginResp);
            String token = parts[1];
            SessionManager.getInstance().setSession(token, testUser);
            
            System.out.println("Logged in with token: " + token.substring(0, 8) + "...\n");
            
            // Test ADD transaction
            System.out.println("=== Testing ADD Transaction ===");
            String addCmd = client.formatCommand("ADD", token, "2025-12-03", "Test", "Food", "expense", "50000", "Cash", "cash");
            System.out.println("Command: " + addCmd);
            String addResp = client.sendCommand(addCmd);
            System.out.println("Response: " + addResp);
            System.out.println();
            
            // Test GET_ALL
            System.out.println("=== Testing GET_ALL ===");
            String getAllCmd = client.formatCommand("GET_ALL", token);
            System.out.println("Command: " + getAllCmd);
            String getAllResp = client.sendCommand(getAllCmd);
            System.out.println("Response: " + getAllResp);
            System.out.println("Response length: " + getAllResp.length());
            System.out.println();
            
            // Test ADD_ACCOUNT
            System.out.println("=== Testing ADD_ACCOUNT ===");
            String addAccCmd = client.formatCommand("ADD_ACCOUNT", token, "Test Account", "123456", "1000000", "Bank");
            System.out.println("Command: " + addAccCmd);
            String addAccResp = client.sendCommand(addAccCmd);
            System.out.println("Response: " + addAccResp);
            System.out.println();
            
            // Test GET_ACCOUNTS
            System.out.println("=== Testing GET_ACCOUNTS ===");
            String getAccCmd = client.formatCommand("GET_ACCOUNTS", token);
            System.out.println("Command: " + getAccCmd);
            String getAccResp = client.sendCommand(getAccCmd);
            System.out.println("Response: " + getAccResp);
            System.out.println();
            
            // Test GET_CATEGORIES
            System.out.println("=== Testing GET_CATEGORIES ===");
            String getCatCmd = client.formatCommand("GET_CATEGORIES", token);
            System.out.println("Command: " + getCatCmd);
            String getCatResp = client.sendCommand(getCatCmd);
            System.out.println("Response: " + getCatResp);
            System.out.println("Response length: " + getCatResp.length());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
