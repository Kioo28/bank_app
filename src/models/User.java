package models;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utils.DBConnection;

public class User {
    public int userId;
    private String fullname;
    private String username;
    private String password;
    private List<Account> accounts = new ArrayList<>();

    public User(int userId, String fullname, String username, String password) {
        this.userId = userId;
        this.fullname = fullname;
        this.username = username;
        this.password = password;
    }

    public User() {}

     public static boolean register(String fullname, String username, String password) {
        try {
            Connection conn = DBConnection.getConnection();

            String sql = "INSERT INTO users(fullname, username, password) VALUES (?, ?, ?)";
            PreparedStatement st = conn.prepareStatement(sql);
            st.setString(1, fullname);
            st.setString(2, username);
            st.setString(3, password);

            return st.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static User login(String username, String password) {
        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement st = conn.prepareStatement(sql);

            st.setString(1, username);
            st.setString(2, password);

            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("fullname"),
                    rs.getString("username"),
                    rs.getString("password")
                );
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }   

 

   // GETTERS
    public int getId() { return userId; }
    public String getFullName() { return fullname; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // SETTERS
    public void setId(int userId) { this.userId = userId; }
    public void setFullName(String fullname) { this.fullname = fullname; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}