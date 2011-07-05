package br.com.buzzo.moneypig.db.ent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.provider.BaseColumns;
import br.com.buzzo.moneypig.db.DomainRepository;
import br.com.buzzo.moneypig.db.FetchDefinition;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.Repository;
import br.com.buzzo.moneypig.db.ent.Expense.ExpenseMode;
import br.com.buzzo.moneypig.db.exception.EntityAlreadyExistsException;
import br.com.buzzo.moneypig.db.exception.InvalidEntityUpdateException;
import br.com.buzzo.moneypig.db.restriction.LabelIdRestriction;

public class ExpenseRepository extends Repository implements DomainRepository<Expense> {
    public static final String TABLE                      = "expenses";
    public static final String COLUMN_TYPE                = "type";
    public static final String COLUMN_VALUE               = "value";
    public static final String COLUMN_DATE                = "date";
    public static final String COLUMN_DESCR               = "descr";

    public static final String TABLE_EXPENSE_TO_LABELS    = "link_expenses_labels";
    public static final String COLUMN_ID_EXPENSE          = "id_expense";
    public static final String COLUMN_ID_LABELS           = "id_labels";

    public static final String CREATE_EXPENSE_TABLE       = "CREATE TABLE IF NOT EXISTS " + ExpenseRepository.TABLE + " ( "
                                                                  + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                                  + ExpenseRepository.COLUMN_TYPE + " INTEGER NOT NULL, "
                                                                  + ExpenseRepository.COLUMN_VALUE + " NUMERIC NOT NULL, "
                                                                  + ExpenseRepository.COLUMN_DATE + " NUMERIC NOT NULL, "
                                                                  + ExpenseRepository.COLUMN_DESCR + " TEXT);";
    public static final String CREATE_EXPENSE_LABEL_TABLE = "CREATE TABLE IF NOT EXISTS " + ExpenseRepository.TABLE_EXPENSE_TO_LABELS
                                                                  + " ( " + ExpenseRepository.COLUMN_ID_EXPENSE + " INTEGER, "
                                                                  + ExpenseRepository.COLUMN_ID_LABELS + " INTEGER ," + " FOREIGN KEY("
                                                                  + ExpenseRepository.COLUMN_ID_EXPENSE + ") REFERENCES "
                                                                  + ExpenseRepository.TABLE + "(" + BaseColumns._ID + ") ,"
                                                                  + " FOREIGN KEY(" + ExpenseRepository.COLUMN_ID_LABELS + ") REFERENCES "
                                                                  + LabelRepository.TABLE + "(" + BaseColumns._ID + ") ,"
                                                                  + " PRIMARY KEY (" + ExpenseRepository.COLUMN_ID_EXPENSE + ","
                                                                  + ExpenseRepository.COLUMN_ID_LABELS + ") );";

    public ExpenseRepository(final Context ctx) {
        super(ctx);
    }

    public long getMaxRows() {
        final long count = DatabaseUtils.queryNumEntries(getReadableDatabase(), ExpenseRepository.TABLE);
        close();
        return count;
    }

