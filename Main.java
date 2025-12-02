import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import utils.FontUtil;
import utils.SocketClient;
import utils.SessionManager;

public class Main {
    private static final String DASHBOARD = "dashboard";
    private static final String TRANSAKSI = "transaksi";
    private static final String LAPORAN = "laporan";
    private static final String AKUN = "akun";
    private static final String KATEGORI = "kategori";
    private static final String PENGATURAN = "pengaturan";
    private static final String AUTH = "auth";
    private static final String APP = "app";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUI);
    }

    private static void createAndShowUI() {
        // Apply global font family chain (Swing equivalent of JavaFX label.setStyle)
        FontUtil.applyGlobalFont("Segoe UI", "San Francisco", "SF Pro Text", "Ubuntu", "Noto Sans", "Arial",
                "SansSerif");

        // Task 8.1: Initialize SocketClient on startup
        try {
            SocketClient.getInstance().connect();
        } catch (IOException e) {
            showConnectionError(e.getMessage());
            return; // Exit if cannot connect to backend
        }

        JFrame frame = new JFrame("FinTrack - Financial Tracker");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1280, 700);
        frame.setLocationRelativeTo(null);

        CardLayout shellLayout = new CardLayout();
        JPanel shell = new JPanel(shellLayout);
        frame.setContentPane(shell);

        // Task 8.3: Create AuthPage instance with onLoginSuccess callback
        AuthPage authPage = new AuthPage(() -> {
            // Callback should switch to APP shell
            shellLayout.show(shell, APP);
        });

        // Task 8.3: Ensure logout from TopBar returns to AUTH page
        JPanel appShell = createAppShell(() -> {
            authPage.logout();
            authPage.resetFields();
            shellLayout.show(shell, AUTH);
        });

        // Task 8.3: Add AuthPage to CardLayout shell
        shell.add(authPage, AUTH);
        shell.add(appShell, APP);

        frame.setVisible(true);

        // Task 8.2: Implement session validation on startup
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.loadFromFile();

        if (sessionManager.hasValidSession()) {
            // If session exists, validate it
            if (sessionManager.validateSession()) {
                // If valid, load all data from backend first
                loadAllDataFromBackend(() -> {
                    // After data loaded, show APP shell
                    SwingUtilities.invokeLater(() -> {
                        shellLayout.show(shell, APP);
                    });
                });
                try {
                    SocketClient.getInstance().startSubscription();
                } catch (Exception e) {
                    System.err.println("Warning: failed to start subscription on resume: " + e.getMessage());
                }
            } else {
                // If invalid, show AUTH page
                shellLayout.show(shell, AUTH);
            }
        } else {
            // If no session, show AUTH page
            shellLayout.show(shell, AUTH);
        }
    }

    /**
     * Show connection error dialog and exit application.
     * 
     * @param errorMessage detailed error message
     */
    private static void showConnectionError(String errorMessage) {
        String message = "Cannot connect to server. Please ensure the backend is running.\n\n" +
                "Error: " + errorMessage;
        JOptionPane.showMessageDialog(null,
                message,
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private static JPanel createAppShell(Runnable onLogout) {
        JPanel root = new JPanel(new BorderLayout());

        // Header stack: TopBar + InfoBar + ActionBar (stacked vertically)
        TopBar topBar = new TopBar(onLogout);
        InfoBar infoBar = new InfoBar();

        // Subscribe InfoBar to data changes for real-time updates
        Runnable updateInfoBar = () -> {
            java.time.YearMonth now = java.time.YearMonth.now();

            // Get current snapshots
            utils.AccountStore.Snapshot accounts = utils.AccountStore.snapshot();
            utils.TransactionStore.Snapshot transactions = utils.TransactionStore.snapshot();

            // Calculate total saldo from accounts
            long totalSaldo = accounts.accounts().stream()
                    .mapToLong(utils.AccountStore.Account::balance)
                    .sum();

            // Calculate income and expense for current month
            long incomeThisMonth = transactions.transactions().stream()
                    .filter(t -> t.yearMonth().equals(now) && t.isIncome())
                    .mapToLong(utils.TransactionStore.Transaction::amount)
                    .sum();

            long expenseThisMonth = transactions.transactions().stream()
                    .filter(t -> t.yearMonth().equals(now) && !t.isIncome())
                    .mapToLong(utils.TransactionStore.Transaction::amount)
                    .sum();

            // Update InfoBar with real data
            javax.swing.SwingUtilities.invokeLater(() -> {
                infoBar.setTotals(totalSaldo, incomeThisMonth, expenseThisMonth);
            });
        };

        // Listen to account and transaction changes
        utils.AccountStore.addListener(snap -> updateInfoBar.run());
        utils.TransactionStore.addListener(snap -> updateInfoBar.run());

        // Initial update
        updateInfoBar.run();

        // Content area with CardLayout to switch pages
        JPanel content = new JPanel(new CardLayout());
        HomePage homePage = new HomePage();
        TransaksiPage transaksiPage = new TransaksiPage();
        LaporanPage laporanPage = new LaporanPage();
        content.add(homePage, DASHBOARD);
        content.add(transaksiPage, TRANSAKSI);
        content.add(laporanPage, LAPORAN);
        content.add(new AkunWalletPage(), AKUN);
        content.add(new KategoriPage(), KATEGORI);
        content.add(new SettingsPage(), PENGATURAN);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(topBar);
        header.add(infoBar);
        root.add(header, BorderLayout.NORTH);

        // Sidebar with navigation callback to switch cards
        Sidebar sidebar = new Sidebar(route -> switchTo(content, route));

        root.add(sidebar, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        // Ensure default is Dashboard
        switchTo(content, DASHBOARD);
        return root;
    }

    private static void switchTo(JPanel content, String route) {
        CardLayout cl = (CardLayout) content.getLayout();
        cl.show(content, route);
    }

    /**
     * Load all data from backend (accounts, categories, transactions) before
     * showing main app.
     * 
     * @param onComplete callback to run after all data is loaded
     */
    private static void loadAllDataFromBackend(Runnable onComplete) {
        new Thread(() -> {
            try {
                // Load accounts
                utils.AccountStore.loadFromBackend(
                        () -> {
                            // Load categories
                            utils.CategoryStore.loadFromBackend(
                                    () -> {
                                        // Load transactions
                                        utils.TransactionStore.loadFromBackend(
                                                () -> {
                                                    // All data loaded successfully
                                                    if (onComplete != null) {
                                                        onComplete.run();
                                                    }
                                                },
                                                error -> {
                                                    System.err.println("Failed to load transactions: " + error);
                                                    if (onComplete != null) {
                                                        onComplete.run();
                                                    }
                                                });
                                    },
                                    error -> {
                                        System.err.println("Failed to load categories: " + error);
                                        if (onComplete != null) {
                                            onComplete.run();
                                        }
                                    });
                        },
                        error -> {
                            System.err.println("Failed to load accounts: " + error);
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        });
            } catch (Exception e) {
                System.err.println("Error loading data: " + e.getMessage());
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }).start();
    }
}
