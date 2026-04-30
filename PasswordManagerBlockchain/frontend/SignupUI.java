package frontend;

import backend.auth.AuthService;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class SignupUI extends JPanel {
    public SignupUI(AuthService authService, Runnable onGoToLogin) {
        setLayout(new GridBagLayout());
        
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextField userField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JButton signupBtn = new JButton("Generate ECDSA Identity");
        JButton backBtn = new JButton("Back to Login");

        panel.add(new JLabel("Desired Global Identity (Username):"));
        panel.add(userField);
        panel.add(new JLabel("Master Password (>=8, uppercase, number, symbol):"));
        panel.add(passField);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        add(panel, gbc);
        
        JPanel btnPanel = new JPanel();
        btnPanel.add(signupBtn);
        btnPanel.add(backBtn);
        
        gbc.gridy = 1;
        add(btnPanel, gbc);

        signupBtn.addActionListener(e -> {
            if (authService.signup(userField.getText(), new String(passField.getPassword()))) {
                JOptionPane.showMessageDialog(this, "Zero-Knowledge identity verified and packed into Blockchain Genesis.");
                userField.setText("");
                passField.setText("");
                onGoToLogin.run();
            } else {
                JOptionPane.showMessageDialog(this, "Registration constraint failure! Needs stronger password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> onGoToLogin.run());
    }
}
