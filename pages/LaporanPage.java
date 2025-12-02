import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import utils.ScrollUtil;
import utils.ComboUtil;

public class LaporanPage extends JPanel {
    private String activeTab = "grafik";
    private final CardLayout tabCards = new CardLayout();
    private final JPanel tabContent = new JPanel(tabCards);
    private TabButton grafButton;
    private TabButton tabelButton;
    private TabButton komparButton;

    private final List<MonthData> monthlyData = List.of(
        new MonthData("Jan", 9_500_000, 7_200_000),
        new MonthData("Feb", 10_200_000, 7_800_000),
        new MonthData("Mar", 11_000_000, 8_500_000),
        new MonthData("Apr", 9_800_000, 7_600_000),
        new MonthData("Mei", 10_500_000, 8_200_000),
        new MonthData("Jun", 10_000_000, 7_500_000),
        new MonthData("Jul", 11_500_000, 8_200_000),
        new MonthData("Agu", 9_800_000, 7_800_000),
        new MonthData("Sep", 12_000_000, 8_500_000),
        new MonthData("Okt", 11_200_000, 9_000_000),
        new MonthData("Nov", 12_500_000, 8_750_000)
    );

    private final List<CategorySlice> categoryBreakdown = List.of(
        new CategorySlice("Makanan & Minuman", 2_500_000, new Color(37,99,235)),
        new CategorySlice("Transportasi", 1_500_000, new Color(5,150,105)),
        new CategorySlice("Belanja", 1_800_000, new Color(220,38,38)),
        new CategorySlice("Tagihan", 2_000_000, new Color(245,158,11)),
        new CategorySlice("Lainnya", 950_000, new Color(100,116,139))
    );

    private final List<ComparisonData> comparisonData = List.of(
        new ComparisonData("Makanan", 2_500_000, 2_200_000),
        new ComparisonData("Transportasi", 1_500_000, 1_800_000),
        new ComparisonData("Belanja", 1_800_000, 1_600_000),
        new ComparisonData("Tagihan", 2_000_000, 2_000_000),
        new ComparisonData("Lainnya", 950_000, 1_100_000)
    );

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
        summaryCards.add(buildSummaryCard("Total Pemasukan", "Rp 125.000.000", color(5, 150, 105)));
        summaryCards.add(buildSummaryCard("Total Pengeluaran", "Rp 87.500.000", color(220, 38, 38)));
        summaryCards.add(buildSummaryCard("Net Income", "Rp 37.500.000", color(37, 99, 235)));
        summaryCards.add(buildSummaryCard("Total Transaksi", "1,247", color(30, 41, 59)));
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
        MonthlyBarChart chart = new MonthlyBarChart(monthlyData);
        chart.setPreferredSize(new Dimension(0, 300));
        barWrap.add(chart, BorderLayout.CENTER);
        wrap.add(barWrap);

        JPanel pieWrap = new JPanel(new BorderLayout());
        pieWrap.setOpaque(false);
        JLabel pieTitle = new JLabel("Breakdown Pengeluaran per Kategori");
        pieTitle.setForeground(color(30, 41, 59));
        pieTitle.setBorder(new EmptyBorder(12, 0, 8, 0));
        pieWrap.add(pieTitle, BorderLayout.NORTH);
        CategoryPieChart pieChart = new CategoryPieChart(categoryBreakdown);
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
        Object[][] rows = {
            {"Pemasukan", 45, 12_500_000, 100},
            {"Makanan & Minuman", 87, 2_500_000, 20},
            {"Transportasi", 52, 1_500_000, 12},
            {"Belanja", 34, 1_800_000, 14.4},
            {"Tagihan", 8, 2_000_000, 16},
            {"Lainnya", 23, 950_000, 7.6}
        };
        DefaultTableModel model = new DefaultTableModel(rows, cols){
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        JTable table = new JTable(model){
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
                    l.setForeground(row==0 ? new Color(5,150,105) : new Color(220,38,38));
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
        ComparisonChart chart = new ComparisonChart(comparisonData);
        chart.setPreferredSize(new Dimension(0, 320));
        wrap.add(chart, BorderLayout.CENTER);
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

    private JComponent buildSummaryCard(String title, String value, Color valueColor) {
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
        private final List<MonthData> data;
        MonthlyBarChart(List<MonthData> data){ this.data = data; setOpaque(false); }
        @Override protected void paintComponent(Graphics g){
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
            int max = data.stream().mapToInt(d->Math.max(d.income(), d.expense())).max().orElse(0);
            int barWidth = (w-padL-padR)/(data.size()*2+data.size());
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
    }

    static class CategoryPieChart extends JPanel {
        private final List<CategorySlice> slices;
        CategoryPieChart(List<CategorySlice> slices){ this.slices=slices; setOpaque(false); }
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            int size = Math.min(w,h) - 60;
            int cx = w/2 - size/2;
            int cy = h/2 - size/2;
            double total = slices.stream().mapToDouble(CategorySlice::value).sum();
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
    }

    static class ComparisonChart extends JPanel {
        private final List<ComparisonData> data;
        ComparisonChart(List<ComparisonData> data){ this.data=data; setOpaque(false); }
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            int padL=120, padR=20, padT=10, padB=30;
            int max = data.stream().mapToInt(d->Math.max(d.thisMonth(), d.lastMonth())).max().orElse(1);
            int barHeight = (h-padT-padB)/(data.size()*2);
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
            // Legend
            int lx = padL; int ly = h - padB + 4;
            g2.setColor(new Color(148,163,184)); g2.fillRect(lx, ly, 12, 12);
            g2.setColor(new Color(30,41,59)); g2.drawString("Bulan Lalu", lx+18, ly+11);
            g2.setColor(new Color(37,99,235)); g2.fillRect(lx+90, ly, 12, 12);
            g2.setColor(new Color(30,41,59)); g2.drawString("Bulan Ini", lx+108, ly+11);
            g2.dispose();
        }
    }
}
