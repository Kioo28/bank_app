package models;

public class BusineesAccount extends Account {

    public BusineesAccount(int accountId,int userId, String username, String num, double initial) {
        super(accountId, userId, username,   num, "Business", initial);
    }

    @Override
    public void monthlyUpdate() {
        // Tidak ada fitur khusus
    }
}
