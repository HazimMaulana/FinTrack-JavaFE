package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class AccountStore {
    public static final String TYPE_BANK = "Bank";
    public static final String TYPE_WALLET = "Dompet Digital";
    public static final String TYPE_CASH = "Cash";
    public static final String TYPE_CREDIT = "Kredit";

    private static final List<Account> accounts = new ArrayList<>();
    private static final List<Consumer<Snapshot>> listeners = new ArrayList<>();
    private static final SocketClient client = SocketClient.getInstance();
    private static final SessionManager sessionManager = SessionManager.getInstance();

    private AccountStore(){}

    public static synchronized void addListener(Consumer<Snapshot> listener){
        listeners.add(listener);
        listener.accept(snapshot());
    }

    /**
     * Add account asynchronously via backend.
     * @param name account name
     * @param number account number
     * @param balance account balance (long integer)
     * @param type account type
     * @param onSuccess callback with account ID on success
     * @param onError callback with error message on failure
     */
    public static void addAccount(String name, String number, long balance, String type,
                                 Consumer<String> onSuccess, Consumer<String> onError) {
        executeAsync(() -> {
            try {
                String sessionToken = sessionManager.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    updateUI(() -> onError.accept("No valid session. Please login."));
                    return;
                }
                
                // Format: ADD_ACCOUNT|sessionToken|name|number|balance|type
                String command = client.formatCommand("ADD_ACCOUNT", sessionToken, name, 
                                                     number, String.valueOf(balance), type);
                String response = client.sendCommand(command);
                
                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }
                
                // Parse response - backend returns OK|accountId
                String[] parts = client.parseResponse(response);
                
                if (parts.length >= 2 && parts[0].equals("OK")) {
                    String accountId = parts[1];
                    
                    synchronized (AccountStore.class) {
                        accounts.add(new Account(accountId, name, number, balance, type));
                        notifyListeners();
                    }
                    
                    updateUI(() -> onSuccess.accept(accountId));
                } else {
                    updateUI(() -> onError.accept("Unexpected response format"));
                }
                
            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to add account: " + e.getMessage()));
            }
        });
    }

    /**
     * Update account asynchronously via backend.
     * @param id account ID
     * @param name account name
     * @param number account number
     * @param balance account balance
     * @param type account type
     * @param onSuccess callback on success
     * @param onError callback with error message on failure
     */
    public static void updateAccount(String id, String name, String number, long balance, 
                                    String type, Runnable onSuccess, Consumer<String> onError) {
        executeAsync(() -> {
            try {
                String sessionToken = sessionManager.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    updateUI(() -> onError.accept("No valid session. Please login."));
                    return;
                }
                
                // Format: UPDATE_ACCOUNT|sessionToken|id|name|number|balance|type
                String command = client.formatCommand("UPDATE_ACCOUNT", sessionToken, id, 
                                                     name, number, String.valueOf(balance), type);
                String response = client.sendCommand(command);
                
                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }
                
                // Update local list
                synchronized (AccountStore.class) {
                    accounts.removeIf(a -> a.id().equals(id));
                    accounts.add(new Account(id, name, number, balance, type));
                    notifyListeners();
                }
                
                updateUI(onSuccess);
                
            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to update account: " + e.getMessage()));
            }
        });
    }

    /**
     * Remove account asynchronously via backend.
     * @param id account ID to remove
     * @param onSuccess callback on success
     * @param onError callback with error message on failure
     */
    public static void removeAccount(String id, Runnable onSuccess, Consumer<String> onError) {
        executeAsync(() -> {
            try {
                String sessionToken = sessionManager.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    updateUI(() -> onError.accept("No valid session. Please login."));
                    return;
                }
                
                // Format: DELETE_ACCOUNT|sessionToken|id
                String command = client.formatCommand("DELETE_ACCOUNT", sessionToken, id);
                String response = client.sendCommand(command);
                
                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }
                
                // Remove from local list
                synchronized (AccountStore.class) {
                    accounts.removeIf(a -> a.id().equals(id));
                    notifyListeners();
                }
                
                updateUI(onSuccess);
                
            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to remove account: " + e.getMessage()));
            }
        });
    }

    /**
     * Load all accounts from backend.
     * @param onSuccess callback on success
     * @param onError callback with error message on failure
     */
    public static void loadFromBackend(Runnable onSuccess, Consumer<String> onError) {
        executeAsync(() -> {
            try {
                String sessionToken = sessionManager.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    updateUI(() -> onError.accept("No valid session. Please login."));
                    return;
                }
                
                // Format: GET_ACCOUNTS|sessionToken
                String command = client.formatCommand("GET_ACCOUNTS", sessionToken);
                String response = client.sendCommand(command);
                
                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }
                
                // Parse DATA_ACCOUNTS response
                // Format: DATA_ACCOUNTS|count|id1|name1|number1|balance1|type1|...
                String[] parts = client.parseResponse(response);
                
                if (parts.length < 2 || !parts[0].equals("DATA_ACCOUNTS")) {
                    updateUI(() -> onError.accept("Unexpected response format"));
                    return;
                }
                
                int count = Integer.parseInt(parts[1]);
                List<Account> newAccounts = new ArrayList<>();
                
                int index = 2;
                for (int i = 0; i < count; i++) {
                    if (index + 4 >= parts.length) {
                        break; // Not enough data
                    }
                    
                    String id = parts[index++];
                    String name = parts[index++];
                    String number = parts[index++];
                    long balance = Long.parseLong(parts[index++]);
                    String type = parts[index++];
                    
                    newAccounts.add(new Account(id, name, number, balance, type));
                }
                
                // Update local list
                synchronized (AccountStore.class) {
                    accounts.clear();
                    accounts.addAll(newAccounts);
                    notifyListeners();
                }
                
                updateUI(onSuccess);
                
            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to load accounts: " + e.getMessage()));
            }
        });
    }

    public static synchronized Snapshot snapshot(){
        return new Snapshot(List.copyOf(accounts));
    }

    private static void notifyListeners(){
        Snapshot snap = snapshot();
        for(Consumer<Snapshot> l : listeners){
            l.accept(snap);
        }
    }

    /**
     * Execute task on background thread.
     * @param task runnable to execute
     */
    private static void executeAsync(Runnable task) {
        new Thread(() -> {
            try {
                task.run();
            } catch (Exception e) {
                // Log error but don't crash
                System.err.println("Background task error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Execute task on Event Dispatch Thread (EDT).
     * @param task runnable to execute on UI thread
     */
    private static void updateUI(Runnable task) {
        javax.swing.SwingUtilities.invokeLater(task);
    }

    public record Account(String id, String name, String number, long balance, String type) {}
    public record Snapshot(List<Account> accounts){}
}
