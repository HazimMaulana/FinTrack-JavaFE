import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPage extends JPanel {
    public SettingsPage() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(0, 16, 16, 16));
        add(root, BorderLayout.CENTER);

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 12, 16, 16));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Pengaturan");
        title.setForeground(color(30, 41, 59));
        title.setFont(title.getFont().deriveFont(Font.PLAIN, title.getFont().getSize2D() + 6f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Kelola preferensi dan konfigurasi aplikasi");
        subtitle.setForeground(color(71, 85, 105));
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Settings cards
        RoundPanel accountCard = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        accountCard.setLayout(new BorderLayout());
        accountCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        accountCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel cardTitle = new JLabel("Profil Pengguna");
        cardTitle.setForeground(color(30, 41, 59));
        cardTitle.setFont(cardTitle.getFont().deriveFont(Font.PLAIN, 16f));
        cardTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        accountCard.add(cardTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);

        formFields.add(createFormField("Nama Lengkap", new JTextField("John Doe")));
        formFields.add(Box.createVerticalStrut(12));
        formFields.add(createFormField("Email", new JTextField("john.doe@example.com")));
        formFields.add(Box.createVerticalStrut(12));
        formFields.add(createFormField("Username", new JTextField("johndoe")));
        formFields.add(Box.createVerticalStrut(16));

        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkPanel.setOpaque(false);
        JCheckBox darkMode = new JCheckBox("Enable Dark Mode");
        darkMode.setOpaque(false);
        darkMode.setForeground(color(71, 85, 105));
        checkPanel.add(darkMode);
        formFields.add(checkPanel);

        accountCard.add(formFields, BorderLayout.CENTER);

        JButton saveBtn = new RoundedButton("Simpan Perubahan", true);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
        btnPanel.add(saveBtn);
        accountCard.add(btnPanel, BorderLayout.SOUTH);

        root.add(accountCard);
        root.add(Box.createVerticalStrut(16));

        // Notification settings
        RoundPanel notifCard = new RoundPanel(10, Color.WHITE, color(226, 232, 240));
        notifCard.setLayout(new BorderLayout());
        notifCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        notifCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel notifTitle = new JLabel("Notifikasi");
        notifTitle.setForeground(color(30, 41, 59));
        notifTitle.setFont(notifTitle.getFont().deriveFont(Font.PLAIN, 16f));
        notifTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        notifCard.add(notifTitle, BorderLayout.NORTH);

        JPanel notifOptions = new JPanel();
        notifOptions.setLayout(new BoxLayout(notifOptions, BoxLayout.Y_AXIS));
        notifOptions.setOpaque(false);
        
        JCheckBox emailNotif = new JCheckBox("Email notifications");
        emailNotif.setOpaque(false);
        emailNotif.setForeground(color(71, 85, 105));
        JCheckBox pushNotif = new JCheckBox("Push notifications");
        pushNotif.setOpaque(false);
        pushNotif.setForeground(color(71, 85, 105));
        JCheckBox smsNotif = new JCheckBox("SMS notifications");
        smsNotif.setOpaque(false);
        smsNotif.setForeground(color(71, 85, 105));

        notifOptions.add(emailNotif);
        notifOptions.add(Box.createVerticalStrut(8));
        notifOptions.add(pushNotif);
        notifOptions.add(Box.createVerticalStrut(8));
        notifOptions.add(smsNotif);

        notifCard.add(notifOptions, BorderLayout.CENTER);
        root.add(notifCard);
        root.add(Box.createVerticalGlue());
    }

    private JPanel createFormField(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(color(71, 85, 105));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        return panel;
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
