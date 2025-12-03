package models;

public class SavingAccount extends Account {

    private double interestRate = 0.01; // 1% per bulan

    public SavingAccount(int accountId, int userId, String username,
               String accountNumber, String accountType, double balance) {
        super(accountId, userId, username, accountNumber, accountType, balance);
    }

    @Override
    public void monthlyUpdate() {
        double interest = balance * interestRate;
        balance += interest;

        addTransaction(new Transaction(
            accountId,
            "INTEREST",
            interest,
            "Bunga bulanan " + (interestRate * 100) + "%"
        ));
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
}