package views;

import utils.Session;
import utils.DBConnection;
import utils.Formatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReportView extends JFrame {

    private JTable tblTransactions;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbAccount;
    private JComboBox<String> cmbType;

    private JLabel lblTotal, lblDeposit, lblWithdraw;

    public ReportView() {
        setTitle("Laporan Transaksi");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        addHeader();
        addFilters();
        addTable();
        addSummary();

        loadAccounts();
        loadTransactions();
    }

    /* ====================== HEADER ====================== */
    private void addHeader() {
        JLabel lbl = new JLabel("LAPORAN TRANSAKSI", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lbl, BorderLayout.NORTH);
    }

    /* ==================== FILTER PANEL ==================== */
    private void addFilters() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        panel.add(new JLabel("Rekening:"));
        cmbAccount = new JComboBox<>();
        cmbAccount.setPreferredSize(new Dimension(180, 25));
        panel.add(cmbAccount);

        panel.add(new JLabel("Jenis:"));
        cmbType = new JComboBox<>(new String[]{"Semua", "DEPOSIT", "WITHDRAWAL", "TRANSFER"});
        cmbType.setPreferredSize(new Dimension(150, 25));
        panel.add(cmbType);

        JButton btnFilter = new JButton("Terapkan");
        btnFilter.setBackground(new Color(52, 152, 219));
        btnFilter.setForeground(Color.white);
        btnFilter.addActionListener(e -> loadTransactions());
        panel.add(btnFilter);

        JButton btnClear = new JButton("Reset");
        btnClear.setBackground(new Color(39, 174, 96));
        btnClear.setForeground(Color.white);
        btnClear.addActionListener(e -> {
            cmbAccount.setSelectedIndex(0);
            cmbType.setSelectedIndex(0);
            loadTransactions();
        });
        panel.add(btnClear);

        add(panel, BorderLayout.SOUTH);
    }

    /* ======================= TABLE ======================= */
    private void addTable() {
        String[] columns = {"ID", "Tanggal", "Rekening", "Jenis", "Jumlah", "Tujuan", "Keterangan"};

        tableModel = new DefaultTableModel(columns, 0);
        tblTransactions = new JTable(tableModel);
        tblTransactions.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tblTransactions);
        add(scrollPane, BorderLayout.CENTER);
    }

    /* ======================= SUMMARY ======================= */
    private void addSummary() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblTotal = createSummaryCard(panel, "Total Transaksi");
        lblDeposit = createSummaryCard(panel, "Total Deposit");
        lblWithdraw = createSummaryCard(panel, "Total Penarikan");

        add(panel, BorderLayout.NORTH);
    }

    private JLabel createSummaryCard(JPanel parent, String title) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(new Color(52, 73, 94));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblValue = new JLabel("0", SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 16));
        lblValue.setForeground(Color.WHITE);

        card.add(lblTitle);
        card.add(lblValue);

        parent.add(card);
        return lblValue;
    }

    /* ======================= LOAD DATA ======================= */
    private void loadAccounts() {
        cmbAccount.removeAllItems();
        cmbAccount.addItem("Semua");

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement st = conn.prepareStatement(
                    "SELECT account_id, account_number FROM accounts WHERE user_id=?"
            );
            st.setInt(1, Session.getCurrentUserId());

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                cmbAccount.addItem(
                        rs.getInt("account_id") + " - " + rs.getString("account_number")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private void loadTransactions() {
    tableModel.setRowCount(0);

    String selectedAcc = (String) cmbAccount.getSelectedItem();
    String selectedType = (String) cmbType.getSelectedItem();

    int total = 0;
    double dep = 0;
    double wd = 0;

    try {
        Connection conn = DBConnection.getConnection();

        StringBuilder q = new StringBuilder(
                "SELECT t.*, a.account_number " +
                "FROM transactions t JOIN accounts a ON t.account_id = a.account_id " +
                "WHERE a.user_id = ?"
        );

        // Filter berdasarkan akun
        if (!selectedAcc.equals("Semua")) {
            int id = Integer.parseInt(selectedAcc.split(" - ")[0]);
            q.append(" AND t.account_id = ").append(id);
        }

        // Filter berdasarkan type
        if (!selectedType.equals("Semua")) {
            q.append(" AND t.type = '").append(selectedType).append("'");
        }

        q.append(" ORDER BY t.created_at DESC");

        PreparedStatement st = conn.prepareStatement(q.toString());
        st.setInt(1, Session.getCurrentUserId());

        ResultSet rs = st.executeQuery();
        while (rs.next()) {

            Object[] row = {
                    rs.getInt("transaction_id"),
                    Formatter.formatDateTime(rs.getTimestamp("created_at")),
                    rs.getString("account_number"),
                    rs.getString("type"),
                    Formatter.formatCurrency(rs.getDouble("amount")),
                    "-", // karena belum ada target_account_id
                    rs.getString("description")
            };

            tableModel.addRow(row);
            total++;

            // COUNT SUMMARY
            if (rs.getString("type").equals("DEPOSIT"))
                dep += rs.getDouble("amount");
            if (rs.getString("type").equals("WITHDRAW") || rs.getString("type").equals("TRANSFER"))
                wd += rs.getDouble("amount");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    lblTotal.setText(String.valueOf(total));
    lblDeposit.setText(Formatter.formatCurrency(dep));
    lblWithdraw.setText(Formatter.formatCurrency(wd));
}

}
