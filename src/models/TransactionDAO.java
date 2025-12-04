package models;

import utils.DBConnection;
import utils.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public static List<Transaction> getHistory(int accountId) {
        List<Transaction> list = new ArrayList<>();

        String sql = "SELECT t.transaction_id, t.account_id, t.transaction_type, t.amount, "
                   + "t.description, t.transaction_date, t.status "
                   + "FROM transactions t "
                   + "WHERE t.account_id = ? "
                   + "ORDER BY t.transaction_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction(
                        rs.getInt("account_id"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                    );

                    t.setTransactionId(rs.getInt("transaction_id"));
                    t.setTransactionDate(rs.getTimestamp("transaction_date"));
                    t.setStatus(rs.getString("status"));

                    list.add(t);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // DEPOSIT
    public static boolean deposit(int accountId, double amount) {
        if (amount <= 0) return false;

        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setInt(2, accountId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                Session.refreshBalance();
                insertLog(accountId, "DEPOSIT", amount, "Deposit ke rekening", "SUCCESS");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // WITHDRAW
    public static boolean withdraw(int accountId, double amount) {
        if (amount <= 0) return false;

        try (Connection conn = DBConnection.getConnection()) {
            String sqlAcc = "SELECT a.balance, a.account_type, ca.overdraft_limit "
                          + "FROM accounts a "
                          + "LEFT JOIN checking_accounts ca ON ca.account_id = a.account_id "
                          + "WHERE a.account_id = ?";
            PreparedStatement psAcc = conn.prepareStatement(sqlAcc);
            psAcc.setInt(1, accountId);
            ResultSet rs = psAcc.executeQuery();

            if (!rs.next()) return false;

            double balance = rs.getDouble("balance");
            String type = rs.getString("account_type");
            double overdraftLimit = rs.getDouble("overdraft_limit");
            if (rs.wasNull()) overdraftLimit = 0;

            // SAVINGS tidak boleh minus
            if (type.equalsIgnoreCase("SAVINGS") && amount > balance) {
                return false;
            }

            // CHECKING boleh minus sampai overdraft_limit
            if (type.equalsIgnoreCase("CHECKING")) {
                double limit = (overdraftLimit <= 0) ? 500000 : overdraftLimit;
                double newBalance = balance - amount;
                if (newBalance < -limit) return false;
            }

            // BUSINESS tidak boleh minus
            if (type.equalsIgnoreCase("BUSINESS") && amount > balance) {
                return false;
            }

            // Update saldo
            String sqlUpdate = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            PreparedStatement psUp = conn.prepareStatement(sqlUpdate);
            psUp.setDouble(1, amount);
            psUp.setInt(2, accountId);
            psUp.executeUpdate();

            // Update session
            Session.refreshBalance();

            insertLog(accountId, "WITHDRAWAL", amount, "Penarikan tunai", "SUCCESS");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // TRANSFER
    public static boolean transfer(int fromId, int toId, double amount) {
        if (fromId == toId || amount <= 0) return false;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Cek saldo pengirim
            String sqlCheck = "SELECT a.balance, a.account_type, ca.overdraft_limit "
                            + "FROM accounts a "
                            + "LEFT JOIN checking_accounts ca ON ca.account_id = a.account_id "
                            + "WHERE a.account_id = ?";
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setInt(1, fromId);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return false;
            }

            double balance = rs.getDouble("balance");
            String type = rs.getString("account_type");
            double overdraftLimit = rs.getDouble("overdraft_limit");
            if (rs.wasNull()) overdraftLimit = 0;

            // Validasi saldo
            if (type.equalsIgnoreCase("SAVINGS") && amount > balance) {
                conn.rollback();
                return false;
            }

            if (type.equalsIgnoreCase("CHECKING")) {
                double limit = (overdraftLimit <= 0) ? 500000 : overdraftLimit;
                if ((balance - amount) < -limit) {
                    conn.rollback();
                    return false;
                }
            }

            if (type.equalsIgnoreCase("BUSINESS") && amount > balance) {
                conn.rollback();
                return false;
            }

            // Kurangi saldo pengirim
            String sqlWithdraw = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            PreparedStatement psWithdraw = conn.prepareStatement(sqlWithdraw);
            psWithdraw.setDouble(1, amount);
            psWithdraw.setInt(2, fromId);
            psWithdraw.executeUpdate();

            // Tambah saldo penerima
            String sqlDeposit = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            PreparedStatement psDeposit = conn.prepareStatement(sqlDeposit);
            psDeposit.setDouble(1, amount);
            psDeposit.setInt(2, toId);
            int updated = psDeposit.executeUpdate();

            if (updated == 0) {
                conn.rollback();
                return false;
            }

            // Update session
            Session.refreshBalance();

            // Log transaksi
            insertLogWithConnection(conn, fromId, "TRANSFER", amount, 
                                  "Transfer ke akun ID: " + toId, "SUCCESS");
            insertLogWithConnection(conn, toId, "DEPOSIT", amount, 
                                  "Transfer dari akun ID: " + fromId, "SUCCESS");

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // LOG TRANSAKSI
    private static void insertLog(int accountId, String type, double amount, 
                                 String description, String status) {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description, status) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.setString(4, description);
            ps.setString(5, status);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertLogWithConnection(Connection conn, int accountId, 
                                               String type, double amount, 
                                               String description, String status) {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description, status) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.setString(4, description);
            ps.setString(5, status);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // MONTHLY UPDATE - Jalankan update bulanan untuk semua akun
    public static void runMonthlyUpdate() {
        try (Connection conn = DBConnection.getConnection()) {
            
            // Update bunga untuk SAVINGS account
            String sqlSaving = "UPDATE accounts SET balance = balance * 1.01 " +
                             "WHERE account_type = 'SAVINGS'";
            conn.prepareStatement(sqlSaving).executeUpdate();

            // Log bunga saving
            String sqlLogSaving = "INSERT INTO transactions (account_id, transaction_type, amount, description, status) " +
                                "SELECT account_id, 'DEPOSIT', balance * 0.01, " +
                                "'Bunga bulanan 1%', 'SUCCESS' FROM accounts WHERE account_type = 'SAVINGS'";
            conn.prepareStatement(sqlLogSaving).executeUpdate();

            // Biaya overdraft untuk CHECKING dengan saldo minus
            String sqlCheckingFee = "UPDATE accounts SET balance = balance - 10000 " +
                                  "WHERE account_type = 'CHECKING' AND balance < 0";
            conn.prepareStatement(sqlCheckingFee).executeUpdate();

            // Log biaya overdraft
            String sqlLogChecking = "INSERT INTO transactions (account_id, transaction_type, amount, description, status) " +
                                  "SELECT account_id, 'WITHDRAWAL', 10000, " +
                                  "'Biaya overdraft bulanan', 'SUCCESS' FROM accounts " +
                                  "WHERE account_type = 'CHECKING' AND balance < 0";
            conn.prepareStatement(sqlLogChecking).executeUpdate();

            System.out.println("Monthly update berhasil dijalankan!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}