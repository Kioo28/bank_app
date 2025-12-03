package models;

public class CheckingAccount extends Account {

    private double overdraftLimit = 500000.0; 

    public CheckingAccount(int accountId, int userId, String username,
                           String accountNumber, String accountType, double balance) {
        super(accountId, userId, username, accountNumber, accountType, balance);
    }

    public CheckingAccount() {
        super();
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) return false;

        // cek apakah melewati overdraft
        double newBalance = balance - amount;

        if (newBalance < -overdraftLimit) {
            return false;
        }

        // hanya ubah balance di memory
        this.balance = newBalance;

        return true; // pencatatan transaksi dilakukan di TransactionView
    }

    public double getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(double overdraftLimit) { this.overdraftLimit = overdraftLimit; }
}
