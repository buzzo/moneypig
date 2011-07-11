package br.com.buzzo.moneypig.activity.sms;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import br.com.buzzo.moneypig.R;
import br.com.buzzo.moneypig.activity.expense.ExpenseList;
import br.com.buzzo.moneypig.db.ent.Expense;
import br.com.buzzo.moneypig.db.ent.Expense.ExpenseMode;
import br.com.buzzo.moneypig.db.ent.Label;
import br.com.buzzo.moneypig.db.ent.SMS;
import br.com.buzzo.moneypig.db.exception.EntityAlreadyExistsException;
import br.com.buzzo.moneypig.db.restriction.LabelIdRestriction;
import br.com.buzzo.moneypig.db.restriction.SMSIdRestriction;

public class SMSExpense extends Activity {

    public static final String SMS_TIME         = "SMS Time";
    public static final String SMS_MESSAGE      = "SMS Message";
    public static final String SMS_ID           = "SMS ID";

    public static final int    DIALOG_LABELS_ID = 0;
    public static final int    DIALOG_DATE_ID   = 1;

    private final List<Label>  checked          = new ArrayList<Label>();

    private Date               date;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
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
                            SMSExpense.this.checked.add(labels.get(which));
                        } else {
                            SMSExpense.this.checked.remove(labels.get(which));
                        }
                    }
                });
                b.setTitle(getString(R.string.outcome_add_labels_add_header))
                        .setPositiveButton(getString(R.string.outcome_add_btn_set), positive)
                        .setNegativeButton(getString(R.string.outcome_add_btn_cancel), negative);
                return b.create();
            case DIALOG_DATE_ID:
                final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(final DatePicker view, final int year, final int month, final int day) {
                        final Calendar c = Calendar.getInstance();
                        c.set(year, month, day);
                        SMSExpense.this.date = c.getTime();
                        updateDateDisplay();
                    }
                };
                final Calendar c = Calendar.getInstance();
                c.setTime(this.date);
                return new DatePickerDialog(this, listener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
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
        this.date = new Date(getIntent().getLongExtra(SMSExpense.SMS_TIME, System.currentTimeMillis()));
        final Button btn = (Button) findViewById(R.id.sms_expense_btn_date);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(SMSExpense.DIALOG_DATE_ID);
            }
        });
        updateDateDisplay();
    }

    private void initLayout() {
        setContentView(R.layout.sms_expense);
        final Button btnIncome = (Button) findViewById(R.id.sms_expense_btn_income);
        btnIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // description
                final String descr = getDescription();
                // value
                final double val = getValue();
                final Expense expense = new Expense(ExpenseMode.INCOME, SMSExpense.this.date, val, descr);
                for (final Label label : SMSExpense.this.checked) {
                    expense.addLabel(label);
                }
                try {
                    expense.persist(SMSExpense.this);
                    Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_suc_income_added), Toast.LENGTH_SHORT).show();
                } catch (final EntityAlreadyExistsException e) {
                    Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_err_income), Toast.LENGTH_SHORT).show();
                }
                SMSExpense.this.finish();
            }

            private String getDescription() {
                final EditText descrTxt = (EditText) findViewById(R.id.sms_expense_descr);
                final String descr = descrTxt.getText().toString();
                return descr;
            }

            private Double getValue() {
                final EditText valueTxt = (EditText) findViewById(R.id.sms_expense_value);
                final String value = valueTxt.getText().toString();
                try {
                    final double val = ExpenseList.NF.parse(value).doubleValue();
                    if (val > Expense.MAX_VAL) {
                        Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_err_value_to_high, Expense.MAX_VAL),
                                Toast.LENGTH_LONG).show();
                        return null;
                    }
                    return val;
                } catch (final ParseException e) {
                    Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_err_invalid_value, value), Toast.LENGTH_SHORT)
                            .show();
                    return null;
                }
            }
        });
        final Button btnOutcome = (Button) findViewById(R.id.sms_expense_btn_outcome);
        btnOutcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // description
                final String descr = getDescription();
                // value
                final double val = getValue();
                final Expense expense = new Expense(ExpenseMode.OUTCOME, SMSExpense.this.date, val, descr);
                for (final Label label : SMSExpense.this.checked) {
                    expense.addLabel(label);
                }
                try {
                    expense.persist(SMSExpense.this);
                    Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_suc_outcome_added), Toast.LENGTH_SHORT).show();
                } catch (final EntityAlreadyExistsException e) {
                    Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_err_outcome), Toast.LENGTH_SHORT).show();
                }
                SMSExpense.this.finish();
            }

            private String getDescription() {
                final EditText descrTxt = (EditText) findViewById(R.id.sms_expense_descr);
                final String descr = descrTxt.getText().toString();
                return descr;
            }

            private Double getValue() {
                final EditText valueTxt = (EditText) findViewById(R.id.sms_expense_value);
                final String value = valueTxt.getText().toString();
                try {
                    final double val = ExpenseList.NF.parse(value).doubleValue();
                    if (val > Expense.MAX_VAL) {
                        Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_err_value_to_high, Expense.MAX_VAL),
                                Toast.LENGTH_LONG).show();
                        return null;
                    }
                    return val;
                } catch (final ParseException e) {
                    Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_err_invalid_value, value), Toast.LENGTH_SHORT)
                            .show();
                    return null;
                }
            }
        });

        final Button btnLabels = (Button) findViewById(R.id.sms_expense_labels);
        btnLabels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(SMSExpense.DIALOG_LABELS_ID);
            }
        });

        initValue();
        initBtnDate();
    }

    private void initValue() {
        final int smsID = getIntent().getIntExtra(SMSExpense.SMS_ID, 0);
        @SuppressWarnings("unchecked")
        final List<SMS> list = SMS.repository(this).list(new SMSIdRestriction(smsID));
        if (list.isEmpty()) {
            Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_err_not_find_sms), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        final SMS sms = list.get(0);
        final String message = getIntent().getStringExtra(SMSExpense.SMS_MESSAGE);
        double value;
        try {
            final Matcher m = Pattern.compile(sms.getRegexp()).matcher(message);
            if (!m.find()) {
                Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_err_no_value_match), Toast.LENGTH_LONG).show();
                value = 0;
            } else {
                final String valueStr = m.group();
                try {
                    value = ExpenseList.NF.parse(valueStr).doubleValue();
                } catch (final ParseException e) {
                    Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_err_parse_value, valueStr), Toast.LENGTH_LONG)
                            .show();
                    value = 0;
                }
            }
        } catch (final PatternSyntaxException e) {
            Toast.makeText(SMSExpense.this, getString(R.string.sms_expense_msg_err_wrong_regex), Toast.LENGTH_LONG).show();
            value = 0;
        }
        final EditText valueTxt = (EditText) findViewById(R.id.sms_expense_value);
        valueTxt.setText(ExpenseList.NF.format(value));
    }

    private void updateDateDisplay() {
        final Button btn = (Button) findViewById(R.id.sms_expense_btn_date);
        btn.setText(DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(this.date));
    }
}
