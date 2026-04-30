package frontend;

import backend.auth.AuthService;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class LoginUI extends JPanel {
    public LoginUI(AuthService authService, Runnable onSuccess, Runnable onGoToSignup) {
        setLayout(new GridBagLayout());
        
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextField userField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JButton loginBtn = new JButton("Login via PBKDF2");
        JButton signupBtn = new JButton("Create New Vault");

        panel.add(new JLabel("Global Identity Username:"));
        panel.add(userField);
        panel.add(new JLabel("Master Password:"));
        panel.add(passField);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        add(panel, gbc);
        
        JPanel btnPanel = new JPanel();
        btnPanel.add(loginBtn);
        btnPanel.add(signupBtn);
        
        gbc.gridy = 1;
        add(btnPanel, gbc);

        loginBtn.addActionListener(e -> {
            if (authService.login(userField.getText(), new String(passField.getPassword()))) {
                userField.setText("");
                passField.setText("");
                onSuccess.run();
            } else {
                JOptionPane.showMessageDialog(this, "Login Blocked! Invalid credentials or tampered blockchain Identity.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        signupBtn.addActionListener(e -> onGoToSignup.run());
    }
}
