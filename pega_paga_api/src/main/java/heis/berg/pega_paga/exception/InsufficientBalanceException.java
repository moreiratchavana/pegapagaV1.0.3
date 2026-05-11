package heis.berg.pega_paga.exception;

public class InsufficientBalanceException extends ApiException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
