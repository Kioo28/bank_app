package views;

import javax.swing.*;
import java.awt.*;
import models.Account;
import models.CheckingAccount;
import models.TransactionDAO;
import utils.Session;

public class TransactionView extends JFrame {

    private JComboBox<String> cmbType;
    private JTextField amountField, toField;

    public TransactionView() {
        setTitle("Transaksi Keuangan");
        setSize(350, 250);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Jenis Transaksi:"));
        cmbType = new JComboBox<>(new String[]{"DEPOSIT", "WITHDRAW", "TRANSFER"});
        add(cmbType);

        add(new JLabel("Jumlah:"));
        amountField = new JTextField();
        add(amountField);

        add(new JLabel("Ke Akun (Transfer):"));
        toField = new JTextField();
        add(toField);

        JButton btnProcess = new JButton("PROSES");
        add(btnProcess);

        btnProcess.addActionListener(e -> process());
    }

    private void process() {
        String type = cmbType.getSelectedItem().toString();
        Account current = Session.getCurrentAccount();

        if (current == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada akun yang aktif!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText());
            int targetId = toField.getText().isEmpty() ? 0 : Integer.parseInt(toField.getText());

            boolean success = false;

            switch (type) {
                case "DEPOSIT":
                    success = TransactionDAO.deposit(current.getAccountId(), amount);
                    break;

                case "WITHDRAW":
                    success = TransactionDAO.withdraw(current.getAccountId(), amount);
                    break;

                case "TRANSFER":
                    if (targetId <= 0) {
                        JOptionPane.showMessageDialog(this, "Masukkan ID akun tujuan untuk transfer!");
                        return;
                    }
                    success = TransactionDAO.transfer(current.getAccountId(), targetId, amount);
                    break;
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "Transaksi berhasil!");
            } else {
                JOptionPane.showMessageDialog(this, "Saldo tidak cukup atau transaksi gagal!");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah atau ID akun tidak valid!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal melakukan transaksi!");
        }
    }
}
