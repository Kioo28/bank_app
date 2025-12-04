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
        setSize(950, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        addHeader();
        addSummary();
        addTable();
        addFilters();

        loadAccounts();
        loadTransactions();
    }

    /* ====================== HEADER ====================== */
    private void addHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel lbl = new JLabel("LAPORAN TRANSAKSI", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 24));
        lbl.setForeground(Color.WHITE);
        headerPanel.add(lbl);
        
        add(headerPanel, BorderLayout.NORTH);
    }

    /* ==================== FILTER PANEL ==================== */
    private void addFilters() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        panel.add(new JLabel("Rekening:"));
        cmbAccount = new JComboBox<>();
        cmbAccount.setPreferredSize(new Dimension(200, 30));
        panel.add(cmbAccount);

        panel.add(new JLabel("Jenis:"));
        cmbType = new JComboBox<>(new String[]{"Semua", "DEPOSIT", "WITHDRAWAL", "TRANSFER"});
        cmbType.setPreferredSize(new Dimension(150, 30));
        panel.add(cmbType);

        JButton btnFilter = new JButton("🔍 Terapkan");
        btnFilter.setBackground(new Color(52, 152, 219));
        btnFilter.setForeground(Color.white);
        btnFilter.setFocusPainted(false);
        btnFilter.setPreferredSize(new Dimension(120, 30));
        btnFilter.addActionListener(e -> loadTransactions());
        panel.add(btnFilter);

        JButton btnClear = new JButton("🔄 Reset");
        btnClear.setBackground(new Color(39, 174, 96));
        btnClear.setForeground(Color.white);
        btnClear.setFocusPainted(false);
        btnClear.setPreferredSize(new Dimension(100, 30));
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
        String[] columns = {"ID", "Tanggal", "Rekening", "Jenis", "Jumlah", "Keterangan"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblTransactions = new JTable(tableModel);
        tblTransactions.setRowHeight(28);
        tblTransactions.setFont(new Font("Arial", Font.PLAIN, 12));
        tblTransactions.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tblTransactions.getTableHeader().setBackground(new Color(52, 73, 94));
        tblTransactions.getTableHeader().setForeground(Color.WHITE);

        tblTransactions.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblTransactions.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblTransactions.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblTransactions.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblTransactions.getColumnModel().getColumn(4).setPreferredWidth(120);
        tblTransactions.getColumnModel().getColumn(5).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(tblTransactions);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }

    /* ======================= SUMMARY ======================= */
    private void addSummary() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(240, 240, 240));

        lblTotal = createSummaryCard(panel, "Total Transaksi", new Color(52, 152, 219));
        lblDeposit = createSummaryCard(panel, "Total Deposit", new Color(46, 204, 113));
        lblWithdraw = createSummaryCard(panel, "Total Penarikan", new Color(231, 76, 60));

        add(panel, BorderLayout.NORTH);
    }

    private JLabel createSummaryCard(JPanel parent, String title, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lblValue = new JLabel("0", SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 20));
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
                q.append(" AND t.transaction_type = '").append(selectedType).append("'");
            }

            q.append(" ORDER BY t.transaction_date DESC");

            PreparedStatement st = conn.prepareStatement(q.toString());
            st.setInt(1, Session.getCurrentUserId());

            ResultSet rs = st.executeQuery();
            while (rs.next()) {

                Object[] row = {
                        rs.getInt("transaction_id"),
                        Formatter.formatDateTime(rs.getTimestamp("transaction_date")),
                        rs.getString("account_number"),
                        rs.getString("transaction_type"),
                        Formatter.formatCurrency(rs.getDouble("amount")),
                        rs.getString("description")
                };

                tableModel.addRow(row);
                total++;

                // COUNT SUMMARY
                String type = rs.getString("transaction_type");
                if (type.equals("DEPOSIT"))
                    dep += rs.getDouble("amount");
                if (type.equals("WITHDRAWAL") || type.equals("TRANSFER"))
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