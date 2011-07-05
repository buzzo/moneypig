package br.com.buzzo.moneypig.activity.label;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import br.com.buzzo.moneypig.R;
import br.com.buzzo.moneypig.activity.expense.ExpenseList;
import br.com.buzzo.moneypig.db.ent.Expense;
import br.com.buzzo.moneypig.db.ent.Label;
import br.com.buzzo.moneypig.db.ent.LabelRepository.SortLabelListType;
import br.com.buzzo.moneypig.db.exception.EntityAlreadyExistsException;
import br.com.buzzo.moneypig.db.exception.InvalidEntityUpdateException;
import br.com.buzzo.moneypig.db.restriction.LabelOrderRestriction;

public class LabelList extends ListActivity {
    private static final int         DIALOG_LABEL_ADD       = 0;
    private static final int         DIALOG_LABEL_EDIT      = 1;
    private static final int         DIALOG_LABEL_SORT_LIST = 2;

    private List<Label>              labels;
    private int                      edit;
    private static SortLabelListType sort                   = SortLabelListType.ALPHABETIC;
    private BaseAdapter              adapter;

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int menuItemIndex = item.getItemId();
        if (menuItemIndex == 0) {
            // EDIT
            this.edit = info.position;
            showDialog(LabelList.DIALOG_LABEL_EDIT);
        } else if (menuItemIndex == 1) {
            // DELETE
            final Label l = this.labels.get(info.position);
            l.delete(this);
            updateList();
            toast(getString(R.string.labels_ctx_menu_item_delete_msg_label_deleted, l.getName()));
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
            menu.setHeaderTitle(getString(R.string.labels_ctx_menu_header));
            final String[] menuItems = getResources().getStringArray(R.array.labels_ctx_menu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.labels_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.labels_opts_add_label:
                showDialog(LabelList.DIALOG_LABEL_ADD);
                return true;
            case R.id.labels_opts_sort:
                showDialog(LabelList.DIALOG_LABEL_SORT_LIST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case DIALOG_LABEL_ADD:
                return getAddDialog();
            case DIALOG_LABEL_EDIT:
                return getEditDialog();
            case DIALOG_LABEL_SORT_LIST:
                return getSortDialog();
        }
        return null;
    }

    private Dialog getAddDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater factory = LayoutInflater.from(this);
        builder.setView(factory.inflate(R.layout.label_add, null));
        builder.setMessage(getString(R.string.label_add_header)).setCancelable(false);
        final AlertDialog alert = builder.create();
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                removeDialog(LabelList.DIALOG_LABEL_ADD);
            }
        });
        final OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                final AlertDialog alertDialog = (AlertDialog) dialog;
                final EditText text = (EditText) alertDialog.findViewById(R.id.label_add_name);
                final String name = text.getText().toString();
                if (name == null || "".equals(name)) {
                    toast(getString(R.string.label_add_err_empty_name));
                    return;
                }
                if (name.length() > 16) {
                    toast(getString(R.string.label_add_err_name_length));
                    return;
                }
                final EditText valueTxt = (EditText) alertDialog.findViewById(R.id.label_add_goal);
                final String value = valueTxt.getText().toString();
                double val;
                if (value == null || "".equals(value)) {
                    val = 0;
                } else {
                    try {
                        val = ExpenseList.NF.parse(value).doubleValue();
                        if (val > Expense.MAX_VAL) {
                            Toast.makeText(LabelList.this, getString(R.string.label_add_msg_err_value_to_high, Expense.MAX_VAL),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    } catch (final ParseException e) {
                        Toast.makeText(LabelList.this, getString(R.string.label_add_msg_err_invalid_value, value), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }
                final Label label = new Label(name, val);
                try {
                    label.persist(LabelList.this);
                    updateList();
                    toast(getString(R.string.label_add_msg_label_added, name));
                } catch (final EntityAlreadyExistsException e) {
                    toast(getString(R.string.label_add_err_label_added, name));
                }
            }
        };
        final OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        };
        alert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.label_add_btn_add), positiveListener);
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_add_btn_cancel), negativeListener);
        return alert;
    }

    private Dialog getEditDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.label_edit, null);
        builder.setView(view);
        builder.setMessage(getString(R.string.label_edit_header)).setCancelable(false);
        final AlertDialog alert = builder.create();
        final TextView nameTxt = (TextView) view.findViewById(R.id.label_add_name);
        nameTxt.setText(this.labels.get(this.edit).getName());
        final EditText goalTxt = (EditText) view.findViewById(R.id.label_add_goal);
        goalTxt.setText(ExpenseList.NF.format(this.labels.get(this.edit).getGoal()));
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                removeDialog(LabelList.DIALOG_LABEL_EDIT);
            }
        });
        final OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                final AlertDialog alertDialog = (AlertDialog) dialog;
                final EditText valueTxt = (EditText) alertDialog.findViewById(R.id.label_add_goal);
                final String value = valueTxt.getText().toString();
                double val;
                if (value == null || "".equals(value)) {
                    val = 0;
                } else {
                    try {
                        val = ExpenseList.NF.parse(value).doubleValue();
                        if (val > Expense.MAX_VAL) {
                            Toast.makeText(LabelList.this, getString(R.string.label_add_msg_err_value_to_high, Expense.MAX_VAL),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    } catch (final ParseException e) {
                        Toast.makeText(LabelList.this, getString(R.string.label_add_msg_err_invalid_value, value), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }
                final Label label = LabelList.this.labels.get(LabelList.this.edit);
                label.setGoal(val);
                try {
                    label.update(LabelList.this);
                    updateList();
                    toast(getString(R.string.label_edit_msg_label_added, label.getName()));
                } catch (final InvalidEntityUpdateException e) {
                    toast(getString(R.string.label_edit_err_label_added, label.getName()));
                }
            }
        };
        final OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        };
        alert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.label_edit_btn_save), positiveListener);
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_edit_btn_cancel), negativeListener);
        return alert;
    }

    private Dialog getSortDialog() {
        final OnClickListener positive = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                updateList();
            }
        };
        final OnClickListener negative = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        };
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setSingleChoiceItems(getResources().getStringArray(R.array.labels_dialog_sort), -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                LabelList.sort = SortLabelListType.getByCode(which);
            }
        });
        b.setTitle(getString(R.string.label_list_sort_header)).setPositiveButton(getString(R.string.label_list_sort_btn_set), positive)
                .setNegativeButton(getString(R.string.label_list_sort_btn_cancel), negative);
        return b.create();
    }

    private void initLayout() {
        setContentView(R.layout.label_list);
        final ListView list = (ListView) findViewById(android.R.id.list);
        registerForContextMenu(list);
        this.labels = new ArrayList<Label>();
        this.adapter = new EfficientAdapter(this);
        setListAdapter(this.adapter);
        updateList();
    }

    private void toast(final String message) {
        Toast.makeText(LabelList.this, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unchecked")
    private void updateList() {
        final LabelOrderRestriction restriction = new LabelOrderRestriction(LabelList.sort);
        this.labels = Label.repository(this).list(restriction);
        this.adapter.notifyDataSetChanged();
    }

    private class EfficientAdapter extends BaseAdapter {
        private final LayoutInflater item;

        public EfficientAdapter(final Context context) {
            this.item = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return LabelList.this.labels.size();
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
                convertView = this.item.inflate(R.layout.label_list_item, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.label_list_item_name);
                holder.goal = (TextView) convertView.findViewById(R.id.label_list_item_goal);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Label label = LabelList.this.labels.get(position);
            holder.name.setText(getString(R.string.label_list_item_txt_name) + " " + label.getName());
            if (label.getGoal() == 0) {
                holder.goal.setText(getString(R.string.label_list_item_txt_goal) + " --- ");
            } else {
                holder.goal.setText(getString(R.string.label_list_item_txt_goal) + " " + label.getGoal());
            }
            return convertView;

        }
    }

    private static class ViewHolder {
        TextView name;
        TextView goal;
    }

}
