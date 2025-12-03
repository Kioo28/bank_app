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

    private JLabel lblSaldo;
    private JLabel lblOverdraft;

    private void updateSaldoLabel() {
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
            
            double balance = Session.getCurrentAccount().getBalance();
            lblSaldo.setText("Saldo: " + Formatter.money(balance));
            
            // Update info overdraft jika CHECKING
            if (Session.getCurrentAccount().getType().equalsIgnoreCase("CHECKING")) {
                if (balance < 0) {
                    lblOverdraft.setText("⚠ Overdraft: " + Formatter.money(Math.abs(balance)));
                    lblOverdraft.setForeground(Color.RED);
                } else {
                    lblOverdraft.setText("");
                }
            }
        }
    }

    public DashboardView() {
        setTitle("Dashboard - Bank App");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));

        // INFO AKUN (TOP)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(5, 1, 5, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        
        if (Session.getCurrentAccount() != null) {
            JLabel lblWelcome = new JLabel("Selamat Datang, " + Session.getCurrentUsername());
            lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
            
            JLabel lblAkun = new JLabel(
                "Tipe Akun: " + Session.getCurrentAccount().getType().toUpperCase()
            );
            lblAkun.setFont(new Font("Arial", Font.PLAIN, 14));
            
            JLabel lblNo = new JLabel(
                "Nomor Rekening: " + Session.getCurrentAccount().getAccountNumber()
            );
            lblNo.setFont(new Font("Arial", Font.PLAIN, 14));
            
            lblSaldo = new JLabel(
                "Saldo: " + Formatter.money(Session.getCurrentAccount().getBalance())
            );
            lblSaldo.setFont(new Font("Arial", Font.BOLD, 16));
            lblSaldo.setForeground(new Color(34, 139, 34));
            
            // Label overdraft untuk checking account
            lblOverdraft = new JLabel("");
            lblOverdraft.setFont(new Font("Arial", Font.BOLD, 12));

            infoPanel.add(lblWelcome);
            infoPanel.add(lblAkun);
            infoPanel.add(lblNo);
            infoPanel.add(lblSaldo);
            infoPanel.add(lblOverdraft);
        } else {
            infoPanel.add(new JLabel("Tidak ada akun aktif."));
        }

        // BUTTON MENU (CENTER)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 1, 10, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton transactionBtn = createMenuButton("💰 Transaksi", new Color(52, 152, 219));
        JButton saldoBtn = createMenuButton("💵 Cek Saldo", new Color(46, 204, 113));
        JButton historyBtn = createMenuButton("📜 Riwayat Transaksi", new Color(155, 89, 182));
        JButton reportBtn = createMenuButton("📊 Laporan Transaksi", new Color(230, 126, 34));
        JButton monthlyBtn = createMenuButton("📅 Update Bulanan", new Color(41, 128, 185));
        JButton logoutBtn = createMenuButton("🚪 Logout", new Color(231, 76, 60));

        buttonPanel.add(transactionBtn);
        buttonPanel.add(saldoBtn);
        buttonPanel.add(historyBtn);
        buttonPanel.add(reportBtn);
        buttonPanel.add(monthlyBtn);
        buttonPanel.add(logoutBtn);

        // AKSI TOMBOL
        transactionBtn.addActionListener(e -> {
            TransactionView tv = new TransactionView();
            tv.setVisible(true);
            tv.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    updateSaldoLabel();
                }
            });
        });

        saldoBtn.addActionListener(e -> {
            updateSaldoLabel();
            JOptionPane.showMessageDialog(this, 
                "Saldo Anda saat ini:\n" + Formatter.money(Session.getCurrentAccount().getBalance()),
                "Cek Saldo", JOptionPane.INFORMATION_MESSAGE);
        });

        historyBtn.addActionListener(e -> new HistoryView().setVisible(true));

        reportBtn.addActionListener(e -> new ReportView().setVisible(true));

        monthlyBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Jalankan update bulanan?\n" +
                "- Saving: Tambah bunga 1%\n" +
                "- Checking: Biaya overdraft Rp 10.000 (jika minus)",
                "Update Bulanan", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                models.TransactionDAO.runMonthlyUpdate();
                updateSaldoLabel();
                JOptionPane.showMessageDialog(this, "Update bulanan berhasil!");
            }
        });

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin ingin logout?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                Session.logout();
                new LoginView().setVisible(true);
                this.dispose();
            }
        });

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        add(mainPanel);
        
        // Update saldo saat dashboard dibuka
        updateSaldoLabel();
    }

    private JButton createMenuButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return btn;
    }
}