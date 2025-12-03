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
        list = Account.getUserAccounts(Session.currentUser.userId);

        for (Account acc : list) {
            cmb.addItem(acc.getType().toUpperCase() + " - " + acc.getAccountNumber());
        }
    }

    private void enterDashboard() {
        int index = cmb.getSelectedIndex();
        Session.setAccount(list.get(index));

        new DashboardView().setVisible(true);
        this.dispose();
    }
}