    @Override
    public List<Expense> list(final FetchRestriction<Expense>... restrictions) {
        final FetchDefinition definition = new FetchDefinition();
        definition.addColumn(BaseColumns._ID);
        definition.addColumn(ExpenseRepository.COLUMN_TYPE);
        definition.addColumn(ExpenseRepository.COLUMN_DATE);
        definition.addColumn(ExpenseRepository.COLUMN_VALUE);
        definition.addColumn(ExpenseRepository.COLUMN_DESCR);
        if (restrictions != null) {
            for (final FetchRestriction<Expense> r : restrictions) {
                r.restrict(definition);
            }
        }
        final Cursor cursor = getReadableDatabase().query(ExpenseRepository.TABLE, definition.getColumns(), definition.getWhere(),
                definition.getWhereArgs(), null, null, definition.getSortOrder(), definition.getLimit());
        final List<Expense> expenses = new ArrayList<Expense>();
        if (cursor.moveToFirst()) {
            do {
                final ExpenseMode mode = ExpenseMode.getByCode(cursor.getInt(1));
                final Date date = new Date(cursor.getLong(2));
                double value;
                if (ExpenseMode.OUTCOME.equals(mode)) {
                    // the value is negative in database. The Expense accepts only positive.
                    value = Math.abs(cursor.getDouble(3));
                } else {
                    value = cursor.getDouble(3);
                }
                final Expense expense = new Expense(cursor.getInt(0), mode, date, value, cursor.getString(4));
                loadLabels(expense);
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        closeResources(cursor);
        return expenses;
    }

    void delete(final Expense expense) {
        getWritableDatabase().delete(ExpenseRepository.TABLE, BaseColumns._ID + "=?", new String[] { String.valueOf(expense.getId()) });
        removeRelationsExpenseLabels(expense.getId());
    }

    void persist(final Expense expense) throws EntityAlreadyExistsException {
        // begin the transaction
        getWritableDatabase().beginTransaction();
        // insert the new expense
        final ContentValues values = new ContentValues();
        // values.put(ExpenseRepository.COLUMN_DATE, new SimpleDateFormat("yyyy-MM-dd",
        // Locale.getDefault()).format(expense.getDate()));
        values.put(ExpenseRepository.COLUMN_DATE, expense.getDate().getTime());
        values.put(ExpenseRepository.COLUMN_TYPE, expense.getMode().getCode());
        if (ExpenseMode.OUTCOME.equals(expense.getMode())) {
            // negativates the value in database
            values.put(ExpenseRepository.COLUMN_VALUE, -1 * expense.getValue());
        } else {
            values.put(ExpenseRepository.COLUMN_VALUE, expense.getValue());
        }
        values.put(ExpenseRepository.COLUMN_DESCR, expense.getDescr());
        final long id = getWritableDatabase().insert(ExpenseRepository.TABLE, null, values);
        // now with the expense ID save the relation between the expense and the labels
        for (final Label label : expense.getLabels()) {
            final ContentValues vals = new ContentValues();
            vals.put(ExpenseRepository.COLUMN_ID_EXPENSE, id);
            vals.put(ExpenseRepository.COLUMN_ID_LABELS, label.getId());
            getWritableDatabase().insert(ExpenseRepository.TABLE_EXPENSE_TO_LABELS, null, vals);
        }
        // finish transaction
        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();
        closeResources(null);
    }

    void update(final Expense expense) throws InvalidEntityUpdateException {
        // insert the new expense
        final ContentValues values = new ContentValues();
        values.put(ExpenseRepository.COLUMN_DATE, expense.getDate().getTime());
        values.put(ExpenseRepository.COLUMN_TYPE, expense.getMode().getCode());
        if (ExpenseMode.OUTCOME.equals(expense.getMode())) {
            // negativates the value in database
            values.put(ExpenseRepository.COLUMN_VALUE, -1 * expense.getValue());
        } else {
            values.put(ExpenseRepository.COLUMN_VALUE, expense.getValue());
        }
        values.put(ExpenseRepository.COLUMN_DESCR, expense.getDescr());
        getWritableDatabase().update(ExpenseRepository.TABLE, values, BaseColumns._ID + "=?",
                new String[] { String.valueOf(expense.getId()) });
        // now with the expense ID save the relation between the expense and the labels
        removeRelationsExpenseLabels(expense.getId());
        for (final Label label : expense.getLabels()) {
            final ContentValues vals = new ContentValues();
            vals.put(ExpenseRepository.COLUMN_ID_EXPENSE, expense.getId());
            vals.put(ExpenseRepository.COLUMN_ID_LABELS, label.getId());
            getWritableDatabase().insert(ExpenseRepository.TABLE_EXPENSE_TO_LABELS, null, vals);
        }
        closeResources(null);
    }

    private void loadLabels(final Expense expense) {
        final Cursor cursor = getReadableDatabase().query(ExpenseRepository.TABLE_EXPENSE_TO_LABELS,
                new String[] { ExpenseRepository.COLUMN_ID_EXPENSE, ExpenseRepository.COLUMN_ID_LABELS },
                ExpenseRepository.COLUMN_ID_EXPENSE + "=?", new String[] { String.valueOf(expense.getId()) }, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                final LabelIdRestriction restriction = new LabelIdRestriction(cursor.getInt(1));
                final Label label = Label.repository(getContext()).list(new LabelIdRestriction[] { restriction }).get(0);
                expense.addLabel(label);
            } while (cursor.moveToNext());
        }
        closeResources(cursor);
    }

    private void removeRelationsExpenseLabels(final long expenseID) {
        final Cursor cursor = getReadableDatabase().query(ExpenseRepository.TABLE_EXPENSE_TO_LABELS,
                new String[] { ExpenseRepository.COLUMN_ID_EXPENSE, ExpenseRepository.COLUMN_ID_LABELS },
                ExpenseRepository.COLUMN_ID_EXPENSE + "=?", new String[] { String.valueOf(expenseID) }, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                getWritableDatabase().delete(ExpenseRepository.TABLE_EXPENSE_TO_LABELS,
                        ExpenseRepository.COLUMN_ID_EXPENSE + "=? AND " + ExpenseRepository.COLUMN_ID_LABELS + "=?",
                        new String[] { cursor.getString(0), cursor.getString(1) });
            } while (cursor.moveToNext());
        }
        closeResources(cursor);
    }

    public static enum SortExpenseListType {
        LAST_CREATED_FIRST(0);

        private final int code;

        private SortExpenseListType(final int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        public static SortExpenseListType getByCode(final int code) {
            for (final SortExpenseListType sort : SortExpenseListType.values()) {
                if (sort.getCode() == code) {
                    return sort;
                }
            }
            throw new IllegalArgumentException("Not implemented code [" + code + "]");
        }
    }
}
