package views;

import javax.swing.*;
import java.awt.*;
import utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import models.AccountDAO;
import models.CheckingAccount;
import models.Account;


public class RegisterView extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField fullNameField;
    private JComboBox<String> accountTypeCombo; // pilihan tipe akun

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

       accountTypeCombo = new JComboBox<>(new String[]{"SAVING", "CHECKING", "BUSINESS"});
        accountTypeCombo.setBounds(120, 180, 180, 25);
        panel.add(accountTypeCombo);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(120, 230, 180, 30);
        registerBtn.setBackground(new Color(52, 152, 219));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> registerUser());
        panel.add(registerBtn);

        add(panel);
    }

    private void registerUser() {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String accountType = (String) accountTypeCombo.getSelectedItem();
        String accountNumber = "ACCT-" + System.currentTimeMillis();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || accountType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        // simpan ke database
       try (Connection conn = DBConnection.getConnection()) {

        // ================= SIMPAN USER =================
        String sqlUser = "INSERT INTO users (username, password) VALUES (?, ?)";
        PreparedStatement psUser = conn.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS);
        psUser.setString(1, username);
        psUser.setString(2, password);
        psUser.executeUpdate();

        // Ambil user_id baru
        ResultSet rs = psUser.getGeneratedKeys();
        int userId = 0;
        if (rs.next()) {
            userId = rs.getInt(1);
        }
        // BUAT AKUN BARU
        String sqlAcc = "INSERT INTO accounts (user_id, account_number, type, balance) VALUES (?, ?, ?, ?)";
        PreparedStatement psAcc = conn.prepareStatement(sqlAcc);
        psAcc.setInt(1, userId);
        psAcc.setString(2, accountNumber);
        psAcc.setString(3, accountType);
        psAcc.setDouble(4, 0);
        psAcc.executeUpdate();


            Account newAcc;

        if (accountType.equals("CHECKING")) {
            newAcc = new CheckingAccount(0, userId, username, accountNumber, accountType, 0);
        } else {
            newAcc = new Account(0, userId, username, accountNumber, accountType, 0);
        }

        AccountDAO.createAccount(newAcc);


        JOptionPane.showMessageDialog(this, "Registrasi berhasil!");
        dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal registrasi: " + ex.getMessage());
        }
    }
}
