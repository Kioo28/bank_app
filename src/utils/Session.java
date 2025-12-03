package utils;

import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import models.Account;

public class Session {

        // ----------------------------
    // REFRESH SALDO DARI DATABASE
    // ----------------------------
    public static void refreshBalance() {
        if (currentAccount != null) {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id=?"
                );
                ps.setInt(1, currentAccount.getAccountId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentAccount.setBalance(rs.getDouble("balance"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}

    public static User currentUser;
    public static Account currentAccount;
    

    public static void setUser(User user) {
        currentUser = user;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    

    // Login user ke session
    public static void login(User user) {
        currentUser = user;
    }

    // Pilih akun yang sedang aktif
    public static void setCurrentAccount(Account acc) {
        currentAccount = acc;
    }
    
    public static void setAccount(Account acc) {
        currentAccount = acc;
    } 



    // Logout user
    public static void logout() {
        currentUser = null;
        currentAccount = null;
    }

    // ----------------------------
    //  CHECK STATUS SESSION
    // ----------------------------

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasSelectedAccount() {
        return currentAccount != null;
    }

    // ----------------------------
    //  GETTER USER
    // ----------------------------

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public static String getCurrentUserPassword() {
        return currentUser != null ? currentUser.getPassword() : null;
    }

    // ----------------------------
    //  GETTER ACCOUNT
    // ----------------------------

    public static Account getCurrentAccount() {
        return currentAccount;
    }

    // ----------------------------
    //  BALANCE HELPER
    // ----------------------------

    public static String getBalance() {
        if (currentAccount != null) {
            return Formatter.money(currentAccount.getBalance());
        }
        return Formatter.money(0);
    }
}
