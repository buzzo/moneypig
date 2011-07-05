package br.com.buzzo.moneypig.db.ent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import br.com.buzzo.moneypig.db.DomainRepository;
import br.com.buzzo.moneypig.db.FetchDefinition;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.Repository;
import br.com.buzzo.moneypig.db.exception.EntityAlreadyExistsException;
import br.com.buzzo.moneypig.db.exception.InvalidEntityUpdateException;

public class SMSRepository extends Repository implements DomainRepository<SMS> {
    public static final String TABLE                = "sms_filter";
    public static final String COLUMN_NAME          = "name";
    public static final String COLUMN_INCOME_NUMBER = "income_number";
    public static final String COLUMN_REGEXP        = "regexp";
    public static final String COLUMN_CREATED       = "created";

    public static final String CREATE_SMS_TABLE     = "CREATE TABLE IF NOT EXISTS " + SMSRepository.TABLE + "( " + BaseColumns._ID
                                                            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + SMSRepository.COLUMN_NAME
                                                            + " TEXT NOT NULL, " + SMSRepository.COLUMN_INCOME_NUMBER
                                                            + " NUMERIC NOT NULL UNIQUE, " + SMSRepository.COLUMN_REGEXP
                                                            + " TEXT NOT NULL, " + SMSRepository.COLUMN_CREATED + " NUMERIC NOT NULL);";

    public SMSRepository(final Context ctx) {
        super(ctx);
    }

    @Override
    public List<SMS> list(final FetchRestriction<SMS>... restrictions) {
        final FetchDefinition definition = new FetchDefinition();
        definition.addColumn(BaseColumns._ID);
        definition.addColumn(SMSRepository.COLUMN_NAME);
        definition.addColumn(SMSRepository.COLUMN_INCOME_NUMBER);
        definition.addColumn(SMSRepository.COLUMN_REGEXP);
        definition.addColumn(SMSRepository.COLUMN_CREATED);
        if (restrictions != null) {
            for (final FetchRestriction<SMS> r : restrictions) {
                r.restrict(definition);
            }
        }
        final Cursor cursor = getReadableDatabase().query(SMSRepository.TABLE, definition.getColumns(), definition.getWhere(),
                definition.getWhereArgs(), null, null, definition.getSortOrder());
        final List<SMS> sms = new ArrayList<SMS>();
        if (cursor.moveToFirst()) {
            do {
                final Date created = new Date(cursor.getLong(4));
                final SMS s = new SMS(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3), created);
                sms.add(s);
            } while (cursor.moveToNext());
        }
        closeResources(cursor);
        return sms;
    }

    void delete(final SMS label) {
        getWritableDatabase().delete(SMSRepository.TABLE, BaseColumns._ID + "=?", new String[] { String.valueOf(label.getId()) });
    }

    void persist(final SMS sms) throws EntityAlreadyExistsException {
        final boolean exists = checkRecordExist(SMSRepository.TABLE, new String[] { SMSRepository.COLUMN_NAME },
                new String[] { sms.getName() });
        if (exists) {
            throw new EntityAlreadyExistsException("There is already a sms filter with name [" + sms.getName() + "]");
        }
        final ContentValues values = new ContentValues();
        values.put(SMSRepository.COLUMN_NAME, sms.getName());
        values.put(SMSRepository.COLUMN_INCOME_NUMBER, sms.getNumber());
        values.put(SMSRepository.COLUMN_REGEXP, sms.getRegexp());
        values.put(SMSRepository.COLUMN_CREATED, sms.getCreated().getTime());
        getWritableDatabase().insert(SMSRepository.TABLE, null, values);
    }

    void update(final SMS label) throws InvalidEntityUpdateException {
        throw new UnsupportedOperationException("Update of SMS is not implemented");
    }

}
