import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TopBar extends JPanel {
    private final Font topFont;
    private final Runnable onLogout;

    public TopBar() {
        this(null);
    }

    public TopBar(Runnable onLogout) {
        this.onLogout = onLogout;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        Font baseButtonFont = UIManager.getFont("Button.font");
        if (baseButtonFont == null) baseButtonFont = new JButton().getFont();
        topFont = baseButtonFont.deriveFont(Font.PLAIN, baseButtonFont.getSize2D() + 2f);

        // Left: Flat buttons acting as menu headers (File, View, Edit, Tools, Help)
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtons.setOpaque(false);

        JButton btnFile = makeTopButton("File", () -> createMenuContent(simpleMenu("New", "Open", "Save", "-", "Exit")));
        JButton btnView = makeTopButton("View", () -> createMenuContent(simpleMenu("Toggle Sidebar", "Refresh")));
        JButton btnEdit = makeTopButton("Edit", () -> createMenuContent(simpleMenu("Undo", "Redo", "-", "Preferences")));
        JButton btnTools = makeTopButton("Tools", () -> createMenuContent(simpleMenu("Import", "Export")));
        JButton btnHelp = makeTopButton("Help", () -> createMenuContent(simpleMenu("Documentation", "About")));

        leftButtons.add(btnFile);
        leftButtons.add(btnView);
        leftButtons.add(btnEdit);
        leftButtons.add(btnTools);
        leftButtons.add(btnHelp);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(leftButtons, BorderLayout.CENTER);

        // Center: App title (match button font size, not bold)
        JLabel title = new JLabel("FinTrack - Financial Tracker", SwingConstants.CENTER);
        title.setFont(topFont);
        // Match button vertical padding so text sits on one line
        title.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        // Right: Profile button
        JButton profileBtn = new JButton("Profile");
        profileBtn.setFont(topFont);
        profileBtn.setFocusPainted(false);
        profileBtn.setBorderPainted(false);
        profileBtn.setContentAreaFilled(false);
        profileBtn.setOpaque(false);
        profileBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        profileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        installHoverEffect(profileBtn);
        profileBtn.addActionListener(e -> showPopupBelow(profileBtn,
            () -> createMenuContent(profileMenuItems())));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(profileBtn);

        add(left, BorderLayout.WEST);
        add(title, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        // Let layout compute preferred height based on contents
    }

    private JButton makeTopButton(String text, Supplier<JComponent> contentSupplier) {
        JButton b = new JButton(text);
        b.setFont(topFont);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        installHoverEffect(b);
        b.addActionListener(e -> showPopupBelow(b, contentSupplier));
        return b;
    }

    private void installHoverEffect(AbstractButton btn) {
        final Color hover = new Color(235, 235, 235);
        btn.setRolloverEnabled(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBackground(new Color(0, 0, 0, 0));
            }
        });
    }

    private void showPopupBelow(JComponent invoker, Supplier<JComponent> contentSupplier) {
        int gap = 3; // space between button and modal card
        JComponent content = contentSupplier.get();
        PopupCard card = new PopupCard(content);
        card.showBelow(invoker, gap);
    }

    private List<MenuItem> simpleMenu(String... items) {
        List<MenuItem> list = new ArrayList<>();
        for (String it : items) {
            if ("-".equals(it)) {
                list.add(MenuItem.divider());
            } else {
                String copy = it;
                list.add(MenuItem.action(copy, () -> System.out.println("Clicked: " + copy)));
            }
        }
        return list;
    }

    private List<MenuItem> profileMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(MenuItem.action("View Profile", () -> System.out.println("Clicked: View Profile")));
        items.add(MenuItem.action("Settings", () -> System.out.println("Clicked: Settings")));
        items.add(MenuItem.divider());
        items.add(MenuItem.action("Logout", () -> {
            if (onLogout != null) onLogout.run();
        }));
        return items;
    }

    private JComponent createMenuContent(List<MenuItem> items) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        java.util.List<JButton> added = new java.util.ArrayList<>();
        for (MenuItem it : items) {
            if (it.divider) {
                JSeparator sep = new JSeparator();
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
                panel.add(Box.createVerticalStrut(4));
                panel.add(sep);
                panel.add(Box.createVerticalStrut(4));
                continue;
            }
            JButton itemBtn = new JButton(it.label);
            itemBtn.setHorizontalAlignment(SwingConstants.LEFT);
            itemBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemBtn.setFocusPainted(false);
            itemBtn.setBorderPainted(false);
            itemBtn.setContentAreaFilled(false);
            itemBtn.setOpaque(false);
            itemBtn.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            itemBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            // Hover style
            itemBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                final Color hover = new Color(245, 245, 245);
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    itemBtn.setOpaque(true);
                    itemBtn.setContentAreaFilled(true);
                    itemBtn.setBackground(hover);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    itemBtn.setOpaque(false);
                    itemBtn.setContentAreaFilled(false);
                    itemBtn.setBackground(new Color(0,0,0,0));
                }
            });
            // Placeholder action
            itemBtn.addActionListener(e -> {
                // Close the popup by finding the ancestor JPopupMenu
                JPopupMenu pop = (JPopupMenu) SwingUtilities.getAncestorOfClass(JPopupMenu.class, itemBtn);
                if (pop != null) pop.setVisible(false);
                if (it.action != null) {
                    it.action.run();
                } else {
                    System.out.println("Clicked: " + it.label);
                }
            });
            panel.add(itemBtn);
            added.add(itemBtn);
        }
        // Normalize width so popup isn't tiny; enforce a sensible minimum width
        int maxW = 0;
        for (JButton b : added) {
            maxW = Math.max(maxW, b.getPreferredSize().width);
        }
        maxW = Math.max(maxW, 200);
        int rowH = Math.max(28, (int) (topFont.getSize2D() + 10));
        for (JButton b : added) {
            Dimension d = new Dimension(maxW, rowH);
            b.setPreferredSize(d);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowH));
            b.setMinimumSize(new Dimension(120, rowH));
        }
        return panel;
    }

    private static class MenuItem {
        final String label;
        final Runnable action;
        final boolean divider;

        private MenuItem(String label, Runnable action, boolean divider) {
            this.label = label;
            this.action = action;
            this.divider = divider;
        }

        static MenuItem action(String label, Runnable action) {
            return new MenuItem(label, action, false);
        }

        static MenuItem divider() {
            return new MenuItem("-", null, true);
        }
    }
}
