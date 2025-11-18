import javax.swing.*;
import java.awt.*;

public class SettingsPage extends JPanel {
    public SettingsPage() {
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Settings");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        panel.add(title);
        panel.add(Box.createVerticalStrut(16));

        panel.add(new JLabel("Dark Mode:"));
        JCheckBox darkMode = new JCheckBox("Enable");
        panel.add(darkMode);

        panel.add(Box.createVerticalStrut(12));
        panel.add(new JLabel("Username:"));
        JTextField username = new JTextField(20);
        panel.add(username);

        panel.add(Box.createVerticalStrut(12));
        JButton save = new JButton("Save");
        panel.add(save);

        add(panel, BorderLayout.NORTH);
    }
}
