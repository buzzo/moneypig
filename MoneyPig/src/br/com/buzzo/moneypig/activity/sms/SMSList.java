package br.com.buzzo.moneypig.activity.sms;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import br.com.buzzo.moneypig.R;
import br.com.buzzo.moneypig.db.ent.SMS;

public class SMSList extends ListActivity {
    private static final int SMS_LIST_RETURN = 0;

    private BaseAdapter      adapter;
    private List<SMS>        sms;

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int menuItemIndex = item.getItemId();
        if (menuItemIndex == 0) {
            // DELETE
            final SMS s = this.sms.get(info.position);
            s.delete(this);
            updateList();
            toast(getString(R.string.sms_ctx_menu_item_delete_msg_sms_deleted));
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
            menu.setHeaderTitle(getString(R.string.sms_ctx_menu_header));
            final String[] menuItems = getResources().getStringArray(R.array.sms_ctx_menu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sms_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sms_opts_add_sms:
                final Intent i = new Intent(SMSList.this, SMSAdd.class);
                startActivityForResult(i, SMSList.SMS_LIST_RETURN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SMS_LIST_RETURN:
                updateList();
                break;
            default:
                break;
        }
    }

    private void initLayout() {
        setContentView(R.layout.sms_list);
        this.sms = new ArrayList<SMS>();
        this.adapter = new EfficientAdapter(this);
        setListAdapter(this.adapter);
        updateList();
        final ListView list = (ListView) findViewById(android.R.id.list);
        registerForContextMenu(list);
    }

    private void toast(final String message) {
        Toast.makeText(SMSList.this, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unchecked")
    private void updateList() {
        this.sms = SMS.repository(this).list();
        this.adapter.notifyDataSetChanged();
    }

    private class EfficientAdapter extends BaseAdapter {
        private final LayoutInflater item;

        public EfficientAdapter(final Context context) {
            this.item = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return SMSList.this.sms.size();
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
                convertView = this.item.inflate(R.layout.sms_list_item, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.sms_list_item_name);
                holder.number = (TextView) convertView.findViewById(R.id.sms_list_item_income_number);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final SMS sms = SMSList.this.sms.get(position);
            holder.name.setText(getString(R.string.sms_list_item_txt_name) + ": " + sms.getName());
            holder.number.setText(getString(R.string.sms_list_item_txt_income_number) + ": " + sms.getNumber());
            return convertView;

        }
    }

    private static class ViewHolder {
        TextView name;
        TextView number;
    }

}
