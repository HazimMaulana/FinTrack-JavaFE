package utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public final class ComboUtil {
    private ComboUtil() {}

    public static void apply(JComboBox<?> combo) {
        combo.setUI(new ModernComboBoxUI());
        combo.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(51, 65, 85));
        combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value == null ? "" : value.toString());
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(6, 8, 6, 8));
            if (isSelected) {
                lbl.setBackground(new Color(239, 246, 255));
                lbl.setForeground(new Color(37, 99, 235));
            } else {
                lbl.setBackground(Color.WHITE);
                lbl.setForeground(new Color(51, 65, 85));
            }
            return lbl;
        });
    }

    private static class ModernComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            return new ArrowButton();
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
            g2.dispose();
        }
    }

    private static class ArrowButton extends JButton {
        private final Color bg = new Color(248, 250, 252);
        private final Color bgHover = new Color(241, 245, 249);
        private final Color fg = new Color(100, 116, 139);

        ArrowButton() {
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(false);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? bgHover : bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            int w = 8, h = 5;
            int x = getWidth() / 2 - w / 2;
            int y = getHeight() / 2 - h / 2;
            Polygon p = new Polygon();
            p.addPoint(x, y);
            p.addPoint(x + w, y);
            p.addPoint(x + w / 2, y + h);
            g2.setColor(fg);
            g2.fillPolygon(p);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(24, 24);
        }
    }
}
