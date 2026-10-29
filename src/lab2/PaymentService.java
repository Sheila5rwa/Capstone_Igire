package lab2;

import lab1.Account;
import java.sql.Connection;
import java.sql.SQLException;

public class PaymentService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ProcessedRequestDAO processedDAO = new ProcessedRequestDAO();

    private interface Work {
        void run(Connection con) throws PaymentException, SQLException;
    }

    public void deposit(int accountId, double amount, String ref) throws PaymentException, SQLException {
        validateAmount(amount);
        inTransaction(con -> {
            checkNotDuplicate(con, ref);
            Account acc = load(con, accountId);
            acc.deposit(amount);
            accountDAO.updateBalance(con, accountId, acc.getBalance());
            transactionDAO.insert(con, accountId, ref, "DEPOSIT", amount);
            markProcessed(con, ref);
        });
    }

    public void withdraw(int accountId, double amount, String ref) throws PaymentException, SQLException {
        validateAmount(amount);
        inTransaction(con -> {
            checkNotDuplicate(con, ref);
            Account acc = load(con, accountId);
            if (!acc.withdraw(amount)) {
                throw new InsufficientBalanceException("Insufficient balance (savings accounts also pay a withdrawal fee).");
            }
            accountDAO.updateBalance(con, accountId, acc.getBalance());
            transactionDAO.insert(con, accountId, ref, "WITHDRAW", amount);
            markProcessed(con, ref);
        });
    }

    public void transfer(int fromId, int toId, double amount, String ref) throws PaymentException, SQLException {
        validateAmount(amount);
        if (fromId == toId) {
            throw new InvalidAccountException("Source and destination accounts must be different.");
        }
        inTransaction(con -> {
            checkNotDuplicate(con, ref);
            Account from = load(con, fromId);
            Account to = load(con, toId);
            if (!from.withdraw(amount)) {
                throw new InsufficientBalanceException("Insufficient balance for this transfer.");
            }
            to.deposit(amount);
            accountDAO.updateBalance(con, fromId, from.getBalance());
            accountDAO.updateBalance(con, toId, to.getBalance());
            transactionDAO.insert(con, fromId, ref, "TRANSFER_OUT", amount);
            transactionDAO.insert(con, toId, ref, "TRANSFER_IN", amount);
            markProcessed(con, ref);
        });
    }

    private void inTransaction(Work work) throws PaymentException, SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                work.run(con);
                con.commit();
            } catch (PaymentException | SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void validateAmount(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
    }

    private void checkNotDuplicate(Connection con, String ref) throws PaymentException, SQLException {
        if (ref == null || ref.isBlank()) {
            throw new PaymentException("A reference ID is required.");
        }
        if (processedDAO.exists(con, ref)) {
            throw new DuplicateTransactionException("Duplicate request: reference " + ref + " was already processed.");
        }
    }

    private void markProcessed(Connection con, String ref) throws PaymentException, SQLException {
        try {
            processedDAO.insert(con, ref);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateTransactionException("Duplicate request: reference " + ref + " was already processed.");
            }
            throw e;
        }
    }

    private Account load(Connection con, int id) throws PaymentException, SQLException {
        Account acc = accountDAO.findByIdForUpdate(con, id);
        if (acc == null) {
            throw new InvalidAccountException("Account " + id + " does not exist.");
        }
        if (!"ACTIVE".equalsIgnoreCase(acc.getStatus())) {
            throw new InvalidAccountException("Account " + id + " is not active.");
        }
        return acc;
    }
}