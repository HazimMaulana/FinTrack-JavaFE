import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import utils.ScrollUtil;
import utils.CategoryStore;
import utils.TransactionStore;

public class KategoriPage extends JPanel {
    private boolean editMode = false;
    private String selectedType = "expense";
    private int editingIndex = -1;
    private boolean editingIncome = false;

    private final List<Category> incomeCategories = new ArrayList<>();
    private final List<Category> expenseCategories = new ArrayList<>();
    
    private TransactionStore.Snapshot transactionSnapshot = TransactionStore.snapshot();

    private final JPanel categoriesList = new JPanel();
    private final CardLayout rightCards = new CardLayout();
    private final JPanel rightPanelCards = new JPanel(rightCards);
    private final JLabel formTitle = new JLabel("Tambah Kategori");
    private TabButton expenseTab;
    private TabButton incomeTab;
    private JButton saveButton;
    private JComboBox<String> formTypeCombo;
    private JTextField formNameField;
    private JTextField formIconField;
    private JPanel summaryCards;

    public KategoriPage() {
        setLayout(new BorderLayout());

        // Listen to category and transaction changes
        CategoryStore.addListener(this::onCategoryUpdate);
        TransactionStore.addListener(this::onTransactionUpdate);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 12, 16, 16));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);
        JLabel title = new JLabel("Kategori");
        title.setForeground(color(30, 41, 59));
        title.setFont(title.getFont().deriveFont(Font.PLAIN, title.getFont().getSize2D() + 6f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Kelola kategori pemasukan dan pengeluaran");
        subtitle.setForeground(color(71, 85, 105));
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerLeft.add(title);
        headerLeft.add(subtitle);
        header.add(headerLeft, BorderLayout.WEST);

        JButton addBtn = new RoundedButton("+ Tambah Kategori Baru", true);
        addBtn.addActionListener(e -> {
            editMode = false;
            formTitle.setText("Tambah Kategori");
            saveButton.setText("Simpan");
            formTypeCombo.setSelectedIndex(0);
            formNameField.setText("");
            formIconField.setText("");
            rightCards.show(rightPanelCards, "form");
        });
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Root content inside scroll so Y overflow is visible
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(0, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        ScrollUtil.apply(scroll);
        add(scroll, BorderLayout.CENTER);

        // Summary Cards
        summaryCards = new JPanel(new GridLayout(1, 4, 12, 12));
        summaryCards.setOpaque(false);
        summaryCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        root.add(summaryCards);
        root.add(Box.createVerticalStrut(16));

        // Dual panel (categories list + form/stats)
        JPanel dualPanel = new JPanel(new GridBagLayout());
        dualPanel.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.65; gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 0, 0, 12);

        // Left: Categories List
        RoundPanel listCard = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        listCard.setLayout(new BorderLayout());
        listCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabPanel.setOpaque(false);
        tabPanel.setBorder(new EmptyBorder(0, 0, 12, 0));
        expenseTab = new TabButton("Pengeluaran");
        incomeTab = new TabButton("Pemasukan");
        expenseTab.setActive(true);
        expenseTab.addActionListener(e -> switchType("expense"));
        incomeTab.addActionListener(e -> switchType("income"));
        tabPanel.add(expenseTab);
        tabPanel.add(incomeTab);
        listCard.add(tabPanel, BorderLayout.NORTH);

        categoriesList.setLayout(new BoxLayout(categoriesList, BoxLayout.Y_AXIS));
        categoriesList.setOpaque(false);
        JScrollPane listScroll = new JScrollPane(categoriesList);
        listScroll.getViewport().setOpaque(false);
        listScroll.setOpaque(false);
        listScroll.setBorder(null);
        listScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        ScrollUtil.apply(listScroll);
        listCard.add(listScroll, BorderLayout.CENTER);

        dualPanel.add(listCard, gc);

        // Right: Stats/Form Panel
        gc.gridx = 1; gc.weightx = 0.35; gc.insets = new Insets(0, 0, 0, 0);
        RoundPanel statsCard = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        statsCard.setLayout(new BorderLayout());
        statsCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel statsContent = buildStatsContent();
        JPanel formContent = buildFormContent();

        rightPanelCards.setOpaque(false);
        rightPanelCards.add(statsContent, "stats");
        rightPanelCards.add(formContent, "form");
        statsCard.add(rightPanelCards, BorderLayout.CENTER);
        rightCards.show(rightPanelCards, "stats");

        dualPanel.add(statsCard, gc);

        root.add(dualPanel);
        root.add(Box.createVerticalGlue());

        // Initial data load
        loadCategoriesFromStore();
        updateSummaryCards();
        renderCategoryList();
    }

    private void onCategoryUpdate(CategoryStore.Snapshot snapshot) {
        SwingUtilities.invokeLater(() -> {
            loadCategoriesFromStore();
            updateSummaryCards();
            renderCategoryList();
        });
    }

    private void onTransactionUpdate(TransactionStore.Snapshot snapshot) {
        SwingUtilities.invokeLater(() -> {
            transactionSnapshot = snapshot;
            loadCategoriesFromStore();
            updateSummaryCards();
            renderCategoryList();
        });
    }

    private void loadCategoriesFromStore() {
        CategoryStore.Snapshot catSnapshot = CategoryStore.snapshot();
        
        // Build categories with real transaction data
        incomeCategories.clear();
        expenseCategories.clear();
        
        // Process income categories
        for (String catName : catSnapshot.incomes()) {
            CategoryStats stats = calculateCategoryStats(catName, true);
            Color color = getColorForCategory(catName, true, incomeCategories.size());
            String icon = getIconForCategory(catName);
            incomeCategories.add(new Category(catName, icon, stats.count, stats.total, color));
        }
        
        // Process expense categories
        for (String catName : catSnapshot.expenses()) {
            CategoryStats stats = calculateCategoryStats(catName, false);
            Color color = getColorForCategory(catName, false, expenseCategories.size());
            String icon = getIconForCategory(catName);
            expenseCategories.add(new Category(catName, icon, stats.count, stats.total, color));
        }
    }

    private CategoryStats calculateCategoryStats(String categoryName, boolean isIncome) {
        int count = 0;
        long total = 0;
        
        for (TransactionStore.Transaction tx : transactionSnapshot.transactions()) {
            boolean matchType = isIncome ? tx.isIncome() : !tx.isIncome();
            if (matchType && categoryName.equalsIgnoreCase(tx.category())) {
                count++;
                total += tx.amount();
            }
        }
        
        return new CategoryStats(count, total);
    }

    private String getIconForCategory(String name) {
        if (name.length() >= 2) {
            return name.substring(0, 2).toUpperCase();
        }
        return name.toUpperCase();
    }

    private Color getColorForCategory(String name, boolean isIncome, int index) {
        Color[] incomeColors = {
            new Color(5,150,105),
            new Color(14,165,233),
            new Color(139,92,246),
            new Color(245,158,11),
            new Color(100,116,139)
        };
        
        Color[] expenseColors = {
            new Color(37,99,235),
            new Color(5,150,105),
            new Color(220,38,38),
            new Color(245,158,11),
            new Color(236,72,153),
            new Color(139,92,246),
            new Color(6,182,212),
            new Color(239,68,68),
            new Color(100,116,139)
        };
        
        Color[] palette = isIncome ? incomeColors : expenseColors;
        return palette[index % palette.length];
    }

    private void updateSummaryCards() {
        summaryCards.removeAll();
        
        int incomeCount = incomeCategories.size();
        int expenseCount = expenseCategories.size();
        int totalCount = incomeCount + expenseCount;
        
        // Find most active category
        String mostActive = "Belum ada";
        int maxTx = 0;
        for (Category cat : expenseCategories) {
            if (cat.transactions > maxTx) {
                maxTx = cat.transactions;
                mostActive = cat.name;
            }
        }
        for (Category cat : incomeCategories) {
            if (cat.transactions > maxTx) {
                maxTx = cat.transactions;
                mostActive = cat.name;
            }
        }
        
        summaryCards.add(buildSummaryCard("Kategori Pemasukan", incomeCount + " kategori", color(5, 150, 105), "↑"));
        summaryCards.add(buildSummaryCard("Kategori Pengeluaran", expenseCount + " kategori", color(220, 38, 38), "↓"));
        summaryCards.add(buildSummaryCard("Total Kategori", totalCount + " kategori", color(37, 99, 235), "#"));
        summaryCards.add(buildSummaryCard("Paling Aktif", mostActive, color(245, 158, 11), "★"));
        
        summaryCards.revalidate();
        summaryCards.repaint();
    }

    private static class CategoryStats {
        final int count;
        final long total;
        CategoryStats(int count, long total) {
            this.count = count;
            this.total = total;
        }
    }

    private void switchType(String type) {
        if (type.equals(selectedType)) return;
        selectedType = type;
        expenseTab.setActive("expense".equals(type));
        incomeTab.setActive("income".equals(type));
        renderCategoryList();
    }

    private void handleSaveCategory() {
        String type = formTypeCombo.getSelectedItem() == null ? "Pengeluaran" : formTypeCombo.getSelectedItem().toString();
        String name = formNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama kategori harus diisi", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String icon = formIconField.getText().trim();
        if (icon.isEmpty()) icon = name.substring(0, Math.min(2, name.length())).toUpperCase();
        Color accent = "Pengeluaran".equalsIgnoreCase(type) ? new Color(37,99,235) : new Color(5,150,105);
        Category cat = new Category(name, icon, 0, 0, accent);
        if (editMode && editingIndex >= 0) {
            if ("Pengeluaran".equalsIgnoreCase(type)) {
                if (editingIncome) incomeCategories.remove(editingIndex);
                ensureIndex(expenseCategories, editingIndex, cat);
            } else {
                if (!editingIncome) expenseCategories.remove(editingIndex);
                ensureIndex(incomeCategories, editingIndex, cat);
            }
        } else {
            if ("Pengeluaran".equalsIgnoreCase(type)) {
                expenseCategories.add(cat);
            } else {
                incomeCategories.add(cat);
            }
        }
        CategoryStore.addCategory(type, name);
        editMode = false;
        editingIndex = -1;
        editingIncome = false;
        renderCategoryList();
        rightCards.show(rightPanelCards, "stats");
    }

    private void ensureIndex(List<Category> list, int index, Category value){
        if(index >= 0 && index < list.size()){
            list.set(index, value);
        } else {
            list.add(value);
        }
    }

    private void renderCategoryList() {
        categoriesList.removeAll();
        List<Category> items = "income".equals(selectedType) ? incomeCategories : expenseCategories;
        for (int i = 0; i < items.size(); i++) {
            categoriesList.add(buildCategoryItem(items.get(i)));
            if (i < items.size() - 1) {
                categoriesList.add(Box.createVerticalStrut(8));
            }
        }
        categoriesList.revalidate();
        categoriesList.repaint();
    }

    private JPanel buildCategoryItem(Category category) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(true);
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color(226, 232, 240)),
            new EmptyBorder(12, 12, 12, 12)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel iconLbl = new JLabel(category.icon);
        iconLbl.setFont(iconLbl.getFont().deriveFont(Font.BOLD, 14f));
        iconLbl.setOpaque(true);
        iconLbl.setBackground(new Color(category.color.getRed(), category.color.getGreen(), category.color.getBlue(), 32));
        iconLbl.setForeground(category.color.darker());
        iconLbl.setBorder(new EmptyBorder(10, 12, 10, 12));
        left.add(iconLbl);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel nameLbl = new JLabel(category.name);
        nameLbl.setForeground(color(30, 41, 59));
        JLabel txLbl = new JLabel(category.transactions + " transaksi");
        txLbl.setForeground(color(71, 85, 105));
        info.add(nameLbl);
        info.add(txLbl);
        left.add(info);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JPanel totals = new JPanel();
        totals.setLayout(new BoxLayout(totals, BoxLayout.Y_AXIS));
        totals.setOpaque(false);
        JLabel totalLabel = new JLabel("Total");
        totalLabel.setForeground(color(71, 85, 105));
        totalLabel.setAlignmentX(RIGHT_ALIGNMENT);
        JLabel totalValue = new JLabel("Rp " + String.format("%,d", category.total).replace(',', '.'));
        totalValue.setForeground(category.color);
        totalValue.setAlignmentX(RIGHT_ALIGNMENT);
        totals.add(totalLabel);
        totals.add(totalValue);
        right.add(totals, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton editBtn = actionIconButton("Edit");
        editBtn.addActionListener(e -> {
            editMode = true;
            List<Category> items = "income".equals(selectedType) ? incomeCategories : expenseCategories;
            editingIndex = items.indexOf(category);
            editingIncome = "income".equals(selectedType);
            formTitle.setText("Edit Kategori");
            saveButton.setText("Update");
            formTypeCombo.setSelectedItem(editingIncome ? "Pemasukan" : "Pengeluaran");
            formNameField.setText(category.name);
            formIconField.setText(category.icon);
            rightCards.show(rightPanelCards, "form");
        });
        JButton deleteBtn = actionIconButton("Hapus");
        deleteBtn.addActionListener(e -> {
            if ("income".equals(selectedType)) {
                incomeCategories.remove(category);
                CategoryStore.removeCategory(CategoryStore.INCOME, category.name);
            } else {
                expenseCategories.remove(category);
                CategoryStore.removeCategory(CategoryStore.EXPENSE, category.name);
            }
            renderCategoryList();
        });
        actions.add(editBtn);
        actions.add(deleteBtn);
        right.add(actions, BorderLayout.EAST);

        item.add(left, BorderLayout.WEST);
        item.add(right, BorderLayout.EAST);
        return item;
    }

    private JButton actionIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(color(226, 232, 240)));
        btn.setBackground(Color.WHITE);
        btn.setForeground(color(37, 99, 235));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildStatsContent() {
        JPanel statsContent = new JPanel();
        statsContent.setOpaque(false);
        statsContent.setLayout(new BoxLayout(statsContent, BoxLayout.Y_AXIS));

        JLabel statsTitle = new JLabel("Statistik Kategori");
        statsTitle.setForeground(color(30, 41, 59));
        statsTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        statsContent.add(statsTitle);

        // Calculate real statistics
        Category mostActive = null;
        Category largestExpense = null;
        Category largestIncome = null;
        
        int maxTx = 0;
        for (Category cat : expenseCategories) {
            if (cat.transactions > maxTx) {
                maxTx = cat.transactions;
                mostActive = cat;
            }
        }
        for (Category cat : incomeCategories) {
            if (cat.transactions > maxTx) {
                maxTx = cat.transactions;
                mostActive = cat;
            }
        }
        
        long maxExpense = 0;
        for (Category cat : expenseCategories) {
            if (cat.total > maxExpense) {
                maxExpense = cat.total;
                largestExpense = cat;
            }
        }
        
        long maxIncome = 0;
        for (Category cat : incomeCategories) {
            if (cat.total > maxIncome) {
                maxIncome = cat.total;
                largestIncome = cat;
            }
        }
        
        // Most active category
        if (mostActive != null) {
            statsContent.add(buildStatsBox(
                "Kategori Paling Aktif", 
                mostActive.name, 
                mostActive.transactions + " transaksi", 
                new Color(239, 246, 255), 
                new Color(191, 219, 254)
            ));
            statsContent.add(Box.createVerticalStrut(12));
        }
        
        // Largest expense
        if (largestExpense != null) {
            statsContent.add(buildStatsBox(
                "Pengeluaran Terbesar", 
                largestExpense.name, 
                "Rp " + String.format("%,d", largestExpense.total).replace(',', '.'), 
                new Color(254, 242, 242), 
                new Color(254, 202, 202)
            ));
            statsContent.add(Box.createVerticalStrut(12));
        }
        
        // Largest income
        if (largestIncome != null) {
            statsContent.add(buildStatsBox(
                "Pemasukan Terbesar", 
                largestIncome.name, 
                "Rp " + String.format("%,d", largestIncome.total).replace(',', '.'), 
                new Color(236, 253, 245), 
                new Color(167, 243, 208)
            ));
            statsContent.add(Box.createVerticalStrut(12));
        }
        
        // Tips
        statsContent.add(buildStatsBox(
            "Tips", 
            "Gunakan emoji/warna unik", 
            "Supaya kategori mudah dikenali", 
            new Color(248, 250, 252), 
            new Color(226, 232, 240)
        ));
        
        return statsContent;
    }

    private JPanel buildFormContent() {
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        formTitle.setForeground(color(30, 41, 59));
        header.add(formTitle, BorderLayout.WEST);
        JButton closeBtn = new JButton("Tutup");
        closeBtn.setForeground(color(71, 85, 105));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.addActionListener(e -> rightCards.show(rightPanelCards, "stats"));
        header.add(closeBtn, BorderLayout.EAST);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        formPanel.add(header, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));

        formTypeCombo = new JComboBox<>(new String[]{"Pengeluaran", "Pemasukan"});
        fields.add(createFormField("Tipe Kategori", formTypeCombo));
        fields.add(Box.createVerticalStrut(12));
        formNameField = new JTextField();
        fields.add(createFormField("Nama Kategori", formNameField));
        fields.add(Box.createVerticalStrut(12));
        formIconField = new JTextField();
        fields.add(createFormField("Icon (2 huruf/emoji)", formIconField));
        fields.add(Box.createVerticalStrut(12));
        fields.add(colorPalette());
        fields.add(Box.createVerticalStrut(12));

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color(203, 213, 225)),
            new EmptyBorder(6, 8, 6, 8)
        ));
        JPanel descPanel = new JPanel();
        descPanel.setOpaque(false);
        descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));
        JLabel descLabel = new JLabel("Deskripsi (Opsional)");
        descLabel.setForeground(color(71, 85, 105));
        descPanel.add(descLabel);
        descPanel.add(Box.createVerticalStrut(4));
        JScrollPane descScroll = new JScrollPane(descArea);
        ScrollUtil.apply(descScroll);
        descPanel.add(descScroll);
        descPanel.setAlignmentX(LEFT_ALIGNMENT);
        fields.add(descPanel);

        formPanel.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(16, 0, 0, 0));
        saveButton = new RoundedButton("Simpan", true);
        saveButton.addActionListener(e -> handleSaveCategory());
        JButton cancelBtn = new RoundedButton("Batal", false);
        cancelBtn.addActionListener(e -> rightCards.show(rightPanelCards, "stats"));
        buttons.add(saveButton);
        buttons.add(cancelBtn);
        formPanel.add(buttons, BorderLayout.SOUTH);
        return formPanel;
    }

    private JPanel colorPalette() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Warna");
        label.setForeground(color(71, 85, 105));
        wrap.add(label);
        wrap.add(Box.createVerticalStrut(6));
        JPanel colors = new JPanel(new GridLayout(2, 5, 8, 8));
        colors.setOpaque(false);
        String[] hexes = {"#2563EB","#059669","#DC2626","#F59E0B","#8B5CF6","#EC4899","#06B6D4","#EF4444","#64748B","#0EA5E9"};
        for(String hex : hexes){
            JButton swatch = new JButton();
            swatch.setBorder(BorderFactory.createLineBorder(color(226,232,240)));
            swatch.setBackground(Color.decode(hex));
            swatch.setPreferredSize(new Dimension(32, 32));
            swatch.setFocusPainted(false);
            colors.add(swatch);
        }
        wrap.add(colors);
        wrap.setAlignmentX(LEFT_ALIGNMENT);
        return wrap;
    }

    private JPanel buildStatsBox(String title, String value, String detail, Color bg, Color border) {
        RoundPanel box = new RoundPanel(10, bg, border);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(12, 12, 12, 12));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        JLabel t = new JLabel(title);
        t.setForeground(color(71, 85, 105));
        t.setAlignmentX(LEFT_ALIGNMENT);
        JLabel v = new JLabel(value);
        v.setForeground(color(30, 41, 59));
        v.setFont(v.getFont().deriveFont(Font.PLAIN, 14f));
        v.setAlignmentX(LEFT_ALIGNMENT);
        v.setBorder(new EmptyBorder(4, 0, 6, 0));
        JLabel d = new JLabel(detail);
        d.setForeground(color(71, 85, 105));
        d.setAlignmentX(LEFT_ALIGNMENT);
        box.add(t);
        box.add(v);
        box.add(d);
        return box;
    }

    private JComponent buildSummaryCard(String title, String value, Color valueColor, String icon) {
        RoundPanel card = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        iconPanel.setOpaque(false);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(iconLbl.getFont().deriveFont(20f));
        iconPanel.add(iconLbl);
        iconPanel.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(iconPanel, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setForeground(color(71, 85, 105));
        t.setAlignmentX(LEFT_ALIGNMENT);
        t.setBorder(new EmptyBorder(0, 0, 6, 0));
        JLabel v = new JLabel(value);
        v.setForeground(valueColor);
        v.setFont(v.getFont().deriveFont(Font.PLAIN, 16f));
        v.setAlignmentX(LEFT_ALIGNMENT);
        content.add(t);
        content.add(v);
        card.add(content, BorderLayout.CENTER);
        return card;
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

    private static Color color(int r, int g, int b) { return new Color(r, g, b); }

    private static class Category {
        final String name;
        final String icon;
        final int transactions;
        final long total;
        final Color color;
        Category(String name, String icon, int transactions, long total, Color color) {
            this.name = name;
            this.icon = icon;
            this.transactions = transactions;
            this.total = total;
            this.color = color;
        }
    }

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

    static class TabButton extends JButton {
        private boolean active;
        TabButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setActive(false);
        }
        void setActive(boolean active) {
            this.active = active;
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(active ? new Color(37, 99, 235) : Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(new Color(226, 232, 240));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            setForeground(active ? Color.WHITE : color(71, 85, 105));
            super.paintComponent(g);
        }
    }
}
