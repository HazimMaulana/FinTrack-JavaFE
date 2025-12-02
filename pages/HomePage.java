import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import utils.ScrollUtil;
import utils.AccountStore;
import utils.TransactionStore;

public class HomePage extends JPanel {
    private JLabel totalSaldoLabel;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel netLabel;
    private JLabel netSubtitle;
    private TrendChartPanel trendChart;
    private CategoryPieChartPanel pieChart;
    private DefaultTableModel recentTableModel;
    private AccountStore.Snapshot accountSnapshot = AccountStore.snapshot();
    private TransactionStore.Snapshot transactionSnapshot = TransactionStore.snapshot();

    public HomePage() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        // Keep overall page padding but remove left/top so header can sit flush left
        root.setBorder(new EmptyBorder(0, 16, 16, 16));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        JLabel title = new JLabel("Dashboard");
        title.setForeground(color(30, 41, 59)); // slate-800
        title.setFont(title.getFont().deriveFont(Font.PLAIN, title.getFont().getSize2D() + 6f));
        JLabel subtitle = new JLabel("Overview keuangan Anda");
        subtitle.setForeground(color(71, 85, 105)); // slate-600
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        // Force left alignment under BoxLayout Y_AXIS
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(subtitle);
        // Place header in page NORTH with a small left gap from the sidebar
        header.setBorder(new EmptyBorder(16, 12, 16, 16));

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        page.add(header);
        page.add(root);

        JScrollPane pageScroll = new JScrollPane(page);
        pageScroll.setBorder(null);
        pageScroll.setOpaque(false);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        ScrollUtil.apply(pageScroll);
        add(pageScroll, BorderLayout.CENTER);

