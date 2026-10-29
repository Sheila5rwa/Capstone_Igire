package lab2;

public class DuplicateTransactionException extends PaymentException {
    public DuplicateTransactionException(String message) { super(message); }
}