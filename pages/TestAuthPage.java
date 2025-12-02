import javax.swing.*;

/**
 * Simple test to verify AuthPage UI renders correctly.
 */
public class TestAuthPage {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AuthPage Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            
            // Create AuthPage with test callback
            AuthPage authPage = new AuthPage(() -> {
                System.out.println("Login success callback triggered!");
                JOptionPane.showMessageDialog(frame, "Login successful!");
            });
            
            frame.add(authPage);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            System.out.println("AuthPage test window opened successfully");
            System.out.println("Test the following:");
            System.out.println("1. UI layout renders correctly");
            System.out.println("2. Toggle between login and register modes");
            System.out.println("3. Form validation (empty fields)");
            System.out.println("4. Login/Register buttons work");
        });
    }
}
