package models;

import utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    /** 
     * LOGIN USER 
     */
    public static Account login(String username, String password) {

        String sql = "SELECT a.account_id, u.id AS user_id, u.username, "
                   + "a.account_number, a.type, a.balance, a.overdraft_limit "
                   + "FROM users u "
                   + "JOIN accounts a ON a.user_id = u.id "
                   + "WHERE u.username = ? AND u.password = ? "
                   + "LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("type");
                    int accountId = rs.getInt("account_id");
                    int userId = rs.getInt("user_id");
                    String uname = rs.getString("username");
                    String accNum = rs.getString("account_number");
                    double balance = rs.getDouble("balance");

                    // Return CheckingAccount jika type CHECKING
                    if (type.equalsIgnoreCase("CHECKING")) {
                        CheckingAccount acc = new CheckingAccount(
                            accountId, userId, uname, accNum, type, balance
                        );
                        acc.setOverdraftLimit(rs.getDouble("overdraft_limit"));
                        return acc;
                    }

                    // Return Account biasa untuk SAVING dan BUSINESS
                    return new Account(accountId, userId, uname, accNum, type, balance);
                }
            }

            System.out.println("Login gagal: username/password salah atau akun belum dibuat.");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * REGISTRASI USER BARU
     */
    public static boolean register(String fullName, String username, String password) {

        String sql = "INSERT INTO users (fullname, username, password) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, fullName);
            st.setString(2, username);
            st.setString(3, password);

            return st.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}