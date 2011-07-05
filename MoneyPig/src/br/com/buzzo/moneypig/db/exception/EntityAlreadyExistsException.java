package br.com.buzzo.moneypig.db.exception;

public class EntityAlreadyExistsException extends Exception {
    private static final long serialVersionUID = 1L;

    public EntityAlreadyExistsException(final String error) {
        super(error);
    }
}
