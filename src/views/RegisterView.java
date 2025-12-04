package views;

import javax.swing.*;
import java.awt.*;
import utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterView extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField fullNameField;
    private JComboBox<String> accountTypeCombo;

    public RegisterView() {
        setTitle("Create Account");
        setSize(350, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(245, 245, 245));

        JLabel title = new JLabel("Register New Account");
        title.setBounds(70, 10, 250, 30);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(title);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setBounds(20, 60, 100, 25);
        panel.add(nameLabel);

        fullNameField = new JTextField();
        fullNameField.setBounds(120, 60, 180, 25);
        panel.add(fullNameField);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 100, 100, 25);
        panel.add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(120, 100, 180, 25);
        panel.add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(20, 140, 100, 25);
        panel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(120, 140, 180, 25);
        panel.add(passwordField);

        JLabel accountLabel = new JLabel("Tipe Akun:");
        accountLabel.setBounds(20, 180, 100, 25);
        panel.add(accountLabel);

        accountTypeCombo = new JComboBox<>(new String[]{"SAVINGS", "CHECKING", "BUSINESS"});
        accountTypeCombo.setBounds(120, 180, 180, 25);
        panel.add(accountTypeCombo);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(120, 230, 180, 30);
        registerBtn.setBackground(new Color(52, 152, 219));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> registerUser());
        panel.add(registerBtn);

        JButton backBtn = new JButton("Back to Login");
        backBtn.setBounds(120, 270, 180, 25);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            new LoginView().setVisible(true);
            dispose();
        });
        panel.add(backBtn);

        add(panel);
    }

    private void registerUser() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String accountType = (String) accountTypeCombo.getSelectedItem();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        String accountNumber = String.format("%08d", System.currentTimeMillis() % 100000000);

        try (Connection conn = DBConnection.getConnection()) {

            // Cek username sudah ada atau belum
            String checkSql = "SELECT user_id FROM users WHERE username = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, username);
            ResultSet checkRs = checkPs.executeQuery();
            
            if (checkRs.next()) {
                JOptionPane.showMessageDialog(this, "Username sudah digunakan!");
                return;
            }

            // Simpan user baru
            String sqlUser = "INSERT INTO users (full_name, username, password) VALUES (?, ?, ?)";
            PreparedStatement psUser = conn.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS);
            psUser.setString(1, fullName);
            psUser.setString(2, username);
            psUser.setString(3, password);
            psUser.executeUpdate();

            // Ambil user_id yang baru dibuat
            ResultSet rs = psUser.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }

            // Buat akun untuk user
            String sqlAcc = "INSERT INTO accounts (user_id, account_number, account_type, balance) " +
                          "VALUES (?, ?, ?, ?)";
            PreparedStatement psAcc = conn.prepareStatement(sqlAcc, PreparedStatement.RETURN_GENERATED_KEYS);
            psAcc.setInt(1, userId);
            psAcc.setString(2, accountNumber);
            psAcc.setString(3, accountType);
            psAcc.setDouble(4, 0.0);
            
            psAcc.executeUpdate();

            // Ambil account_id yang baru dibuat
            ResultSet rsAcc = psAcc.getGeneratedKeys();
            int accountId = 0;
            if (rsAcc.next()) {
                accountId = rsAcc.getInt(1);
            }
            
            // Jika CHECKING, buat entry di checking_accounts
            if (accountType.equals("CHECKING")) {
                String sqlChecking = "INSERT INTO checking_accounts (account_id, overdraft_limit, monthly_fee) " +
                                   "VALUES (?, ?, ?)";
                PreparedStatement psChecking = conn.prepareStatement(sqlChecking);
                psChecking.setInt(1, accountId);
                psChecking.setDouble(2, 500000.0);
                psChecking.setDouble(3, 5000.0);
                psChecking.executeUpdate();
            }
            
            // Jika SAVINGS, buat entry di savings_accounts
            if (accountType.equals("SAVINGS")) {
                String sqlSavings = "INSERT INTO savings_accounts (account_id, interest_rate, minimum_balance) " +
                                  "VALUES (?, ?, ?)";
                PreparedStatement psSavings = conn.prepareStatement(sqlSavings);
                psSavings.setInt(1, accountId);
                psSavings.setDouble(2, 2.5);
                psSavings.setDouble(3, 1000.0);
                psSavings.executeUpdate();
            }
            
            // Jika BUSINESS, buat entry di business_accounts
            if (accountType.equals("BUSINESS")) {
                String sqlBusiness = "INSERT INTO business_accounts (account_id, business_name, transaction_limit) " +
                                   "VALUES (?, ?, ?)";
                PreparedStatement psBusiness = conn.prepareStatement(sqlBusiness);
                psBusiness.setInt(1, accountId);
                psBusiness.setString(2, fullName + " Business");
                psBusiness.setDouble(3, 100000000.0);
                psBusiness.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, 
                "Registrasi berhasil!\n" +
                "Username: " + username + "\n" +
                "Nomor Rekening: " + accountNumber + "\n" +
                "Tipe: " + accountType);
            
            new LoginView().setVisible(true);
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal registrasi: " + ex.getMessage());
        }
    }
}