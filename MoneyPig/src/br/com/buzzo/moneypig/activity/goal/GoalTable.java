package br.com.buzzo.moneypig.activity.goal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import br.com.buzzo.moneypig.R;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.ent.Summary;
import br.com.buzzo.moneypig.db.ent.SummaryRepository;
import br.com.buzzo.moneypig.db.restriction.SummaryDateRestriction;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.MonthYearDateSlider;

public class GoalTable extends Activity {
    private static final String  HEADER_DATE_FORMAT  = "MMMM - yyyy";

    private static final int     DIALOG_SELECT_MONTH = 0;
    private static final int     DIALOG_SORT_SUMMARY = 1;

    private static SummarySort   sort                = SummarySort.HIGHER_VALUE_FIRST;

    private Calendar             cal;

    private float                total;
    private final List<TableRow> rows                = new ArrayList<TableRow>();

    private View                 separator;
    private TableRow             footer;
    private TableLayout          table;

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.goal_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.goal_opts_select_month:
                showDialog(GoalTable.DIALOG_SELECT_MONTH);
                return true;
            case R.id.goal_opts_sort:
                showDialog(GoalTable.DIALOG_SORT_SUMMARY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case DIALOG_SELECT_MONTH:
                final DateSlider.OnDateSetListener listener = new DateSlider.OnDateSetListener() {
                    @Override
                    public void onDateSet(final DateSlider view, final Calendar selectedDate) {
                        GoalTable.this.cal = selectedDate;
                        refreshTable();
                    }
                };
                return new MonthYearDateSlider(this, listener, Calendar.getInstance());
            case DIALOG_SORT_SUMMARY:
                return getSortDialog();
        }
        return null;
    }

    private void cleanTableFooter() {
        this.table.removeView(this.separator);
        this.footer.removeAllViews();
        this.table.removeView(this.footer);
    }

    private void cleanTableView() {
        for (final TableRow r : GoalTable.this.rows) {
            this.table.removeView(r);
        }
        this.total = 0;
        this.rows.clear();
    }

    private FetchRestriction<Summary> getDateRestriction() {
        // clone the Calendar instance
        final Calendar localCal = Calendar.getInstance();
        localCal.setTimeInMillis(this.cal.getTimeInMillis());
        // clear fields
        int year = localCal.get(Calendar.YEAR);
        int month = localCal.get(Calendar.MONTH);
        int day = localCal.get(Calendar.DATE);
        localCal.clear();
        localCal.set(year, month, day);
        // go to the first day of this month (always 1)
        localCal.set(Calendar.DAY_OF_MONTH, localCal.getActualMinimum(Calendar.DAY_OF_MONTH));
        final long begin = localCal.getTimeInMillis();
        // then goes to the first day of the next month
        localCal.set(Calendar.MONTH, localCal.get(Calendar.MONTH) + 1);
        final long end = localCal.getTimeInMillis();
        return new SummaryDateRestriction(begin, end, true);
    }

    private Dialog getSortDialog() {
        final OnClickListener positive = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                refreshTable();
            }
        };
        final OnClickListener negative = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        };
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setSingleChoiceItems(getResources().getStringArray(R.array.goal_dialog_sort), -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                GoalTable.sort = SummarySort.getByIndex(which);
            }
        });
        b.setTitle(getString(R.string.goal_sort_txt_header)).setPositiveButton(getString(R.string.goal_sort_btn_set), positive)
                .setNegativeButton(getString(R.string.goal_sort_btn_cancel), negative);
        return b.create();
    }

    private void initLayout() {
        setContentView(R.layout.goal_table);
        this.cal = Calendar.getInstance();
        this.table = (TableLayout) findViewById(R.id.goal_table);
        this.footer = new TableRow(this);
        this.separator = new View(this);
        this.separator.setBackgroundColor(Color.parseColor("#FF909090"));
        refreshTable();
    }

    private void refreshTable() {
        cleanTableView();
        cleanTableFooter();
        restoreTableData();
        restoreTableView();
        restoreFooter();
    }

    private void restoreFooter() {
        final TextView label = new TextView(this);
        label.setText(getString(R.string.goal_table_txt_total));
        label.setPadding(3, 3, 3, 3);
        this.footer.addView(label, new TableRow.LayoutParams(0));

        final TextView value = new TextView(this);
        value.setText(String.valueOf(this.total));
        value.setPadding(3, 3, 3, 3);
        value.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        this.footer.addView(value, new TableRow.LayoutParams(1));

        final TextView goal = new TextView(this);
        goal.setText(String.valueOf(this.total));
        goal.setPadding(3, 3, 3, 3);
        goal.setGravity(Gravity.RIGHT | Gravity.TOP);
        this.footer.addView(goal, new TableRow.LayoutParams(1));

        this.table.addView(this.separator, new TableLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, 2));
        this.table.addView(this.footer, new TableLayout.LayoutParams());
    }

    private void restoreTableData() {
        // pois o método list exige generics
        @SuppressWarnings("unchecked")
        final List<Summary> summaries = Summary.repository(this).list(GoalTable.sort.getSorter(), getDateRestriction());
        for (final Summary summary : summaries) {
            final TableRow row = new TableRow(this);

            final TextView label = new TextView(this);
            // TODO: fixada apenas um label por linha
            label.setText(summary.getLabels().get(0).getName());
            label.setPadding(3, 3, 3, 3);
            row.addView(label, new TableRow.LayoutParams(0));

            final TextView value = new TextView(this);
            value.setText(String.valueOf(summary.getSum()));
            value.setPadding(3, 3, 3, 3);
            value.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            row.addView(value, new TableRow.LayoutParams(1));

            final TextView goalTxt = new TextView(this);
            final double goal = summary.getLabels().get(0).getGoal();
            if (goal == 0) {
                goalTxt.setText(String.valueOf(" - "));
            } else {
                goalTxt.setText(String.valueOf(goal));
            }
            goalTxt.setPadding(3, 3, 3, 3);
            goalTxt.setGravity(Gravity.RIGHT | Gravity.TOP);
            row.addView(goalTxt, new TableRow.LayoutParams(2));

            this.rows.add(row);
            this.total += summary.getSum();
        }
    }

    private void restoreTableView() {
        final TextView header = (TextView) findViewById(R.id.goal_table_header);
        header.setText(new SimpleDateFormat(GoalTable.HEADER_DATE_FORMAT, Locale.getDefault()).format(this.cal.getTime()));
        for (final TableRow row : this.rows) {
            this.table.addView(row, new TableLayout.LayoutParams());
        }
    }

    private static enum SummarySort {
        ALPHABETIC_LABEL(0, SummaryRepository.alphabeticLabelOrder()), HIGHER_VALUE_FIRST(1, SummaryRepository.greaterValueFirstOrder());

        private final int                 index;
        private final Comparator<Summary> sorter;

        private SummarySort(final int index, final Comparator<Summary> sorter) {
            this.index = index;
            this.sorter = sorter;
        }

        public Comparator<Summary> getSorter() {
            return this.sorter;
        }

        public static SummarySort getByIndex(final int index) {
            for (final SummarySort sort : SummarySort.values()) {
                if (sort.index == index) {
                    return sort;
                }
            }
            throw new IllegalArgumentException("Not implemented index [" + index + "]");
        }
    }
}
