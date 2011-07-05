package br.com.buzzo.moneypig.activity.expense;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import br.com.buzzo.moneypig.R;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.ent.Expense;
import br.com.buzzo.moneypig.db.ent.Expense.ExpenseMode;
import br.com.buzzo.moneypig.db.ent.ExpenseRepository.SortExpenseListType;
import br.com.buzzo.moneypig.db.ent.Label;
import br.com.buzzo.moneypig.db.restriction.ExpenseLimitRestriction;
import br.com.buzzo.moneypig.db.restriction.ExpenseOrderRestriction;

public class ExpenseList extends ListActivity {
    public static final String       ID_TO_EDIT          = "id_to_edit";

    public static final NumberFormat NF                  = NumberFormat.getInstance(Locale.getDefault());
    static {
        ExpenseList.NF.setMinimumFractionDigits(2);
        ExpenseList.NF.setMaximumFractionDigits(2);
    }

    private static final int         ITENS_PER_PAGE      = 8;

    private static final int         EXPENSE_LIST_RETURN = 0;

    private List<Expense>            expenses;
    private int                      begin               = 0;
    private boolean                  loadingMore         = false;
    private BaseAdapter              adapter;
    private View                     footerView;

    public void initLayout() {
        setContentView(R.layout.expenses_list);
        final ListView list = (ListView) findViewById(android.R.id.list);
        registerForContextMenu(list);
        this.expenses = new ArrayList<Expense>();
        // puts the 'loading itens' bellow the list.
        this.footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.expenses_list_footer, null,
                false);
        list.addFooterView(this.footerView);
        // and hide it for future use.
        this.footerView.setVisibility(View.GONE);
        // now we set the adapter for the list.
        this.adapter = new EfficientAdapter(this);
        setListAdapter(this.adapter);
        // creates the scroll listener for paginating (lazy load) the list.
        final OnScrollListener scrollListener = new OnScrollListener() {
            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
                if (visibleItemCount == 0) {
                    // as javadoc of this method.
                    return;
                }
                // calculates the index of the last item.
                final int lastInScreen = firstVisibleItem + visibleItemCount;
                // is the last item visible AND not loading more?
                final long maxRows = Expense.repository(ExpenseList.this).getMaxRows();
                //
                // && maxRows > 0
                if (lastInScreen == totalItemCount && !ExpenseList.this.loadingMore && ExpenseList.this.expenses.size() < maxRows) {
                    ExpenseList.this.loadingMore = true;
                    ExpenseList.this.footerView.setVisibility(View.VISIBLE);
                    ExpenseList.this.begin = totalItemCount - 1;
                    final Thread thread = new Thread(null, new LoadMoreItens());
                    thread.start();
                }
            }

            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
            }
        };
        getListView().setOnScrollListener(scrollListener);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int menuItemIndex = item.getItemId();
        if (menuItemIndex == 0) {
            // EDIT
            Intent i;
            if (ExpenseMode.INCOME.equals(this.expenses.get(info.position).getMode())) {
                i = new Intent(ExpenseList.this, IncomeEdit.class);
            } else {
                i = new Intent(ExpenseList.this, OutcomeEdit.class);
            }
            i.putExtra(ExpenseList.ID_TO_EDIT, this.expenses.get(info.position).getId());
            startActivityForResult(i, ExpenseList.EXPENSE_LIST_RETURN);
        } else if (menuItemIndex == 1) {
            // DELETE
            final Expense e = this.expenses.get(info.position);
            e.delete(this);
            update();
            toast(getString(R.string.expense_ctx_menu_item_delete_msg_expense_deleted));
        } else {
            toast("NOT IMPLEMENTED");
        }
        return true;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        if (v.getId() == android.R.id.list) {
            menu.setHeaderTitle(getString(R.string.expense_ctx_menu_header));
            final String[] menuItems = getResources().getStringArray(R.array.expenses_ctx_menu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.expenses_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.expenses_opts_add_income:
                startActivityForResult(new Intent(ExpenseList.this, IncomeAdd.class), ExpenseList.EXPENSE_LIST_RETURN);
                return true;
            case R.id.expenses_opts_add_outcome:
                startActivityForResult(new Intent(ExpenseList.this, OutcomeAdd.class), ExpenseList.EXPENSE_LIST_RETURN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EXPENSE_LIST_RETURN:
                update();
                break;
            default:
                break;
        }
    }

    private void toast(final String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void update() {
        this.expenses.clear();
        this.begin = 1;
        this.adapter.notifyDataSetChanged();
    }

    private class EfficientAdapter extends BaseAdapter {
        private final LayoutInflater item;
        private final Bitmap         addIcon;
        private final Bitmap         removeIcon;

        public EfficientAdapter(final Context context) {
            this.item = LayoutInflater.from(context);
            this.addIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.basket_add);
            this.removeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.basket_remove);
        }

        @Override
        public int getCount() {
            return ExpenseList.this.expenses.size();
        }

        @Override
        public Object getItem(final int position) {
            return position;
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = this.item.inflate(R.layout.expenses_list_item, null);
                holder = new ViewHolder();
                holder.date = (TextView) convertView.findViewById(R.id.expense_list_item_date);
                holder.value = (TextView) convertView.findViewById(R.id.expense_list_item_value);
                holder.descr = (TextView) convertView.findViewById(R.id.expense_list_item_descr);
                holder.labels = (TextView) convertView.findViewById(R.id.expense_list_item_labels);
                holder.icon = (ImageView) convertView.findViewById(R.id.expense_list_item_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Expense expense = ExpenseList.this.expenses.get(position);
            holder.icon.setImageBitmap(ExpenseMode.INCOME.equals(expense.getMode()) ? this.addIcon : this.removeIcon);
            holder.date.setText(getString(R.string.expense_list_item_date) + " "
                    + DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(expense.getDate()));
            holder.value.setText(getString(R.string.expense_list_item_value) + " " + ExpenseList.NF.format(expense.getValue()));
            holder.descr.setText(getString(R.string.expense_list_item_descr) + " " + expense.getDescr());
            final StringBuilder sb = new StringBuilder(getString(R.string.expense_list_item_labels));
            sb.append(" ");
            int counter = 0;
            for (final Label label : expense.getLabels()) {
                sb.append(label.getName());
                if (counter < expense.getLabels().size() - 1) {
                    sb.append(", ");
                }
                counter++;
            }
            holder.labels.setText(sb.toString());
            return convertView;
        }
    }

    /**
     * Runnable for loading more {@link ExpenseList#ITENS_PER_PAGE} itens from database and append then to the list.
     */
    private class LoadMoreItens implements Runnable {
        @Override
        public void run() {
            final FetchRestriction<Expense> limitRestriction = new ExpenseLimitRestriction(ExpenseList.this.begin,
                    ExpenseList.ITENS_PER_PAGE);
            final FetchRestriction<Expense> orderRestriction = new ExpenseOrderRestriction(SortExpenseListType.LAST_CREATED_FIRST);
            @SuppressWarnings("unchecked")
            final List<Expense> exps = Expense.repository(ExpenseList.this).list(limitRestriction, orderRestriction);
            // Log.d(ExpenseList.class.getSimpleName(), "Itens loaded:" + exps.size());
            for (final Expense exp : exps) {
                ExpenseList.this.expenses.add(exp);
            }
            runOnUiThread(new UpdateListUI());
        }
    }

    /**
     * Runnable responsable for updating the UI adapter.
     */
    private class UpdateListUI implements Runnable {
        @Override
        public void run() {
            ExpenseList.this.footerView.setVisibility(View.GONE);
            ExpenseList.this.adapter.notifyDataSetChanged();
            ExpenseList.this.loadingMore = false;
        }
    }

    private static class ViewHolder {
        TextView  date;
        TextView  value;
        TextView  descr;
        TextView  labels;
        ImageView icon;
    }
}