        // Summary cards grid (4 cols)
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(0, 0, 16, 0));
        totalSaldoLabel = new JLabel("Rp 0");
        incomeLabel = new JLabel("Rp 0");
        expenseLabel = new JLabel("Rp 0");
        netLabel = new JLabel("Rp 0");
        netSubtitle = new JLabel("+0% vs bulan lalu");
        cards.add(buildSummaryCard("Total Saldo", totalSaldoLabel, new WalletIcon(), color(37,99,235), color(239,246,255), null));
        cards.add(buildSummaryCard("Pemasukan Bulan Ini", incomeLabel, new ArrowUpCircleIcon(), color(5,150,105), color(236,253,245), null));
        cards.add(buildSummaryCard("Pengeluaran Bulan Ini", expenseLabel, new ArrowDownCircleIcon(), color(220,38,38), color(254,242,242), null));
        cards.add(buildSummaryCard("Selisih (Profit/Loss)", netLabel, new TrendingUpIcon(), color(5,150,105), color(236,253,245), netSubtitle));
        root.add(cards);

        // Charts row (2:1)
        JPanel chartsRow = new JPanel(new GridBagLayout());
        chartsRow.setOpaque(false);
        chartsRow.setBorder(new EmptyBorder(0, 0, 16, 0));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 12);
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 2; gc.fill = GridBagConstraints.BOTH; gc.weighty = 1;
        RoundPanel lineCard = new RoundPanel(10, Color.WHITE, color(226,232,240));
        lineCard.setLayout(new BorderLayout());
        lineCard.setBorder(new EmptyBorder(16,16,16,16));
        JLabel lineTitle = label("Trend 6 Bulan Terakhir", color(30,41,59), 0, 0, 0, 0);
        lineTitle.setFont(lineTitle.getFont().deriveFont(Font.PLAIN, 14f));
        lineCard.add(lineTitle, BorderLayout.NORTH);
        trendChart = new TrendChartPanel();
        trendChart.setPreferredSize(new Dimension(0, 300));
        lineCard.add(trendChart, BorderLayout.CENTER);
        chartsRow.add(lineCard, gc);

        gc.insets = new Insets(0, 0, 0, 0);
        gc.gridx = 1; gc.weightx = 1;
        RoundPanel pieCard = new RoundPanel(10, Color.WHITE, color(226,232,240));
        pieCard.setLayout(new BorderLayout());
        pieCard.setBorder(new EmptyBorder(16,16,16,16));
        JLabel pieTitle = label("Kategori Pengeluaran", color(30,41,59), 0, 0, 0, 0);
        pieTitle.setFont(pieTitle.getFont().deriveFont(Font.PLAIN, 14f));
        pieCard.add(pieTitle, BorderLayout.NORTH);
        pieChart = new CategoryPieChartPanel();
        pieChart.setPreferredSize(new Dimension(0, 300));
        pieCard.add(pieChart, BorderLayout.CENTER);
        chartsRow.add(pieCard, gc);
        chartsRow.setPreferredSize(new Dimension(0, 360));
        root.add(chartsRow);

        // Transactions table (single wide card)
        RoundPanel txCard = new RoundPanel(10, Color.WHITE, color(226,232,240));
        txCard.setLayout(new BorderLayout());
        txCard.setBorder(new EmptyBorder(16,16,16,16));
        JLabel txTitle = label("10 Transaksi Terakhir", color(30,41,59), 0, 0, 12, 12);
        txTitle.setFont(txTitle.getFont().deriveFont(Font.PLAIN, 14f));
        txCard.add(txTitle, BorderLayout.NORTH);
        JComponent tablePane = buildTransactionsTable();
        txCard.add(tablePane, BorderLayout.CENTER);
        root.add(txCard);

        // Sync with data stores
        AccountStore.addListener(snap -> {
            accountSnapshot = snap;
            refreshData();
        });
        TransactionStore.addListener(snap -> {
            transactionSnapshot = snap;
            refreshData();
        });
        refreshData();
    }

    private static JLabel label(String text, Color fg, int l, int t, int r, int b){
        JLabel l1 = new JLabel(text);
        l1.setForeground(fg);
        l1.setBorder(new EmptyBorder(t,l,b,r));
        return l1;
    }

    private static Color color(int r, int g, int b){ return new Color(r,g,b); }

    private String formatRupiah(long value){
        String raw = String.format("%,d", value).replace(",", ".");
        return "Rp " + raw;
    }

    private String formatSigned(long value, boolean income){
        return (income ? "+ " : "- ") + formatRupiah(value);
    }

    private JComponent buildSummaryCard(String title, JLabel valueLabel, Icon icon, Color accent, Color accentBg, JLabel subtitleLabel){
        RoundPanel card = new RoundPanel(10, Color.WHITE, color(226,232,240));
        card.setLayout(new BorderLayout());
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BorderLayout());
        JPanel iconWrap = new RoundPanel(8, accentBg, accentBg);
        iconWrap.setLayout(new GridBagLayout());
        JLabel i = new JLabel(icon);
        i.setForeground(accent);
        iconWrap.add(i);
        iconWrap.setPreferredSize(new Dimension(40, 40));
        JPanel topPad = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topPad.setOpaque(false);
        topPad.add(iconWrap);
        top.add(topPad, BorderLayout.WEST);
        card.add(top, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel t1 = label(title, color(71,85,105), 8, 0, 0, 4);
        valueLabel.setForeground(color(30,41,59));
        valueLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, valueLabel.getFont().getSize2D() + 2f));
        content.add(t1);
        content.add(valueLabel);
        if(subtitleLabel!=null){
            subtitleLabel.setForeground(color(5,150,105));
            subtitleLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
            content.add(subtitleLabel);
        }
        card.add(content, BorderLayout.CENTER);
        card.setBorder(new EmptyBorder(16,16,16,16));
        return card;
    }

    private JComponent buildTransactionsTable(){
        String[] cols = {"Tanggal", "Keterangan", "Kategori", "Nominal", "Saldo"};
        recentTableModel = new DefaultTableModel(new Object[0][0], cols){
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        JTable table = new JTable(recentTableModel){
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col){
                Component c = super.prepareRenderer(r, row, col);
                if(!isRowSelected(row)){
                    c.setBackground(row % 2 == 0 ? new Color(248,250,252) : Color.WHITE); // slate-50/50
                }
                if(col==3){
                    JLabel l = (JLabel)c;
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                    l.setForeground(l.getText().startsWith("+") ? new Color(5,150,105) : new Color(220,38,38));
                } else if(col==4){
                    JLabel l = (JLabel)c; l.setHorizontalAlignment(SwingConstants.RIGHT); l.setForeground(new Color(30,41,59));
                } else {
                    c.setForeground(new Color(71,85,105));
                }
                return c;
            }
        };
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(table.getTableHeader(), BorderLayout.NORTH);
        wrap.add(table, BorderLayout.CENTER);
        return wrap;
    }

    private void refreshData() {
        long totalSaldo = accountSnapshot.accounts().stream().mapToLong(AccountStore.Account::balance).sum();
        totalSaldoLabel.setText(formatRupiah(totalSaldo));

        YearMonth now = YearMonth.now();
        YearMonth lastMonth = now.minusMonths(1);

        long incomeThisMonth = transactionSnapshot.transactions().stream()
            .filter(t -> t.yearMonth().equals(now) && t.isIncome())
            .mapToLong(TransactionStore.Transaction::amount)
            .sum();
        long expenseThisMonth = transactionSnapshot.transactions().stream()
            .filter(t -> t.yearMonth().equals(now) && !t.isIncome())
            .mapToLong(TransactionStore.Transaction::amount)
            .sum();
        long netThisMonth = incomeThisMonth - expenseThisMonth;

        long netLastMonth = transactionSnapshot.transactions().stream()
            .filter(t -> t.yearMonth().equals(lastMonth))
            .mapToLong(t -> t.isIncome() ? t.amount() : -t.amount())
            .sum();

        incomeLabel.setText(formatRupiah(incomeThisMonth));
        expenseLabel.setText(formatRupiah(expenseThisMonth));
        netLabel.setText(formatRupiah(netThisMonth));
        double change = netLastMonth == 0 ? 0 : ((double)(netThisMonth - netLastMonth) / Math.max(1, Math.abs(netLastMonth))) * 100;
        netSubtitle.setText(String.format("%+.1f%% vs bulan lalu", change));

        // Trend 6 months
        List<YearMonth> months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            months.add(now.minusMonths(i));
        }
        String[] monthLabels = new String[months.size()];
        int[] incomes = new int[months.size()];
        int[] expenses = new int[months.size()];
        for (int i = 0; i < months.size(); i++) {
            YearMonth ym = months.get(i);
            monthLabels[i] = ym.getMonth().toString().substring(0, 3);
            incomes[i] = (int) transactionSnapshot.transactions().stream().filter(t -> t.yearMonth().equals(ym) && t.isIncome()).mapToLong(TransactionStore.Transaction::amount).sum();
            expenses[i] = (int) transactionSnapshot.transactions().stream().filter(t -> t.yearMonth().equals(ym) && !t.isIncome()).mapToLong(TransactionStore.Transaction::amount).sum();
        }
        trendChart.setData(monthLabels, incomes, expenses);

        // Category breakdown (current month expenses)
        List<CategoryPieChartPanel.Slice> slices = new ArrayList<>();
        Color[] palette = new Color[]{color(37,99,235), color(5,150,105), color(220,38,38), color(245,158,11), color(100,116,139), color(14,165,233)};
        Map<String, Long> groupedExpenses = transactionSnapshot.transactions().stream()
            .filter(t -> t.yearMonth().equals(now) && !t.isIncome())
            .collect(java.util.stream.Collectors.groupingBy(TransactionStore.Transaction::category, java.util.stream.Collectors.summingLong(TransactionStore.Transaction::amount)));
        int idx = 0;
        for (Map.Entry<String, Long> entry : groupedExpenses.entrySet()) {
            Color c = palette[idx % palette.length];
            slices.add(new CategoryPieChartPanel.Slice(entry.getKey(), entry.getValue(), c));
            idx++;
        }
        pieChart.setSlices(slices);

        // Recent transactions
        recentTableModel.setRowCount(0);
        long running = 0;
        List<TransactionStore.Transaction> recent = transactionSnapshot.transactions().stream()
            .sorted(Comparator.comparing(TransactionStore.Transaction::date).reversed())
            .limit(10)
            .toList();
        for (TransactionStore.Transaction tx : recent) {
            boolean income = tx.isIncome();
            running += income ? tx.amount() : -tx.amount();
            String nominal = formatSigned(tx.amount(), income);
            recentTableModel.addRow(new Object[]{
                tx.date().toString(),
                tx.description() == null || tx.description().isBlank() ? "-" : tx.description(),
                tx.category(),
                nominal,
                formatRupiah(running)
            });
        }
    }

    private JComponent buildUpcomingBills(){
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.add(billItem("Cicilan KPR", "Rp 3.500.000", "25 Nov 2025"));
        wrap.add(Box.createVerticalStrut(10));
        wrap.add(billItem("Asuransi Kesehatan", "Rp 500.000", "28 Nov 2025"));
        wrap.add(Box.createVerticalStrut(10));
        wrap.add(billItem("Kartu Kredit", "Rp 1.250.000", "30 Nov 2025"));
        JScrollPane sp = new JScrollPane(wrap){
            { getViewport().setOpaque(false); setOpaque(false); setBorder(null); }
        };
        ScrollUtil.apply(sp);
        return sp;
    }

    private JComponent billItem(String name, String amount, String due){
        RoundPanel p = new RoundPanel(10, new Color(255,251,235), new Color(253,230,138)); // amber-50 + border-amber-200
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12,12,12,12));
        JLabel n = label(name, color(30,41,59), 0,0,0,4);
        JLabel a = label(amount, color(30,41,59), 0,0,0,6);
        JLabel d = label("Jatuh tempo: "+due, color(71,85,105), 0,0,0,0);
        p.add(n); p.add(a); p.add(d);
        return p;
    }

    // Simple rounded card panel with border
    static class RoundPanel extends JPanel {
        private final int arc; private final Color bg; private final Color border;
        RoundPanel(int arc, Color bg, Color border){ this.arc=arc; this.bg=bg; this.border=border; setOpaque(false);}        
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            g2.setColor(bg); g2.fillRoundRect(0,0,w-1,h-1,arc,arc);
            if(border!=null){ g2.setColor(border); g2.drawRoundRect(0,0,w-1,h-1,arc,arc);}            
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Icons
    static class WalletIcon implements Icon {
        private final int s=24;
        @Override public void paintIcon(Component c, Graphics g, int x, int y){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(37,99,235));
            g2.fillRoundRect(x+3,y+6,s-6,s-12,6,6);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x+6,y+9,s-12,s-18,6,6);
            g2.setColor(new Color(37,99,235));
            g2.fillOval(x+s-10,y+s/2-2,4,4);
            g2.dispose();
        }
        @Override public int getIconWidth(){ return s; }
        @Override public int getIconHeight(){ return s; }
    }
    static class ArrowUpCircleIcon implements Icon {
        private final int s=24;
        @Override public void paintIcon(Component c, Graphics g, int x, int y){
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(5,150,105)); g2.drawOval(x+2,y+2,s-4,s-4);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x+s/2,y+6,x+s/2,y+s-8);
            g2.drawLine(x+s/2,y+6,x+s/2-6,y+12);
            g2.drawLine(x+s/2,y+6,x+s/2+6,y+12);
            g2.dispose();
        }
        @Override public int getIconWidth(){ return s; }
        @Override public int getIconHeight(){ return s; }
    }
    static class ArrowDownCircleIcon implements Icon {
        private final int s=24;
        @Override public void paintIcon(Component c, Graphics g, int x, int y){
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(220,38,38)); g2.drawOval(x+2,y+2,s-4,s-4);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x+s/2,y+6,x+s/2,y+s-8);
            g2.drawLine(x+s/2,y+s-8,x+s/2-6,y+s-14);
            g2.drawLine(x+s/2,y+s-8,x+s/2+6,y+s-14);
            g2.dispose();
        }
        @Override public int getIconWidth(){ return s; }
        @Override public int getIconHeight(){ return s; }
    }
    static class TrendingUpIcon implements Icon {
        private final int s=24;
        @Override public void paintIcon(Component c, Graphics g, int x, int y){
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(5,150,105)); g2.setStroke(new BasicStroke(2f));
            Path2D p = new Path2D.Double();
            p.moveTo(x+4, y+s-6);
            p.lineTo(x+10, y+s-12);
            p.lineTo(x+14, y+s-8);
            p.lineTo(x+s-6, y+6);
            g2.draw(p);
            g2.drawLine(x+s-10, y+6, x+s-6, y+6);
            g2.drawLine(x+s-6, y+6, x+s-6, y+10);
            g2.dispose();
        }
        @Override public int getIconWidth(){ return s; }
        @Override public int getIconHeight(){ return s; }
    }

    // Trend chart panel
    static class TrendChartPanel extends JPanel {
        private String[] months = new String[0];
        private int[] pemasukan = new int[0];
        private int[] pengeluaran = new int[0];

        void setData(String[] months, int[] pemasukan, int[] pengeluaran) {
            this.months = months == null ? new String[0] : months;
            this.pemasukan = pemasukan == null ? new int[0] : pemasukan;
            this.pengeluaran = pengeluaran == null ? new int[0] : pengeluaran;
            repaint();
        }

        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            int padL=50, padR=16, padT=10, padB=30;
            g2.setColor(new Color(226,232,240));
            int gridLines = 5;
            for(int i=0;i<=gridLines;i++){
                int y = padT + (h-padT-padB) * i / gridLines;
                g2.drawLine(padL, y, w-padR, y);
            }
            g2.setColor(new Color(100,116,139));
            int cols = months.length;
            for(int i=0;i<cols;i++){
                int x = padL + (w-padL-padR) * (cols == 1 ? 0 : i) / Math.max(1, cols-1);
                g2.drawString(months[i], x-8, h-10);
            }
            int maxIncome = java.util.Arrays.stream(pemasukan).max().orElse(1);
            int maxExpense = java.util.Arrays.stream(pengeluaran).max().orElse(1);
            int max = Math.max(1, Math.max(maxIncome, maxExpense));
            drawSeries(g2, pemasukan, new Color(5,150,105), padL,padR,padT,padB,w,h,max);
            drawSeries(g2, pengeluaran, new Color(220,38,38), padL,padR,padT,padB,w,h,max);
            int ly = padT+10; int lx = w - padR - 160;
            drawLegend(g2, lx, ly, new Color(5,150,105), "Pemasukan");
            drawLegend(g2, lx+90, ly, new Color(220,38,38), "Pengeluaran");
            g2.dispose();
        }
        private void drawSeries(Graphics2D g2, int[] data, Color col, int padL,int padR,int padT,int padB,int w,int h,int max){
            if(data.length == 0) return;
            g2.setColor(col); g2.setStroke(new BasicStroke(2f));
            Path2D p = new Path2D.Double();
            for(int i=0;i<data.length;i++){
                double norm = Math.max(0, Math.min(1, data[i]/(double)max));
                int x = padL + (w-padL-padR) * (data.length == 1 ? 0 : i) / Math.max(1, data.length-1);
                int y = padT + (int)((1-norm) * (h-padT-padB));
                if(i==0) p.moveTo(x,y); else p.lineTo(x,y);
            }
            g2.draw(p);
        }
        private void drawLegend(Graphics2D g2, int x, int y, Color col, String name){
            g2.setColor(col); g2.fillRect(x,y-8,14,3);
            g2.setColor(new Color(30,41,59)); g2.drawString(name, x+18, y);
        }
    }

    // Pie chart for categories
    static class CategoryPieChartPanel extends JPanel {
        static class Slice { String name; double value; Color color; Slice(String n,double v,Color c){name=n;value=v;color=c;} }
        private List<Slice> slices = new ArrayList<>();

        void setSlices(List<Slice> slices){
            this.slices = slices == null ? new ArrayList<>() : new ArrayList<>(slices);
            repaint();
        }

        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            int size = Math.min(w,h) - 40; int cx = w/2 - size/2; int cy = h/2 - size/2;
            double total = slices.stream().mapToDouble(s->s.value).sum();
            if (total <= 0) {
                g2.setColor(new Color(148,163,184));
                g2.drawString("Belum ada pengeluaran bulan ini", 16, 20);
                g2.dispose();
                return;
            }
            double start = 90; // start at top
            for(Slice s : slices){
                double extent = -360 * (s.value/total);
                g2.setColor(s.color);
                g2.fill(new Arc2D.Double(cx, cy, size, size, start, extent, Arc2D.PIE));
                start += extent;
            }
            int lx = 10, ly = 10;
            for(Slice s : slices){
                g2.setColor(s.color); g2.fillRect(lx, ly, 12, 12);
                g2.setColor(new Color(30,41,59)); g2.drawString(s.name, lx+18, ly+11);
                ly += 18;
            }
            g2.dispose();
        }
    }
}
