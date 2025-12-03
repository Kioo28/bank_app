package views;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.text.SimpleDateFormat;
import models.Transaction;
import models.TransactionDAO;
import utils.Session;
import utils.Formatter;

public class HistoryView extends JFrame {


private JPanel listPanel;

public HistoryView() {
    setTitle("Riwayat Transaksi");
    setSize(600, 400);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    initComponents();
}

private void initComponents() {
    listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
    JScrollPane scrollPane = new JScrollPane(listPanel);
    add(scrollPane, BorderLayout.CENTER);

    loadTransactionData();
}

private void loadTransactionData() {
    int accountId = Session.getCurrentAccount().getAccountId();
    List<Transaction> transactions = TransactionDAO.getHistory(accountId);

    listPanel.removeAll();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // format tanggal

    for (Transaction t : transactions) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // ===================== UPDATE TANGGAL =====================
        String formattedDate = sdf.format(t.getTransactionDate()); // gunakan timestamp format rapi
        // ============================================================

        panel.add(new JLabel("ID Transaksi: " + t.getTransactionId()));
        panel.add(new JLabel("Tanggal: " + formattedDate)); // pakai formattedDate
        panel.add(new JLabel("Jenis: " + t.getTransactionType()));
        panel.add(new JLabel("Jumlah: " + Formatter.money(t.getAmount())));
        panel.add(new JLabel("Keterangan: " + t.getDescription()));
        panel.add(new JLabel("Status: " + t.getStatus()));

        listPanel.add(panel);
        listPanel.add(Box.createRigidArea(new Dimension(0, 5))); // jarak antar panel
    }

    listPanel.revalidate();
    listPanel.repaint();
}

}
