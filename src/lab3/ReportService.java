package lab3;

import lab1.Account;
import lab1.Transaction;
import lab2.AccountDAO;
import lab2.DatabaseConnection;
import lab2.TransactionDAO;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;

public class ReportService {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public void exportCsv(int accountId, String path) throws SQLException, IOException {
        try (Connection con = DatabaseConnection.getConnection();
             PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.println("id,reference_id,type,amount,timestamp");
            for (Transaction t : transactionDAO.findByAccount(con, accountId)) {
                out.println(t.getId() + "," + t.getReferenceId() + "," + t.getTransactionType() + ","
                        + t.getAmount() + "," + Instant.ofEpochMilli(t.getTimeStamp()));
            }
        }
    }

    public void printDailySummary() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            System.out.println("Date | Type | Count | Total");
            for (String line : transactionDAO.dailySummary(con)) {
                System.out.println(line);
            }
        }
    }

    public void printStatement(int customerId) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            for (Account a : accountDAO.findByCustomer(con, customerId)) {
                System.out.println("\nAccount " + a.getAccountNumber() + " (" + a.getClass().getSimpleName()
                        + ") balance: " + a.getBalance());
                for (Transaction t : transactionDAO.findByAccount(con, Integer.parseInt(a.getAccountNumber()))) {
                    System.out.println("   " + t);
                }
            }
        }
    }
}