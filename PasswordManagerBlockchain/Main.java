import backend.auth.AuthService;
import backend.blockchain.Block;
import backend.blockchain.Blockchain;
import backend.db.DBConnection;
import backend.db.LogDAO;
import backend.db.UserDAO;
import frontend.DashboardUI;
import frontend.LoginUI;
import frontend.SignupUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Main {
    private static JFrame frame;
    private static JPanel cardPanel;
    private static CardLayout cardLayout;

    public static void main(String[] args) {
        // Enforce Native Windows OS Visuals aggressively
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        DBConnection.initializeDatabase();
        
        UserDAO userDAO = new UserDAO();
        LogDAO logDAO = new LogDAO();
        Blockchain blockchain = new Blockchain();
        
        ArrayList<Block> savedBlocks = userDAO.loadAllBlocks();
        if (savedBlocks.size() > 0) {
            blockchain.chain = savedBlocks;
        }

        AuthService authService = new AuthService(blockchain, userDAO, logDAO);
        
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Decentralized Vault (Zero-Knowledge Architecture)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null); 

            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);

            Runnable onLoginSuccess = () -> {
                // Hotload Dashboard context via latest user session
                DashboardUI dashboardUI = new DashboardUI(authService, blockchain, logDAO, userDAO, () -> {
                    authService.logout();
                    cardLayout.show(cardPanel, "Login");
                });
                cardPanel.add(dashboardUI, "Dashboard");
                cardLayout.show(cardPanel, "Dashboard");
            };

            Runnable onGoToSignup = () -> cardLayout.show(cardPanel, "Signup");
            Runnable onGoToLogin = () -> cardLayout.show(cardPanel, "Login");

            LoginUI loginUI = new LoginUI(authService, onLoginSuccess, onGoToSignup);
            SignupUI signupUI = new SignupUI(authService, onGoToLogin);

            cardPanel.add(loginUI, "Login");
            cardPanel.add(signupUI, "Signup");

            frame.add(cardPanel);
            frame.setVisible(true);
        });
    }
}
