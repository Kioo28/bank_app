package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.Formatter;

public class Account {

    protected int accountId;
    protected int userId;
    protected String username;
    protected String accountNumber;
    protected String Type;
    protected double balance;
    protected String ownerName;
    protected List<Transaction> transactions = new ArrayList<>();

    public Account(int accountId, int userId, String username,
               String accountNumber, String Type, double balance) {
        this.accountId = accountId;
        this.userId = userId;
        this.username = username;
        this.accountNumber = accountNumber;
        this.Type = Type;
        this.balance = balance;
    }

    public Account() {}

    // GET ACCOUNT LIST FOR USER
    public static ArrayList<Account> getUserAccounts(int userId) {
        ArrayList<Account> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT a.*, u.username AS owner FROM accounts a " +
                        "JOIN users u ON a.user_id = u.user_id WHERE a.user_id = ?";
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                Account acc;
                String type = rs.getString("account_type");
                
                if (type.equals("CHECKING")) {
                    CheckingAccount chk = new CheckingAccount();
                    // Load overdraft limit dari checking_accounts table
                    try {
                        PreparedStatement ps = conn.prepareStatement(
                            "SELECT overdraft_limit FROM checking_accounts WHERE account_id = ?"
                        );
                        ps.setInt(1, rs.getInt("account_id"));
                        ResultSet rsChk = ps.executeQuery();
                        if (rsChk.next()) {
                            chk.setOverdraftLimit(rsChk.getDouble("overdraft_limit"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    acc = chk;
                } else {
                    acc = new Account();
                }

                acc.accountId = rs.getInt("account_id");
                acc.userId = rs.getInt("user_id");
                acc.username = rs.getString("owner");
                acc.accountNumber = rs.getString("account_number");
                acc.Type = type;
                acc.balance = rs.getDouble("balance");
                list.add(acc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // CREATE NEW ACCOUNT
    public static boolean createAccount(int userId, String type) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "INSERT INTO accounts (user_id, account_number, account_type, balance) VALUES (?, ?, ?, 0)";
            PreparedStatement st = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            String randomNumber = String.valueOf(System.currentTimeMillis() % 100000000);
            st.setInt(1, userId);
            st.setString(2, Formatter.formatAccountNumber(randomNumber));
            st.setString(3, type);
            
            int result = st.executeUpdate();
            
            if (result > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int accountId = rs.getInt(1);
                    
                    // Create entry in specific account type table
                    if (type.equalsIgnoreCase("CHECKING")) {
                        PreparedStatement psChk = conn.prepareStatement(
                            "INSERT INTO checking_accounts (account_id, overdraft_limit, monthly_fee) VALUES (?, ?, ?)"
                        );
                        psChk.setInt(1, accountId);
                        psChk.setDouble(2, 500000.0);
                        psChk.setDouble(3, 5000.0);
                        psChk.executeUpdate();
                    } else if (type.equalsIgnoreCase("SAVINGS")) {
                        PreparedStatement psSav = conn.prepareStatement(
                            "INSERT INTO savings_accounts (account_id, interest_rate, minimum_balance) VALUES (?, ?, ?)"
                        );
                        psSav.setInt(1, accountId);
                        psSav.setDouble(2, 2.5);
                        psSav.setDouble(3, 1000.0);
                        psSav.executeUpdate();
                    } else if (type.equalsIgnoreCase("BUSINESS")) {
                        PreparedStatement psBus = conn.prepareStatement(
                            "INSERT INTO business_accounts (account_id, business_name, transaction_limit) VALUES (?, ?, ?)"
                        );
                        psBus.setInt(1, accountId);
                        psBus.setString(2, "Business Account");
                        psBus.setDouble(3, 100000000.0);
                        psBus.executeUpdate();
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // GETTERS
    public int getUserId() { return userId; }
    public int getAccountId() { return accountId; }
    public String getAccountNumber() { return accountNumber; }
    public String getType() { return Type; }
    public double getBalance() { return balance; }
    public String getOwnerName() { return ownerName; }
    public List<Transaction> getTransactions() { return transactions; }

    protected void addTransaction(Transaction tx) {
        transactions.add(tx);
    }

    public void setBalance(double newBalance) {
        this.balance = newBalance;
    }

    // BASIC TRANSACTIONS
    public void deposit(double amount) {
        if (amount <= 0) return;
        balance += amount;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0) return false;
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public boolean transfer(Account target, double amount) {
        if (withdraw(amount)) {
            target.deposit(amount);
            return true;
        }
        return false;
    }

    // MONTHLY UPDATE (OVERRIDE IN SUBCLASSES)
    public void monthlyUpdate() {
        // Default: tidak ada update khusus
    }
}