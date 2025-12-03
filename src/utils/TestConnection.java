package utils;

import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();
        System.out.println("CONNECTION = " + conn);
    }
}

