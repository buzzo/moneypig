package br.com.buzzo.moneypig.activity.sms;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import br.com.buzzo.moneypig.R;
import br.com.buzzo.moneypig.db.ent.SMS;
import br.com.buzzo.moneypig.db.exception.EntityAlreadyExistsException;

public class SMSAdd extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
    }

    private void initLayout() {
        setContentView(R.layout.sms_add_filter);
        final Button btnCancel = (Button) findViewById(R.id.sms_add_btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                SMSAdd.this.finish();
            }
        });
        final Button btnAdd = (Button) findViewById(R.id.sms_add_btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final EditText nameTxt = (EditText) findViewById(R.id.sms_add_name);
                final String name = nameTxt.getText().toString();
                final EditText numberTxt = (EditText) findViewById(R.id.sms_add_income_number);
                final String number = numberTxt.getText().toString();
                final EditText regexpTxt = (EditText) findViewById(R.id.sms_add_regexp_value);
                final String regexp = regexpTxt.getText().toString();

                try {
                    Integer.parseInt(number);
                } catch (final NumberFormatException e) {
                    Toast.makeText(SMSAdd.this, getString(R.string.sms_add_msg_err_invalid_income_number), Toast.LENGTH_SHORT).show();
                    return;
                }

                final SMS sms = new SMS(name, Integer.parseInt(number), regexp);
                try {
                    sms.persist(SMSAdd.this);
                    Toast.makeText(SMSAdd.this, getString(R.string.sms_add_msg_sms_filter_added), Toast.LENGTH_SHORT).show();
                } catch (final EntityAlreadyExistsException e) {
                    Toast.makeText(SMSAdd.this, getString(R.string.sms_add_msg_err_invalid_name_alread_exists, name), Toast.LENGTH_LONG)
                            .show();
                }

                SMSAdd.this.finish();
            }
        });

    }
}
