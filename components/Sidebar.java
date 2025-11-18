import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Sidebar extends JPanel {
    private final Consumer<String> onNavigate;
    private final Map<String, NavButton> buttons = new LinkedHashMap<>();
    private String activeKey = null;

    public Sidebar(Consumer<String> onNavigate) {
        this(onNavigate, defaultMenu());
    }

    public Sidebar(Consumer<String> onNavigate, Map<String, String> menuItems) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(226, 232, 240))); // slate-200 on right
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(200, 400));

        // Header (p-4 with bottom border)
        JPanel header = new JPanel();
        header.setOpaque(true);
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,new Color(226,232,240)),
            BorderFactory.createEmptyBorder(12,12,12,12)
        ));
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        JLabel badge = new JLabel(new WalletBadgeIcon(20, new Color(37,99,235))); // blue-600
        badge.setBorder(BorderFactory.createEmptyBorder(0,0,0,8));
        JLabel brand = new JLabel("FinTrack");
        brand.setForeground(new Color(30,41,59)); // slate-800
        header.add(badge);
        header.add(brand);
        add(header, BorderLayout.NORTH);

        // List container to force full-width rows
        JPanel list = new JPanel(new GridBagLayout());
        list.setOpaque(false);
        add(list, BorderLayout.CENTER);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.PAGE_START;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.insets = new Insets(4, 0, 4, 0); // no horizontal gap; full-bleed buttons

        for (Map.Entry<String, String> entry : menuItems.entrySet()) {
            String key = entry.getKey();
            String label = entry.getValue();
            NavButton btn = createNavButton(label);
            btn.addActionListener(e -> {
                if (onNavigate != null) onNavigate.accept(key);
                setActive(key);
            });
            buttons.put(key, btn);
            gc.gridy = list.getComponentCount();
            list.add(btn, gc);
        }

        // Spacer to push items to top and keep layout tidy
        gc.gridy = list.getComponentCount();
        gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        list.add(spacer, gc);

        // Set first as active by default
        if (!buttons.isEmpty()) setActive(buttons.keySet().iterator().next());
    }

    private static Map<String, String> defaultMenu() {
        Map<String, String> menu = new LinkedHashMap<>();
        menu.put("dashboard", "Dashboard");
        menu.put("transaksi", "Transaksi");
        menu.put("laporan", "Laporan");
        menu.put("akun", "Akun & Wallet");
        menu.put("kategori", "Kategori");
        menu.put("pengaturan", "Pengaturan");
        return menu;
    }

    private NavButton createNavButton(String text) {
        NavButton btn = new NavButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    public void setActive(String key) {
        activeKey = key;
        buttons.forEach((k,b) -> b.setActive(k.equals(key)));
        repaint();
    }

    // Minimal wallet badge (blue circle with white wallet glyph)
    static class WalletBadgeIcon implements Icon {
        private final int size; private final Color bg;
        WalletBadgeIcon(int size, Color bg){ this.size=size; this.bg=bg; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillOval(x, y, size, size);
            // wallet glyph
            int pad=5; int w=size-2*pad; int h=size-2*pad-3; int ox=x+pad; int oy=y+pad+2;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(ox, oy, w, h, 6,6);
            g2.setColor(bg.darker());
            g2.drawRoundRect(ox, oy, w, h, 6,6);
            g2.fillRoundRect(ox+w-6, oy+h/2-2, 4, 4, 3,3); // clasp
            g2.dispose();
        }
        @Override public int getIconWidth(){ return size; }
        @Override public int getIconHeight(){ return size; }
    }

    // Sidebar button with hover/active styles and rounded background
    static class NavButton extends JButton {
        private boolean hover=false; private boolean active=false;
        private final Color hoverBg = new Color(241,245,249); // slate-100
        private final Color activeBg = new Color(239,246,255); // blue-50
        private final Color activeFg = new Color(37,99,235); // blue-600
        private final int arc=10;
        private final int fixedHeight = 48; // taller buttons
        NavButton(String text){
            super(text);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setForeground(new Color(51,65,85)); // slate-700
            Font f = UIManager.getFont("Button.font");
            if (f == null) f = getFont();
            setFont(f.deriveFont(Font.PLAIN, f.getSize2D() + 2f));
            addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){ hover=true; repaint(); }
                @Override public void mouseExited(MouseEvent e){ hover=false; repaint(); }
            });
        }
        void setActive(boolean a){ this.active=a; setForeground(a?activeFg:new Color(51,65,85)); }
        @Override public Dimension getPreferredSize(){
            Dimension d = super.getPreferredSize();
            d.height = Math.max(d.height, fixedHeight);
            return d;
        }
        @Override public Dimension getMaximumSize(){
            Dimension d = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, d.height);
        }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            if(active){
                g2.setColor(activeBg);
                g2.fillRoundRect(0, 2, w-1, h-4, arc, arc);
            } else if(hover){
                g2.setColor(hoverBg);
                g2.fillRoundRect(0, 2, w-1, h-4, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
