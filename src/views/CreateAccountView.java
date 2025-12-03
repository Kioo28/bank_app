package views;

import javax.swing.*;
import models.Account;
import utils.Session;

public class CreateAccountView extends JFrame {

    JComboBox<String> cmb;

    public CreateAccountView() {
        setTitle("Buat Rekening Baru");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setLayout(null);

        cmb = new JComboBox<>(new String[]{"saving", "checking", "business"});
        cmb.setBounds(50, 40, 200, 25);
        add(cmb);

        JButton btn = new JButton("Buat Akun");
        btn.setBounds(80, 90, 140, 30);
        add(btn);

        btn.addActionListener(e -> create());
    }

    private void create() {
        String type = cmb.getSelectedItem().toString();

        if (Account.createAccount(Session.currentUser.userId, type)) {
            JOptionPane.showMessageDialog(this, "Rekening berhasil dibuat!");
            new ChooseAccountView().setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal membuat rekening!");
        }
    }
}
