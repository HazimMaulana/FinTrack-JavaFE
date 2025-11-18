import javax.swing.*;
import java.awt.*;
import utils.FontUtil;

public class Main {
    private static final String HOME = "HOME";
    private static final String SETTINGS = "SETTINGS";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUI);
    }

    private static void createAndShowUI() {
        // Apply global font family chain (Swing equivalent of JavaFX label.setStyle)
        FontUtil.applyGlobalFont("Segoe UI", "San Francisco", "SF Pro Text", "Ubuntu", "Noto Sans", "Arial", "SansSerif");

        JFrame frame = new JFrame("Reusable Sidebar Demo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1280, 700);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        frame.setContentPane(root);

        // Header stack: TopBar + InfoBar + ActionBar (stacked vertically)
        TopBar topBar = new TopBar();
        InfoBar infoBar = new InfoBar();
        infoBar.setTotals(0, 0, 0); // placeholder values; update as needed
        ActionBar actionBar = new ActionBar();

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(topBar);
        header.add(infoBar);
        header.add(actionBar);
        root.add(header, BorderLayout.NORTH);

        // Content area with CardLayout to switch pages
        JPanel content = new JPanel(new CardLayout());
        content.add(new HomePage(), HOME);
        content.add(new SettingsPage(), SETTINGS);

        // Sidebar with navigation callback to switch cards
        Sidebar sidebar = new Sidebar(route -> switchTo(content, route));

        root.add(sidebar, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        frame.setVisible(true);
        // Ensure default is HOME
        switchTo(content, HOME);
    }

    private static void switchTo(JPanel content, String route) {
        CardLayout cl = (CardLayout) content.getLayout();
        cl.show(content, route);
    }
}
