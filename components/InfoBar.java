import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InfoBar extends JPanel {
    private final JLabel dateTimeLabel;
    private final JLabel totalSaldoLabel;
    private final JLabel pemasukanLabel;
    private final JLabel pengeluaranLabel;
    private final JButton notifButton;
    private final NumberFormat currency;

    public InfoBar() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(new Color(240, 240, 240));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        // Fonts and padding
        Font base = UIManager.getFont("Label.font");
        if (base == null) base = new JLabel().getFont();
        Font f = base.deriveFont(Font.PLAIN, base.getSize2D());

        // Left: calendar icon + date + time
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(f);
        dateTimeLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 12));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        JComponent calIcon = new OriginalCalendarIcon(24, 24);
        calIcon.setAlignmentY(Component.CENTER_ALIGNMENT);
        dateTimeLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        left.add(Box.createHorizontalStrut(12));
        left.add(calIcon);
        left.add(Box.createHorizontalStrut(8));
        left.add(dateTimeLabel);

        // Right: totals + notif
        totalSaldoLabel = pillLabel("Saldo: -", f);
        pemasukanLabel = pillLabel("Pemasukan: -", f);
        pengeluaranLabel = pillLabel("Pengeluaran: -", f);

        notifButton = new JButton("🔔");
        notifButton.setFocusPainted(false);
        notifButton.setBorderPainted(false);
        notifButton.setContentAreaFilled(false);
        notifButton.setOpaque(false);
        notifButton.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 12));
        notifButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setOpaque(false);
        totalSaldoLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        pemasukanLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        pengeluaranLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        notifButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        right.add(totalSaldoLabel);
        right.add(Box.createHorizontalStrut(8));
        right.add(pemasukanLabel);
        right.add(Box.createHorizontalStrut(8));
        right.add(pengeluaranLabel);
        right.add(Box.createHorizontalStrut(8));
        right.add(notifButton);
        right.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 12));
        add(left);
        add(Box.createHorizontalGlue());
        add(right);

        // Currency formatter for Indonesia
        currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

        // Update date/time every second
        Timer t = new Timer(1000, e -> refreshDateTime());
        t.setInitialDelay(0);
        t.start();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension barHeight = super.getPreferredSize();
        barHeight.height = 55; // fixed height for the InfoBar
        return barHeight;
    }

    private JLabel pillLabel(String text, Font f) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setOpaque(true);
        l.setBackground(new Color(240, 240, 240));
        l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return l;
    }

    private void refreshDateTime() {
        LocalDateTime now = LocalDateTime.now();
        // Example: 19 Nov 2025 • 14:05:09
        String date = now.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id")));
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        dateTimeLabel.setText(date + " • " + time);
    }

    public void setTotals(double saldo, double pemasukan, double pengeluaran) {
        totalSaldoLabel.setText("Saldo: " + currency.format(saldo));
        pemasukanLabel.setText("Pemasukan: " + currency.format(pemasukan));
        pengeluaranLabel.setText("Pengeluaran: " + currency.format(pengeluaran));
    }

    public JButton getNotifButton() { return notifButton; }
}
class OriginalCalendarIcon extends JComponent {
    private final int w;
    private final int h;
    private final Color color;
    OriginalCalendarIcon(int w, int h) {
        this.w = w; this.h = h;
        setPreferredSize(new Dimension(w, h));
        setMinimumSize(new Dimension(w, h));
        setMaximumSize(new Dimension(w, h));
        setOpaque(false);
        // Use label foreground to emulate currentColor
        color = UIManager.getColor("Label.foreground");
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color != null ? color : Color.DARK_GRAY);
        // Recreate original SVG path roughly with primitives (border, header line, inner separation)
        int bw = w - 2; // body width
        int bh = h - 4; // body height
        int x = 1;
        int y = 3;
        int headerH = 7;
        // Outer rectangle
        g2.drawRect(x, y, bw, bh);
        // Header fill
        g2.fillRect(x, y, bw, headerH);
        // Two top pegs (simulate the separate top bars for date cells)
        int pegW = 3; int pegH = 3;
        g2.fillRect(x + 4, y - 3, pegW, pegH);
        g2.fillRect(x + bw - 4 - pegW, y - 3, pegW, pegH);
        // Inner horizontal separator for the row after header (similar to second segment of path)
        g2.drawLine(x, y + headerH + 5, x + bw, y + headerH + 5);
        // Simulate bottom section (no fill, just keep border)
        g2.dispose();
    }
}
