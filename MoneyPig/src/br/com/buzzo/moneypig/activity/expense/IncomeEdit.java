package br.com.buzzo.moneypig.activity.expense;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import br.com.buzzo.moneypig.R;
import br.com.buzzo.moneypig.db.ent.Expense;
import br.com.buzzo.moneypig.db.ent.Label;
import br.com.buzzo.moneypig.db.exception.InvalidEntityUpdateException;
import br.com.buzzo.moneypig.db.restriction.ExpenseIdRestriction;
import br.com.buzzo.moneypig.db.restriction.LabelIdRestriction;

public class IncomeEdit extends Activity {
    public static final int DIALOG_DATE_ID   = 0;
    public static final int DIALOG_VALUE_ID  = 1;
    public static final int DIALOG_LABELS_ID = 2;

    private Date            date;
    private double          value;
    private Expense         expense;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int id = getIntent().getIntExtra(ExpenseList.ID_TO_EDIT, 0);
        this.expense = Expense.repository(this).list(new ExpenseIdRestriction[] { new ExpenseIdRestriction(id) }).get(0);
        initLayout();
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case DIALOG_DATE_ID:
                final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(final DatePicker view, final int year, final int month, final int day) {
                        final Calendar c = Calendar.getInstance();
                        c.set(year, month, day);
                        IncomeEdit.this.date = c.getTime();
                        updateDateDisplay();
                    }
                };
                final Calendar c = Calendar.getInstance();
                c.setTime(this.date);
                return new DatePickerDialog(this, listener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            case DIALOG_LABELS_ID:
                final OnClickListener positive = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                    }
                };
                final OnClickListener negative = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                };
                final List<Label> labels = Label.repository(this).list(new LabelIdRestriction[0]);
                final AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setMultiChoiceItems(getLabels(), getCheckedLabels(), new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
                        if (isChecked) {
                            IncomeEdit.this.expense.getLabels().add(labels.get(which));
                        } else {
                            IncomeEdit.this.expense.getLabels().remove(labels.get(which));
                        }
                    }
                });
                b.setTitle(getString(R.string.income_edit_labels_edit_header))
                        .setPositiveButton(getString(R.string.income_edit_btn_set), positive)
                        .setNegativeButton(getString(R.string.income_edit_btn_cancel), negative);
                return b.create();
            case DIALOG_VALUE_ID:
                final OnClickListener positiveBtn = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        final AlertDialog alertDialog = (AlertDialog) dialog;
                        final EditText text = (EditText) alertDialog.findViewById(R.id.income_add_value);
                        final String value = text.getText().toString();
                        try {
                            final double val = ExpenseList.NF.parse(value).doubleValue();
                            if (val > Expense.MAX_VAL) {
                                Toast.makeText(IncomeEdit.this, getString(R.string.income_edit_msg_err_value_to_high, Expense.MAX_VAL),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            IncomeEdit.this.value = val;
                            updateMoneyDisplay();
                        } catch (final ParseException e) {
                            Toast.makeText(IncomeEdit.this, getString(R.string.income_edit_msg_err_invalid_value, value),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                final OnClickListener negativeBtn = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                };
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final View view = LayoutInflater.from(this).inflate(R.layout.income_add_value, null);
                final EditText text = (EditText) view.findViewById(R.id.income_add_value);
                text.setText(ExpenseList.NF.format(this.value));
                builder.setView(view).setTitle(getString(R.string.income_edit_value_header)).setCancelable(false)
                        .setPositiveButton(getString(R.string.income_edit_btn_set), positiveBtn)
                        .setNegativeButton(getString(R.string.income_edit_btn_cancel), negativeBtn);
                return builder.create();
        }
        return null;
    }

    private boolean[] getCheckedLabels() {
        final List<Label> labels = Label.repository(this).list(new LabelIdRestriction[0]);
        final boolean[] checked = new boolean[labels.size()];
        int c = 0;
        for (final Label label : labels) {
            checked[c] = this.expense.getLabels().contains(label);
            c++;
        }
        return checked;
    }

    private String[] getLabels() {
        final List<Label> labels = Label.repository(this).list(new LabelIdRestriction[0]);
        final List<String> names = new ArrayList<String>();
        for (final Label label : labels) {
            names.add(label.getName());
        }
        return names.toArray(new String[] {});
    }

    private void initBtnDate() {
        this.date = this.expense.getDate();
        final Button btn = (Button) findViewById(R.id.income_edit_btn_date);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(IncomeEdit.DIALOG_DATE_ID);
            }
        });
        updateDateDisplay();
    }

    private void initBtnMoney() {
        this.value = this.expense.getValue();
        final Button btn = (Button) findViewById(R.id.income_edit_btn_value);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(IncomeEdit.DIALOG_VALUE_ID);
            }
        });
        updateMoneyDisplay();
    }

    private void initLayout() {
        setContentView(R.layout.income_edit);
        final Button btnCancel = (Button) findViewById(R.id.income_edit_btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                IncomeEdit.this.finish();
            }
        });
        final Button btnAdd = (Button) findViewById(R.id.income_edit_btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    final EditText txt = (EditText) findViewById(R.id.income_edit_descr);
                    IncomeEdit.this.expense.setDescr(txt.getText().toString());
                    IncomeEdit.this.expense.update(IncomeEdit.this);
                    Toast.makeText(IncomeEdit.this, getString(R.string.income_edit_msg_income_added), Toast.LENGTH_SHORT).show();
                } catch (final InvalidEntityUpdateException e) {
                    Toast.makeText(IncomeEdit.this, getString(R.string.income_edit_msg_err_income), Toast.LENGTH_SHORT).show();
                }
                IncomeEdit.this.finish();
            }
        });
        final Button btnLabels = (Button) findViewById(R.id.income_edit_btn_labels);
        btnLabels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(IncomeEdit.DIALOG_LABELS_ID);
            }
        });
        initBtnMoney();
        initBtnDate();
        initTxtDescr();
    }

    private void initTxtDescr() {
        final EditText txt = (EditText) findViewById(R.id.income_edit_descr);
        txt.setText(this.expense.getDescr());
    }

    private void updateDateDisplay() {
        final Button btn = (Button) findViewById(R.id.income_edit_btn_date);
        btn.setText(DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(this.date));
        this.expense.setDate(this.date);
    }

    private void updateMoneyDisplay() {
        final Button btn = (Button) findViewById(R.id.income_edit_btn_value);
        btn.setText(getString(R.string.income_add_value, ExpenseList.NF.format(this.value)));
        this.expense.setValue(this.value);
    }
}
