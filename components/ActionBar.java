import javax.swing.*;
import java.awt.*;

public class ActionBar extends JPanel {
    private int barHeight = 50;

    public ActionBar() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));

        JButton newTxn = new RoundedButton("+ Transaksi Baru", new Color(33,150,243), new Color(25,118,210));
        newTxn.setForeground(Color.WHITE);

        JSeparator vSep = new JSeparator(SwingConstants.VERTICAL);
        vSep.setMaximumSize(new Dimension(1, 22));
        vSep.setForeground(new Color(210,210,210));

        JButton calendarBtn = new RoundedButton(null, Color.WHITE, new Color(200,200,200));
        calendarBtn.setFocusPainted(false);
        calendarBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calendarBtn.setToolTipText("Kalender");
        calendarBtn.setIcon(new CalendarIcon(18, 18, new Color(80,80,80)));

        PlaceholderTextField search = new PlaceholderTextField("Cari Transaksi");
        search.setMaximumSize(new Dimension(260, 32));
        search.setPreferredSize(new Dimension(260, 32));

        JButton refreshBtn = new RoundedButton(null, Color.WHITE, new Color(200,200,200));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.setToolTipText("Reload");
        refreshBtn.setIcon(new RefreshIcon(18, 18, new Color(80,80,80)));

        JButton exportBtn = new RoundedButton("Export", Color.WHITE, new Color(200,200,200));
        exportBtn.setForeground(new Color(60,60,60));

        left.add(Box.createHorizontalStrut(10));
        left.add(newTxn);
        left.add(Box.createHorizontalStrut(12));
        left.add(vSep);
        left.add(Box.createHorizontalStrut(12));
        left.add(calendarBtn);
        left.add(Box.createHorizontalStrut(8));
        left.add(search);
        left.add(Box.createHorizontalStrut(8));
        left.add(refreshBtn);
        left.add(Box.createHorizontalStrut(8));
        left.add(exportBtn);
        left.add(Box.createHorizontalStrut(10));

        add(left, BorderLayout.WEST);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = barHeight;
        return d;
    }

    public void setBarHeight(int h) {
        this.barHeight = h;
        revalidate();
        repaint();
    }

    // styling helpers no longer needed; rounded painting handled in RoundedButton

    static class RoundedButton extends JButton {
        private final Color fill;
        private final Color stroke;
        private final int arc = 12;
        RoundedButton(String text, Color fill, Color stroke){
            super(text);
            this.fill = fill;
            this.stroke = stroke;
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            // Fill
            Color bg = (fill != null) ? fill : getBackground();
            if (bg == null) bg = Color.WHITE;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w-1, h-1, arc, arc);
            // Border
            Color br = (stroke != null) ? stroke : new Color(200,200,200);
            g2.setColor(br);
            g2.drawRoundRect(0, 0, w-1, h-1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class PlaceholderTextField extends JTextField {
        private final String placeholder;
        PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(150,150,150));
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                Insets in = getInsets();
                g2.drawString(placeholder, in.left + 4, getHeight()/2 + g2.getFontMetrics().getAscent()/2 - 2);
                g2.dispose();
            }
        }
    }

    static class CalendarIcon implements Icon {
        private final int w,h; private final Color c;
        CalendarIcon(int w, int h, Color c){ this.w=w; this.h=h; this.c=c; }
        @Override public void paintIcon(Component cpt, Graphics g, int x, int y) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c);
            int bw=w-2, bh=h-3; int ox=x+1, oy=y+1;
            g2.drawRect(ox, oy+2, bw, bh-2);
            g2.fillRect(ox, oy+2, bw, 4);
            g2.fillRect(ox+3, oy, 2, 3);
            g2.fillRect(ox+bw-5, oy, 2, 3);
            g2.drawLine(ox, oy+8, ox+bw, oy+8);
            g2.dispose();
        }
        @Override public int getIconWidth(){ return w; }
        @Override public int getIconHeight(){ return h; }
    }

    static class RefreshIcon implements Icon {
        private final int w,h; private final Color c;
        RefreshIcon(int w, int h, Color c){ this.w=w; this.h=h; this.c=c; }
        @Override public void paintIcon(Component cpt, Graphics g, int x, int y) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c);
            int r = Math.min(w,h)/2 - 2;
            int cx = x + w/2; int cy = y + h/2;
            g2.drawArc(cx-r, cy-r, 2*r, 2*r, 30, 260);
            int ax = cx + (int)(Math.cos(Math.toRadians(30))*r);
            int ay = cy - (int)(Math.sin(Math.toRadians(30))*r);
            Polygon p = new Polygon();
            p.addPoint(ax, ay);
            p.addPoint(ax-5, ay+2);
            p.addPoint(ax-1, ay+6);
            g2.fillPolygon(p);
            g2.dispose();
        }
        @Override public int getIconWidth(){ return w; }
        @Override public int getIconHeight(){ return h; }
    }
}
