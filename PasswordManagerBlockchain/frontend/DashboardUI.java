package frontend;

import backend.auth.AuthService;
import backend.auth.CryptoUtil;
import backend.blockchain.Blockchain;
import backend.db.LogDAO;
import backend.db.UserDAO;
import backend.models.LogEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DashboardUI extends JPanel {
    
    private AuthService authService;
    private Blockchain blockchain;
    private LogDAO logDAO;
    private UserDAO userDAO;
    
    private DefaultTableModel vaultModel;
    private DefaultTableModel logModel;

    public DashboardUI(AuthService authService, Blockchain blockchain, LogDAO logDAO, UserDAO userDAO, Runnable onLogout) {
        this.authService = authService;
        this.blockchain = blockchain;
        this.logDAO = logDAO;
        this.userDAO = userDAO;
        
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome back to your Encrypted Vault, " + authService.getCurrentUserBlock().username);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout & Wipe Memory");
        logoutBtn.addActionListener(e -> onLogout.run());
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Core App Tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: VAULT ---
        JPanel vaultTab = new JPanel(new BorderLayout(10, 10));
        vaultTab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add form
        JPanel addPanel = new JPanel();
        JTextField serviceField = new JTextField(15);
        JTextField passField = new JTextField(15);
        JButton addBtn = new JButton("+ Encrypt into Vault");
        
        addPanel.add(new JLabel("Service Name:"));
        addPanel.add(serviceField);
        addPanel.add(new JLabel("Password:"));
        addPanel.add(passField);
        addPanel.add(addBtn);
        vaultTab.add(addPanel, BorderLayout.NORTH);

        // Vault Display Table
        vaultModel = new DefaultTableModel(new String[]{"Service Platform", "Decrypted Password"}, 0);
        JTable vaultTable = new JTable(vaultModel);
        vaultTab.add(new JScrollPane(vaultTable), BorderLayout.CENTER);
        
        addBtn.addActionListener(e -> {
            String svc = serviceField.getText().trim();
            String pwd = passField.getText().trim();
            if(!svc.isEmpty() && !pwd.isEmpty()) {
                addServiceToEncryptedVault(svc, pwd);
                serviceField.setText("");
                passField.setText("");
            }
        });
        
        refreshVaultTable();

        // --- TAB 2: AUDIT LOGS ---
        JPanel logTab = new JPanel(new BorderLayout());
        logModel = new DefaultTableModel(new String[]{"Action", "User Identity", "Timestamp"}, 0);
        JTable logTable = new JTable(logModel);
        logTab.add(new JScrollPane(logTable), BorderLayout.CENTER);
        JButton refreshLogBtn = new JButton("Refresh Audit Logs");
        refreshLogBtn.addActionListener(e -> refreshLogs());
        logTab.add(refreshLogBtn, BorderLayout.SOUTH);
        refreshLogs();

        // --- TAB 3: SECURITY & BLOCKCHAIN ---
        JPanel secTab = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridy = 0;
        
        JButton validateBtn = new JButton("Validate Whole Blockchain (ECDSA & Hashes)");
        secTab.add(validateBtn, gbc);
        
        gbc.gridy = 1;
        JPanel tamperPanel = new JPanel();
        JTextField blockIndexField = new JTextField(5);
        JButton tamperBtn = new JButton("Simulate Breach (Corrupt Hash)");
        tamperPanel.add(new JLabel("Block Index ID:"));
        tamperPanel.add(blockIndexField);
        tamperPanel.add(tamperBtn);
        
        secTab.add(tamperPanel, gbc);
        
        validateBtn.addActionListener(e -> {
            if (blockchain.isChainValid()) {
                JOptionPane.showMessageDialog(this, "All mathematically linked hashes and ECDSA Signatures verify perfectly. Data is untampered.", "Integrity Good", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "BLOCKCHAIN INTEGIRY BREACHED! A signature or hash linkage has been actively changed on the remote DB.", "Critical Breach", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        tamperBtn.addActionListener(e -> {
            try {
                int index = Integer.parseInt(blockIndexField.getText());
                if(index >=0 && index < blockchain.chain.size()){
                    blockchain.chain.get(index).currentHash = "SIMULATED_BREACH_CORRUPTION!!";
                    userDAO.corruptBlock(index);
                    JOptionPane.showMessageDialog(this, "Successfully simulated a hack corruption on Block " + index + ". Click Validate to see the alarm.");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Block boundary.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Numeric input required.");
            }
        });

        // Add Tabs
        tabbedPane.add("📦 My Secure Vault", vaultTab);
        tabbedPane.add("📑 Audit Trails", logTab);
        tabbedPane.add("⛓️ Blockchain Integrity", secTab);

        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void addServiceToEncryptedVault(String service, String pass) {
        try {
            String encryptedBlob = authService.getCurrentUserBlock().servicePasswords;
            String plaintextCsv = "";
            if (encryptedBlob != null && !encryptedBlob.isEmpty()) {
                plaintextCsv = CryptoUtil.decryptAESGCM(authService.getCurrentMasterKey(), encryptedBlob);
            }
            if (!plaintextCsv.isEmpty()) plaintextCsv += ",";
            plaintextCsv += service + ":" + pass;
            
            String newEncryptedBlob = CryptoUtil.encryptAESGCM(authService.getCurrentMasterKey(), plaintextCsv);
            authService.addEncryptedVaultData(newEncryptedBlob);
            
            JOptionPane.showMessageDialog(this, "Encrypted strictly into military-grade block.");
            refreshVaultTable();
            refreshLogs();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Encryption processing error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshVaultTable() {
        vaultModel.setRowCount(0); // clear existing
        String encryptedVault = authService.getCurrentUserBlock().servicePasswords;
        if(encryptedVault != null && !encryptedVault.isEmpty()) {
            try {
                String decryptedStr = CryptoUtil.decryptAESGCM(authService.getCurrentMasterKey(), encryptedVault);
                String[] parts = decryptedStr.split(",");
                for(String p : parts) {
                    if (p.contains(":")) {
                        String[] pair = p.split(":");
                        vaultModel.addRow(new Object[]{pair[0], pair[1]});
                    }
                }
            } catch (Exception e) {
                vaultModel.addRow(new Object[]{"ERROR", "Corrupted/Missing PBKDF2 Key"});
            }
        }
    }
    
    private void refreshLogs() {
        logModel.setRowCount(0);
        for (LogEntry log : logDAO.getAllLogs()) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = sdf.format(new java.util.Date(log.timestamp));
            logModel.addRow(new Object[]{log.action, log.username, dateString});
        }
    }
}
