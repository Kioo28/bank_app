package models;

public class SavingAccount extends Account {

    private double interestRate = 0.02; // 2%

    public SavingAccount(int accountId, int userId, String username,
               String accountNumber, String accountType, double balance) {
        super(accountId, userId, username, accountNumber, accountType, balance);
    }

    @Override
public void monthlyUpdate() {
    double interest = balance * 0.01;  // 1% / bulan
    balance += interest;

    addTransaction(new Transaction(
        accountId,
        "INTEREST",
        interest,
        "Monthly interest added"
    ));
}

}
