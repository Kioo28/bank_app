package views;

import javax.swing.*;
import java.awt.*;
import utils.RoundedButton;
import models.Account;
import models.UserDAO;
import utils.Session;
import models.AccountDAO;

public class LoginView extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginView() {
        setTitle("Bank App - Login");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(245, 245, 245)); 

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        userLabel.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(userLabel);

        usernameField = new JTextField(20);
        usernameField.setBounds(100, 20, 165, 25);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 60, 80, 25);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(passwordLabel);

        passwordField = new JPasswordField(20);
        passwordField.setBounds(100, 60, 165, 25);
        panel.add(passwordField);

        JButton loginButton = new RoundedButton("Login");
        loginButton.setBounds(100, 100, 165, 30);
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 13));
        loginButton.setFocusPainted(false);
        panel.add(loginButton);

        loginButton.addActionListener(e -> login());

        // ================================
        //   TOMBOL CREATE ACCOUNT
        // ================================
        JButton registerButton = new JButton("Create Account");
        registerButton.setBounds(100, 140, 165, 30);
        registerButton.setBackground(new Color(30, 130, 76));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 13));
        registerButton.setFocusPainted(false);
        panel.add(registerButton);

        registerButton.addActionListener(e -> {
            new RegisterView().setVisible(true);
            this.dispose();
        });

        // Tambahkan panel ke frame
        add(panel);
    }

   private void login() {
    String u = usernameField.getText();
    String p = new String(passwordField.getPassword());

    Account account = UserDAO.login(u, p);
    if (account != null) {

        Account realAcc = AccountDAO.getAccountById(account.getAccountId());
        Session.setCurrentAccount(realAcc);

        JOptionPane.showMessageDialog(this, "Login berhasil!");
        new DashboardView().setVisible(true);
        this.dispose();
    } else {
        JOptionPane.showMessageDialog(this, "Username atau password salah");
    }
    }

}
