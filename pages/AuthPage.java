import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AuthPage extends JPanel {
    private static final String LOGIN = "login";
    private static final String SIGNUP = "signup";

    private final CardLayout formLayout = new CardLayout();
    private final JPanel formStack = new JPanel(formLayout);
    private final Runnable onAuthenticated;

    private RoundedTextField loginEmail;
    private RoundedPasswordField loginPassword;
    private RoundedTextField signupName;
    private RoundedTextField signupEmail;
    private RoundedPasswordField signupPassword;
    private RoundedPasswordField signupConfirm;
    private JLabel loginStatus;
    private JLabel signupStatus;

    public AuthPage(Runnable onAuthenticated) {
        this.onAuthenticated = onAuthenticated;
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel gradient = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                // Gradient from top-left to bottom-right with modern blue shades
                GradientPaint gp = new GradientPaint(0, 0, new Color(79, 70, 229), getWidth(), getHeight(), new Color(99, 102, 241));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decorative orbs with better positioning
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillOval(-80, -40, 300, 300);
                g2.fillOval(getWidth() - 250, getHeight() - 250, 350, 350);
                // Additional accent orbs
                g2.setColor(new Color(147, 51, 234, 20));
                g2.fillOval(getWidth() / 2 - 100, -100, 200, 200);
                g2.dispose();
            }
        };
        gradient.setBorder(new EmptyBorder(40, 40, 40, 40));
        add(gradient, BorderLayout.CENTER);

        JPanel glass = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                // Subtle shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 32, 32);
                // Glass background
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                // Border
                g2.setColor(new Color(255, 255, 255, 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 32, 32);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        glass.setOpaque(false);
        glass.setBorder(new EmptyBorder(32, 32, 32, 32));
        gradient.add(glass, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(16, 16, 16, 16);

        JPanel hero = buildHero();
        glass.add(hero, gc);

        JPanel authCard = buildAuthCard();
        gc.gridx = 1;
        gc.weightx = 1.1;
        glass.add(authCard, gc);

        showLogin();
    }

    public void resetFields() {
        loginEmail.setText("");
        loginPassword.setText("");
        signupName.setText("");
        signupEmail.setText("");
        signupPassword.setText("");
        signupConfirm.setText("");
        loginStatus.setText("");
        signupStatus.setText("");
        showLogin();
    }

    private JPanel buildHero() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Logo badge with better styling
        JLabel badge = new JLabel("💰 FinTrack");
        badge.setForeground(Color.WHITE);
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, badge.getFont().getSize2D() + 12f));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headline = new JLabel("<html>Kelola finansial<br/>lebih <span style='color: #FDE047;'>cerdas</span>.</html>");
        headline.setForeground(Color.WHITE);
        headline.setFont(headline.getFont().deriveFont(Font.BOLD, headline.getFont().getSize2D() + 22f));
        headline.setBorder(new EmptyBorder(24, 0, 16, 0));
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><div style='line-height: 1.6;'>Dashboard yang kaya insight, kategori rapi, dan laporan modern dalam satu aplikasi desktop yang powerful.</div></html>");
        subtitle.setForeground(new Color(226, 232, 240, 230));
        subtitle.setBorder(new EmptyBorder(0, 0, 28, 0));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, subtitle.getFont().getSize2D() + 3f));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(badge);
        panel.add(headline);
        panel.add(subtitle);

        // Features with icons
        panel.add(heroStat("⚡ 5 Menit", "Setup cepat tanpa konfigurasi rumit", new Color(96, 165, 250)));
        panel.add(Box.createVerticalStrut(12));
        panel.add(heroStat("📊 Realtime", "Pantau arus kas & laporan secara live", new Color(167, 139, 250)));
        panel.add(Box.createVerticalStrut(12));
        panel.add(heroStat("🔒 Aman", "Data tersimpan lokal, privasi terjaga", new Color(134, 239, 172)));

        return panel;
    }

    private JPanel heroStat(String title, String desc, Color dotColor) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(t.getFont().deriveFont(Font.BOLD, t.getFont().getSize2D() + 2f));
        JLabel d = new JLabel(desc);
        d.setForeground(new Color(226, 232, 240, 220));
        d.setBorder(new EmptyBorder(6, 0, 0, 0));
        d.setFont(d.getFont().deriveFont(Font.PLAIN, d.getFont().getSize2D() + 1f));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(t);
        text.add(d);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildAuthCard() {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BorderLayout());

        RoundPanel wrapper = new RoundPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(32, 32, 32, 32));
        card.add(wrapper, BorderLayout.CENTER);

        formStack.setOpaque(false);
        formStack.add(buildLoginForm(), LOGIN);
        formStack.add(buildSignupForm(), SIGNUP);
        wrapper.add(formStack, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildLoginForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Masuk");
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 8f));
        title.setForeground(new Color(15, 23, 42));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Selamat datang kembali! 👋");
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setBorder(new EmptyBorder(6, 0, 24, 0));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, subtitle.getFont().getSize2D() + 1f));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginEmail = new RoundedTextField("Email atau username");
        loginPassword = new RoundedPasswordField("Kata sandi");

        PrimaryButton loginBtn = new PrimaryButton("Masuk Sekarang");
        loginBtn.addActionListener(e -> attemptLogin());

        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linkRow.setOpaque(false);
        linkRow.add(new JLabel("Belum punya akun? "));
        linkRow.add(linkLabel("Buat akun", this::showSignup));

        loginStatus = statusLabel();

        form.add(title);
        form.add(subtitle);
        form.add(loginEmail);
        form.add(Box.createVerticalStrut(12));
        form.add(loginPassword);
        form.add(Box.createVerticalStrut(16));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(linkRow);
        form.add(Box.createVerticalStrut(8));
        form.add(loginStatus);

        return form;
    }

    private JPanel buildSignupForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Buat Akun");
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 8f));
        title.setForeground(new Color(15, 23, 42));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Mulai pantau finansial Anda ✨");
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setBorder(new EmptyBorder(6, 0, 24, 0));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, subtitle.getFont().getSize2D() + 1f));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        signupName = new RoundedTextField("Nama lengkap");
        signupEmail = new RoundedTextField("Email aktif");
        signupPassword = new RoundedPasswordField("Kata sandi");
        signupConfirm = new RoundedPasswordField("Ulangi kata sandi");

        PrimaryButton signupBtn = new PrimaryButton("Daftar & Masuk");
        signupBtn.addActionListener(e -> attemptSignup());

        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linkRow.setOpaque(false);
        linkRow.add(new JLabel("Sudah punya akun? "));
        linkRow.add(linkLabel("Masuk", this::showLogin));

        signupStatus = statusLabel();

        form.add(title);
        form.add(subtitle);
        form.add(signupName);
        form.add(Box.createVerticalStrut(10));
        form.add(signupEmail);
        form.add(Box.createVerticalStrut(10));
        form.add(signupPassword);
        form.add(Box.createVerticalStrut(10));
        form.add(signupConfirm);
        form.add(Box.createVerticalStrut(16));
        form.add(signupBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(linkRow);
        form.add(Box.createVerticalStrut(8));
        form.add(signupStatus);

        return form;
    }

    private void attemptLogin() {
        loginStatus.setForeground(new Color(220, 38, 38));
        if (loginEmail.getText().isBlank() || loginPassword.getPassword().length == 0) {
            loginStatus.setText("Lengkapi email dan kata sandi.");
            return;
        }
        loginStatus.setForeground(new Color(22, 163, 74));
        loginStatus.setText("Login berhasil. Mengalihkan...");
        if (onAuthenticated != null) {
            onAuthenticated.run();
        }
    }

    private void attemptSignup() {
        signupStatus.setForeground(new Color(220, 38, 38));
        if (signupName.getText().isBlank() || signupEmail.getText().isBlank() ||
            signupPassword.getPassword().length == 0 || signupConfirm.getPassword().length == 0) {
            signupStatus.setText("Lengkapi semua data akun.");
            return;
        }
        String pwd = new String(signupPassword.getPassword());
        String confirm = new String(signupConfirm.getPassword());
        if (!pwd.equals(confirm)) {
            signupStatus.setText("Kata sandi tidak sama.");
            return;
        }
        signupStatus.setForeground(new Color(22, 163, 74));
        signupStatus.setText("Akun dibuat! Silakan masuk.");
        showLogin();
    }

    private void showLogin() {
        formLayout.show(formStack, LOGIN);
    }

    private void showSignup() {
        formLayout.show(formStack, SIGNUP);
    }

    private JLabel linkLabel(String text, Runnable onClick) {
        JLabel l = new JLabel("<html><u>" + text + "</u></html>");
        l.setForeground(new Color(37, 99, 235));
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.run();
            }
        });
        return l;
    }

    private JLabel statusLabel() {
        JLabel l = new JLabel(" ");
        l.setForeground(new Color(71, 85, 105));
        return l;
    }

    // Rounded glass card
    static class ColorDotIcon implements Icon {
        private final int size;
        private final Color color;

        ColorDotIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
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

    static class RoundPanel extends JPanel {
        RoundPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            // Multi-layer shadow for depth
            g2.setColor(new Color(0, 0, 0, 8));
            g2.fillRoundRect(6, 6, getWidth() - 6, getHeight() - 6, 20, 20);
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 20, 20);
            // Main card background
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            // Subtle border
            g2.setColor(new Color(226, 232, 240, 100));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedTextField extends JTextField {
        private final String placeholder;
        private final int arc = 12;

        RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setFont(getFont().deriveFont(Font.PLAIN, getFont().getSize2D() + 1f));
            setForeground(new Color(15, 23, 42));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // Background
            g2.setColor(new Color(248, 250, 252));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            // Border (different color when focused)
            if (isFocusOwner()) {
                g2.setColor(new Color(99, 102, 241, 180));
                g2.setStroke(new BasicStroke(2f));
            } else {
                g2.setColor(new Color(226, 232, 240));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g3.setColor(new Color(148, 163, 184));
                g3.setFont(getFont());
                g3.drawString(placeholder, 16, getHeight() / 2 + g3.getFontMetrics().getAscent() / 2 - 3);
                g3.dispose();
            }
        }
    }

    static class RoundedPasswordField extends JPasswordField {
        private final String placeholder;
        private final int arc = 12;

        RoundedPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setFont(getFont().deriveFont(Font.PLAIN, getFont().getSize2D() + 1f));
            setForeground(new Color(15, 23, 42));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // Background
            g2.setColor(new Color(248, 250, 252));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            // Border (different color when focused)
            if (isFocusOwner()) {
                g2.setColor(new Color(99, 102, 241, 180));
                g2.setStroke(new BasicStroke(2f));
            } else {
                g2.setColor(new Color(226, 232, 240));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
            if (getPassword().length == 0 && !isFocusOwner()) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g3.setColor(new Color(148, 163, 184));
                g3.setFont(getFont());
                g3.drawString(placeholder, 16, getHeight() / 2 + g3.getFontMetrics().getAscent() / 2 - 3);
                g3.dispose();
            }
        }
    }

    static class PrimaryButton extends JButton {
        private final Color start = new Color(79, 70, 229);
        private final Color end = new Color(99, 102, 241);
        private final int arc = 12;
        private boolean hovered = false;

        PrimaryButton(String text) {
            super(text);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(14, 20, 14, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(getFont().deriveFont(Font.BOLD, getFont().getSize2D() + 1f));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            // Shadow
            if (hovered) {
                g2.setColor(new Color(79, 70, 229, 40));
                g2.fillRoundRect(2, 4, getWidth() - 2, getHeight() - 2, arc, arc);
            }
            // Gradient background
            Color s = hovered ? new Color(67, 56, 202) : start;
            Color e = hovered ? new Color(79, 70, 229) : end;
            GradientPaint gp = new GradientPaint(0, 0, s, getWidth(), getHeight(), e);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
