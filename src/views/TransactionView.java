package views;

import javax.swing.*;
import java.awt.*;
import models.Account;
import models.CheckingAccount;
import models.TransactionDAO;
import utils.Session;
import utils.Formatter;

public class TransactionView extends JFrame {

    private JComboBox<String> cmbType;
    private JTextField amountField, toField;
    private JLabel lblInfo;

    public TransactionView() {
        setTitle("Transaksi Keuangan");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel("Transaksi Keuangan");
        title.setBounds(20, 10, 300, 30);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title);

        // Info akun
        lblInfo = new JLabel("Saldo: " + Session.getBalance());
        lblInfo.setBounds(20, 45, 350, 25);
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(lblInfo);

        // Jenis Transaksi
        JLabel lblType = new JLabel("Jenis Transaksi:");
        lblType.setBounds(20, 85, 120, 25);
        panel.add(lblType);

        cmbType = new JComboBox<>(new String[]{"DEPOSIT", "WITHDRAW", "TRANSFER"});
        cmbType.setBounds(150, 85, 200, 25);
        panel.add(cmbType);

        // Jumlah
        JLabel lblAmount = new JLabel("Jumlah:");
        lblAmount.setBounds(20, 125, 120, 25);
        panel.add(lblAmount);

        amountField = new JTextField();
        amountField.setBounds(150, 125, 200, 25);
        panel.add(amountField);

        // Akun Tujuan
        JLabel lblTo = new JLabel("ID Akun Tujuan:");
        lblTo.setBounds(20, 165, 120, 25);
        panel.add(lblTo);

        toField = new JTextField();
        toField.setBounds(150, 165, 200, 25);
        toField.setEnabled(false);
        panel.add(toField);

        // Enable/disable toField based on transaction type
        cmbType.addActionListener(e -> {
            String type = cmbType.getSelectedItem().toString();
            toField.setEnabled(type.equals("TRANSFER"));
            if (!type.equals("TRANSFER")) {
                toField.setText("");
            }
        });

        // Button Proses
        JButton btnProcess = new JButton("PROSES TRANSAKSI");
        btnProcess.setBounds(20, 215, 330, 35);
        btnProcess.setBackground(new Color(52, 152, 219));
        btnProcess.setForeground(Color.WHITE);
        btnProcess.setFont(new Font("Arial", Font.BOLD, 14));
        btnProcess.setFocusPainted(false);
        panel.add(btnProcess);

        // Button Batal
        JButton btnCancel = new JButton("BATAL");
        btnCancel.setBounds(20, 260, 330, 30);
        btnCancel.setFocusPainted(false);
        panel.add(btnCancel);

        btnProcess.addActionListener(e -> process());
        btnCancel.addActionListener(e -> this.dispose());

        add(panel);
    }

    private void process() {
        String type = cmbType.getSelectedItem().toString();
        Account current = Session.getCurrentAccount();

        if (current == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada akun yang aktif!");
            return;
        }

        try {
            String amountText = amountField.getText().trim();
            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan jumlah transaksi!");
                return;
            }

            double amount = Double.parseDouble(amountText);
            
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Jumlah harus lebih dari 0!");
                return;
            }

            boolean success = false;

            switch (type) {
                case "DEPOSIT":
                    success = TransactionDAO.deposit(current.getAccountId(), amount);
                    if (success) {
                        JOptionPane.showMessageDialog(this, 
                            "Deposit berhasil!\n" +
                            "Jumlah: " + Formatter.money(amount) + "\n" +
                            "Saldo baru: " + Formatter.money(current.getBalance()),
                            "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;

                case "WITHDRAW":
                    // Cek apakah checking account dengan overdraft
                    if (current instanceof CheckingAccount) {
                        CheckingAccount checking = (CheckingAccount) current;
                        double maxWithdraw = current.getBalance() + checking.getOverdraftLimit();
                        if (amount > maxWithdraw) {
                            JOptionPane.showMessageDialog(this, 
                                "Penarikan melebihi limit!\n" +
                                "Saldo: " + Formatter.money(current.getBalance()) + "\n" +
                                "Overdraft: " + Formatter.money(checking.getOverdraftLimit()) + "\n" +
                                "Maksimal penarikan: " + Formatter.money(maxWithdraw),
                                "Gagal", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    
                    success = TransactionDAO.withdraw(current.getAccountId(), amount);
                    if (success) {
                        JOptionPane.showMessageDialog(this, 
                            "Penarikan berhasil!\n" +
                            "Jumlah: " + Formatter.money(amount) + "\n" +
                            "Saldo baru: " + Formatter.money(current.getBalance()),
                            "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Saldo tidak cukup!\n" +
                            "Saldo Anda: " + Formatter.money(current.getBalance()),
                            "Gagal", JOptionPane.ERROR_MESSAGE);
                    }
                    break;

                case "TRANSFER":
                    String targetText = toField.getText().trim();
                    if (targetText.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Masukkan ID akun tujuan untuk transfer!");
                        return;
                    }
                    
                    int targetId = Integer.parseInt(targetText);
                    
                    if (targetId == current.getAccountId()) {
                        JOptionPane.showMessageDialog(this, "Tidak bisa transfer ke akun sendiri!");
                        return;
                    }
                    
                    success = TransactionDAO.transfer(current.getAccountId(), targetId, amount);
                    if (success) {
                        JOptionPane.showMessageDialog(this, 
                            "Transfer berhasil!\n" +
                            "Jumlah: " + Formatter.money(amount) + "\n" +
                            "Ke akun ID: " + targetId + "\n" +
                            "Saldo baru: " + Formatter.money(current.getBalance()),
                            "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Transfer gagal!\n" +
                            "Periksa saldo atau ID tujuan.",
                            "Gagal", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
            }

            if (success) {
                amountField.setText("");
                toField.setText("");
                lblInfo.setText("Saldo: " + Session.getBalance());
                
                // Tutup window setelah sukses
                this.dispose();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Format tidak valid!\nMasukkan angka yang benar.",
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Gagal melakukan transaksi!\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}