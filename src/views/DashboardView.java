package views;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import utils.Session;
import utils.DBConnection;
import utils.Formatter;

public class DashboardView extends JFrame {
// ================= FIELD KELAS =====================
private JLabel lblSaldo; // label saldo di dalam dashboard

// ================== METHOD UPDATE SALDO =====================
private void updateSaldoLabel() {
    // refresh saldo dari database
    if (Session.getCurrentAccount() != null) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT balance FROM accounts WHERE account_id=?"
            );
            ps.setInt(1, Session.getCurrentAccount().getAccountId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Session.getCurrentAccount().setBalance(rs.getDouble("balance"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // update label
        lblSaldo.setText("Saldo: " + Formatter.money(Session.getCurrentAccount().getBalance()));
    }
}

public DashboardView() {
    setTitle("Dashboard - Bank App");
    setSize(400, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    // ================= PANEL UTAMA ==================
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // ================== INFO AKUN (TOP) =====================
    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new GridLayout(3, 1));

    if (Session.getCurrentAccount() != null) {
        JLabel lblAkun = new JLabel(
            "Akun: " + Session.getCurrentAccount().getType().toUpperCase()
        );
        JLabel lblNo = new JLabel(
            "Nomor Akun: " + Session.getCurrentAccount().getAccountNumber()
        );
        
        // ===================== INI YANG DIUPDATE =====================
        lblSaldo = new JLabel(
            "Saldo: " + Formatter.money(Session.getCurrentAccount().getBalance())
        ); // gunakan field lblSaldo, bukan variabel lokal
        // ============================================================

        infoPanel.add(lblAkun);
        infoPanel.add(lblNo);
        infoPanel.add(lblSaldo);
    } else {
        infoPanel.add(new JLabel("Tidak ada akun aktif."));
    }


    // ================== BUTTON MENU (CENTER) ==================
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(4, 1, 10, 10));

    JButton transferBtn = new JButton("Transaksi");
    JButton saldoBtn = new JButton("Cek Saldo");
    JButton transaksiBtn = new JButton("Riwayat Transaksi");
    JButton laporanBtn = new JButton("Laporan Transaksi");

    buttonPanel.add(transferBtn);
    buttonPanel.add(saldoBtn);
    buttonPanel.add(transaksiBtn);
    buttonPanel.add(laporanBtn);

    // ================== AKSI TOMBOL =====================
    transferBtn.addActionListener(e -> {
        TransactionView tv = new TransactionView();
        tv.setVisible(true);

        tv.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                updateSaldoLabel(); // langsung update saldo setelah transaksi
            }
        });
    });

    saldoBtn.addActionListener(e -> updateSaldoLabel()); // cek saldo langsung update label

    transaksiBtn.addActionListener(e -> new HistoryView().setVisible(true));

    laporanBtn.addActionListener(e -> new ReportView().setVisible(true));

    // ================== TAMBAHKAN KE FRAME ==================
    mainPanel.add(infoPanel, BorderLayout.NORTH);
    mainPanel.add(buttonPanel, BorderLayout.CENTER);

    add(mainPanel);
}
}