package br.com.buzzo.moneypig.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import br.com.buzzo.moneypig.db.ent.ExpenseRepository;
import br.com.buzzo.moneypig.db.ent.LabelRepository;
import br.com.buzzo.moneypig.db.ent.SMSRepository;

public abstract class Repository extends SQLiteOpenHelper {
    private static final String DATABASE_NAME    = "moneypig.db";
    private static final int    DATABASE_VERSION = 1;

    private final Context       ctx;

    public Repository(final Context ctx) {
        super(ctx, Repository.DATABASE_NAME, null, Repository.DATABASE_VERSION);
        this.ctx = ctx;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        if (!db.isReadOnly()) {
            db.execSQL(LabelRepository.CREATE_LABEL_TABLE);
            db.execSQL(ExpenseRepository.CREATE_EXPENSE_TABLE);
            db.execSQL(ExpenseRepository.CREATE_EXPENSE_LABEL_TABLE);
            db.execSQL(SMSRepository.CREATE_SMS_TABLE);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // empty this time.
    }

    protected boolean checkRecordExist(final String tableName, final String[] keys, final String[] values) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i]).append("=\"").append(values[i]).append("\" ");
            if (i < keys.length - 1) {
                sb.append("AND ");
            }
        }
        final Cursor cursor = getReadableDatabase().query(tableName, null, sb.toString(), null, null, null, null);
        final boolean exists = cursor.getCount() > 0;
        closeResources(cursor);
        return exists;
    }

    /**
     * Closes the cursor resources and the database after that or we will have a 'memory leak' logged.
     * 
     * @param cursor
     *            {@link Cursor} used for querying the database.
     * @see http://stackoverflow.com/questions/2280345/sqlite-database-leak-found-exception-in-android
     */
    protected void closeResources(final Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        // now closes the database.
        close();
    }

    protected Context getContext() {
        return this.ctx;
    }

}
