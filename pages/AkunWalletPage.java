import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import utils.ScrollUtil;
import utils.AccountStore;

public class AkunWalletPage extends JPanel {
    private final JPanel accountsGrid;
    private final JLabel totalSaldoLabel;
    private final JLabel bankCountLabel;
    private final JLabel walletCountLabel;
    private final JLabel cashLabel;
    private final JLabel creditLabel;
    private JLabel loadingLabel;
    private AccountStore.Snapshot snapshot = AccountStore.snapshot();

    public AkunWalletPage() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(0, 16, 16, 16));
        add(root, BorderLayout.CENTER);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 12, 16, 16));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);
        JLabel title = new JLabel("Akun & Wallet");
        title.setForeground(color(30, 41, 59));
        title.setFont(title.getFont().deriveFont(Font.PLAIN, title.getFont().getSize2D() + 6f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Kelola akun bank dan dompet digital Anda");
        subtitle.setForeground(color(71, 85, 105));
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerLeft.add(title);
        headerLeft.add(subtitle);
        header.add(headerLeft, BorderLayout.WEST);

        JButton addBtn = new RoundedButton("+ Tambah Akun Baru", true);
        addBtn.addActionListener(e -> promptAddAccount());
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Summary Cards
        JPanel summaryCards = new JPanel(new GridLayout(1, 5, 12, 12));
        summaryCards.setOpaque(false);
        summaryCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        totalSaldoLabel = buildSummaryCard("Total Saldo", "Rp 0", color(37, 99, 235));
        bankCountLabel = buildSummaryCard("Akun Bank", "0 akun", color(5, 150, 105));
        walletCountLabel = buildSummaryCard("Dompet Digital", "0 wallet", color(245, 158, 11));
        cashLabel = buildSummaryCard("Cash", "Rp 0", color(100, 116, 139));
        creditLabel = buildSummaryCard("Kredit", "0 kartu", color(109, 40, 217));
        summaryCards.add(totalSaldoLabel);
        summaryCards.add(bankCountLabel);
        summaryCards.add(walletCountLabel);
        summaryCards.add(cashLabel);
        summaryCards.add(creditLabel);
        root.add(summaryCards);
        root.add(Box.createVerticalStrut(16));

        // Accounts Grid
        RoundPanel accountsCard = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        accountsCard.setLayout(new BorderLayout());
        accountsCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel accountsTitle = new JLabel("Daftar Akun");
        accountsTitle.setForeground(color(30, 41, 59));
        accountsTitle.setFont(accountsTitle.getFont().deriveFont(Font.PLAIN, 16f));
        accountsTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        accountsCard.add(accountsTitle, BorderLayout.NORTH);

        accountsGrid = new JPanel(new GridLayout(0, 2, 16, 16));
        accountsGrid.setOpaque(false);

        JScrollPane accountsScroll = new JScrollPane(accountsGrid);
        accountsScroll.getViewport().setOpaque(false);
        accountsScroll.setOpaque(false);
        accountsScroll.setBorder(null);
        ScrollUtil.apply(accountsScroll);
        accountsCard.add(accountsScroll, BorderLayout.CENTER);

        root.add(accountsCard);
        root.add(Box.createVerticalGlue());

        // Loading indicator
        loadingLabel = new JLabel("Memuat data...");
        loadingLabel.setForeground(color(37, 99, 235));
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setVisible(false);
        root.add(loadingLabel);

        AccountStore.addListener(this::renderSnapshot);
        renderSnapshot(AccountStore.snapshot());
        
        // Load data from backend
        loadDataFromBackend();
    }

    private void promptAddAccount() {
        JTextField nameField = new JTextField();
        JTextField numberField = new JTextField();
        JTextField balanceField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Bank", "Dompet Digital", "Cash", "Kredit"});

        JPanel form = new JPanel();
        form.setLayout(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Nama Akun"));
        form.add(nameField);
        form.add(new JLabel("Nomor/Rekening"));
        form.add(numberField);
        form.add(new JLabel("Saldo (angka)"));
        form.add(balanceField);
        form.add(new JLabel("Tipe"));
        form.add(typeCombo);

        int res = JOptionPane.showConfirmDialog(this, form, "Tambah Akun Baru", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String number = numberField.getText().trim();
        long balance = parseNumber(balanceField.getText().trim());
        String type = (String) typeCombo.getSelectedItem();
        if (name.isEmpty() || type == null) {
            JOptionPane.showMessageDialog(this, "Nama dan tipe akun wajib diisi", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        showLoading(true);
        AccountStore.addAccount(name, number.isEmpty() ? "-" : number, balance, type,
            accountId -> {
                // onSuccess
                showLoading(false);
            },
            error -> {
                // onError
                showLoading(false);
                JOptionPane.showMessageDialog(this, "Gagal menambah akun: " + error, "Error", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void renderSnapshot(AccountStore.Snapshot snap) {
        this.snapshot = snap;
        accountsGrid.removeAll();
        for (AccountStore.Account acc : snap.accounts()) {
            accountsGrid.add(buildAccountCard(acc));
        }
        accountsGrid.revalidate();
        accountsGrid.repaint();
        updateSummaries();
    }

    private JPanel buildAccountCard(AccountStore.Account acc) {
        RoundPanel card = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(0, 140));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        iconPanel.setOpaque(false);
        JLabel iconLbl = new JLabel(switch (acc.type()) {
            case AccountStore.TYPE_BANK -> "BK";
            case AccountStore.TYPE_WALLET -> "DW";
            case AccountStore.TYPE_CREDIT -> "CR";
            default -> "CA";
        });
        iconLbl.setFont(iconLbl.getFont().deriveFont(Font.BOLD, 16f));
        Color accent = acc.type().equals(AccountStore.TYPE_BANK) ? color(37,99,235)
            : acc.type().equals(AccountStore.TYPE_WALLET) ? color(245,158,11)
            : acc.type().equals(AccountStore.TYPE_CREDIT) ? color(109,40,217)
            : color(100,116,139);
        iconLbl.setForeground(accent);
        iconLbl.setBorder(new EmptyBorder(8, 12, 8, 12));
        iconPanel.add(iconLbl);
        top.add(iconPanel, BorderLayout.WEST);

        JButton menuBtn = new JButton("...");
        menuBtn.setFont(menuBtn.getFont().deriveFont(16f));
        menuBtn.setForeground(color(100, 116, 139));
        menuBtn.setBorderPainted(false);
        menuBtn.setContentAreaFilled(false);
        menuBtn.setFocusPainted(false);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Hapus akun");
        deleteItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Hapus akun ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            
            showLoading(true);
            AccountStore.removeAccount(acc.id(),
                () -> {
                    // onSuccess
                    showLoading(false);
                },
                error -> {
                    // onError
                    showLoading(false);
                    JOptionPane.showMessageDialog(this, "Gagal menghapus akun: " + error, "Error", JOptionPane.ERROR_MESSAGE);
                }
            );
        });
        menu.add(deleteItem);
        menuBtn.addActionListener(e -> menu.show(menuBtn, 0, menuBtn.getHeight()));
        top.add(menuBtn, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(12, 0, 0, 0));

        JLabel nameLbl = new JLabel(acc.name());
        nameLbl.setForeground(color(30, 41, 59));
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 16f));
        nameLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel numberLbl = new JLabel(acc.number());
        numberLbl.setForeground(color(100, 116, 139));
        numberLbl.setBorder(new EmptyBorder(4, 0, 12, 0));
        numberLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel balanceLbl = new JLabel("Rp " + String.format("%,d", acc.balance()).replace(',', '.'));
        balanceLbl.setForeground(accent);
        balanceLbl.setFont(balanceLbl.getFont().deriveFont(Font.BOLD, 20f));
        balanceLbl.setAlignmentX(LEFT_ALIGNMENT);

        content.add(nameLbl);
        content.add(numberLbl);
        content.add(balanceLbl);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JLabel buildSummaryCard(String title, String value, Color valueColor) {
        RoundPanel card = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setForeground(color(71, 85, 105));
        t.setAlignmentX(LEFT_ALIGNMENT);
        t.setBorder(new EmptyBorder(0, 0, 6, 0));
        JLabel v = new JLabel(value);
        v.setForeground(valueColor);
        v.setFont(v.getFont().deriveFont(Font.PLAIN, 18f));
        v.setAlignmentX(LEFT_ALIGNMENT);
        content.add(t);
        content.add(v);
        card.add(content, BorderLayout.CENTER);
        return v;
    }

    private void updateSummaries() {
        long total = snapshot.accounts().stream().mapToLong(AccountStore.Account::balance).sum();
        long bankCount = snapshot.accounts().stream().filter(a -> a.type().equals(AccountStore.TYPE_BANK)).count();
        long walletCount = snapshot.accounts().stream().filter(a -> a.type().equals(AccountStore.TYPE_WALLET)).count();
        long cash = snapshot.accounts().stream().filter(a -> a.type().equals(AccountStore.TYPE_CASH)).mapToLong(AccountStore.Account::balance).sum();
        long credit = snapshot.accounts().stream().filter(a -> a.type().equals(AccountStore.TYPE_CREDIT)).count();

        totalSaldoLabel.setText("Rp " + String.format("%,d", total).replace(',', '.'));
        bankCountLabel.setText(bankCount + " akun");
        walletCountLabel.setText(walletCount + " wallet");
        cashLabel.setText("Rp " + String.format("%,d", cash).replace(',', '.'));
        creditLabel.setText(credit + " kartu");
    }

    private long parseNumber(String text) {
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try { return Long.parseLong(digits); } catch (NumberFormatException e) { return 0; }
    }

    private void showLoading(boolean show) {
        if (loadingLabel != null) {
            loadingLabel.setVisible(show);
        }
    }

    private void loadDataFromBackend() {
        showLoading(true);
        AccountStore.loadFromBackend(
            () -> {
                // onSuccess
                showLoading(false);
            },
            error -> {
                // onError
                showLoading(false);
                JOptionPane.showMessageDialog(this, "Gagal memuat data: " + error, "Error", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private static Color color(int r, int g, int b) { return new Color(r, g, b); }
    // Account data is managed centrally in AccountStore

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

    static class RoundedButton extends JButton {
        private final boolean primary;

        RoundedButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setForeground(primary ? Color.WHITE : color(71, 85, 105));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (primary) {
                g2.setColor(getModel().isPressed() ? new Color(29, 78, 216) : new Color(37, 99, 235));
            } else {
                g2.setColor(getModel().isPressed() ? new Color(241, 245, 249) : Color.WHITE);
            }
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            if (!primary) {
                g2.setColor(color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
