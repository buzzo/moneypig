package br.com.buzzo.moneypig.db.ent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import br.com.buzzo.moneypig.db.DomainRepository;
import br.com.buzzo.moneypig.db.FetchDefinition;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.Repository;
import br.com.buzzo.moneypig.db.restriction.LabelIdRestriction;

/**
 * Repository of summaries. Use to retrive a list of summaries from database.<br>
 * <br>
 * As {@link Summary} is not a entity itself, this repository class doesn't implements {@link DomainRepository}
 * interface.
 */
public class SummaryRepository extends Repository {

    // TODO: this query is wrong! We select the sum of expenses from ONE label. This join is repeated over an iteraction
    // over a list of labels. As probably there will be just a few labels this approach may not be a problem but the
    // right join would return all data we needed with just one query. The idea is to make the join and use the GROUP
    // BY and HAVING sql resourses to filter the data that we want to create the Summary objects.
    public static final String SUM_JOIN_QUERY = "SELECT SUM(" + ExpenseRepository.TABLE + "." + ExpenseRepository.COLUMN_VALUE + ") FROM "
                                                      + LabelRepository.TABLE + " JOIN " + ExpenseRepository.TABLE_EXPENSE_TO_LABELS
                                                      + " JOIN " + ExpenseRepository.TABLE + " WHERE " + LabelRepository.TABLE + "."
                                                      + BaseColumns._ID + " = " + ExpenseRepository.TABLE_EXPENSE_TO_LABELS + "."
                                                      + ExpenseRepository.COLUMN_ID_LABELS + " AND "
                                                      + ExpenseRepository.TABLE_EXPENSE_TO_LABELS + "."
                                                      + ExpenseRepository.COLUMN_ID_EXPENSE + " = " + ExpenseRepository.TABLE + "."
                                                      + BaseColumns._ID + " AND " + LabelRepository.TABLE + "." + BaseColumns._ID + " = ? ";

    public SummaryRepository(final Context ctx) {
        super(ctx);
    }

    /**
     * Retrives a list of {@link Summary} from database using the restriction and sorter parameters.
     * 
     * @param sorter
     *            if <code>null</code> doesn't apply any {@link Comparator} over the {@link Summary} list.
     * @param restrictions
     *            restrictions applied over the calculation of {@link Summary} itens.
     * @return list of summaries based on the restrictions.
     */
    public List<Summary> list(final Comparator<Summary> sorter, final FetchRestriction<Summary>... restrictions) {
        final List<Summary> summaries = new ArrayList<Summary>();

        final List<Label> labels = Label.repository(getContext()).list(new LabelIdRestriction[0]);
        for (final Label label : labels) {
            final FetchDefinition definition = new FetchDefinition();
            if (restrictions != null) {
                for (final FetchRestriction<Summary> r : restrictions) {
                    r.restrict(definition);
                }
            }
            final List<String> args = new ArrayList<String>();
            args.add(String.valueOf(label.getId()));
            args.addAll(Arrays.asList(definition.getWhereArgs()));
            final StringBuffer query = new StringBuffer(SummaryRepository.SUM_JOIN_QUERY).append(" AND ").append(definition.getWhere());
            final Cursor cursor = getReadableDatabase().rawQuery(query.toString(), args.toArray(new String[] {}));
            if (cursor.moveToFirst()) {
                do {
                    final Summary summary = new Summary(cursor.getFloat(0), label);
                    summaries.add(summary);
                } while (cursor.moveToNext());
            }
            closeResources(cursor);
        }

        if (sorter != null) {
            Collections.sort(summaries, sorter);
        }

        return summaries;
    }

    public static Comparator<Summary> alphabeticLabelOrder() {
        return new SummaryLabelComparator();
    }

    public static Comparator<Summary> greaterValueFirstOrder() {
        return new SummaryExpenseComparator();
    }

    private static class SummaryExpenseComparator implements Comparator<Summary> {
        @Override
        public int compare(final Summary sum1, final Summary sum2) {
            // TODO uses just the first label. Must change if we accept more than one label in summary.
            return new Float(sum2.getSum() - sum1.getSum()).intValue();
        }
    }

    private static class SummaryLabelComparator implements Comparator<Summary> {
        @Override
        public int compare(final Summary sum1, final Summary sum2) {
            // TODO uses just the first label. Must change if we accept more than one label in summary.
            return sum1.getLabels().get(0).getName().compareTo(sum2.getLabels().get(0).getName());
        }
    }

}
