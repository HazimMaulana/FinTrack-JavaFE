package utils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public final class ScrollUtil {
    private ScrollUtil() {}

    public static void apply(JScrollPane sp){
        if (sp == null) return;
        if (sp.getVerticalScrollBar() != null) {
            sp.getVerticalScrollBar().setUnitIncrement(16);
            sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        }
        if (sp.getHorizontalScrollBar() != null) {
            sp.getHorizontalScrollBar().setUnitIncrement(16);
            sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        private final Color track = new Color(248, 250, 252);
        private final Color thumb = new Color(203, 213, 225);
        private final Color thumbHover = new Color(148, 163, 184);

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(track);
            g2.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!scrollbar.isEnabled() || thumbBounds.width <= 0 || thumbBounds.height <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = isThumbRollover() ? thumbHover : thumb;
            g2.setColor(fill);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 10, 10);
            g2.dispose();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

        private JButton zeroButton(){
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0,0));
            btn.setMinimumSize(new Dimension(0,0));
            btn.setMaximumSize(new Dimension(0,0));
            return btn;
        }
    }
}
