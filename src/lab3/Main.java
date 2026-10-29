package lab3;

import lab1.Account;
import lab1.Customer;
import lab1.Transaction;
import lab2.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static final Scanner in = new Scanner(System.in);
    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static final AccountDAO accountDAO = new AccountDAO();
    private static final TransactionDAO transactionDAO = new TransactionDAO();
    private static final PaymentService payments = new PaymentService();
    private static final ReportService reports = new ReportService();

    private static Customer current;

    private interface Action { void go() throws Exception; }

    public static void main(String[] args) {
        try (Connection c = DatabaseConnection.getConnection()) {
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.out.println("Cannot connect to the database: " + e.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\n=== IgirePay ===");
            System.out.println("1. Register customer");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            switch (ask("Choose: ")) {
                case "1" -> run(Main::register);
                case "2" -> run(Main::login);
                case "0" -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
        System.out.println("Goodbye.");
    }

    private static String ask(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private static void run(Action a) {
        try {
            a.go();
        } catch (PaymentException e) {
            System.out.println("Refused: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static int askOwnAccount() throws SQLException, PaymentException {
        int id = Integer.parseInt(ask("Account ID: "));
        try (Connection con = DatabaseConnection.getConnection()) {
            for (Account a : accountDAO.findByCustomer(con, current.getCustomerId())) {
                if (a.getAccountNumber().equals(String.valueOf(id))) {
                    return id;
                }
            }
        }
        throw new InvalidAccountException("Account " + id + " is not one of your accounts.");
    }

    private static void register() throws Exception {
        String name = ask("Full name: ");
        String email = ask("Email: ");
        String phone = ask("Phone number: ");
        String pin = ask("Choose a 4-digit PIN: ");
        if (!PinUtil.isValidFormat(pin)) {
            System.out.println("PIN must be exactly 4 digits.");
            return;
        }
        try (Connection con = DatabaseConnection.getConnection()) {
            int id = customerDAO.create(con, new Customer(0, name, email, phone, ""), PinUtil.hash(pin));
            System.out.println("Registered. Your customer ID is " + id + " (use it to log in).");
        }
    }

    private static void login() throws Exception {
        int id = Integer.parseInt(ask("Customer ID: "));
        String pin = ask("PIN: ");
        try (Connection con = DatabaseConnection.getConnection()) {
            String hash = customerDAO.getPinHash(con, id);
            if (hash == null || !hash.equals(PinUtil.hash(pin))) {
                System.out.println("Invalid customer ID or PIN.");
                return;
            }
            current = customerDAO.findById(con, id);
        }
        System.out.println("Welcome, " + current.getCustomerName() + "!");
        customerMenu();
        current = null;
    }

    private static void customerMenu() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- Menu ---");
            System.out.println(" 1. Update my information     8. Deposit money");
            System.out.println(" 2. View my accounts          9. Withdraw money");
            System.out.println(" 3. Create wallet account    10. Transfer money");
            System.out.println(" 4. Create savings account   11. Transaction history");
            System.out.println(" 5. View account balance     12. Export history to CSV");
            System.out.println(" 6. Deactivate account       13. Daily summary");
            System.out.println(" 7. Delete inactive account  14. My statement");
            System.out.println("15. Change PIN                0. Logout");
            switch (ask("Choose: ")) {
                case "1" -> run(Main::updateInfo);
                case "2" -> run(Main::viewAccounts);
                case "3" -> run(() -> createAccount("WALLET"));
                case "4" -> run(() -> createAccount("SAVINGS"));
                case "5" -> run(Main::viewBalance);
                case "6" -> run(Main::deactivate);
                case "7" -> run(Main::deleteInactive);
                case "8" -> run(Main::deposit);
                case "9" -> run(Main::withdraw);
                case "10" -> run(Main::transfer);
                case "11" -> run(Main::history);
                case "12" -> run(Main::exportCsv);
                case "13" -> run(reports::printDailySummary);
                case "14" -> run(() -> reports.printStatement(current.getCustomerId()));
                case "15" -> run(Main::changePin);
                case "0" -> loggedIn = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void updateInfo() throws Exception {
        String name = ask("New full name: ");
        String email = ask("New email: ");
        String phone = ask("New phone number: ");
        try (Connection con = DatabaseConnection.getConnection()) {
            customerDAO.update(con, current.getCustomerId(), name, email, phone);
            current = customerDAO.findById(con, current.getCustomerId());
        }
        System.out.println("Information updated.");
    }



    private static void viewAccounts() throws Exception {
        try (Connection con = DatabaseConnection.getConnection()) {
            var list = accountDAO.findByCustomer(con, current.getCustomerId());
            if (list.isEmpty()) {
                System.out.println("You have no accounts yet.");
            }
            for (Account a : list) {
                System.out.println("ID " + a.getAccountNumber() + " | " + a.getClass().getSimpleName()
                        + " | balance " + a.getBalance() + " | " + a.getStatus());
            }
        }
    }

    private static void createAccount(String type) throws Exception {
        try (Connection con = DatabaseConnection.getConnection()) {
            int id = accountDAO.create(con, current.getCustomerId(), type);
            System.out.println(type + " account created. Account ID: " + id);
        }
    }

    private static void viewBalance() throws Exception {
        int id = askOwnAccount();
        try (Connection con = DatabaseConnection.getConnection()) {
            Account a = accountDAO.findById(con, id);
            System.out.println("Balance: " + a.getBalance() + " " + a.getCurrency());
        }
    }

    private static void deactivate() throws Exception {
        int id = askOwnAccount();
        try (Connection con = DatabaseConnection.getConnection()) {
            accountDAO.updateStatus(con, id, "INACTIVE");
        }
        System.out.println("Account " + id + " is now INACTIVE.");
    }

    private static void deleteInactive() throws Exception {
        int id = askOwnAccount();
        try (Connection con = DatabaseConnection.getConnection()) {
            if (accountDAO.deleteInactive(con, id)) {
                System.out.println("Account deleted.");
            } else {
                System.out.println("Only INACTIVE accounts can be deleted. Deactivate it first.");
            }
        }
    }

    private static void deposit() throws Exception {
        int id = askOwnAccount();
        double amount = Double.parseDouble(ask("Amount: "));
        String ref = ask("Reference ID: ");
        payments.deposit(id, amount, ref);
        System.out.println("Deposit successful.");
    }

    private static void withdraw() throws Exception {
        int id = askOwnAccount();
        double amount = Double.parseDouble(ask("Amount: "));
        String ref = ask("Reference ID: ");
        payments.withdraw(id, amount, ref);
        System.out.println("Withdrawal successful.");
    }

    private static void transfer() throws Exception {
        System.out.println("From which of your accounts?");
        int from = askOwnAccount();
        int to = Integer.parseInt(ask("Destination account ID: "));
        double amount = Double.parseDouble(ask("Amount: "));
        String ref = ask("Reference ID: ");
        payments.transfer(from, to, amount, ref);
        System.out.println("Transfer successful.");
    }

    private static void history() throws Exception {
        int id = askOwnAccount();
        try (Connection con = DatabaseConnection.getConnection()) {
            var list = transactionDAO.findByAccount(con, id);
            if (list.isEmpty()) {
                System.out.println("No transactions yet.");
            }
            for (Transaction t : list) {
                System.out.println(t);
            }
        }
    }

    private static void exportCsv() throws Exception {
        int id = askOwnAccount();
        String path = "transactions_account_" + id + ".csv";
        reports.exportCsv(id, path);
        System.out.println("Exported to " + path + " (in your project folder).");
    }

    private static void changePin() throws Exception {
        String oldPin = ask("Current PIN: ");
        String newPin = ask("New 4-digit PIN: ");
        if (!PinUtil.isValidFormat(newPin)) {
            System.out.println("PIN must be exactly 4 digits.");
            return;
        }
        try (Connection con = DatabaseConnection.getConnection()) {
            String hash = customerDAO.getPinHash(con, current.getCustomerId());
            if (!PinUtil.hash(oldPin).equals(hash)) {
                System.out.println("Current PIN is wrong.");
                return;
            }
            customerDAO.updatePin(con, current.getCustomerId(), PinUtil.hash(newPin));
        }
        System.out.println("PIN changed.");
    }
}