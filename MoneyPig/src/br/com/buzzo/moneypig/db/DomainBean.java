package br.com.buzzo.moneypig.db;

import android.content.Context;
import br.com.buzzo.moneypig.db.exception.EntityAlreadyExistsException;
import br.com.buzzo.moneypig.db.exception.InvalidEntityUpdateException;

public interface DomainBean<T> {

    void delete(final Context ctx);

    void persist(final Context ctx) throws EntityAlreadyExistsException;

    void update(final Context ctx) throws InvalidEntityUpdateException;
}
