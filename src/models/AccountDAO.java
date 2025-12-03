package models;

import java.sql.*;
import models.Account;
import utils.DBConnection;

public class AccountDAO {
//    insert akun baru ke database
    public static boolean createAccount(Account account) {
        String sql = "INSERT INTO accounts (user_id, username, account_number, type, balance) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, account.userId);
            pstmt.setString(2, account.username);
            pstmt.setString(3, account.accountNumber);
            pstmt.setString(4, account.Type);
            pstmt.setDouble(5, account.balance);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //    ambil akun berdasarkan userId
    public static Account getAccountByUserId(int userId) {
    String sql = "SELECT * FROM accounts WHERE user_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {

            String type = rs.getString("type");
            int accountId = rs.getInt("account_id");
            int uid = rs.getInt("user_id");
            String username = rs.getString("username");
            String accNum = rs.getString("account_number");
            double balance = rs.getDouble("balance");

            // ==== PENTING: Pilih class sesuai tipe ====
            if (type.equalsIgnoreCase("CHECKING")) {

                double limit = rs.getDouble("overdraft_limit"); // kalau ada
                CheckingAccount acc = new CheckingAccount(
                        accountId,
                        uid,
                        username,
                        accNum,
                        type,
                        balance
                );

                // set overdraft dari DB jika kamu ingin pakai kolom tersebut
                acc.setOverdraftLimit(limit);

                return acc;
            }

            // default: SAVING / BUSINESS / lainnya adalah Account biasa
            return new Account(
                    accountId,
                    uid,
                    username,
                    accNum,
                    type,
                    balance
            );
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

public static Account getAccountById(int accountId) {
    String sql = "SELECT * FROM accounts WHERE account_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, accountId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String type = rs.getString("type");
            int uid = rs.getInt("user_id");
            String username = rs.getString("username");
            String accNum = rs.getString("account_number");
            double balance = rs.getDouble("balance");

            if (type.equalsIgnoreCase("CHECKING")) {
                double limit = rs.getDouble("overdraft_limit");
                CheckingAccount acc = new CheckingAccount(
                        accountId, uid, username, accNum, type, balance
                );
                acc.setOverdraftLimit(limit);
                return acc;
            }

            return new Account(accountId, uid, username, accNum, type, balance);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}


    //    update saldo akun
    public static boolean updateAccountBalance(int accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, accountId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
