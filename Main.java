import javax.swing.*;
import java.awt.*;
import utils.FontUtil;

public class Main {
    private static final String DASHBOARD = "dashboard";
    private static final String TRANSAKSI = "transaksi";
    private static final String LAPORAN = "laporan";
    private static final String AKUN = "akun";
    private static final String KATEGORI = "kategori";
    private static final String PENGATURAN = "pengaturan";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUI);
    }

    private static void createAndShowUI() {
        // Apply global font family chain (Swing equivalent of JavaFX label.setStyle)
        FontUtil.applyGlobalFont("Segoe UI", "San Francisco", "SF Pro Text", "Ubuntu", "Noto Sans", "Arial", "SansSerif");

        JFrame frame = new JFrame("FinTrack - Financial Tracker");
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
        content.add(new HomePage(), DASHBOARD);
        content.add(new TransaksiPage(), TRANSAKSI);
        content.add(new LaporanPage(), LAPORAN);
        content.add(new AkunWalletPage(), AKUN);
        content.add(new KategoriPage(), KATEGORI);
        content.add(new SettingsPage(), PENGATURAN);

        // Sidebar with navigation callback to switch cards
        Sidebar sidebar = new Sidebar(route -> switchTo(content, route));

        root.add(sidebar, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        frame.setVisible(true);
        // Ensure default is Dashboard
        switchTo(content, DASHBOARD);
    }

    private static void switchTo(JPanel content, String route) {
        CardLayout cl = (CardLayout) content.getLayout();
        cl.show(content, route);
    }
}
