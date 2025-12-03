package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.text.SimpleDateFormat;
import models.Transaction;
import models.TransactionDAO;
import utils.Session;
import utils.Formatter;

public class HistoryView extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    public HistoryView() {
        setTitle("Riwayat Transaksi");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel title = new JLabel("RIWAYAT TRANSAKSI", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Info akun
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblInfo = new JLabel(
            "Akun: " + Session.getCurrentAccount().getAccountNumber() + 
            " | Saldo: " + Session.getBalance()
        );
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 14));
        infoPanel.add(lblInfo);
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Tanggal", "Jenis", "Jumlah", "Keterangan", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(250);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadTransactionData());
        
        JButton btnClose = new JButton("❌ Tutup");
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());
        
        btnPanel.add(btnRefresh);
        btnPanel.add(btnClose);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);
        
        loadTransactionData();
    }

    private void loadTransactionData() {
        int accountId = Session.getCurrentAccount().getAccountId();
        List<Transaction> transactions = TransactionDAO.getHistory(accountId);

        tableModel.setRowCount(0);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        if (transactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Belum ada transaksi.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        for (Transaction t : transactions) {
            String formattedDate = sdf.format(t.getTransactionDate());
            
            Object[] row = {
                t.getTransactionId(),
                formattedDate,
                t.getTransactionType(),
                Formatter.money(t.getAmount()),
                t.getDescription(),
                t.getStatus()
            };
            
            tableModel.addRow(row);
        }
    }
}