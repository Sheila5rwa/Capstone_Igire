package lab1;

public class WalletAccount extends Account {

    public WalletAccount(String accountNumber, String accountOwnerName, double balance, String currency, String status) {
        super(accountNumber, accountOwnerName, balance, currency, status);
    }

    @Override
    public boolean withdraw(double amount) {
        boolean success = super.withdraw(amount);
        if (success) {
            System.out.println("Instant transfer completed");
        }
        return success;
    }

    @Override
    public String toString() {
        return "Lab1.WalletAccount{" + super.toString() + "}";
    }
}