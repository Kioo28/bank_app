package models;

import utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    /** 
     * LOGIN USER 
     * Mengembalikan Account jika username + password valid dan user memiliki akun.
     */
    public static Account login(String username, String password) {

        String sql = "SELECT a.account_id, u.id AS user_id, u.username, "
                   + "a.account_number, a.type, a.balance "
                   + "FROM users u "
                   + "JOIN accounts a ON a.user_id = u.id "
                   + "WHERE u.username = ? AND u.password = ? "
                   + "LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            // Debug query
            System.out.println("SQL Executed: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                        rs.getInt("account_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("account_number"),
                        rs.getString("type"),
                        rs.getDouble("balance")
                    );
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
