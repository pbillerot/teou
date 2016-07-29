package eu.pbillerot.android.teou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Reception des SMS
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, ".onReceive " + intent.getAction());
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Log.w(TAG, "BroadcastReceiver failed, no intent data to process.");
            return;
        }
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            String smsOriginatingAddress = "";
            String smsDisplayMessage = "";
            /**
             * You have to CHOOSE which code snippet to use NEW (KitKat+), or legacy
             * Please comment out the for{} you don't want to use.
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.d(TAG, "KitKat or newer + API " + Build.VERSION.SDK_INT);
                // API level 19 (KitKat 4.4) getMessagesFromIntent
                for (SmsMessage message : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    if (message == null) {
                        Log.e(TAG, "SMS message is null -- ABORT");
                        break;
                    }
                    smsOriginatingAddress = message.getDisplayOriginatingAddress();
                    smsDisplayMessage = message.getDisplayMessageBody(); //see getMessageBody();
                }
            }
            else { // BELOW KITKAT
                Log.d(TAG, "legacy SMS implementation (before KitKat) API " + Build.VERSION.SDK_INT);
                // Processing SMS messages the OLD way, before KitKat, this WILL work on KitKat or newer Android
                // PDU is a “protocol data unit”, which is the industry format for an SMS message
                Object[] data = (Object[]) bundle.get("pdus");
                for (Object pdu : data) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
                    if (message == null) {
                        Log.e(TAG, "SMS message is null -- ABORT");
                        break;
                    }
                    smsOriginatingAddress = message.getDisplayOriginatingAddress();
                    smsDisplayMessage = message.getDisplayMessageBody(); // see getMessageBody();
                }
            }

            if ( contactExists(context, smsOriginatingAddress) ) {
                Toast.makeText(context.getApplicationContext(),
                        "TEOU sms de " + smsOriginatingAddress + " accepté"
                        , Toast.LENGTH_SHORT).show();
                Log.d(TAG, smsOriginatingAddress + ' ' + smsDisplayMessage);
                // On envoie le message au servics.msgReceiver
                Intent intentTeou = new Intent("TEOU_MESSAGE");
                intentTeou.putExtra("message", smsDisplayMessage);
                intentTeou.putExtra("telephone", smsOriginatingAddress);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentTeou);
            } else {
                Toast.makeText(context.getApplicationContext(),
                        "TEOU sms de " + smsOriginatingAddress + " refusé car inconnu des contacts"
                        , Toast.LENGTH_SHORT).show();
            }

        }
    } // end onReceive

    public boolean contactExists(Context context, String number) {
        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.DISPLAY_NAME
        };
        Cursor cur = context.getContentResolver().query(
                lookupUri,mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }
}
