package utils;

import java.text.DecimalFormat;

public class Formatter {
    private static DecimalFormat df = new DecimalFormat("#,###.00");

    public static String money(double value) {
        return "Rp " + df.format(value);
    }

public static String formatAccountNumber(String accountNumber) {
        return String.format("%08d", Integer.parseInt(accountNumber));
    } 

public static String formatDateTime(java.sql.Timestamp timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(timestamp);
    }

    public static String formatCurrency(double amount) {
        return "Rp " + df.format(amount);
    }
}