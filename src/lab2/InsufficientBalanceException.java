package lab2;

public class InsufficientBalanceException extends PaymentException {
    public InsufficientBalanceException(String message) { super(message); }
}