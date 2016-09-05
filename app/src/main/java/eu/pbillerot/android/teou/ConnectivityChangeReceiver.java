package eu.pbillerot.android.teou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by billerot on 02/09/16.
 */
public class ConnectivityChangeReceiver
        extends BroadcastReceiver {
    private static final String TAG = "ConnectivityChange";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(TAG, "action: " + intent.getAction());

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

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        // on informe le service
        if ( isConnected ) {
            Log.v(TAG, "CONNECTED");
            //Log.v(TAG, "" + activeNetwork.getTypeName() + " " + activeNetwork.getExtraInfo() + " " + activeNetwork.getState());
            Intent intentTeou = new Intent("TEOU_MESSAGE");
            intentTeou.putExtra("message", "NETWORK_OK");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentTeou);
        } else {
            Log.v(TAG, "NOT CONNECTED");
            //setMobileConnectionEnabled(context, true);
        }

    }

    /** The absence of a connection type. */
    public static final int TYPE_NONE = -1;
    /** Unknown network class. */
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    /** Class of broadly defined "2G" networks. */
    public static final int NETWORK_CLASS_2_G = 1;
    /** Class of broadly defined "3G" networks. */
    public static final int NETWORK_CLASS_3_G = 2;
    /** Class of broadly defined "4G" networks. */
    public static final int NETWORK_CLASS_4_G = 3;
    public static String getNetworkClass(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info==null || !info.isConnected())
            return "-"; //not connected
        if(info.getType() == ConnectivityManager.TYPE_WIFI)
            return "WIFI";
        if(info.getType() == ConnectivityManager.TYPE_MOBILE){
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                    return "4G";
                default:
                    return "?";
            }
        }
        return "?";
    }

    /**
     * Returns details about the currently active default data network. When connected, this network
     * is the default route for outgoing connections. You should always check {@link
     * NetworkInfo#isConnected()} before initiating network traffic. This may return {@code null}
     * when there is no default network.
     *
     * @return a {@link NetworkInfo} object for the current default network or {@code null} if no
     * network default network is currently active
     *
     * This method requires the call to hold the permission
     * {@link android.Manifest.permission#ACCESS_NETWORK_STATE}.
     * @see ConnectivityManager#getActiveNetworkInfo()
     */
    public static NetworkInfo getInfo(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
    }

    /**
     * Reports the current network type.
     *
     * @return {@link ConnectivityManager#TYPE_MOBILE}, {@link ConnectivityManager#TYPE_WIFI} ,
     * {@link ConnectivityManager#TYPE_WIMAX}, {@link ConnectivityManager#TYPE_ETHERNET}, {@link
     * ConnectivityManager#TYPE_BLUETOOTH}, or other types defined by {@link ConnectivityManager}.
     * If there is no network connection then -1 is returned.
     * @see NetworkInfo#getType()
     */
    public static int getType(Context context) {
        NetworkInfo info = getInfo(context);
        if (info == null || !info.isConnected()) {
            return TYPE_NONE;
        }
        return info.getType();
    }

    /**
     * Return a network-type-specific integer describing the subtype of the network.
     *
     * @return the network subtype
     * @see NetworkInfo#getSubtype()
     */
    public static int getSubType(Context context) {
        NetworkInfo info = getInfo(context);
        if (info == null || !info.isConnected()) {
            return TYPE_NONE;
        }
        return info.getSubtype();
    }

    /** Returns the NETWORK_TYPE_xxxx for current data connection. */
    public static int getNetworkType(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                .getNetworkType();
    }

    /** Check if there is any connectivity */
    public static boolean isConnected(Context context) {
        return getType(context) != TYPE_NONE;
    }

    /** Check if there is any connectivity to a Wifi network */
    public static boolean isWifiConnection(Context context) {
        NetworkInfo info = getInfo(context);
        if (info == null || !info.isConnected()) {
            return false;
        }
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                return true;
            default:
                return false;
        }
    }

    /** Check if there is any connectivity to a mobile network */
    public static boolean isMobileConnection(Context context) {
        NetworkInfo info = getInfo(context);
        if (info == null || !info.isConnected()) {
            return false;
        }
        switch (info.getType()) {
            case ConnectivityManager.TYPE_MOBILE:
                return true;
            default:
                return false;
        }
    }

    /** Check if the current connection is fast. */
    public static boolean isConnectionFast(Context context) {
        NetworkInfo info = getInfo(context);
        if (info == null || !info.isConnected()) {
            return false;
        }
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_ETHERNET:
                return true;
            case ConnectivityManager.TYPE_MOBILE:
                int networkClass = getNetworkClass(getNetworkType(context));
                switch (networkClass) {
                    case NETWORK_CLASS_UNKNOWN:
                    case NETWORK_CLASS_2_G:
                        return false;
                    case NETWORK_CLASS_3_G:
                    case NETWORK_CLASS_4_G:
                        return true;
                }
            default:
                return false;
        }
    }

    private static int getNetworkClassReflect(int networkType)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getNetworkClass = TelephonyManager.class.getDeclaredMethod("getNetworkClass", int.class);
        if (!getNetworkClass.isAccessible()) {
            getNetworkClass.setAccessible(true);
        }
        return (int) getNetworkClass.invoke(null, networkType);
    }

    /**
     * Return general class of network type, such as "3G" or "4G". In cases where classification is
     * contentious, this method is conservative.
     */
    public static int getNetworkClass(int networkType) {
        try {
            return getNetworkClassReflect(networkType);
        } catch (Exception ignored) {
        }

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case 16: // TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case 17: // TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return NETWORK_CLASS_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
            case 18: // TelephonyManager.NETWORK_TYPE_IWLAN:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }

    private static boolean setMobileConnectionEnabled(Context context, boolean enabled)
    {
        try{
            // Requires: android.permission.CHANGE_NETWORK_STATE
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD){
                // pre-Gingerbread sucks!
                final TelephonyManager telMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                final Method getITelephony = telMgr.getClass().getDeclaredMethod("getITelephony");
                getITelephony.setAccessible(true);
                final Object objITelephony = getITelephony.invoke(telMgr);
                final Method toggleDataConnectivity = objITelephony.getClass()
                        .getDeclaredMethod(enabled ? "enableDataConnectivity" : "disableDataConnectivity");
                toggleDataConnectivity.setAccessible(true);
                toggleDataConnectivity.invoke(objITelephony);
            }
            // Requires: android.permission.CHANGE_NETWORK_STATE
            else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                // Gingerbread to KitKat inclusive
                final Field serviceField = connMgr.getClass().getDeclaredField("mService");
                serviceField.setAccessible(true);
                final Object connService = serviceField.get(connMgr);
                try{
                    final Method setMobileDataEnabled = connService.getClass()
                            .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                    setMobileDataEnabled.setAccessible(true);
                    setMobileDataEnabled.invoke(connService, Boolean.valueOf(enabled));
                }
                catch(NoSuchMethodException e){
                    // Support for CyanogenMod 11+
                    final Method setMobileDataEnabled = connService.getClass()
                            .getDeclaredMethod("setMobileDataEnabled", String.class, Boolean.TYPE);
                    setMobileDataEnabled.setAccessible(true);
                    setMobileDataEnabled.invoke(connService, context.getPackageName(), Boolean.valueOf(enabled));
                }
            }
            // Requires: android.permission.MODIFY_PHONE_STATE (System only, here for completions sake)
            else{
                // Lollipop and into the Future!
                final TelephonyManager telMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                final Method setDataEnabled = telMgr.getClass().getDeclaredMethod("setDataEnabled", Boolean.TYPE);
                setDataEnabled.setAccessible(true);
                setDataEnabled.invoke(telMgr, Boolean.valueOf(enabled));
            }
            return true;
        }
        catch(NoSuchFieldException e){
            Log.e(TAG, "setMobileConnectionEnabled", e);
        }
        catch(IllegalAccessException e){
            Log.e(TAG, "setMobileConnectionEnabled", e);
        }
        catch(IllegalArgumentException e){
            Log.e(TAG, "setMobileConnectionEnabled", e);
        }
        catch(NoSuchMethodException e){
            Log.e(TAG, "setMobileConnectionEnabled", e);
        }
        catch(InvocationTargetException e){
            Log.e(TAG, "setMobileConnectionEnabled", e);
        }
        return false;
    }

}
