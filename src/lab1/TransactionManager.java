package lab1;

import java.util.*;

public class TransactionManager {

    private Map<Integer, Customer> customers = new HashMap<>();
    private Map<String, Account> accounts = new HashMap<>();
    private Map<Integer, List<Account>> customerAccounts = new HashMap<>();

    private List<Transaction> transactionHistory = new ArrayList<>();
    private Set<String> processedReferenceIds = new HashSet<>();
    private List<String> failedTransactionLog = new ArrayList<>();

    public boolean registerCustomer(Customer customer) {
        if (customer == null || customers.containsKey(customer.getCustomerId())) {
            return false;
        }
        customers.put(customer.getCustomerId(), customer);
        customerAccounts.put(customer.getCustomerId(), new ArrayList<>());
        return true;
    }

    public boolean openAccount(int customerId, Account account) {
        if (account == null
                || !customers.containsKey(customerId)
                || accounts.containsKey(account.getAccountNumber())) {
            return false;
        }
        accounts.put(account.getAccountNumber(), account);
        customerAccounts.get(customerId).add(account);
        return true;
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public List<Account> getAccountsForCustomer(int customerId) {
        List<Account> list = customerAccounts.get(customerId);
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    // Deposit or withdraw on one account, with duplicate protection
    public boolean processTransaction(String accountNumber, Transaction transaction) {
        if (transaction == null) {
            failedTransactionLog.add("Null transaction");
            return false;
        }
        Account account = accounts.get(accountNumber);
        if (account == null) {
            failedTransactionLog.add(transaction.getReferenceId() + ": account not found");
            return false;
        }
        if (processedReferenceIds.contains(transaction.getReferenceId())) {
            failedTransactionLog.add(transaction.getReferenceId() + ": duplicate rejected");
            return false;
        }

        boolean success = account.processTransaction(transaction);

        if (success) {
            processedReferenceIds.add(transaction.getReferenceId());
            transactionHistory.add(transaction);
        } else {
            failedTransactionLog.add(transaction.getReferenceId() + ": failed (invalid or insufficient funds)");
        }
        return success;
    }

    // Send money from one account to another
    public boolean transfer(String fromAccount, String toAccount, double amount, String referenceId) {
        Account from = accounts.get(fromAccount);
        Account to = accounts.get(toAccount);

        if (from == null || to == null || amount <= 0 || fromAccount.equals(toAccount)) {
            failedTransactionLog.add(referenceId + ": invalid transfer");
            return false;
        }
        if (processedReferenceIds.contains(referenceId)) {
            failedTransactionLog.add(referenceId + ": duplicate rejected");
            return false;
        }
        if (!from.withdraw(amount)) {
            failedTransactionLog.add(referenceId + ": insufficient funds");
            return false;
        }
        to.deposit(amount);

        processedReferenceIds.add(referenceId);
        transactionHistory.add(new Transaction(transactionHistory.size() + 1, referenceId,
                amount, "TRANSFER", System.currentTimeMillis()));
        return true;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public List<String> getFailedTransactionLog() {
        return failedTransactionLog;
    }
}