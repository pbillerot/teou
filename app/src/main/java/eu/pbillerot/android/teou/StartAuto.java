package eu.pbillerot.android.teou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartAuto extends BroadcastReceiver {
/*
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>

        <receiver android:name=".StartAuto">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"/>
            </intent-filter>
        </receiver>
 */
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, ServiceTeou.class);
        context.startService(i);
    }
}
