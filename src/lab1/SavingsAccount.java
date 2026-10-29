package lab1;

public class SavingsAccount extends Account {

    private double withdrawalFee = 50;

    public SavingsAccount(String accountNumber, String accountOwnerName, double balance, String currency, String status) {
        super(accountNumber, accountOwnerName, balance, currency, status);
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            return false;
        }
        double totalCost = amount + withdrawalFee;
        return super.withdraw(totalCost);
    }

    @Override
    public String toString() {
        return "Lab1.SavingsAccount{" + super.toString() + ", withdrawalFee=" + withdrawalFee + "}";
    }
}