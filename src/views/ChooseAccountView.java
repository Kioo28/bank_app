package views;

import javax.swing.*;
import java.util.ArrayList;
import utils.Session;
import models.Account;

public class ChooseAccountView extends JFrame {

    JComboBox<String> cmb;
    ArrayList<Account> list;

    public ChooseAccountView() {
        setTitle("Pilih Akun");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblTitle = new JLabel("Pilih Akun:");
        lblTitle.setBounds(50, 20, 200, 25);
        add(lblTitle);

        cmb = new JComboBox<>();
        cmb.setBounds(50, 50, 200, 25);
        add(cmb);

        loadAccounts();

        JButton btn = new JButton("Masuk");
        btn.setBounds(100, 100, 100, 30);
        add(btn);

        btn.addActionListener(e -> enterDashboard());
    }

    private void loadAccounts() {
        list = Account.getUserAccounts(Session.getCurrentUserId());

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Anda belum memiliki akun. Silakan buat akun terlebih dahulu.");
            return;
        }

        for (Account acc : list) {
            cmb.addItem(acc.getType().toUpperCase() + " - " + acc.getAccountNumber());
        }
    }

    private void enterDashboard() {
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada akun yang tersedia!");
            return;
        }

        int index = cmb.getSelectedIndex();
        if (index >= 0 && index < list.size()) {
            Session.setCurrentAccount(list.get(index));
            new DashboardView().setVisible(true);
            this.dispose();
        }
    }
}