package eu.pbillerot.android.teou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by billerot on 02/09/16.
 */
public class ConnectivityChangeReceiver
        extends BroadcastReceiver {
    private static final String TAG = "ConnectivityChange";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(TAG, "action: " + intent.getAction());

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        // on informe le service
        if ( isConnected ) {
            Log.v(TAG, "" + activeNetwork.getTypeName() + " " + activeNetwork.getExtraInfo() + " " + activeNetwork.getState());
            Intent intentTeou = new Intent("TEOU_MESSAGE");
            intentTeou.putExtra("message", "NETWORK_OK");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentTeou);
        } else {
            Log.v(TAG, "NOT CONNECTED");
        }

//        Bundle extras = intent.getExtras();
//        if (extras != null) {
//            for (String key: extras.keySet()) {
//                Log.d(TAG, "key [" + key + "]: " +
//                        extras.get(key));
//            }
//        }
//        else {
//            Log.d(TAG, "no extras");
//        }
    }
}
