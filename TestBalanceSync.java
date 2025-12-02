import utils.SocketClient;
import utils.SessionManager;
import utils.AccountStore;
import utils.TransactionStore;

/**
 * Test to verify that:
 * 1. Account balance is correct after recalculation (599,400,000)
 * 2. Adding a new transaction updates the account balance
 * 3. The transaction appears in the correct month
 */
public class TestBalanceSync {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing Balance Synchronization ===\n");

        // Step 1: Login
        System.out.println("1. Logging in as aldi...");
        SocketClient client = SocketClient.getInstance();
        String response = client.sendCommand("LOGIN|aldi|indrakimak");
        System.out.println("Response: " + response);

        if (response.startsWith("OK|")) {
            String token = response.split("\\|")[1];
            SessionManager.getInstance().setSession(token, "aldi");
            SessionManager.getInstance().saveToFile();
            System.out.println("✓ Logged in successfully with token: " + token);
        } else {
            System.out.println("✗ Login failed!");
            return;
        }

        // Step 2: Load accounts
        System.out.println("\n2. Loading accounts...");
        Object lock = new Object();
        final boolean[] loaded = { false };

        utils.AccountStore.loadFromBackend(
                () -> {
                    synchronized (lock) {
                        loaded[0] = true;
                        lock.notify();
                    }
                },
                error -> {
                    System.out.println("✗ Error loading accounts: " + error);
                    synchronized (lock) {
                        lock.notify();
                    }
                });

        synchronized (lock) {
            lock.wait(5000);
        }

        if (loaded[0]) {
            utils.AccountStore.Snapshot snap = utils.AccountStore.snapshot();
            System.out.println("✓ Loaded " + snap.accounts().size() + " accounts:");
            for (utils.AccountStore.Account acc : snap.accounts()) {
                System.out.println("  - " + acc.name() + " (" + acc.type() + "): Rp " + acc.balance());
            }

            // Verify balance is 599,400,000 (after expense)
            long expectedBalance = 599_400_000L;
            if (snap.accounts().size() > 0) {
                long actualBalance = snap.accounts().get(0).balance();
                if (actualBalance == expectedBalance) {
                    System.out.println("✓ Balance is correct: Rp " + actualBalance);
                } else {
                    System.out.println(
                            "✗ Balance mismatch! Expected: Rp " + expectedBalance + ", Got: Rp " + actualBalance);
                }
            }
        }

        // Step 3: Add a new transaction for December
        System.out.println("\n3. Adding new INCOME transaction for December 2025...");
        loaded[0] = false;

        TransactionStore.addTransaction(
                "2025-12-03", // Today's date
                "Pemasukan", // Income
                "Gaji", // Category
                "bca", // Account name
                "Bank", // Account type
                5_000_000L, // 5 million
                "Gaji Desember", // Description
                id -> {
                    System.out.println("✓ Transaction added with ID: " + id);
                    synchronized (lock) {
                        loaded[0] = true;
                        lock.notify();
                    }
                },
                error -> {
                    System.out.println("✗ Error adding transaction: " + error);
                    synchronized (lock) {
                        lock.notify();
                    }
                });

        synchronized (lock) {
            lock.wait(5000);
        }

        // Step 4: Reload accounts to check balance update
        if (loaded[0]) {
            System.out.println("\n4. Reloading accounts to verify balance update...");
            Thread.sleep(500); // Small delay for backend to process

            loaded[0] = false;
            utils.AccountStore.loadFromBackend(
                    () -> {
                        synchronized (lock) {
                            loaded[0] = true;
                            lock.notify();
                        }
                    },
                    error -> {
                        System.out.println("✗ Error reloading accounts: " + error);
                        synchronized (lock) {
                            lock.notify();
                        }
                    });

            synchronized (lock) {
                lock.wait(5000);
            }

            if (loaded[0]) {
                utils.AccountStore.Snapshot snap = utils.AccountStore.snapshot();
                System.out.println("✓ Accounts reloaded:");
                for (utils.AccountStore.Account acc : snap.accounts()) {
                    System.out.println("  - " + acc.name() + " (" + acc.type() + "): Rp " + acc.balance());
                }

                // Verify balance increased by 5,000,000
                long expectedNewBalance = 604_400_000L;
                if (snap.accounts().size() > 0) {
                    long actualBalance = snap.accounts().get(0).balance();
                    if (actualBalance == expectedNewBalance) {
                        System.out.println("✓ Balance updated correctly: Rp " + actualBalance);
                    } else {
                        System.out.println("✗ Balance mismatch! Expected: Rp " + expectedNewBalance + ", Got: Rp "
                                + actualBalance);
                    }
                }
            }
        }

        // Step 5: Load transactions and check December data
        System.out.println("\n5. Loading transactions to verify December income...");
        loaded[0] = false;

        TransactionStore.loadFromBackend(
                () -> {
                    synchronized (lock) {
                        loaded[0] = true;
                        lock.notify();
                    }
                },
                error -> {
                    System.out.println("✗ Error loading transactions: " + error);
                    synchronized (lock) {
                        lock.notify();
                    }
                });

        synchronized (lock) {
            lock.wait(5000);
        }

        if (loaded[0]) {
            TransactionStore.Snapshot tsnap = TransactionStore.snapshot();
            System.out.println("✓ Loaded " + tsnap.transactions().size() + " transactions");

            // Filter December 2025 transactions
            java.time.YearMonth december2025 = java.time.YearMonth.of(2025, 12);
            long decemberIncome = tsnap.transactions().stream()
                    .filter(t -> t.yearMonth().equals(december2025) && t.isIncome())
                    .mapToLong(TransactionStore.Transaction::amount)
                    .sum();

            long decemberExpense = tsnap.transactions().stream()
                    .filter(t -> t.yearMonth().equals(december2025) && !t.isIncome())
                    .mapToLong(TransactionStore.Transaction::amount)
                    .sum();

            System.out.println("December 2025 Income: Rp " + decemberIncome);
            System.out.println("December 2025 Expense: Rp " + decemberExpense);

            if (decemberIncome >= 5_000_000L) {
                System.out.println("✓ December income includes the new transaction!");
            } else {
                System.out.println("✗ December income is incorrect!");
            }
        }

        System.out.println("\n=== Test Complete ===");
        client.disconnect();
        System.exit(0);
    }
}
