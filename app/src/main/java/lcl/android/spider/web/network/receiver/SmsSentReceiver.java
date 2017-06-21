package lcl.android.spider.web.network.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

/**
 * Created by CHS on 2017-06-19.
 */
public class SmsSentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context _context, Intent _intent) {
        String msg="";
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                // 전송 성공 처리; break;
                msg="sms 전송 성공";
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                // 일반적인 실패 처리; break;
                msg="sms 전송 실패";
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                // 무선 꺼짐 처리; break;
                msg="무선 꺼짐";
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                // PDU 실패 처리; break;
                msg="pdu 오류";
                break;
        }
        Toast t = Toast.makeText(_context, msg, Toast.LENGTH_SHORT);
        t.show();
    }
}
