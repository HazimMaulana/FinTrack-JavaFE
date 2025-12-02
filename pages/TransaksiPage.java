import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import utils.ScrollUtil;
import utils.ComboUtil;

public class TransaksiPage extends JPanel {
    private boolean editMode = false;
    private JLabel formTitle;
    private JButton saveBtn;
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
        JComboBox<String> categoryFilter = new JComboBox<>(new String[]{"Semua Kategori", "Pemasukan", "Belanja", "Tagihan", "Makanan", "Transportasi"});
        JComboBox<String> accountFilter = new JComboBox<>(new String[]{"Semua Akun", "BCA", "Mandiri", "Cash"});
        ComboUtil.apply(categoryFilter);
        ComboUtil.apply(accountFilter);
        toolbar.add(filterBtn);
        toolbar.add(dateFilter);
        toolbar.add(categoryFilter);
        toolbar.add(accountFilter);
        tableCard.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Tanggal", "Keterangan", "Kategori", "Debit", "Kredit", "Saldo", "Aksi"};
        Object[][] rows = new Object[][]{
            {"18/11/2025", "Gaji Bulanan", "Pemasukan", "Rp 8.500.000", "-", "Rp 45.250.000", "Edit / Hapus"},
            {"17/11/2025", "Belanja Bulanan", "Belanja", "-", "Rp 1.200.000", "Rp 36.750.000", "Edit / Hapus"},
            {"16/11/2025", "Bayar Listrik", "Tagihan", "-", "Rp 450.000", "Rp 37.950.000", "Edit / Hapus"},
            {"15/11/2025", "Makan Siang", "Makanan", "-", "Rp 75.000", "Rp 38.400.000", "Edit / Hapus"},
            {"14/11/2025", "Freelance Project", "Pemasukan", "Rp 2.500.000", "-", "Rp 38.475.000", "Edit / Hapus"},
            {"13/11/2025", "Transportasi", "Transportasi", "-", "Rp 150.000", "Rp 35.975.000", "Edit / Hapus"},
            {"12/11/2025", "Bayar Internet", "Tagihan", "-", "Rp 350.000", "Rp 36.125.000", "Edit / Hapus"},
            {"11/11/2025", "Cicilan Usaha", "Pemasukan", "Rp 1.500.000", "-", "Rp 36.475.000", "Edit / Hapus"},
            {"10/11/2025", "Belanja Groceries", "Belanja", "-", "Rp 850.000", "Rp 34.975.000", "Edit / Hapus"},
            {"09/11/2025", "Penjualan Produk", "Pemasukan", "Rp 3.200.000", "-", "Rp 35.825.000", "Edit / Hapus"},
        };
        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(248, 250, 252) : Color.WHITE);
                }
                if (col == 3) { // Debit
                    JLabel l = (JLabel) c;
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                    l.setForeground(l.getText().equals("-") ? new Color(71, 85, 105) : new Color(5, 150, 105));
                } else if (col == 4) { // Kredit
                    JLabel l = (JLabel) c;
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                    l.setForeground(l.getText().equals("-") ? new Color(71, 85, 105) : new Color(220, 38, 38));
                } else if (col == 5) { // Saldo
                    JLabel l = (JLabel) c;
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                    l.setForeground(new Color(30, 41, 59));
                } else if (col == 6) {
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
        table.getColumnModel().getColumn(6).setPreferredWidth(120);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col == 6) {
                    editMode = true;
                    formTitle.setText("Edit Transaksi");
                    if (saveBtn != null) {
                        saveBtn.setText("Update");
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
        JLabel pageInfo = new JLabel("Showing 1-10 of 1,247 transaksi");
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
        formFields.add(createFormField("Tanggal", new JTextField("2025-11-18")));
        formFields.add(Box.createVerticalStrut(12));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Pengeluaran (Kredit)", "Pemasukan (Debit)"});
        ComboUtil.apply(typeCombo);
        formFields.add(createFormField("Tipe Transaksi", typeCombo));
        formFields.add(Box.createVerticalStrut(12));
        JComboBox<String> accountCombo = new JComboBox<>(new String[]{"BCA - Rp 45.250.000", "Mandiri - Rp 12.500.000", "Cash - Rp 2.150.000"});
        ComboUtil.apply(accountCombo);
        formFields.add(createFormField("Akun", accountCombo));
        formFields.add(Box.createVerticalStrut(12));
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Pemasukan", "Makanan & Minuman", "Transportasi", "Belanja", "Tagihan", "Lainnya"});
        ComboUtil.apply(categoryCombo);
        formFields.add(createFormField("Kategori", categoryCombo));
        formFields.add(Box.createVerticalStrut(12));
        formFields.add(createFormField("Nominal", new JTextField()));
        formFields.add(Box.createVerticalStrut(12));
        formFields.add(createFormField("Keterangan", new JTextField()));
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
        JButton cancelBtn = new RoundedButton("Batal", false);
        cancelBtn.addActionListener(e -> {
            editMode = false;
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
        formTitle.setText("Tambah Transaksi Baru");
        if (saveBtn != null) {
            saveBtn.setText("Simpan");
        }
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
