package eu.pbillerot.android.teou;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import android.util.Log;

public class ServiceTeou extends Service implements LocationListener {
    private static final String TAG = "ServiceTeou";

    private NotificationManager mNotificationManager;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int ID_NOTIFICATION = R.string.local_service_started;

    // SMS
    SmsReceiver smsReceiver;
    boolean isRegistered = false;
    BroadcastReceiver msgReceiver;
    String mTelephoneDemandeur = "";

    // GPS
    private LocationManager locationManager = null;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location mLocation; // mLocation
    double latitude = 0; // latitude
    double longitude = 0; // longitude
    String messageRetourGPS = "";
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    public ServiceTeou() {
    }

    @Override
    public void onCreate() {

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotify();
        if ( BuildConfig.DEBUG ) Log.d(TAG, "Notification ok");

        // Reception des SMS
        this.smsReceiver = new SmsReceiver();
//            LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(smsReceiver, new IntentFilter(
//                    "android.provider.Telephony.SMS_RECEIVED"));
        // Attention en enregistrant le receiver sur l'applicationContext cela ne marche pas
        // La raison est sans doute la présence en continu du service
        this.registerReceiver(smsReceiver, new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED"));
        this.isRegistered = true;
        if ( BuildConfig.DEBUG ) Log.d(TAG, "smsReceiver ok");

        // Recepteur local pour les intents
        msgReceiver = new MsgReceiver();
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(msgReceiver,
                new IntentFilter("TEOU_MESSAGE"));

        //Toast.makeText(this, TAG + " --> Service started", Toast.LENGTH_SHORT).show();
        if ( BuildConfig.DEBUG ) Log.d(TAG, "Service started");

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // Redemarrage en cas de d'arret
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onDestroy");
        // Cancel the persistent notification.
        mNotificationManager.cancel(ID_NOTIFICATION);

        // Arrêt msgReceiver
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(msgReceiver);

        // Arrêt réception SMS
        if (this.isRegistered) {
            //LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(smsReceiver);
            this.unregisterReceiver(smsReceiver);
            this.isRegistered = false;
            if ( BuildConfig.DEBUG ) Log.d(TAG, "SmsReceiver done");
        }
        if ( BuildConfig.DEBUG ) Log.d(TAG, "Service done");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), "TEOU_MESSAGE")) {

                String message = intent.getStringExtra("message");
                if ( BuildConfig.DEBUG ) Log.d(TAG, "Message: " + message);

                if (message.toUpperCase().startsWith("TEOU")) {
                    /*
                    Réception du SMS_TEOU
                    on récupère la postion de l'appareil
                    on envoie la postion au demandeur
                     */
                    // téléphone du demandeur
                    mTelephoneDemandeur = intent.getStringExtra("telephone");
                    messageRetourGPS = "GPS_RETURN";
                    getmLocation();
                } else if (message.equalsIgnoreCase("GPS_RETURN")) {
                    /*
                    Réception du gps
                     */
                    // Point du contact
                    GpxPoint gpxPoint = (GpxPoint)intent.getSerializableExtra("gpxPoint");
                    String url = gpxPoint.getUrl();
                    SmsSender mySms = new SmsSender();
                    mySms.sendSMS(mTelephoneDemandeur, "SUILA " + url, getApplicationContext());

                } else if (message.equalsIgnoreCase("REQ_POSITION")) {
                    /*
                        Demande de position de l'activité
                     */
                    messageRetourGPS = "POSITION_RECEIVER";
                    getmLocation();
                    //String position = ServiceTeou.URL_OSM + "url=" + latitude + "&lon=" + longitude;
                    // timeout
                    // Execute some code after 2 seconds have passed
                    // je ne sais pas comment l'arrêter si la réponse arrive avant
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if ( BuildConfig.DEBUG ) Log.d(TAG, "timeout GPS");
//                            stopUsingGPS();
//                        }
//                    }, 15000); // en millisecondes
                    // envoi de la dernière position à l'ecouteur MapActivity.PositionReceiver
                    // la position actualisée sera envoyée par onLocationChanged
//                    Intent intentPosition = new Intent("POSITION_RECEIVER");
//                    intentPosition.putExtra("position", position);
//                    intentPosition.putExtra("from", "local");
//                    getApplicationContext().sendBroadcast(intentPosition);
                } else if (message.startsWith("SUILA ")) {
                    /*
                    Réception du SMS SUILA avec la position derrière
                     */
                    if ( BuildConfig.DEBUG ) Log.d(TAG, "demande activité en avant plan");

                    String url = message.substring("SUILA ".length());
                    String telephone = intent.getStringExtra("telephone");
                    GpxPoint gpxPoint;
                    gpxPoint = new GpxPoint(0, telephone, telephone, url);
                    // sauvegarde du point
                    GpxDataSource gpxDataSource = new GpxDataSource(getApplicationContext());
                    gpxDataSource.open();
                    long indexId = gpxDataSource.createGpx(gpxPoint);
                    gpxDataSource.close();
                    gpxPoint.setId(indexId);

                    // Mise en avant plan de l'activité
                    Intent i = new Intent();
                    i.setClass(getBaseContext(), MapActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                    i.putExtra("gpxPoint", gpxPoint);
                    getBaseContext().startActivity(i);

                    Intent intentPosition = new Intent("POSITION_RECEIVER");
                    intentPosition.putExtra("gpxPoint", gpxPoint);
                    getBaseContext().sendBroadcast(intentPosition);

                }
            }
        }
    }

    //Méthode qui crée la notification
    private void createNotify() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.teou_zzz)
                        .setOngoing(true)
                        .setContentTitle("TEOU")
                        .setContentText(getString(R.string.teou_ecoute) + "...");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MapActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MapActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        // mId allows you to update the notification later on.
        mNotificationManager.notify(this.ID_NOTIFICATION, notification);
    }


    /*
        GESTION DU GPS
     */
    @Override
    public void onProviderDisabled(String provider) {
        if ( BuildConfig.DEBUG ) Log.d(TAG, provider + " disabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        if ( BuildConfig.DEBUG ) Log.d(TAG, provider + " disabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onLocationChanged " + messageRetourGPS + " " + location.toString());

        mLocation = location;

        TelephonyManager tMgr;
        tMgr= (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String telephoneDevice = tMgr.getLine1Number();

        GpxPoint gpxPoint = new GpxPoint(0, telephoneDevice, telephoneDevice
                , location.getLatitude(), location.getLongitude());

        try {
            if (messageRetourGPS.equals("POSITION_RECEIVER")) {
                // envoi de la position à l'ecouteur MapActivity.PositionReceiver
                gpxPoint.setName(getString(R.string.from_local));
                // sauvegarde du point
                GpxDataSource gpxDataSource = new GpxDataSource(getApplicationContext());
                gpxDataSource.open();
                long indexId = gpxDataSource.createGpx(gpxPoint);
                gpxDataSource.close();
                gpxPoint.setId(indexId);

                // Mise en avant plan de l'activité
                Intent i = new Intent();
                i.setClass(getBaseContext(), MapActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                i.putExtra("gpxPoint", gpxPoint);
                getBaseContext().startActivity(i);

                Intent intentPosition = new Intent("POSITION_RECEIVER");
                intentPosition.putExtra("gpxPoint", gpxPoint);
                getApplicationContext().sendBroadcast(intentPosition);
            }
            if (messageRetourGPS.equals("GPS_RETURN")) {
                // envoi de la position à l'ecouteur MapActivity.PositionReceiver
                Intent intentPosition = new Intent("TEOU_MESSAGE");
                intentPosition.putExtra("message", "GPS_RETURN");
                intentPosition.putExtra("gpxPoint", gpxPoint);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentPosition);
            }
            // dès l'obtention de la mise à jour on arrête le GPS. Les batteries vont apprécier.
            stopUsingGPS();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Location getmLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            if ( BuildConfig.DEBUG ) Log.d(TAG, "isGPSEnabled : " + isGPSEnabled);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if ( BuildConfig.DEBUG ) Log.d(TAG, "isNetworkEnabled : " + isNetworkEnabled);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enable
                ;
            } else {
                this.canGetLocation = true;

                if (Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    return mLocation;
                }

                // First get mLocation from Network Provider

                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if ( BuildConfig.DEBUG ) Log.d(TAG, "GPS Enabled");
                    if (locationManager != null) {
                        mLocation = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (mLocation != null) {
                            latitude = mLocation.getLatitude();
                            longitude = mLocation.getLongitude();
                        }
                    }
                }
                // if Network Enabled get mLocation from Network Provider
                if (isNetworkEnabled) {
                    if (mLocation == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if ( BuildConfig.DEBUG ) Log.d(TAG, "Network");
                        if (locationManager != null) {
                            mLocation = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (mLocation != null) {
                                latitude = mLocation.getLatitude();
                                longitude = mLocation.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mLocation;
    }

    private void stopUsingGPS() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    public static String getQueryString(String url, String tag) {
        try {
            Uri uri=Uri.parse(url);
            return uri.getQueryParameter(tag);
        }catch(Exception e){
            if ( BuildConfig.DEBUG ) Log.e(TAG,"getQueryString() " + e.getMessage());
        }
        return "";
    }

}
