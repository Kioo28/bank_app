package models;

import java.sql.*;
import utils.DBConnection;

public class AccountDAO {

    public static boolean createAccount(Account account) {
        String sql = "INSERT INTO accounts (user_id, account_number, account_type, balance) " +
                    "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, account.userId);
            pstmt.setString(2, account.accountNumber);
            pstmt.setString(3, account.Type);
            pstmt.setDouble(4, account.balance);

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int accountId = rs.getInt(1);
                    
                    // Create entry in specific account type table
                    if (account instanceof CheckingAccount) {
                        CheckingAccount chk = (CheckingAccount) account;
                        PreparedStatement psChk = conn.prepareStatement(
                            "INSERT INTO checking_accounts (account_id, overdraft_limit, monthly_fee) VALUES (?, ?, ?)"
                        );
                        psChk.setInt(1, accountId);
                        psChk.setDouble(2, chk.getOverdraftLimit());
                        psChk.setDouble(3, 5000.0);
                        psChk.executeUpdate();
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Account getAccountByUserId(int userId) {
        String sql = "SELECT a.*, u.username FROM accounts a " +
                    "JOIN users u ON a.user_id = u.user_id " +
                    "WHERE a.user_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String type = rs.getString("account_type");
                int accountId = rs.getInt("account_id");
                int uid = rs.getInt("user_id");
                String username = rs.getString("username");
                String accNum = rs.getString("account_number");
                double balance = rs.getDouble("balance");

                if (type.equalsIgnoreCase("CHECKING")) {
                    // Get overdraft limit from checking_accounts table
                    PreparedStatement psChk = conn.prepareStatement(
                        "SELECT overdraft_limit FROM checking_accounts WHERE account_id = ?"
                    );
                    psChk.setInt(1, accountId);
                    ResultSet rsChk = psChk.executeQuery();
                    
                    CheckingAccount acc = new CheckingAccount(
                            accountId, uid, username, accNum, type, balance
                    );
                    
                    if (rsChk.next()) {
                        acc.setOverdraftLimit(rsChk.getDouble("overdraft_limit"));
                    }
                    return acc;
                }

                return new Account(accountId, uid, username, accNum, type, balance);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Account getAccountById(int accountId) {
        String sql = "SELECT a.*, u.username FROM accounts a " +
                    "JOIN users u ON a.user_id = u.user_id " +
                    "WHERE a.account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String type = rs.getString("account_type");
                int aid = rs.getInt("account_id");
                int uid = rs.getInt("user_id");
                String username = rs.getString("username");
                String accNum = rs.getString("account_number");
                double balance = rs.getDouble("balance");

                if (type.equalsIgnoreCase("CHECKING")) {
                    // Get overdraft limit from checking_accounts table
                    PreparedStatement psChk = conn.prepareStatement(
                        "SELECT overdraft_limit FROM checking_accounts WHERE account_id = ?"
                    );
                    psChk.setInt(1, aid);
                    ResultSet rsChk = psChk.executeQuery();
                    
                    CheckingAccount acc = new CheckingAccount(
                            aid, uid, username, accNum, type, balance
                    );
                    
                    if (rsChk.next()) {
                        acc.setOverdraftLimit(rsChk.getDouble("overdraft_limit"));
                    }
                    return acc;
                }

                return new Account(aid, uid, username, accNum, type, balance);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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