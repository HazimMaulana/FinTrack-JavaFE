package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public final class CategoryStore {
    public static final String EXPENSE = "Pengeluaran";
    public static final String INCOME = "Pemasukan";

    private static final List<String> expenseCategories = new ArrayList<>();
    private static final List<String> incomeCategories = new ArrayList<>();
    private static final List<Consumer<Snapshot>> listeners = new ArrayList<>();
    private static final SocketClient client = SocketClient.getInstance();
    private static final SessionManager sessionManager = SessionManager.getInstance();

    private CategoryStore() {}

    public static synchronized void addListener(Consumer<Snapshot> listener) {
        listeners.add(listener);
        listener.accept(snapshot());
    }

    /**
     * Add category asynchronously via backend.
     * @param type category type (Pemasukan/Pengeluaran)
     * @param name category name
     * @param onSuccess callback on success
     * @param onError callback with error message on failure
     */
    public static void addCategory(String type, String name, Runnable onSuccess, Consumer<String> onError) {
        if (name == null || name.trim().isEmpty()) {
            onError.accept("Category name cannot be empty");
            return;
        }
        
        String normalized = name.trim();
        
        executeAsync(() -> {
            try {
                String sessionToken = sessionManager.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    updateUI(() -> onError.accept("No valid session. Please login."));
                    return;
                }
                
                // Format: ADD_CATEGORY|sessionToken|type|name
                String command = client.formatCommand("ADD_CATEGORY", sessionToken, type, normalized);
                String response = client.sendCommand(command);
                
                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }
                
                // Response should be OK
                if (response != null && response.startsWith("OK")) {
                    // Add to local list
                    synchronized (CategoryStore.class) {
                        if (INCOME.equalsIgnoreCase(type)) {
                            if (!containsIgnoreCase(incomeCategories, normalized)) {
                                incomeCategories.add(normalized);
                                notifyListeners();
                            }
                        } else {
                            if (!containsIgnoreCase(expenseCategories, normalized)) {
                                expenseCategories.add(normalized);
                                notifyListeners();
                            }
                        }
                    }
                    
                    updateUI(onSuccess);
                } else {
                    updateUI(() -> onError.accept("Unexpected response format"));
                }
                
            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to add category: " + e.getMessage()));
            }
        });
    }

    public static synchronized Snapshot snapshot() {
        return new Snapshot(
            List.copyOf(expenseCategories),
            List.copyOf(incomeCategories)
        );
    }

    /**
     * Remove category asynchronously via backend.
     * @param type category type (Pemasukan/Pengeluaran)
     * @param name category name
     * @param onSuccess callback on success
     * @param onError callback with error message on failure
     */
    public static void removeCategory(String type, String name, Runnable onSuccess, Consumer<String> onError) {
        if (name == null || name.trim().isEmpty()) {
            onError.accept("Category name cannot be empty");
            return;
        }
        
        String target = name.trim();
        
        executeAsync(() -> {
            try {
                String sessionToken = sessionManager.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    updateUI(() -> onError.accept("No valid session. Please login."));
                    return;
                }
                
                // Format: DELETE_CATEGORY|sessionToken|type|name
                String command = client.formatCommand("DELETE_CATEGORY", sessionToken, type, target);
                String response = client.sendCommand(command);
                
                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }
                
                // Response should be OK
                if (response != null && response.startsWith("OK")) {
                    // Remove from local list
                    synchronized (CategoryStore.class) {
                        boolean removed = false;
                        if (INCOME.equalsIgnoreCase(type)) {
                            removed = incomeCategories.removeIf(s -> s.equalsIgnoreCase(target));
                        } else {
                            removed = expenseCategories.removeIf(s -> s.equalsIgnoreCase(target));
                        }
                        if (removed) {
                            notifyListeners();
                        }
                    }
                    
                    updateUI(onSuccess);
                } else {
                    updateUI(() -> onError.accept("Unexpected response format"));
                }
                
            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to remove category: " + e.getMessage()));
            }
        });
    }

    private static boolean containsIgnoreCase(List<String> list, String value) {
        String cmp = value.toLowerCase(Locale.ROOT);
        for (String s : list) {
            if (s.toLowerCase(Locale.ROOT).equals(cmp)) return true;
        }
        return false;
    }

    /**
     * Load all categories from backend.
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
                
                // Format: GET_CATEGORIES|sessionToken
                String command = client.formatCommand("GET_CATEGORIES", sessionToken);
                String response = client.sendCommand(command);
                
                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }
                
                // Parse DATA_CATEGORIES response
                // Format: DATA_CATEGORIES|count|type1|name1|type2|name2|...
                String[] parts = client.parseResponse(response);
                
                if (parts.length < 2 || !parts[0].equals("DATA_CATEGORIES")) {
                    updateUI(() -> onError.accept("Unexpected response format"));
                    return;
                }
                
                int count = Integer.parseInt(parts[1]);
                List<String> newExpenseCategories = new ArrayList<>();
                List<String> newIncomeCategories = new ArrayList<>();
                
                int index = 2;
                for (int i = 0; i < count; i++) {
                    if (index + 1 >= parts.length) {
                        break; // Not enough data
                    }
                    
                    String type = parts[index++];
                    String name = parts[index++];
                    
                    // Separate by type
                    if (INCOME.equalsIgnoreCase(type)) {
                        newIncomeCategories.add(name);
                    } else {
                        newExpenseCategories.add(name);
                    }
                }
                
                // Update local lists
                synchronized (CategoryStore.class) {
                    expenseCategories.clear();
                    expenseCategories.addAll(newExpenseCategories);
                    incomeCategories.clear();
                    incomeCategories.addAll(newIncomeCategories);
                    notifyListeners();
                }
                
                updateUI(onSuccess);
                
            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to load categories: " + e.getMessage()));
            }
        });
    }

    private static void notifyListeners() {
        Snapshot snap = snapshot();
        for (Consumer<Snapshot> l : listeners) {
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

    public record Snapshot(List<String> expenses, List<String> incomes) {}
}
