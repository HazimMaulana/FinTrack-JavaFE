import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import utils.ScrollUtil;
import utils.ComboUtil;
import utils.CategoryStore;
import utils.AccountStore;
import utils.TransactionStore;

public class TransaksiPage extends JPanel {
    private boolean editMode = false;
    private int editingRow = -1;
    private JLabel formTitle;
    private JButton saveBtn;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField dateField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> accountCombo;
    private JComboBox<String> categoryCombo;
    private JTextField nominalField;
    private JTextField descField;
    private JLabel pageInfo;
    private CategoryStore.Snapshot categorySnapshot = CategoryStore.snapshot();
    private final Map<String, String> accountTypeMap = new HashMap<>();
    private final List<String> rowIds = new ArrayList<>();
    public TransaksiPage() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(0, 16, 16, 16));

        JScrollPane rootScroll = new JScrollPane(root);
        rootScroll.setBorder(null);
        rootScroll.setOpaque(false);
        rootScroll.getViewport().setOpaque(false);
        rootScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        ScrollUtil.apply(rootScroll);
        add(rootScroll, BorderLayout.CENTER);

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 12, 16, 16));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Transaksi");
        title.setForeground(color(30, 41, 59));
        title.setFont(title.getFont().deriveFont(Font.PLAIN, title.getFont().getSize2D() + 6f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Kelola semua transaksi keuangan Anda");
        subtitle.setForeground(color(71, 85, 105));
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Dual panel layout (60% table / 40% form)
        JPanel dualPanel = new JPanel(new GridBagLayout());
        dualPanel.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.6; gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 0, 0, 12);

        // Left: Table card
        RoundPanel tableCard = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 12, 0));
        JButton filterBtn = new RoundedButton("Filter", false);
        JTextField dateFilter = new JTextField("2025-11-18");
        dateFilter.setPreferredSize(new Dimension(140, 28));
        JComboBox<String> categoryFilter = new JComboBox<>();
        JComboBox<String> accountFilter = new JComboBox<>();
        ComboUtil.apply(categoryFilter);
        ComboUtil.apply(accountFilter);
        CategoryStore.addListener(snap -> {
            categorySnapshot = snap;
            categoryFilter.removeAllItems();
            categoryFilter.addItem("Semua Kategori");
            snap.expenses().forEach(categoryFilter::addItem);
            snap.incomes().forEach(categoryFilter::addItem);
        });
        AccountStore.addListener(accSnap -> {
            accountFilter.removeAllItems();
            accountFilter.addItem("Semua Akun");
            accSnap.accounts().forEach(a -> accountFilter.addItem(a.name()));
        });
        toolbar.add(filterBtn);
        toolbar.add(dateFilter);
        toolbar.add(categoryFilter);
        toolbar.add(accountFilter);
        tableCard.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Tanggal", "Keterangan", "Kategori", "Akun", "Debit", "Kredit", "Saldo", "Aksi"};
        tableModel = new DefaultTableModel(new Object[0][0], cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(248, 250, 252) : Color.WHITE);
                }
                if (col == 4) { // Debit
                    JLabel l = (JLabel) c;
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                    l.setForeground(l.getText().equals("-") ? new Color(71, 85, 105) : new Color(5, 150, 105));
                } else if (col == 5) { // Kredit
                    JLabel l = (JLabel) c;
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                    l.setForeground(l.getText().equals("-") ? new Color(71, 85, 105) : new Color(220, 38, 38));
                } else if (col == 6) { // Saldo
                    JLabel l = (JLabel) c;
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                    l.setForeground(new Color(30, 41, 59));
                } else if (col == 7) {
                    JLabel l = (JLabel) c;
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                    l.setForeground(new Color(37, 99, 235));
                } else {
                    c.setForeground(new Color(71, 85, 105));
                }
                return c;
            }
        };
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col == 7) {
                    int row = table.rowAtPoint(e.getPoint());
                    Rectangle cell = table.getCellRect(row, col, true);
                    int xWithin = e.getX() - cell.x;
                    if (xWithin < cell.width / 2) {
                        loadRowForEdit(row);
                    } else {
                        deleteRow(row);
                    }
                }
            }
        });
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.setPreferredSize(new Dimension(0, 460));
        ScrollUtil.apply(tableScroll);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // Pagination
        JPanel pagination = new JPanel(new BorderLayout());
        pagination.setOpaque(false);
        pagination.setBorder(new EmptyBorder(12, 0, 0, 0));
        pageInfo = new JLabel("Total: 0 transaksi");
        pageInfo.setForeground(color(71, 85, 105));
        pagination.add(pageInfo, BorderLayout.WEST);
        JPanel pageControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pageControls.setOpaque(false);
        JButton prevBtn = new RoundedButton("<", false);
        JLabel pageLabel = new JLabel("Page 1 of 125");
        pageLabel.setForeground(color(30, 41, 59));
        JButton nextBtn = new RoundedButton(">", false);
        pageControls.add(prevBtn);
        pageControls.add(pageLabel);
        pageControls.add(nextBtn);
        pagination.add(pageControls, BorderLayout.EAST);
        tableCard.add(pagination, BorderLayout.SOUTH);

        dualPanel.add(tableCard, gc);

        // Right: Form card
        gc.gridx = 1; gc.weightx = 0.4; gc.insets = new Insets(0, 0, 0, 0);
        RoundPanel formCard = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setOpaque(false);
        formHeader.setBorder(new EmptyBorder(0, 0, 12, 0));
        formTitle = new JLabel("Tambah Transaksi Baru");
        formTitle.setForeground(color(30, 41, 59));
        formTitle.setFont(formTitle.getFont().deriveFont(Font.PLAIN, 14f));
        JButton resetBtn = new JButton("Reset");
        resetBtn.setForeground(color(71, 85, 105));
        resetBtn.setBorderPainted(false);
        resetBtn.setContentAreaFilled(false);
        resetBtn.addActionListener(e -> {
            editMode = false;
            formTitle.setText("Tambah Transaksi Baru");
            if (saveBtn != null) {
                saveBtn.setText("Simpan");
            }
        });
        formHeader.add(formTitle, BorderLayout.WEST);
        formHeader.add(resetBtn, BorderLayout.EAST);
        formCard.add(formHeader, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);
        dateField = new JTextField("2025-11-18");
        formFields.add(createFormField("Tanggal", dateField));
        formFields.add(Box.createVerticalStrut(12));
        typeCombo = new JComboBox<>(new String[]{"Pengeluaran", "Pemasukan"});
        ComboUtil.apply(typeCombo);
        formFields.add(createFormField("Tipe Transaksi", typeCombo));
        formFields.add(Box.createVerticalStrut(12));
        accountCombo = new JComboBox<>();
        ComboUtil.apply(accountCombo);
        AccountStore.addListener(accSnap -> {
            accountCombo.removeAllItems();
            accountTypeMap.clear();
            accSnap.accounts().forEach(a -> {
                accountCombo.addItem(a.name());
                accountTypeMap.put(a.name(), a.type());
            });
        });
        formFields.add(createFormField("Akun", accountCombo));
        formFields.add(Box.createVerticalStrut(12));
        categoryCombo = new JComboBox<>();
        ComboUtil.apply(categoryCombo);
        CategoryStore.addListener(snap -> {
            categorySnapshot = snap;
            refreshCategoryCombo();
        });
        typeCombo.addActionListener(e -> refreshCategoryCombo());
        formFields.add(createFormField("Kategori", categoryCombo));
        formFields.add(Box.createVerticalStrut(12));
        nominalField = new JTextField();
        formFields.add(createFormField("Nominal", nominalField));
        formFields.add(Box.createVerticalStrut(12));
        descField = new JTextField();
        formFields.add(createFormField("Keterangan", descField));
        formFields.add(Box.createVerticalStrut(12));
        
        JPanel notesPanel = new JPanel();
        notesPanel.setLayout(new BoxLayout(notesPanel, BoxLayout.Y_AXIS));
        notesPanel.setOpaque(false);
        notesPanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel notesLabel = new JLabel("Catatan (Optional)");
        notesLabel.setForeground(color(71, 85, 105));
        notesLabel.setAlignmentX(LEFT_ALIGNMENT);
        JTextArea notesArea = new JTextArea(3, 20);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color(203, 213, 225)),
            new EmptyBorder(6, 8, 6, 8)
        ));
        notesPanel.add(notesLabel);
        notesPanel.add(Box.createVerticalStrut(4));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        ScrollUtil.apply(notesScroll);
        notesPanel.add(notesScroll);
        formFields.add(notesPanel);

        formCard.add(formFields, BorderLayout.CENTER);

        JPanel formButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        formButtons.setOpaque(false);
        formButtons.setBorder(new EmptyBorder(16, 0, 0, 0));
        saveBtn = new RoundedButton("Simpan", true);
        saveBtn.addActionListener(e -> handleSave());
        JButton cancelBtn = new RoundedButton("Batal", false);
        cancelBtn.addActionListener(e -> {
            editMode = false;
            editingRow = -1;
            formTitle.setText("Tambah Transaksi Baru");
            saveBtn.setText("Simpan");
        });
        formButtons.add(saveBtn);
        formButtons.add(cancelBtn);
        formCard.add(formButtons, BorderLayout.SOUTH);

        dualPanel.add(formCard, gc);
        root.add(dualPanel);

        // Add vertical glue to give scroll some slack
        root.add(Box.createVerticalGlue());

        // Load initial data from store
        loadFromStore(TransactionStore.snapshot());
        TransactionStore.addListener(this::loadFromStore);
    }

    private JPanel createFormField(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(color(71, 85, 105));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        return panel;
    }

    public void startNewTransaction() {
        editMode = false;
        editingRow = -1;
        formTitle.setText("Tambah Transaksi Baru");
        if (saveBtn != null) {
            saveBtn.setText("Simpan");
        }
        dateField.setText("2025-11-18");
        typeCombo.setSelectedIndex(0);
        if (accountCombo.getItemCount() > 0) {
            accountCombo.setSelectedIndex(0);
        }
        if (categoryCombo.getItemCount() > 0) {
            categoryCombo.setSelectedIndex(0);
        }
        nominalField.setText("");
        descField.setText("");
    }

    private void handleSave() {
        String date = dateField.getText().trim();
        String desc = descField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String type = (String) typeCombo.getSelectedItem();
        String accountName = (String) accountCombo.getSelectedItem();
        String accountType = accountTypeMap.getOrDefault(accountName, "-");
        String accountLabel = (accountName == null || accountName.isBlank()) ? "-" : accountName;
        String nominalText = nominalField.getText().trim();

        if (date.isEmpty() || desc.isEmpty() || nominalText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tanggal, Keterangan, dan Nominal harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long amount = parseNumber(nominalText);
        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Nominal harus lebih besar dari 0.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (editMode && editingRow >= 0 && editingRow < tableModel.getRowCount()) {
            String id = rowIds.get(editingRow);
            TransactionStore.updateTransaction(id, date, type, category, accountLabel, accountType, amount, desc);
        } else {
            TransactionStore.addTransaction(date, type, category, accountLabel, accountType, amount, desc);
        }

        startNewTransaction();
        loadFromStore(TransactionStore.snapshot());
    }

    private long parseNumber(String text) {
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatRupiah(long value) {
        String raw = String.format("%,d", value).replace(",", ".");
        return "Rp " + raw;
    }

    private String formatWithType(String accountType, long amount) {
        String label = switch (accountType) {
            case AccountStore.TYPE_BANK -> "Bank";
            case AccountStore.TYPE_WALLET -> "E-Wallet";
            case AccountStore.TYPE_CASH -> "Cash";
            case AccountStore.TYPE_CREDIT -> "Kredit";
            default -> accountType == null ? "" : accountType;
        };
        String prefix = (label == null || label.isBlank()) ? "" : label + " • ";
        return prefix + formatRupiah(amount);
    }

    private void loadRowForEdit(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;
        editingRow = row;
        editMode = true;
        formTitle.setText("Edit Transaksi");
        saveBtn.setText("Update");

        dateField.setText(tableModel.getValueAt(row, 0).toString());
        descField.setText(tableModel.getValueAt(row, 1).toString());
        categoryCombo.setSelectedItem(tableModel.getValueAt(row, 2));
        Object accountVal = tableModel.getValueAt(row, 3);
        if (accountVal != null) {
            accountCombo.setSelectedItem(accountVal.toString());
        }

        String debit = tableModel.getValueAt(row, 4).toString();
        String credit = tableModel.getValueAt(row, 5).toString();
        boolean isIncome = !debit.equals("-");
        typeCombo.setSelectedIndex(isIncome ? 1 : 0);
        String amountStr = isIncome ? debit : credit;
        nominalField.setText(String.valueOf(parseNumber(amountStr)));
    }

    private void deleteRow(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;
        if (row < rowIds.size()) {
            String id = rowIds.get(row);
            TransactionStore.removeTransaction(id);
        }
        tableModel.removeRow(row);
        if (editingRow == row) {
            startNewTransaction();
        }
        loadFromStore(TransactionStore.snapshot());
    }

    private void updatePagination() {
        if (pageInfo != null) {
            pageInfo.setText("Total: " + tableModel.getRowCount() + " transaksi");
        }
    }

    private void refreshCategoryCombo() {
        if (categoryCombo == null || categorySnapshot == null) return;
        Object selectedType = typeCombo.getSelectedItem();
        boolean isIncome = selectedType != null && "Pemasukan".equalsIgnoreCase(selectedType.toString());
        categoryCombo.removeAllItems();
        if (isIncome) {
            categorySnapshot.incomes().forEach(categoryCombo::addItem);
        } else {
            categorySnapshot.expenses().forEach(categoryCombo::addItem);
        }
    }

    private void loadFromStore(TransactionStore.Snapshot snap) {
        tableModel.setRowCount(0);
        rowIds.clear();
        long runningBalance = 0;
        for (TransactionStore.Transaction tx : snap.transactions()) {
            boolean isIncome = tx.isIncome();
            long amount = tx.amount();
            runningBalance += isIncome ? amount : -amount;
            String debit = isIncome ? formatWithType(tx.accountType(), amount) : "-";
            String credit = isIncome ? "-" : formatWithType(tx.accountType(), amount);
            String desc = tx.description() == null || tx.description().isBlank() ? "-" : tx.description();
            tableModel.addRow(new Object[]{
                tx.date().toString(),
                desc,
                tx.category(),
                tx.accountName(),
                debit,
                credit,
                formatRupiah(runningBalance),
                "Edit | Hapus"
            });
            rowIds.add(tx.id());
        }
        updatePagination();
    }

    private static Color color(int r, int g, int b) { return new Color(r, g, b); }

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
