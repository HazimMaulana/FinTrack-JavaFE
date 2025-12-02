package utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public final class TransactionStore {
    public static final String TYPE_INCOME = "Pemasukan";
    public static final String TYPE_EXPENSE = "Pengeluaran";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final List<Transaction> transactions = new ArrayList<>();
    private static final List<Consumer<Snapshot>> listeners = new ArrayList<>();
    private static final SocketClient client = SocketClient.getInstance();
    private static final SessionManager sessionManager = SessionManager.getInstance();

    private TransactionStore() {
    }

    public static synchronized void addListener(Consumer<Snapshot> listener) {
        listeners.add(listener);
        listener.accept(snapshot());
    }

    /**
     * Add transaction asynchronously via backend.
     * 
     * @param date        transaction date (yyyy-MM-dd)
     * @param type        transaction type (Pemasukan/Pengeluaran)
     * @param category    transaction category
     * @param accountName account name
     * @param accountType account type
     * @param amount      transaction amount (long integer)
     * @param desc        transaction description
     * @param onSuccess   callback with transaction ID on success
     * @param onError     callback with error message on failure
     */
    public static void addTransaction(String date, String type, String category,
            String accountName, String accountType,
            long amount, String desc,
            Consumer<String> onSuccess,
            Consumer<String> onError) {
        executeAsync(() -> {
            try {
                String sessionToken = sessionManager.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    updateUI(() -> onError.accept("No valid session. Please login."));
                    return;
                }

                // Format:
                // ADD|sessionToken|date|description|category|type|amount|accountName|accountType
                String command = client.formatCommand("ADD", sessionToken, date, desc,
                        category, type, String.valueOf(amount),
                        accountName, accountType);
                String response = client.sendCommand(command);

                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }

                // Parse response - backend returns SUMMARY with transaction data
                // For now, we'll generate a local ID and add to list
                // Then reload from backend to get the actual ID
                String[] parts = client.parseResponse(response);

                // Response format:
                // SUMMARY|id|username|date|desc|category|type|amount|accountName|accountType|balance
                if (parts.length >= 2 && parts[0].equals("SUMMARY")) {
                    String transactionId = parts[1];

                    synchronized (TransactionStore.class) {
                        addInternal(transactionId, parseDate(date), type, category,
                                accountName, accountType, amount, desc);
                        notifyListeners();
                    }

                    updateUI(() -> onSuccess.accept(transactionId));
                } else {
                    updateUI(() -> onError.accept("Unexpected response format"));
                }

            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to add transaction: " + e.getMessage()));
            }
        });
    }

    /**
     * Update transaction asynchronously via backend.
     * 
     * @param id          transaction ID
     * @param date        transaction date (yyyy-MM-dd)
     * @param type        transaction type
     * @param category    transaction category
     * @param accountName account name
     * @param accountType account type
     * @param amount      transaction amount
     * @param desc        transaction description
     * @param onSuccess   callback on success
     * @param onError     callback with error message on failure
     */
    public static void updateTransaction(String id, String date, String type,
            String category, String accountName,
            String accountType, long amount, String desc,
            Runnable onSuccess, Consumer<String> onError) {
        executeAsync(() -> {
            try {
                String sessionToken = sessionManager.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    updateUI(() -> onError.accept("No valid session. Please login."));
                    return;
                }

                // Format:
                // UPDATE|sessionToken|id|date|description|category|type|amount|accountName|accountType
                String command = client.formatCommand("UPDATE", sessionToken, id, date, desc,
                        category, type, String.valueOf(amount),
                        accountName, accountType);
                String response = client.sendCommand(command);

                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }

                // Update local list
                synchronized (TransactionStore.class) {
                    transactions.removeIf(t -> t.id().equals(id));
                    addInternal(id, parseDate(date), type, category, accountName, accountType, amount, desc);
                    notifyListeners();
                }

                updateUI(onSuccess);

            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to update transaction: " + e.getMessage()));
            }
        });
    }

    /**
     * Remove transaction asynchronously via backend.
     * 
     * @param id        transaction ID to remove
     * @param onSuccess callback on success
     * @param onError   callback with error message on failure
     */
    public static void removeTransaction(String id, Runnable onSuccess, Consumer<String> onError) {
        executeAsync(() -> {
            try {
                String sessionToken = sessionManager.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    updateUI(() -> onError.accept("No valid session. Please login."));
                    return;
                }

                // Format: DELETE|sessionToken|id
                String command = client.formatCommand("DELETE", sessionToken, id);
                String response = client.sendCommand(command);

                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }

                // Remove from local list
                synchronized (TransactionStore.class) {
                    transactions.removeIf(t -> t.id().equals(id));
                    notifyListeners();
                }

                updateUI(onSuccess);

            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to remove transaction: " + e.getMessage()));
            }
        });
    }

    public static synchronized Snapshot snapshot() {
        List<Transaction> copy = new ArrayList<>(transactions);
        copy.sort(Comparator.comparing(Transaction::date).reversed());
        return new Snapshot(copy);
    }

    private static void addInternal(String id, LocalDate date, String type, String category, String accountName,
            String accountType, long amount, String desc) {
        transactions.add(new Transaction(id, date, type, category, accountName, accountType, amount, desc));
    }

    private static LocalDate parseDate(String input) {
        try {
            return LocalDate.parse(input, FMT);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    public record Transaction(String id, LocalDate date, String type, String category, String accountName,
            String accountType, long amount, String description) {
        public boolean isIncome() {
            return TYPE_INCOME.equalsIgnoreCase(type);
        }

        public YearMonth yearMonth() {
            return YearMonth.from(date);
        }
    }

    public record Snapshot(List<Transaction> transactions) {
    }

    private static void notifyListeners() {
        Snapshot snap = snapshot();
        for (Consumer<Snapshot> l : listeners) {
            l.accept(snap);
        }
    }

    /**
     * Load all transactions from backend.
     * 
     * @param onSuccess callback on success
     * @param onError   callback with error message on failure
     */
    public static void loadFromBackend(Runnable onSuccess, Consumer<String> onError) {
        executeAsync(() -> {
            try {
                String sessionToken = sessionManager.getSessionToken();
                if (sessionToken == null || sessionToken.isEmpty()) {
                    updateUI(() -> onError.accept("No valid session. Please login."));
                    return;
                }

                // Format: GET_ALL|sessionToken
                String command = client.formatCommand("GET_ALL", sessionToken);
                String response = client.sendCommand(command);

                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    updateUI(() -> onError.accept(errorMsg));
                    return;
                }

                // Parse DATA_ALL response
                // Format:
                // DATA_ALL|count|id1|username1|date1|desc1|cat1|type1|amount1|accName1|accType1|...
                String[] parts = client.parseResponse(response);

                if (parts.length < 2 || !parts[0].equals("DATA_ALL")) {
                    updateUI(() -> onError.accept("Unexpected response format"));
                    return;
                }

                int count = Integer.parseInt(parts[1]);
                List<Transaction> newTransactions = new ArrayList<>();

                int index = 2;
                for (int i = 0; i < count; i++) {
                    if (index + 8 >= parts.length) {
                        break; // Not enough data
                    }

                    String id = parts[index++];
                    index++; // Skip username field
                    String date = parts[index++];
                    String desc = parts[index++];
                    String category = parts[index++];
                    String type = parts[index++];
                    long amount = Long.parseLong(parts[index++]);
                    String accountName = parts[index++];
                    String accountType = parts[index++];

                    newTransactions.add(new Transaction(id, parseDate(date), type,
                            category, accountName, accountType,
                            amount, desc));
                }

                // Update local list
                synchronized (TransactionStore.class) {
                    transactions.clear();
                    transactions.addAll(newTransactions);
                    notifyListeners();
                }

                updateUI(onSuccess);

            } catch (Exception e) {
                updateUI(() -> onError.accept("Failed to load transactions: " + e.getMessage()));
            }
        });
    }

    /**
     * Execute task on background thread.
     * 
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
     * 
     * @param task runnable to execute on UI thread
     */
    private static void updateUI(Runnable task) {
        javax.swing.SwingUtilities.invokeLater(task);
    }
}
