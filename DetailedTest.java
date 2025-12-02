import utils.SocketClient;
import utils.SessionManager;
import utils.TransactionStore;
import utils.AccountStore;
import utils.CategoryStore;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Detailed test to debug session and store issues
 */
public class DetailedTest {
    public static void main(String[] args) {
        System.out.println("=== Detailed Test ===\n");
        
        String testUser = "detailtest_" + System.currentTimeMillis();
        String testPass = "test123";
        
        try {
            // Step 1: Connect and login
            System.out.println("Step 1: Connecting and logging in...");
            SocketClient client = SocketClient.getInstance();
            client.connect();
            
            String regCmd = client.formatCommand("REGISTER", testUser, testPass);
            client.sendCommand(regCmd);
            
            String loginCmd = client.formatCommand("LOGIN", testUser, testPass);
            String loginResp = client.sendCommand(loginCmd);
            String[] parts = client.parseResponse(loginResp);
            String token = parts[1];
            
            SessionManager.getInstance().setSession(token, testUser);
            System.out.println("✓ Logged in with token: " + token.substring(0, 8) + "...");
            System.out.println("✓ Session set in SessionManager");
            System.out.println();
            
            // Step 2: Test adding transaction via Store
            System.out.println("Step 2: Adding transaction via TransactionStore...");
            final boolean[] addSuccess = {false};
            final String[] addedId = {null};
            
            String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            TransactionStore.addTransaction(
                date,
                "expense",
                "Food",
                "Cash",
                "cash",
                50000,
                "Test transaction",
                (id) -> {
                    addedId[0] = id;
                    addSuccess[0] = true;
                    System.out.println("✓ Transaction added with ID: " + id);
                },
                (error) -> {
                    System.out.println("✗ Add failed: " + error);
                }
            );
            
            Thread.sleep(3000);
            
            if (!addSuccess[0]) {
                System.out.println("✗ Transaction add failed");
                return;
            }
            System.out.println();
            
            // Step 3: Test loading transactions
            System.out.println("Step 3: Loading transactions via TransactionStore...");
            System.out.println("Current session token: " + SessionManager.getInstance().getSessionToken());
            System.out.println("Current username: " + SessionManager.getInstance().getUsername());
            
            final boolean[] loadSuccess = {false};
            TransactionStore.loadFromBackend(
                () -> {
                    loadSuccess[0] = true;
                    int count = TransactionStore.snapshot().transactions().size();
                    System.out.println("✓ Loaded " + count + " transactions");
                },
                (error) -> {
                    System.out.println("✗ Load failed: " + error);
                }
            );
            
            Thread.sleep(3000);
            System.out.println();
            
            // Step 4: Test adding account
            System.out.println("Step 4: Adding account via AccountStore...");
            System.out.println("Current session token: " + SessionManager.getInstance().getSessionToken());
            
            final boolean[] accAddSuccess = {false};
            AccountStore.addAccount(
                "Test Account",
                "123456",
                1000000,
                "Bank",
                (id) -> {
                    accAddSuccess[0] = true;
                    System.out.println("✓ Account added with ID: " + id);
                },
                (error) -> {
                    System.out.println("✗ Account add failed: " + error);
                }
            );
            
            Thread.sleep(3000);
            System.out.println();
            
            // Step 5: Test loading accounts
            System.out.println("Step 5: Loading accounts via AccountStore...");
            System.out.println("Current session token: " + SessionManager.getInstance().getSessionToken());
            
            final boolean[] accLoadSuccess = {false};
            AccountStore.loadFromBackend(
                () -> {
                    accLoadSuccess[0] = true;
                    int count = AccountStore.snapshot().accounts().size();
                    System.out.println("✓ Loaded " + count + " accounts");
                },
                (error) -> {
                    System.out.println("✗ Account load failed: " + error);
                }
            );
            
            Thread.sleep(3000);
            System.out.println();
            
            // Step 6: Test adding category
            System.out.println("Step 6: Adding category via CategoryStore...");
            System.out.println("Current session token: " + SessionManager.getInstance().getSessionToken());
            
            final boolean[] catAddSuccess = {false};
            CategoryStore.addCategory(
                "Pengeluaran",
                "Test Category",
                () -> {
                    catAddSuccess[0] = true;
                    System.out.println("✓ Category added");
                },
                (error) -> {
                    System.out.println("✗ Category add failed: " + error);
                }
            );
            
            Thread.sleep(3000);
            System.out.println();
            
            System.out.println("=== Test Complete ===");
            
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
