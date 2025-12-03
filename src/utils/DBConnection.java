package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/bank_app?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {

        String url = "jdbc:mysql://localhost:3306/bank_app";
        String user = "root";
        String pass = "";
        Class.forName("com.mysql.cj.jdbc.Driver");
            // Return connection
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Gagal konek database: " + e.getMessage());
        }

        return null;
    }
}
