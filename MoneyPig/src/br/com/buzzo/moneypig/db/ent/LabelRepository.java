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

public class LabelRepository extends Repository implements DomainRepository<Label> {
    public static final String TABLE              = "labels";
    public static final String COLUMN_NAME        = "name";
    public static final String COLUMN_GOAL        = "goal";
    public static final String COLUMN_CREATED     = "created";

    public static final String CREATE_LABEL_TABLE = "CREATE TABLE IF NOT EXISTS " + LabelRepository.TABLE + "( " + BaseColumns._ID
                                                          + " INTEGER PRIMARY KEY AUTOINCREMENT, " + LabelRepository.COLUMN_NAME
                                                          + " TEXT NOT NULL UNIQUE, " + LabelRepository.COLUMN_CREATED
                                                          + " NUMERIC NOT NULL, " + LabelRepository.COLUMN_GOAL + " NUMERIC);";

    public LabelRepository(final Context ctx) {
        super(ctx);
    }

    @Override
    public List<Label> list(final FetchRestriction<Label>... restrictions) {
        final FetchDefinition definition = new FetchDefinition();
        definition.addColumn(BaseColumns._ID);
        definition.addColumn(LabelRepository.COLUMN_NAME);
        definition.addColumn(LabelRepository.COLUMN_CREATED);
        definition.addColumn(LabelRepository.COLUMN_GOAL);
        if (restrictions != null) {
            for (final FetchRestriction<Label> r : restrictions) {
                r.restrict(definition);
            }
        }
        final Cursor cursor = getReadableDatabase().query(LabelRepository.TABLE, definition.getColumns(), definition.getWhere(),
                definition.getWhereArgs(), null, null, definition.getSortOrder());
        final List<Label> labels = new ArrayList<Label>();
        if (cursor.moveToFirst()) {
            do {
                final Date created = new Date(cursor.getLong(2));
                final Label label = new Label(cursor.getInt(0), cursor.getString(1), created, cursor.getDouble(3));
                labels.add(label);
            } while (cursor.moveToNext());
        }
        closeResources(cursor);
        return labels;
    }

    void delete(final Label label) {
        getWritableDatabase().delete(LabelRepository.TABLE, BaseColumns._ID + "=?", new String[] { String.valueOf(label.getId()) });
        removeRelationsExpenseLabels(label.getId());
    }

    void persist(final Label label) throws EntityAlreadyExistsException {
        final boolean exists = checkRecordExist(LabelRepository.TABLE, new String[] { LabelRepository.COLUMN_NAME },
                new String[] { label.getName() });
        if (exists) {
            throw new EntityAlreadyExistsException("There is already a label with name [" + label.getName() + "]");
        }
        final ContentValues values = new ContentValues();
        values.put(LabelRepository.COLUMN_NAME, label.getName());
        values.put(LabelRepository.COLUMN_CREATED, label.getCreated().getTime());
        values.put(LabelRepository.COLUMN_GOAL, label.getGoal());
        getWritableDatabase().insert(LabelRepository.TABLE, null, values);
        closeResources(null);
    }

    void update(final Label label) {
        final ContentValues values = new ContentValues();
        values.put(LabelRepository.COLUMN_NAME, label.getName());
        values.put(LabelRepository.COLUMN_CREATED, label.getCreated().getTime());
        values.put(LabelRepository.COLUMN_GOAL, label.getGoal());
        getWritableDatabase().update(LabelRepository.TABLE, values, BaseColumns._ID + "=?", new String[] { String.valueOf(label.getId()) });
        closeResources(null);
    }

    private void removeRelationsExpenseLabels(final long labelID) {
        final Cursor cursor = getReadableDatabase().query(ExpenseRepository.TABLE_EXPENSE_TO_LABELS,
                new String[] { ExpenseRepository.COLUMN_ID_EXPENSE, ExpenseRepository.COLUMN_ID_LABELS },
                ExpenseRepository.COLUMN_ID_LABELS + "=?", new String[] { String.valueOf(labelID) }, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                getWritableDatabase().delete(ExpenseRepository.TABLE_EXPENSE_TO_LABELS,
                        ExpenseRepository.COLUMN_ID_EXPENSE + "=? AND " + ExpenseRepository.COLUMN_ID_LABELS + "=?",
                        new String[] { cursor.getString(0), cursor.getString(1) });
            } while (cursor.moveToNext());
        }
        closeResources(cursor);
    }

    public static enum SortLabelListType {
        ALPHABETIC(0), LAST_CREATED_FIRST(1);

        private final int code;

        private SortLabelListType(final int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        public static SortLabelListType getByCode(final int code) {
            for (final SortLabelListType sort : SortLabelListType.values()) {
                if (sort.getCode() == code) {
                    return sort;
                }
            }
            throw new IllegalArgumentException("Not implemented code [" + code + "]");
        }
    }
}
