package models;

public class BusinessAccount extends Account {

    private double transactionLimit = 100000000.0; // 100 juta per transaksi

    public BusinessAccount(int accountId, int userId, String username, 
                          String accountNumber, double balance) {
        super(accountId, userId, username, accountNumber, "BUSINESS", balance);
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount > transactionLimit) {
            System.out.println("Melebihi limit transaksi bisnis!");
            return false;
        }
        return super.withdraw(amount);
    }

    @Override
    public void monthlyUpdate() {
        // Business account tidak ada update khusus
    }

    public double getTransactionLimit() {
        return transactionLimit;
    }

    public void setTransactionLimit(double transactionLimit) {
        this.transactionLimit = transactionLimit;
    }
}