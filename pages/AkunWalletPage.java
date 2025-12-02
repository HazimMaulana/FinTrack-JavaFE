import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import utils.ScrollUtil;

public class AkunWalletPage extends JPanel {
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
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Summary Cards
        JPanel summaryCards = new JPanel(new GridLayout(1, 4, 12, 12));
        summaryCards.setOpaque(false);
        summaryCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        summaryCards.add(buildSummaryCard("Total Saldo", "Rp 59.900.000", color(37, 99, 235)));
        summaryCards.add(buildSummaryCard("Akun Bank", "3 akun", color(5, 150, 105)));
        summaryCards.add(buildSummaryCard("Dompet Digital", "2 wallet", color(245, 158, 11)));
        summaryCards.add(buildSummaryCard("Cash", "Rp 2.150.000", color(100, 116, 139)));
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

        JPanel accountsGrid = new JPanel(new GridLayout(0, 2, 16, 16));
        accountsGrid.setOpaque(false);

        // Bank accounts
        accountsGrid.add(buildAccountCard("BK", "BCA", "****1234", 45_250_000, color(37, 99, 235)));
        accountsGrid.add(buildAccountCard("BK", "Mandiri", "****5678", 12_500_000, color(5, 150, 105)));
        accountsGrid.add(buildAccountCard("GP", "GoPay", "0812****5678", 1_500_000, new Color(0, 168, 89)));
        accountsGrid.add(buildAccountCard("OV", "OVO", "0812****5678", 800_000, new Color(75, 0, 130)));
        accountsGrid.add(buildAccountCard("CA", "Cash", "Tunai", 2_150_000, color(100, 116, 139)));

        JScrollPane accountsScroll = new JScrollPane(accountsGrid);
        accountsScroll.getViewport().setOpaque(false);
        accountsScroll.setOpaque(false);
        accountsScroll.setBorder(null);
        ScrollUtil.apply(accountsScroll);
        accountsCard.add(accountsScroll, BorderLayout.CENTER);

        root.add(accountsCard);
        root.add(Box.createVerticalGlue());
    }

    private JPanel buildAccountCard(String icon, String name, String number, int balance, Color accent) {
        RoundPanel card = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(0, 140));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        iconPanel.setOpaque(false);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(iconLbl.getFont().deriveFont(Font.BOLD, 16f));
        iconLbl.setForeground(accent);
        iconLbl.setBorder(new EmptyBorder(8, 12, 8, 12));
        iconPanel.add(iconLbl);
        top.add(iconPanel, BorderLayout.WEST);

        JButton editBtn = new JButton("...");
        editBtn.setFont(editBtn.getFont().deriveFont(16f));
        editBtn.setForeground(color(100, 116, 139));
        editBtn.setBorderPainted(false);
        editBtn.setContentAreaFilled(false);
        editBtn.setFocusPainted(false);
        top.add(editBtn, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(12, 0, 0, 0));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setForeground(color(30, 41, 59));
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 16f));
        nameLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel numberLbl = new JLabel(number);
        numberLbl.setForeground(color(100, 116, 139));
        numberLbl.setBorder(new EmptyBorder(4, 0, 12, 0));
        numberLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel balanceLbl = new JLabel("Rp " + String.format("%,d", balance).replace(',', '.'));
        balanceLbl.setForeground(accent);
        balanceLbl.setFont(balanceLbl.getFont().deriveFont(Font.BOLD, 20f));
        balanceLbl.setAlignmentX(LEFT_ALIGNMENT);

        content.add(nameLbl);
        content.add(numberLbl);
        content.add(balanceLbl);

        card.add(content, BorderLayout.CENTER);
        return card;
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
