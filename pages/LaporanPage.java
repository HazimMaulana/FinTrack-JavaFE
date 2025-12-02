import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import utils.ScrollUtil;
import utils.ComboUtil;
import utils.TransactionStore;

public class LaporanPage extends JPanel {
    private String activeTab = "grafik";
    private final CardLayout tabCards = new CardLayout();
    private final JPanel tabContent = new JPanel(tabCards);
    private TabButton grafButton;
    private TabButton tabelButton;
    private TabButton komparButton;
    private TransactionStore.Snapshot transactionSnapshot = TransactionStore.snapshot();
    private List<MonthData> monthlyData = new ArrayList<>();
    private List<CategorySlice> categoryBreakdown = new ArrayList<>();
    private List<ComparisonData> comparisonData = new ArrayList<>();
    private MonthlyBarChart monthlyChart;
    private CategoryPieChart pieChart;
    private ComparisonChart comparisonChart;
    private DefaultTableModel summaryTableModel;
    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel netIncomeLabel;
    private JLabel totalTxLabel;

    public LaporanPage() {
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
        styleScrollBar(rootScroll);
        add(rootScroll, BorderLayout.CENTER);

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 12, 16, 16));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Laporan Keuangan");
        title.setForeground(color(30, 41, 59));
        title.setFont(title.getFont().deriveFont(Font.PLAIN, title.getFont().getSize2D() + 6f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Analisis dan visualisasi data keuangan Anda");
        subtitle.setForeground(color(71, 85, 105));
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Filter Panel
        RoundPanel filterCard = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        filterCard.setLayout(new GridLayout(1, 5, 12, 0));
        filterCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        JTextField dateFrom = new JTextField("2025-01-01");
        JTextField dateTo = new JTextField("2025-11-18");
        JComboBox<String> akunFilter = new JComboBox<>(new String[]{"Semua Akun", "BCA", "Mandiri", "Cash"});
        JComboBox<String> kategoriFilter = new JComboBox<>(new String[]{"Semua Kategori", "Pemasukan", "Makanan", "Transportasi", "Belanja", "Tagihan"});
        ComboUtil.apply(akunFilter);
        ComboUtil.apply(kategoriFilter);
        filterCard.add(createFormField("Dari Tanggal", dateFrom));
        filterCard.add(createFormField("Sampai Tanggal", dateTo));
        filterCard.add(createFormField("Akun", akunFilter));
        filterCard.add(createFormField("Kategori", kategoriFilter));
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setOpaque(false);
        JButton showBtn = new RoundedButton("Tampilkan", true);
        btnPanel.add(showBtn, BorderLayout.SOUTH);
        filterCard.add(btnPanel);
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        root.add(filterCard);
        root.add(Box.createVerticalStrut(16));

        // Summary Cards
        JPanel summaryCards = new JPanel(new GridLayout(1, 4, 12, 12));
        summaryCards.setOpaque(false);
        summaryCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        totalIncomeLabel = new JLabel("Rp 0");
        totalExpenseLabel = new JLabel("Rp 0");
        netIncomeLabel = new JLabel("Rp 0");
        totalTxLabel = new JLabel("0");
        summaryCards.add(buildSummaryCard("Total Pemasukan", totalIncomeLabel, color(5, 150, 105)));
        summaryCards.add(buildSummaryCard("Total Pengeluaran", totalExpenseLabel, color(220, 38, 38)));
        summaryCards.add(buildSummaryCard("Net Income", netIncomeLabel, color(37, 99, 235)));
        summaryCards.add(buildSummaryCard("Total Transaksi", totalTxLabel, color(30, 41, 59)));
        root.add(summaryCards);
        root.add(Box.createVerticalStrut(16));

        // Visualization Area with Tabs
        RoundPanel chartCard = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel chartHeader = new JPanel(new BorderLayout());
        chartHeader.setOpaque(false);
        chartHeader.setBorder(new EmptyBorder(0, 0, 12, 0));
        JPanel tabButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabButtons.setOpaque(false);
        grafButton = new TabButton("Grafik");
        tabelButton = new TabButton("Tabel");
        komparButton = new TabButton("Komparasi");
        grafButton.setActive(true);
        grafButton.addActionListener(e -> switchTab("grafik"));
        tabelButton.addActionListener(e -> switchTab("tabel"));
        komparButton.addActionListener(e -> switchTab("komparasi"));
        tabButtons.add(grafButton);
        tabButtons.add(tabelButton);
        tabButtons.add(komparButton);
        chartHeader.add(tabButtons, BorderLayout.WEST);

        JPanel exportButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        exportButtons.setOpaque(false);
        exportButtons.add(new RoundedButton("Export PDF", false));
        exportButtons.add(new RoundedButton("Export Excel", false));
        exportButtons.add(new RoundedButton("Print", false));
        chartHeader.add(exportButtons, BorderLayout.EAST);
        chartCard.add(chartHeader, BorderLayout.NORTH);

        tabContent.setOpaque(false);
        tabContent.add(buildGrafikTab(), "grafik");
        tabContent.add(buildTabelTab(), "tabel");
        tabContent.add(buildKomparasiTab(), "komparasi");
        chartCard.add(tabContent, BorderLayout.CENTER);
        root.add(chartCard);
        root.add(Box.createVerticalGlue());

        TransactionStore.addListener(snap -> {
            transactionSnapshot = snap;
            refreshData();
        });
        refreshData();
    }

    private void switchTab(String key) {
        if (key.equals(activeTab)) return;
        activeTab = key;
        grafButton.setActive("grafik".equals(key));
        tabelButton.setActive("tabel".equals(key));
        komparButton.setActive("komparasi".equals(key));
        tabCards.show(tabContent, key);
    }

    private JPanel buildGrafikTab() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JPanel barWrap = new JPanel(new BorderLayout());
        barWrap.setOpaque(false);
        barWrap.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel barTitle = new JLabel("Pemasukan vs Pengeluaran Bulanan");
        barTitle.setForeground(color(30, 41, 59));
        barTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        barWrap.add(barTitle, BorderLayout.NORTH);
        monthlyChart = new MonthlyBarChart(monthlyData);
        monthlyChart.setPreferredSize(new Dimension(0, 300));
        barWrap.add(monthlyChart, BorderLayout.CENTER);
        wrap.add(barWrap);

        JPanel pieWrap = new JPanel(new BorderLayout());
        pieWrap.setOpaque(false);
        JLabel pieTitle = new JLabel("Breakdown Pengeluaran per Kategori");
        pieTitle.setForeground(color(30, 41, 59));
        pieTitle.setBorder(new EmptyBorder(12, 0, 8, 0));
        pieWrap.add(pieTitle, BorderLayout.NORTH);
        pieChart = new CategoryPieChart(categoryBreakdown);
        pieChart.setPreferredSize(new Dimension(0, 320));
        pieWrap.add(pieChart, BorderLayout.CENTER);
        wrap.add(pieWrap);
        return wrap;
    }

    private JPanel buildTabelTab() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JLabel tableTitle = new JLabel("Ringkasan per Kategori");
        tableTitle.setForeground(color(30, 41, 59));
        tableTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrap.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Kategori", "Jumlah Transaksi", "Total Nominal", "% dari Total"};
        summaryTableModel = new DefaultTableModel(new Object[0][0], cols){
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        JTable table = new JTable(summaryTableModel){
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col){
                Component c = super.prepareRenderer(r, row, col);
                if(!isRowSelected(row)){
                    c.setBackground(row % 2 == 0 ? new Color(248,250,252) : Color.WHITE);
                }
                if(col==1 || col==2 || col==3){
                    JLabel l=(JLabel)c;
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                }
                if(col==2){
                    JLabel l=(JLabel)c;
                    try {
                        long val = Long.parseLong(l.getText());
                        l.setForeground(val >= 0 ? new Color(5,150,105) : new Color(220,38,38));
                    } catch (NumberFormatException ex) {
                        l.setForeground(new Color(71,85,105));
                    }
                } else {
                    c.setForeground(new Color(71,85,105));
                }
                return c;
            }
        };
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setPreferredSize(new Dimension(0, 280));
        styleScrollBar(sp);
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildKomparasiTab() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JLabel title = new JLabel("Perbandingan Bulan Ini vs Bulan Lalu");
        title.setForeground(color(30, 41, 59));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrap.add(title, BorderLayout.NORTH);
        comparisonChart = new ComparisonChart(comparisonData);
        comparisonChart.setPreferredSize(new Dimension(0, 320));
        wrap.add(comparisonChart, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createFormField(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(color(71, 85, 105));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        return panel;
    }

    private void refreshData() {
        YearMonth now = YearMonth.now();
        long totalIncome = transactionSnapshot.transactions().stream().filter(TransactionStore.Transaction::isIncome).mapToLong(TransactionStore.Transaction::amount).sum();
        long totalExpense = transactionSnapshot.transactions().stream().filter(t -> !t.isIncome()).mapToLong(TransactionStore.Transaction::amount).sum();
        long net = totalIncome - totalExpense;
        if (totalIncomeLabel != null) totalIncomeLabel.setText(formatRupiah(totalIncome));
        if (totalExpenseLabel != null) totalExpenseLabel.setText(formatRupiah(totalExpense));
        if (netIncomeLabel != null) netIncomeLabel.setText(formatRupiah(net));
        if (totalTxLabel != null) totalTxLabel.setText(String.valueOf(transactionSnapshot.transactions().size()));

        List<MonthData> months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            int income = (int) transactionSnapshot.transactions().stream().filter(t -> t.yearMonth().equals(ym) && t.isIncome()).mapToLong(TransactionStore.Transaction::amount).sum();
            int expense = (int) transactionSnapshot.transactions().stream().filter(t -> t.yearMonth().equals(ym) && !t.isIncome()).mapToLong(TransactionStore.Transaction::amount).sum();
            months.add(new MonthData(shortMonth(ym), income, expense));
        }
        monthlyData = months;
        if (monthlyChart != null) {
            monthlyChart.setData(monthlyData);
        }

        Map<String, Long> expenseByCat = transactionSnapshot.transactions().stream()
            .filter(t -> t.yearMonth().equals(now) && !t.isIncome())
            .collect(Collectors.groupingBy(TransactionStore.Transaction::category, Collectors.summingLong(TransactionStore.Transaction::amount)));
        categoryBreakdown = new ArrayList<>();
        Color[] palette = new Color[]{color(37,99,235), color(5,150,105), color(220,38,38), color(245,158,11), color(100,116,139), color(14,165,233)};
        int idx = 0;
        for (Map.Entry<String, Long> e : expenseByCat.entrySet()) {
            categoryBreakdown.add(new CategorySlice(e.getKey(), e.getValue(), palette[idx % palette.length]));
            idx++;
        }
        if (pieChart != null) {
            pieChart.setData(categoryBreakdown);
        }

        YearMonth lastMonth = now.minusMonths(1);
        comparisonData = new ArrayList<>();
        Map<String, Long> thisMonth = transactionSnapshot.transactions().stream()
            .filter(t -> t.yearMonth().equals(now) && !t.isIncome())
            .collect(Collectors.groupingBy(TransactionStore.Transaction::category, Collectors.summingLong(TransactionStore.Transaction::amount)));
        Map<String, Long> prevMonth = transactionSnapshot.transactions().stream()
            .filter(t -> t.yearMonth().equals(lastMonth) && !t.isIncome())
            .collect(Collectors.groupingBy(TransactionStore.Transaction::category, Collectors.summingLong(TransactionStore.Transaction::amount)));
        thisMonth.keySet().stream().sorted().forEach(cat -> {
            long cur = thisMonth.getOrDefault(cat, 0L);
            long prev = prevMonth.getOrDefault(cat, 0L);
            comparisonData.add(new ComparisonData(cat, (int) cur, (int) prev));
        });
        if (comparisonChart != null) {
            comparisonChart.setData(comparisonData);
        }

        if (summaryTableModel != null) {
            summaryTableModel.setRowCount(0);
            Map<String, List<TransactionStore.Transaction>> grouped = transactionSnapshot.transactions().stream()
                .collect(Collectors.groupingBy(TransactionStore.Transaction::category));
            long totalNominal = transactionSnapshot.transactions().stream().mapToLong(TransactionStore.Transaction::amount).sum();
            grouped.forEach((cat, txs) -> {
                long total = txs.stream().mapToLong(TransactionStore.Transaction::amount).sum();
                int count = txs.size();
                double pct = totalNominal == 0 ? 0 : (total * 100.0 / totalNominal);
                summaryTableModel.addRow(new Object[]{cat, count, total, pct});
            });
        }
    }

    private String shortMonth(YearMonth ym) {
        return ym.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault());
    }

    private JComponent buildSummaryCard(String title, JLabel valueLabel, Color valueColor) {
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
        valueLabel.setForeground(valueColor);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, 18f));
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(t);
        content.add(valueLabel);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private static Color color(int r, int g, int b) { return new Color(r, g, b); }

    private record MonthData(String month, int income, int expense) {}
    private record CategorySlice(String name, double value, Color color) {}
    private record ComparisonData(String category, int thisMonth, int lastMonth) {}

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

    private void styleScrollBar(JScrollPane sp){
        ScrollUtil.apply(sp);
    }

    static class MonthlyBarChart extends JPanel {
        private List<MonthData> data;
        private JComponent xchartPanel;
        MonthlyBarChart(List<MonthData> data){
            this.data = new ArrayList<>(data);
            setOpaque(false);
            setLayout(new BorderLayout());
            xchartPanel = buildXChartPanel();
            if (xchartPanel != null) {
                add(xchartPanel, BorderLayout.CENTER);
            }
        }

        void setData(List<MonthData> data){
            this.data = new ArrayList<>(data);
            rebuildChart();
            repaint();
        }

        private void rebuildChart() {
            if (xchartPanel != null) {
                remove(xchartPanel);
                xchartPanel = null;
            }
            xchartPanel = buildXChartPanel();
            if (xchartPanel != null) {
                add(xchartPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
            }
        }
        @Override protected void paintComponent(Graphics g){
            if (xchartPanel != null) {
                super.paintComponent(g);
                return;
            }
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            int padL=60, padR=20, padT=10, padB=40;
            g2.setColor(new Color(226,232,240));
            for(int i=0;i<=5;i++){
                int y=padT+(h-padT-padB)*i/5;
                g2.drawLine(padL, y, w-padR, y);
            }
            if (data.isEmpty()) {
                g2.setColor(new Color(148,163,184));
                g2.drawString("Belum ada data transaksi", padL, padT + 20);
                g2.dispose();
                return;
            }
            int max = data.stream().mapToInt(d->Math.max(d.income(), d.expense())).max().orElse(1);
            int barWidth = Math.max(8, (w-padL-padR)/Math.max(1, (data.size()*2+data.size())));
            int x=padL;
            for(MonthData d : data){
                int incHeight = (int)((double)d.income()/max*(h-padT-padB));
                int expHeight = (int)((double)d.expense()/max*(h-padT-padB));
                int base = h-padB;
                g2.setColor(new Color(5,150,105));
                g2.fillRoundRect(x, base-incHeight, barWidth, incHeight, 6, 6);
                g2.setColor(new Color(220,38,38));
                g2.fillRoundRect(x+barWidth+4, base-expHeight, barWidth, expHeight, 6, 6);
                g2.setColor(new Color(100,116,139));
                g2.drawString(d.month(), x, h-18);
                x += barWidth*2 + 12;
            }
            // Legend
            int lx=padL, ly=padT+8;
            drawLegend(g2, lx, ly, new Color(5,150,105), "Pemasukan");
            drawLegend(g2, lx+120, ly, new Color(220,38,38), "Pengeluaran");
            g2.dispose();
        }
        private void drawLegend(Graphics2D g2, int x, int y, Color col, String name){
            g2.setColor(col); g2.fillRect(x,y,14,14);
            g2.setColor(new Color(30,41,59)); g2.drawString(name, x+20, y+12);
        }

        private JComponent buildXChartPanel() {
            try {
                Class<?> builderCls = Class.forName("org.knowm.xchart.CategoryChartBuilder");
                Object builder = builderCls.getConstructor().newInstance();
                Method width = builderCls.getMethod("width", int.class);
                Method height = builderCls.getMethod("height", int.class);
                Method title = builderCls.getMethod("title", String.class);
                Method xTitle = builderCls.getMethod("xAxisTitle", String.class);
                Method yTitle = builderCls.getMethod("yAxisTitle", String.class);
                width.invoke(builder, 720);
                height.invoke(builder, 360);
                title.invoke(builder, "Pemasukan vs Pengeluaran Bulanan (XChart)");
                xTitle.invoke(builder, "Bulan");
                yTitle.invoke(builder, "Nominal");
                Object chart = builderCls.getMethod("build").invoke(builder);

                List<String> months = new ArrayList<>();
                List<Double> incomes = new ArrayList<>();
                List<Double> expenses = new ArrayList<>();
                for (MonthData d : data) {
                    months.add(d.month());
                    incomes.add((double) d.income());
                    expenses.add((double) d.expense());
                }

                Method addSeries = chart.getClass().getMethod("addSeries", String.class, List.class, List.class);
                addSeries.invoke(chart, "Pemasukan", months, incomes);
                addSeries.invoke(chart, "Pengeluaran", months, expenses);

                Object styler = chart.getClass().getMethod("getStyler").invoke(chart);
                try {
                    Method hasAnnotations = styler.getClass().getMethod("setHasAnnotations", boolean.class);
                    hasAnnotations.invoke(styler, true);
                } catch (NoSuchMethodException ignored) {}

                Class<?> panelCls = Class.forName("org.knowm.xchart.XChartPanel");
                Constructor<?> ctor = findSingleArgConstructor(panelCls);
                if (ctor != null) {
                    return (JComponent) ctor.newInstance(chart);
                }
            } catch (Exception ex) {
                // XChart not available; fallback to custom painter
            }
            return null;
        }

    private Constructor<?> findSingleArgConstructor(Class<?> cls) {
        for (Constructor<?> c : cls.getConstructors()) {
            if (c.getParameterCount() == 1) {
                return c;
            }
        }
        return null;
    }

    private String formatRupiah(long value) {
        String raw = String.format("%,d", value).replace(",", ".");
        return "Rp " + raw;
    }
}

    static class CategoryPieChart extends JPanel {
        private List<CategorySlice> slices;
        private JComponent xchartPanel;
        CategoryPieChart(List<CategorySlice> slices){
            this.slices=new ArrayList<>(slices);
            setOpaque(false);
            setLayout(new BorderLayout());
            xchartPanel = buildXChartPanel();
            if (xchartPanel != null) {
                add(xchartPanel, BorderLayout.CENTER);
            }
        }

        void setData(List<CategorySlice> slices){
            this.slices = new ArrayList<>(slices);
            rebuildChart();
            repaint();
        }

        private void rebuildChart() {
            if (xchartPanel != null) {
                remove(xchartPanel);
                xchartPanel = null;
            }
            xchartPanel = buildXChartPanel();
            if (xchartPanel != null) {
                add(xchartPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
            }
        }
        @Override protected void paintComponent(Graphics g){
            if (xchartPanel != null) {
                super.paintComponent(g);
                return;
            }
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            int size = Math.min(w,h) - 60;
            int cx = w/2 - size/2;
            int cy = h/2 - size/2;
            double total = slices.stream().mapToDouble(CategorySlice::value).sum();
            if (total <= 0) {
                g2.setColor(new Color(148,163,184));
                g2.drawString("Belum ada pengeluaran bulan ini", 16, 20);
                g2.dispose();
                return;
            }
            double start = 90;
            for(CategorySlice s : slices){
                double extent = -360 * (s.value()/total);
                g2.setColor(s.color());
                g2.fillArc(cx, cy, size, size, (int)start, (int)extent);
                start += extent;
            }
            int lx = 10, ly = 10;
            for(CategorySlice s : slices){
                g2.setColor(s.color()); g2.fillRect(lx, ly, 12, 12);
                g2.setColor(new Color(30,41,59)); g2.drawString(s.name(), lx+18, ly+11);
                ly += 18;
            }
            g2.dispose();
        }

        private JComponent buildXChartPanel() {
            try {
                Class<?> builderCls = Class.forName("org.knowm.xchart.PieChartBuilder");
                Object builder = builderCls.getConstructor().newInstance();
                Method width = builderCls.getMethod("width", int.class);
                Method height = builderCls.getMethod("height", int.class);
                Method title = builderCls.getMethod("title", String.class);
                width.invoke(builder, 360);
                height.invoke(builder, 320);
                title.invoke(builder, "Pengeluaran per Kategori (XChart)");

                Object chart = builderCls.getMethod("build").invoke(builder);
                Method addSeries = chart.getClass().getMethod("addSeries", String.class, Number.class);
                for (CategorySlice slice : slices) {
                    addSeries.invoke(chart, slice.name(), slice.value());
                }
                Object styler = chart.getClass().getMethod("getStyler").invoke(chart);
                try {
                    Method legend = styler.getClass().getMethod("setLegendVisible", boolean.class);
                    legend.invoke(styler, true);
                } catch (NoSuchMethodException ignored) {}

                Class<?> panelCls = Class.forName("org.knowm.xchart.XChartPanel");
                Constructor<?> ctor = findSingleArgConstructor(panelCls);
                if (ctor != null) {
                    return (JComponent) ctor.newInstance(chart);
                }
            } catch (Exception ex) {
                // XChart unavailable, fallback to custom painter
            }
            return null;
        }

        private Constructor<?> findSingleArgConstructor(Class<?> cls) {
            for (Constructor<?> c : cls.getConstructors()) {
                if (c.getParameterCount() == 1) {
                    return c;
                }
            }
            return null;
        }
    }

    static class ComparisonChart extends JPanel {
        private List<ComparisonData> data;
        ComparisonChart(List<ComparisonData> data){ this.data=new ArrayList<>(data); setOpaque(false); }

        void setData(List<ComparisonData> data){
            this.data = new ArrayList<>(data);
            repaint();
        }

        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            int padL=120, padR=20, padT=10, padB=30;
            if (data.isEmpty()) {
                g2.setColor(new Color(148,163,184));
                g2.drawString("Belum ada perbandingan kategori", padL, padT + 20);
                g2.dispose();
                return;
            }
            int max = data.stream().mapToInt(d->Math.max(d.thisMonth(), d.lastMonth())).max().orElse(1);
            int barHeight = Math.max(8, (h-padT-padB)/Math.max(1, data.size()*2));
            int y = padT;
            for(ComparisonData d : data){
                int lastW = (int)((double)d.lastMonth()/max*(w-padL-padR));
                int thisW = (int)((double)d.thisMonth()/max*(w-padL-padR));
                g2.setColor(new Color(148,163,184));
                g2.fillRoundRect(padL, y, lastW, barHeight, 8, 8);
                g2.setColor(new Color(37,99,235));
                g2.fillRoundRect(padL, y+barHeight+4, thisW, barHeight, 8, 8);
                g2.setColor(new Color(30,41,59));
                g2.drawString(d.category(), 10, y+barHeight);
                y += barHeight*2 + 12;
            }
            int lx = padL; int ly = h - padB + 4;
            g2.setColor(new Color(148,163,184)); g2.fillRect(lx, ly, 12, 12);
            g2.setColor(new Color(30,41,59)); g2.drawString("Bulan Lalu", lx+18, ly+11);
            g2.setColor(new Color(37,99,235)); g2.fillRect(lx+90, ly, 12, 12);
            g2.setColor(new Color(30,41,59)); g2.drawString("Bulan Ini", lx+108, ly+11);
            g2.dispose();
        }
    }

    private String formatRupiah(long value) {
        String raw = String.format("%,d", value).replace(",", ".");
        return "Rp " + raw;
    }
}
