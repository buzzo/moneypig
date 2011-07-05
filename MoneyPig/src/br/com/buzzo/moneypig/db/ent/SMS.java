package br.com.buzzo.moneypig.db.ent;

import java.util.Date;

import android.content.Context;
import br.com.buzzo.moneypig.db.DomainBean;
import br.com.buzzo.moneypig.db.exception.EntityAlreadyExistsException;
import br.com.buzzo.moneypig.db.exception.InvalidEntityUpdateException;

public class SMS implements DomainBean<SMS> {
    private int          id;
    private final String name;
    private final int    number;
    private final String regexp;
    private final Date   created;

    public SMS(final String name, final int number, final String smsRegexp) {
        this.name = name;
        this.number = number;
        this.regexp = smsRegexp;
        this.created = new Date();
    }

    SMS(final int id, final String name, final int number, final String smsRegexp, final Date created) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.regexp = smsRegexp;
        this.created = created;
    }

    @Override
    public void delete(final Context ctx) {
        SMS.repository(ctx).delete(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SMS other = (SMS) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public Date getCreated() {
        return this.created;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getNumber() {
        return this.number;
    }

    public String getRegexp() {
        return this.regexp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        return result;
    }

    @Override
    public void persist(final Context ctx) throws EntityAlreadyExistsException {
        SMS.repository(ctx).persist(this);
    }

    @Override
    public void update(final Context ctx) throws InvalidEntityUpdateException {
        throw new UnsupportedOperationException("Update of SMS is not implemented");
    }

    public static SMSRepository repository(final Context ctx) {
        return new SMSRepository(ctx);
    }

}
