package br.com.buzzo.moneypig.db.exception;

public class InvalidEntityUpdateException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidEntityUpdateException(final String error) {
        super(error);
    }
}
