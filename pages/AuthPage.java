import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import utils.SocketClient;
import utils.SessionManager;

/**
 * AuthPage provides login and registration UI.
 * Handles user authentication with backend server.
 */
public class AuthPage extends JPanel {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton toggleButton;
    private final JLabel errorLabel;
    private final JLabel formTitle;
    private final Runnable onLoginSuccess;

    private boolean isRegisterMode;

    /**
     * Create AuthPage with login success callback.
     * 
     * @param onLoginSuccess callback to invoke when login succeeds
     */
    public AuthPage(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        this.isRegisterMode = false;

        setLayout(new GridBagLayout());
        setBackground(new Color(248, 250, 252)); // slate-50

        // Create centered card panel
        RoundPanel card = new RoundPanel(12, Color.WHITE, new Color(226, 232, 240));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(32, 32, 32, 32));
        card.setPreferredSize(new Dimension(400, 500));

        // Header with icon and title
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        // App icon
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setOpaque(false);
        JLabel iconLabel = new JLabel(new WalletIcon());
        iconLabel.setForeground(new Color(37, 99, 235)); // blue-600
        iconPanel.add(iconLabel);

        // Title
        formTitle = new JLabel("Login ke FinTrack");
        formTitle.setForeground(new Color(30, 41, 59)); // slate-800
        formTitle.setFont(formTitle.getFont().deriveFont(Font.BOLD, 24f));
        formTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("Kelola keuangan Anda dengan mudah");
        subtitle.setForeground(new Color(71, 85, 105)); // slate-600
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(8, 0, 0, 0));

        header.add(iconPanel);
        header.add(formTitle);
        header.add(subtitle);

        card.add(header, BorderLayout.NORTH);

        // Form fields
        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);

        // Username field
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(8, 12, 8, 12)));
        formFields.add(createFormField("Username", usernameField));
        formFields.add(Box.createVerticalStrut(16));

        // Password field
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(8, 12, 8, 12)));
        formFields.add(createFormField("Password", passwordField));
        formFields.add(Box.createVerticalStrut(8));

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(220, 38, 38)); // red-600
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setFont(errorLabel.getFont().deriveFont(Font.PLAIN, 12f));
        formFields.add(errorLabel);
        formFields.add(Box.createVerticalStrut(16));

        // Login button
        loginButton = new RoundedButton("Login", true);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> {
            if (isRegisterMode) {
                handleRegister();
            } else {
                handleLogin();
            }
        });
        formFields.add(loginButton);
        formFields.add(Box.createVerticalStrut(16));

        // Toggle mode link
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        togglePanel.setOpaque(false);
        JLabel toggleLabel = new JLabel("Belum punya akun? ");
        toggleLabel.setForeground(new Color(71, 85, 105));
        toggleButton = new JButton("Daftar di sini");
        toggleButton.setForeground(new Color(37, 99, 235)); // blue-600
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleButton.setFocusPainted(false);
        toggleButton.addActionListener(e -> toggleMode());
        togglePanel.add(toggleLabel);
        togglePanel.add(toggleButton);
        formFields.add(togglePanel);

        card.add(formFields, BorderLayout.CENTER);

        // Add card to center of page
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        add(card, gbc);

        // Add Enter key support
        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> loginButton.doClick());
    }

    /**
     * Create form field with label and input component.
     */
    private JPanel createFormField(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(71, 85, 105)); // slate-600
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 14f));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lbl);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);

        return panel;
    }

    /**
     * Handle login button click.
     * Validates input, sends LOGIN command to backend.
     * On success: saves session and calls onLoginSuccess callback.
     * On error: displays error message.
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Validate fields
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username dan password harus diisi");
            return;
        }

        // Clear error
        clearError();

        // Disable button during operation
        loginButton.setEnabled(false);
        loginButton.setText("Loading...");

        // Execute in background thread
        new Thread(() -> {
            try {
                SocketClient client = SocketClient.getInstance();
                String command = client.formatCommand("LOGIN", username, password);

                // Debug logging
                System.out.println("[AuthPage] Sending command: " + command);

                String response = client.sendCommand(command);

                // Debug logging
                System.out.println("[AuthPage] Received response: " + response);

                // Parse response
                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    System.out.println("[AuthPage] Error response detected: " + errorMsg);
                    SwingUtilities.invokeLater(() -> {
                        showError(errorMsg);
                        loginButton.setEnabled(true);
                        loginButton.setText(isRegisterMode ? "Daftar" : "Login");
                    });
                    return;
                }

                // Extract session token from OK response
                String[] parts = client.parseResponse(response);
                System.out.println("[AuthPage] Response parts: " + java.util.Arrays.toString(parts));

                if (parts.length >= 2 && parts[0].equals("OK")) {
                    String sessionToken = parts[1];
                    System.out.println("[AuthPage] Login successful! Token: " + sessionToken);

                    // Save session
                    SessionManager sessionManager = SessionManager.getInstance();
                    sessionManager.setSession(sessionToken, username);
                    sessionManager.saveToFile();
                    // Start realtime subscription
                    try {
                        SocketClient.getInstance().startSubscription();
                    } catch (Exception subEx) {
                        System.err.println("Warning: failed to start subscription: " + subEx.getMessage());
                    }

                    // Load all data from backend before showing dashboard
                    loadAllDataFromBackend(() -> {
                        // Call success callback on EDT after data loaded
                        SwingUtilities.invokeLater(() -> {
                            if (onLoginSuccess != null) {
                                onLoginSuccess.run();
                            }
                        });
                    });
                } else if (response != null && response.startsWith("LOGIN_FAIL")) {
                    System.out.println("[AuthPage] Login failed: " + response);
                    SwingUtilities.invokeLater(() -> {
                        showError("Username atau password salah");
                        loginButton.setEnabled(true);
                        loginButton.setText(isRegisterMode ? "Daftar" : "Login");
                    });
                } else {
                    System.out.println("[AuthPage] Unexpected response format: " + response);
                    SwingUtilities.invokeLater(() -> {
                        showError("Format response tidak valid: " + response);
                        loginButton.setEnabled(true);
                        loginButton.setText(isRegisterMode ? "Daftar" : "Login");
                    });
                }
            } catch (Exception e) {
                System.err.println(
                        "[AuthPage] Exception during login: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    showError("Gagal terhubung ke server: " + e.getMessage());
                    loginButton.setEnabled(true);
                    loginButton.setText(isRegisterMode ? "Daftar" : "Login");
                });
            }
        }).start();
    }

    /**
     * Handle register button click.
     * Validates input, sends REGISTER command to backend.
     * On success: automatically calls handleLogin().
     * On error: displays error message.
     */
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Validate fields
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username dan password harus diisi");
            return;
        }

        // Clear error
        clearError();

        // Disable button during operation
        loginButton.setEnabled(false);
        loginButton.setText("Loading...");

        // Execute in background thread
        new Thread(() -> {
            try {
                SocketClient client = SocketClient.getInstance();
                String command = client.formatCommand("REGISTER", username, password);
                String response = client.sendCommand(command);

                // Parse response
                if (client.isErrorResponse(response)) {
                    String errorMsg = client.getErrorMessage(response);
                    SwingUtilities.invokeLater(() -> {
                        showError(errorMsg);
                        loginButton.setEnabled(true);
                        loginButton.setText("Daftar");
                    });
                    return;
                }

                // Accept REGISTER_OK as success
                if (response != null && (response.startsWith("OK") || response.startsWith("REGISTER_OK"))) {
                    // Registration successful, now login
                    SwingUtilities.invokeLater(() -> {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                        handleLogin();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        showError("Registrasi gagal");
                        loginButton.setEnabled(true);
                        loginButton.setText("Daftar");
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Gagal terhubung ke server: " + e.getMessage());
                    loginButton.setEnabled(true);
                    loginButton.setText("Daftar");
                });
            }
        }).start();
    }

    /**
     * Logout method to be called from TopBar.
     * Sends LOGOUT command with session token.
     * Clears session and returns to AuthPage.
     */
    public void logout() {
        SessionManager sessionManager = SessionManager.getInstance();
        String sessionToken = sessionManager.getSessionToken();

        if (sessionToken != null && !sessionToken.isEmpty()) {
            // Send LOGOUT command in background
            new Thread(() -> {
                try {
                    SocketClient client = SocketClient.getInstance();
                    String command = client.formatCommand("LOGOUT", sessionToken);
                    client.sendCommand(command);
                } catch (Exception e) {
                    // Ignore errors during logout
                    System.err.println("Logout error: " + e.getMessage());
                }
            }).start();
        }

        // Clear session
        sessionManager.clearSession();
        // Stop realtime subscription
        try {
            SocketClient.getInstance().stopSubscription();
        } catch (Exception ignored) {
        }

        // Reset form
        resetFields();
    }

    /**
     * Toggle between login and register mode.
     * Updates form title and button text.
     */
    private void toggleMode() {
        isRegisterMode = !isRegisterMode;

        if (isRegisterMode) {
            formTitle.setText("Daftar ke FinTrack");
            loginButton.setText("Daftar");
            toggleButton.setText("Login di sini");
            JLabel toggleLabel = (JLabel) ((JPanel) toggleButton.getParent()).getComponent(0);
            toggleLabel.setText("Sudah punya akun? ");
        } else {
            formTitle.setText("Login ke FinTrack");
            loginButton.setText("Login");
            toggleButton.setText("Daftar di sini");
            JLabel toggleLabel = (JLabel) ((JPanel) toggleButton.getParent()).getComponent(0);
            toggleLabel.setText("Belum punya akun? ");
        }

        resetFields();
    }

    /**
     * Reset form fields to empty state.
     */
    public void resetFields() {
        usernameField.setText("");
        passwordField.setText("");
        clearError();
        loginButton.setEnabled(true);
        loginButton.setText(isRegisterMode ? "Daftar" : "Login");
    }

    /**
     * Display error message to user.
     */
    private void showError(String message) {
        errorLabel.setText(message);
    }

    /**
     * Clear error message.
     */
    private void clearError() {
        errorLabel.setText(" ");
    }

    // UI Components

    /**
     * Rounded panel with background and border.
     */
    static class RoundPanel extends JPanel {
        private final int arc;
        private final Color bg;
        private final Color border;

        RoundPanel(int arc, Color bg, Color border) {
            this.arc = arc;
            this.bg = bg;
            this.border = border;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Rounded button with primary/secondary styling.
     */
    static class RoundedButton extends JButton {
        private final boolean primary;

        RoundedButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setForeground(primary ? Color.WHITE : new Color(71, 85, 105));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(getFont().deriveFont(Font.PLAIN, 14f));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (primary) {
                if (!isEnabled()) {
                    g2.setColor(new Color(148, 163, 184)); // slate-400
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(29, 78, 216)); // blue-700
                } else {
                    g2.setColor(new Color(37, 99, 235)); // blue-600
                }
            } else {
                if (getModel().isPressed()) {
                    g2.setColor(new Color(241, 245, 249)); // slate-100
                } else {
                    g2.setColor(Color.WHITE);
                }
            }

            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            if (!primary) {
                g2.setColor(new Color(226, 232, 240)); // slate-200
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Wallet icon for auth page header.
     */
    static class WalletIcon implements Icon {
        private final int size = 48;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw wallet shape
            g2.setColor(new Color(37, 99, 235)); // blue-600
            g2.fillRoundRect(x + 6, y + 12, size - 12, size - 24, 8, 8);

            // Draw card inside
            g2.setColor(new Color(219, 234, 254)); // blue-100
            g2.fillRoundRect(x + 12, y + 18, size - 24, size - 36, 6, 6);

            // Draw button/clasp
            g2.setColor(new Color(37, 99, 235));
            g2.fillOval(x + size - 16, y + size / 2 - 3, 6, 6);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    /**
     * Load all data from backend (accounts, categories, transactions) after login.
     * 
     * @param onComplete callback to run after all data is loaded
     */
    private void loadAllDataFromBackend(Runnable onComplete) {
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
