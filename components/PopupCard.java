import javax.swing.*;
import java.awt.*;

public class PopupCard extends JPopupMenu {
    private final int arc = 12;

    public PopupCard(JComponent content) {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());

        JPanel wrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // Background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
                // Border
                g2.setColor(new Color(210, 210, 210));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        wrapper.add(content, BorderLayout.CENTER);
        add(wrapper);
    }

    public void showBelow(Component invoker, int gap) {
        show(invoker, 0, invoker.getHeight() + gap);
    }
}
