package br.com.buzzo.moneypig;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TabHost;
import br.com.buzzo.moneypig.activity.expense.ExpenseList;
import br.com.buzzo.moneypig.activity.label.LabelList;
import br.com.buzzo.moneypig.activity.summary.SummaryTable;

public class Main extends TabActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // show EULA only the first time the program is run.
        Eula.show(this);
        initLayout();
    }

    private void initLayout() {
        // initializing layout
        final TabHost tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.main_tabs, tabHost.getTabContentView(), true);
        // summary
        tabHost.addTab(tabHost.newTabSpec(getString(R.string.tab_summary_header))
                // getLayoutInflater().inflate(R.layout.tab_summary, null)
                .setIndicator(getString(R.string.tab_summary_header), getResources().getDrawable(R.drawable.tab_summary))
                .setContent(new Intent(this, SummaryTable.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        // expenses
        tabHost.addTab(tabHost.newTabSpec(getString(R.string.tab_expenses_header))
                .setIndicator(getString(R.string.tab_expenses_header), getResources().getDrawable(R.drawable.tab_expenses))
                .setContent(new Intent(this, ExpenseList.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        // labels
        tabHost.addTab(tabHost.newTabSpec(getString(R.string.tab_labels_header))
                .setIndicator(getString(R.string.tab_labels_header), getResources().getDrawable(R.drawable.tab_labels))
                .setContent(new Intent(this, LabelList.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
    }
}
