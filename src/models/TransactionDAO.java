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
                   + "t.description, t.transaction_date, t.status, a.account_number "
                   + "FROM transactions t "
                   + "JOIN accounts a ON t.account_id = a.account_id "
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

    // ============================
    // DEPOSIT
    // ============================
    public static boolean deposit(int accountId, double amount) {
        if (amount <= 0) return false;

        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, amount);
            ps.setInt(2, accountId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                // update session
                Session.getCurrentAccount().setBalance(
                    Session.getCurrentAccount().getBalance() + amount
                );

                insertLog(accountId, "DEPOSIT", amount, "Deposit", "SUCCESS");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ============================
    // WITHDRAW
    // ============================
    public static boolean withdraw(int accountId, double amount) {
        if (amount <= 0) return false;

        try (Connection conn = DBConnection.getConnection()) {
            String sqlAcc = "SELECT balance, type, overdraft_limit FROM accounts WHERE account_id = ?";
            PreparedStatement psAcc = conn.prepareStatement(sqlAcc);
            psAcc.setInt(1, accountId);
            ResultSet rs = psAcc.executeQuery();

            if (!rs.next()) return false;

            double balance = rs.getDouble("balance");
            String type = rs.getString("type");
            double overdraftLimit = rs.getDouble("overdraft_limit");

            // Saving tidak boleh minus
            if (type.equalsIgnoreCase("SAVING") && amount > balance) {
                return false;
            }

            // Checking boleh minus sampai overdraft_limit
            if (type.equalsIgnoreCase("CHECKING")) {
                double limit = (overdraftLimit <= 0) ? 500_000 : overdraftLimit;
                double newBalance = balance - amount;
                if (newBalance < -limit) return false;
            }

            // update saldo
            String sqlUpdate = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            PreparedStatement psUp = conn.prepareStatement(sqlUpdate);
            psUp.setDouble(1, amount);
            psUp.setInt(2, accountId);
            psUp.executeUpdate();

            // update session
            Session.getCurrentAccount().setBalance(
                Session.getCurrentAccount().getBalance() - amount
            );

            insertLog(accountId, "WITHDRAW", amount, "Withdraw", "SUCCESS");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================
    // TRANSFER
    // ============================
    public static boolean transfer(int fromId, int toId, double amount) {
        if (fromId == toId || amount <= 0) return false;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            boolean withdrawSuccess = withdraw(fromId, amount);
            if (!withdrawSuccess) {
                conn.rollback();
                return false;
            }

            String sqlDeposit = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            PreparedStatement ps = conn.prepareStatement(sqlDeposit);
            ps.setDouble(1, amount);
            ps.setInt(2, toId);
            ps.executeUpdate();

            // update session pengirim
            Session.getCurrentAccount().setBalance(
                Session.getCurrentAccount().getBalance() - amount
            );

            insertLog(fromId, "TRANSFER", amount, "Transfer ke akun " + toId, "SUCCESS");
            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(true);
            } catch (Exception ignored) {}
        }
    }

    // ============================
    // LOG TRANSAKSI
    // ============================
    private static void insertLog(int accountId, String type, double amount, String description, String status) {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description, status) VALUES (?, ?, ?, ?, ?)";
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
}
