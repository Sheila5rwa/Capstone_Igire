package lab1;

import java.util.HashSet;
import java.util.Set;

public class Account {
    private String accountNumber;
    private String accountOwnerName;
    private double balance;
    private String currency;
    private String status;
    private Set<String> processedReferences = new HashSet<>();

    public Account(String accountNumber, String accountOwnerName, double balance, String currency, String status) {
        this.accountNumber = accountNumber;
        this.accountOwnerName = accountOwnerName;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountOwnerName() { return accountOwnerName; }
    public void setAccountOwnerName(String accountOwnerName) { this.accountOwnerName = accountOwnerName; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        balance = balance + amount;
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance = balance - amount;
        return true;
    }

    public boolean processTransaction(Transaction transaction) {
        // 1. Validate
        if (transaction == null || transaction.getAmount() <= 0) {
            return false;
        }
        // 2. Duplicate check
        if (processedReferences.contains(transaction.getReferenceId())) {
            return false;
        }
        // 3. Move the money
        boolean success = false;
        String type = transaction.getTransactionType();
        double amount = transaction.getAmount();

        if ("DEPOSIT".equals(type)) {
            success = deposit(amount);
        } else if ("WITHDRAW".equals(type)) {
            success = withdraw(amount);
        }
        // 4. Remember the reference only if it worked
        if (success) {
            processedReferences.add(transaction.getReferenceId());
        }
        // 5. Return the result
        return success;
    }

    public boolean isReferenceProcessed(String referenceId) {
        return processedReferences.contains(referenceId);
    }

    @Override
    public String toString() {
        return "Lab1.Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", accountOwnerName='" + accountOwnerName + '\'' +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}