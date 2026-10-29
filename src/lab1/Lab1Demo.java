package lab1;

public class Lab1Demo {
    public static void main(String[] args) {
        TransactionManager manager = new TransactionManager();

        manager.registerCustomer(new Customer(1, "Sheila", "0788000000", "1234"));
        manager.openAccount(1, new WalletAccount("W-001", "Sheila", 200, "RWF", "ACTIVE"));
        manager.openAccount(1, new SavingsAccount("S-001", "Sheila", 200, "RWF", "ACTIVE"));

        Transaction t1 = new Transaction(1, "TXN-001", 500, "DEPOSIT", System.currentTimeMillis());
        System.out.println(manager.processTransaction("W-001", t1));   // true
        System.out.println(manager.processTransaction("W-001", t1));   // false (duplicate)
        System.out.println(manager.getAccount("W-001").getBalance());  // 700.0

        Transaction t3 = new Transaction(3, "TXN-003", 100, "WITHDRAW", System.currentTimeMillis());
        System.out.println(manager.processTransaction("S-001", t3));   // true
        System.out.println(manager.getAccount("S-001").getBalance());  // 50.0 (100 + 50 fee)

        System.out.println(manager.getFailedTransactionLog());
    }
}