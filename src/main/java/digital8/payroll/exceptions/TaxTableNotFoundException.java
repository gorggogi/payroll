package digital8.payroll.exceptions;

public class TaxTableNotFoundException extends RuntimeException {

    public TaxTableNotFoundException(String message) {
        super(message);
    }
}