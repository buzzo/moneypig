package br.com.buzzo.moneypig.activity.sms;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import br.com.buzzo.moneypig.R;
import br.com.buzzo.moneypig.db.ent.SMS;

public class SMSReceiver extends BroadcastReceiver {
    public static final int SMS_NOTIFICATION = 0;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Bundle bundle = intent.getExtras();
        final Object messages[] = (Object[]) bundle.get("pdus");
        final SmsMessage smsMessage[] = new SmsMessage[messages.length];
        for (int n = 0; n < messages.length; n++) {
            smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
        }
        final String message = smsMessage[0].getMessageBody();
        final String originator = smsMessage[0].getOriginatingAddress();
        if (originator != null && message != null && !"".equals(originator) && !"".equals(message)) {
            checkSMS(context, originator, message);
        }
    }

    private void checkSMS(final Context context, final String originator, final String message) {
        @SuppressWarnings("unchecked")
        final List<SMS> list = SMS.repository(context).list();
        for (final SMS sms : list) {
            if (originator.equals(String.valueOf(sms.getNumber()))) {
                // SMS originator match filter. Notify in bar.
                notification(context, sms, message);
            }
        }
    }

    private void notification(final Context context, final SMS sms, final String message) {
        final long now = System.currentTimeMillis();
        final Intent intent = new Intent(context, SMSExpense.class);
        intent.putExtra(SMSExpense.SMS_ID, sms.getId());
        intent.putExtra(SMSExpense.SMS_MESSAGE, message);
        intent.putExtra(SMSExpense.SMS_TIME, now);

        // this is MANDATORY to unique identify the intent or it will be reused!
        // details check:
        // http://stackoverflow.com/questions/3140072/android-keeps-caching-my-intents-extras-how-to-declare-a-pending-intent-that-kee
        intent.setAction("id" + System.currentTimeMillis());

        final PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);

        final Notification notification = new Notification(R.drawable.cash_register, context.getString(R.string.sms_expense_msg_bar), now);
        notification.setLatestEventInfo(context, context.getString(R.string.sms_expense_title),
                context.getString(R.string.sms_expense_text, sms.getName()), pending);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // make this notification unique by putting the 'tag' as the SMS ID.
        manager.notify(String.valueOf(sms.getId()), SMSReceiver.SMS_NOTIFICATION, notification);
    }
}
