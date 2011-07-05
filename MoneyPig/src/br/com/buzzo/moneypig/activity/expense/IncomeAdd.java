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
import br.com.buzzo.moneypig.db.ent.Expense.ExpenseMode;
import br.com.buzzo.moneypig.db.ent.Label;
import br.com.buzzo.moneypig.db.exception.EntityAlreadyExistsException;
import br.com.buzzo.moneypig.db.restriction.LabelIdRestriction;

public class IncomeAdd extends Activity {
    public static final int   DIALOG_DATE_ID   = 0;
    public static final int   DIALOG_VALUE_ID  = 1;
    public static final int   DIALOG_LABELS_ID = 2;

    private Date              date;
    private double            value;
    private final List<Label> checked          = new ArrayList<Label>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                        IncomeAdd.this.date = c.getTime();
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
                b.setMultiChoiceItems(getLabels(), null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
                        if (isChecked) {
                            IncomeAdd.this.checked.add(labels.get(which));
                        } else {
                            IncomeAdd.this.checked.remove(labels.get(which));
                        }
                    }
                });
                b.setTitle(getString(R.string.income_add_labels_add_header))
                        .setPositiveButton(getString(R.string.income_add_btn_set), positive)
                        .setNegativeButton(getString(R.string.income_add_btn_cancel), negative);
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
                                Toast.makeText(IncomeAdd.this, getString(R.string.income_add_msg_err_value_to_high, Expense.MAX_VAL),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            IncomeAdd.this.value = val;
                            updateMoneyDisplay();
                        } catch (final ParseException e) {
                            Toast.makeText(IncomeAdd.this, getString(R.string.income_add_msg_err_invalid_value, value), Toast.LENGTH_SHORT)
                                    .show();
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
                builder.setView(LayoutInflater.from(this).inflate(R.layout.income_add_value, null))
                        .setTitle(getString(R.string.income_add_value_header)).setCancelable(false)
                        .setPositiveButton(getString(R.string.income_add_btn_set), positiveBtn)
                        .setNegativeButton(getString(R.string.income_add_btn_cancel), negativeBtn);
                return builder.create();
        }
        return null;
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
        this.date = new Date();
        final Button btn = (Button) findViewById(R.id.income_add_btn_date);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(IncomeAdd.DIALOG_DATE_ID);
            }
        });
        updateDateDisplay();
    }

    private void initBtnMoney() {
        this.value = 0;
        final Button btn = (Button) findViewById(R.id.income_add_btn_value);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(IncomeAdd.DIALOG_VALUE_ID);
            }
        });
        updateMoneyDisplay();
    }

    private void initLayout() {
        setContentView(R.layout.income_add);
        final Button btnCancel = (Button) findViewById(R.id.income_add_btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                IncomeAdd.this.finish();
            }
        });
        final Button btnAdd = (Button) findViewById(R.id.income_add_btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final EditText text = (EditText) findViewById(R.id.income_add_descr);
                final String descr = text.getText().toString();
                final Expense expense = new Expense(ExpenseMode.INCOME, IncomeAdd.this.date, IncomeAdd.this.value, descr);
                for (final Label label : IncomeAdd.this.checked) {
                    expense.addLabel(label);
                }
                try {
                    expense.persist(IncomeAdd.this);
                    Toast.makeText(IncomeAdd.this, getString(R.string.income_add_msg_income_added), Toast.LENGTH_SHORT).show();
                } catch (final EntityAlreadyExistsException e) {
                    Toast.makeText(IncomeAdd.this, getString(R.string.income_add_msg_err_income), Toast.LENGTH_SHORT).show();
                }

                IncomeAdd.this.finish();
            }
        });
        final Button btnLabels = (Button) findViewById(R.id.income_add_btn_labels);
        btnLabels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(IncomeAdd.DIALOG_LABELS_ID);
            }
        });
        initBtnMoney();
        initBtnDate();
    }

    private void updateDateDisplay() {
        final Button btn = (Button) findViewById(R.id.income_add_btn_date);
        btn.setText(DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(this.date));
    }

    private void updateMoneyDisplay() {
        final Button btn = (Button) findViewById(R.id.income_add_btn_value);
        btn.setText(getString(R.string.income_add_value, ExpenseList.NF.format(this.value)));
    }
}
