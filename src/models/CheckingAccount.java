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

        // Cek apakah melewati overdraft
        double newBalance = balance - amount;

        if (newBalance < -overdraftLimit) {
            return false;
        }

        this.balance = newBalance;
        return true;
    }

    @Override
    public void monthlyUpdate() {
        // Checking account bisa dikenakan biaya bulanan jika saldo minus
        if (balance < 0) {
            double fee = 10000; // Biaya overdraft Rp 10.000
            balance -= fee;
            addTransaction(new Transaction(
                accountId,
                "WITHDRAW",
                fee,
                "Biaya overdraft bulanan"
            ));
        }
    }

    public double getOverdraftLimit() { 
        return overdraftLimit; 
    }
    
    public void setOverdraftLimit(double overdraftLimit) { 
        this.overdraftLimit = overdraftLimit; 
    }
}