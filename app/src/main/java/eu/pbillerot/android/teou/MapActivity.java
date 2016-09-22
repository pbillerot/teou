package eu.pbillerot.android.teou;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";

    // paramètre function displayUrl(String audio_url)
    private final String URL_ACCUEIL = "accueil.html";
    private final String URL_GUIDE = "guide.html";
    private final String URL_PATIENTER = "patienter.html";
    private final String URL_PATIENTER_AFFICHAGE = "patienter_affichage.html";
    private final String URL_PATIENTER_LOCAL = "patienter_local.html";

    private int mRouteNumber = GpxPoint.MAP_POINT;

    private WebView mWebView;

    private GpxPoint mGpxPoint;
    private String mUrl;

    // parametre retour de la sélection d'un lieu dans ListActivity
    private final int RESULT_PICK_GPX = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorOrange));
        //toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new MyBrowser());
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        FloatingActionButton fab_locate = (FloatingActionButton) findViewById(R.id.fab_locate);
        fab_locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportActionBar().setSubtitle("");
                displayUrl(Ja.getAssetsPath(URL_PATIENTER_LOCAL));
                ((FloatingActionButton) findViewById(R.id.fab_locate)).show();
                ((FloatingActionButton) findViewById(R.id.fab_record)).hide();
                ((FloatingActionButton) findViewById(R.id.fab_teou)).hide();

                mGpxPoint = null;
                mUrl = null;
                // Appel du service demande de position
                Intent intent = new Intent("TEOU_MESSAGE");
                intent.putExtra("message", "REQ_POSITION");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        });

        // Par défaut le bouton SUILA ne sera pas affiché
        // Il sera affiché onStart si présence d'une map
        FloatingActionButton fab_record = (FloatingActionButton) findViewById(R.id.fab_record);
        fab_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSaveDB();
            }
        });

        FloatingActionButton fab_teou = (FloatingActionButton) findViewById(R.id.fab_teou);
        fab_teou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSelectContact(ACTION_SMS_TEOU, getString(R.string.message_sms_teou));
            }
        });

        if (BuildConfig.DEBUG) Log.d(TAG, ".onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        // Activity being restarted from stopped state

        // Restauration du lieu
        if ( mGpxPoint == null ) {
            mGpxPoint = new GpxPoint(getApplicationContext());
            if ( mGpxPoint.getUrl() == null )
                mGpxPoint = null;
        }

        if ( mGpxPoint != null ) {
            if ( mGpxPoint.getTypeMap() == GpxPoint.MAP_POINT) {
                getSupportActionBar().setLogo(R.drawable.logo_point);
            }
            if ( mGpxPoint.getTypeMap() == GpxPoint.MAP_FOOT) {
                getSupportActionBar().setLogo(R.drawable.logo_foot);
            }
            if ( mGpxPoint.getTypeMap() == GpxPoint.MAP_BICYCLE) {
                getSupportActionBar().setLogo(R.drawable.logo_bicycle);
            }
            if ( mGpxPoint.getTypeMap() == GpxPoint.MAP_CAR) {
                getSupportActionBar().setLogo(R.drawable.logo_car);
            }
        }

        // Démarrage du service
        if (!isMyServiceRunning(ServiceTeou.class)) {
            Intent i = new Intent(this.getApplicationContext(), ServiceTeou.class);
            this.getApplicationContext().startService(i);
        }

        if (mGpxPoint == null) {
            getSupportActionBar().setSubtitle("");
            if (mUrl == null) {
                this.displayUrl(Ja.getAssetsPath(URL_ACCUEIL));
            } else {
                this.displayUrl(mUrl);
            }
        } else {
            getSupportActionBar().setSubtitle(mGpxPoint.getName());
            this.displayUrl(mGpxPoint.getUrl());
        }

        // affichage des boutons
        if (mUrl != null && !mUrl.matches("(.*)asset(.*)")) {
            ((FloatingActionButton) findViewById(R.id.fab_locate)).show();
            ((FloatingActionButton) findViewById(R.id.fab_record)).show();
            ((FloatingActionButton) findViewById(R.id.fab_teou)).show();
        } else {
            ((FloatingActionButton) findViewById(R.id.fab_locate)).show();
            ((FloatingActionButton) findViewById(R.id.fab_record)).hide();
            ((FloatingActionButton) findViewById(R.id.fab_teou)).show();
        }

        if (BuildConfig.DEBUG) Log.d(TAG, ".onStart");
    }

    public void displayUrl(String url) {
        mUrl = url;
        if ( url.startsWith("http")) {
            if ( ! Ja.isConnected(getApplicationContext()) ) {
                Toast.makeText(getApplicationContext()
                        , getApplicationContext().getString(R.string.message_not_connected)
                        , Toast.LENGTH_SHORT).show();
            } else {
                mWebView.loadUrl(url);
            }
        } else {
            mWebView.loadUrl(url);
        }
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, ".onPrepareOptionsMenu");
        if (mUrl != null) {
            if (mUrl.matches("(.*)asset(.*)")) {
                menu.findItem(R.id.menu_envoyer).setVisible(false);
                menu.findItem(R.id.menu_route).setVisible(false);
            } else {
                menu.findItem(R.id.menu_envoyer).setVisible(true);
                if ( mGpxPoint != null && mGpxPoint.getTypeMap() == GpxPoint.MAP_POINT ) {
                    menu.findItem(R.id.menu_route).setVisible(true);
                } else {
                    menu.findItem(R.id.menu_route).setVisible(false);
                }
            }
        } else {
            menu.findItem(R.id.menu_envoyer).setVisible(false);
            menu.findItem(R.id.menu_route).setVisible(false);
        }
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if ( myPrefs.getBoolean("pref_audio_check", false) != true ) {
            menu.findItem(R.id.action_audio).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.btn_list:
                // Appel de l'activité Liste des cartes
                mGpxPoint = null;
                mUrl = null;
                Intent i = new Intent();
                i.setClass(this, ListActivity.class);
                startActivityForResult(i, RESULT_PICK_GPX);
                return true;

            case R.id.btn_contact:
                // Appel de l'activité Gestion des contacts de l'historique
                Intent ic = new Intent();
                ic.setClass(getBaseContext(), ContactActivity.class);
                startActivity(ic);
                return true;

            case R.id.menu_envoyer:
                dialogSelectContact(ACTION_SMS_SUILA, getString(R.string.message_sms_suila));
                return true;

            case R.id.menu_item_delete:
                dialogMapDelete();
                return true;

            case R.id.menu_route:
                mGpxPoint = null;
                mUrl = null;

                dialogSelectRoute();

                return true;

            case R.id.action_settings:
                Intent is = new Intent();
                is.setClass(getBaseContext(), MyPreferencesActivity.class);
                startActivity(is);
                return true;

            case R.id.action_help:
                Intent ih = new Intent();
                ih.setClass(getBaseContext(), HelpActivity.class);
                startActivity(ih);
                return true;

            case R.id.action_audio:
                Intent ir = new Intent();
                ir.setClass(getBaseContext(), AudioActivity.class);
                startActivity(ir);
                return true;

            case R.id.action_quitter:
                if (BuildConfig.DEBUG) Log.d(TAG, ".action_quitter");
                if ( isMyServiceRunning(ServiceTeou.class)) {
                    Intent istop = new Intent(this.getApplicationContext(), ServiceTeou.class);
                    this.getApplicationContext().stopService(istop);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MapActivity.this.finish();
                    }
                }, 2000);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            //Log.d(TAG, service.service.getClassName());
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();  // Always call the superclass method first
        if (BuildConfig.DEBUG) Log.d(TAG, ".onPause");

    }

    @Override
    protected void onResume() {
        super.onResume();  // Always call the superclass method first
        if (BuildConfig.DEBUG) Log.d(TAG, ".onResume");

    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        if (BuildConfig.DEBUG) Log.d(TAG, ".onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        if (BuildConfig.DEBUG) Log.d(TAG, ".onDestroy finishing: " + isFinishing());

    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        if (BuildConfig.DEBUG) Log.d(TAG, ".onRestart");
    }

    protected void dialogSaveDB() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MapActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_map, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.editTextFavori);
        editText.setText(mGpxPoint.getName());

        // setup a dialog window
        alertDialogBuilder
                .setMessage(R.string.action_map_rename)
                .setPositiveButton(R.string.btn_return, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mGpxPoint.setName(editText.getText().toString());
                        // enregistrement du point dans la base
                        mGpxPoint.save_in_db(getApplicationContext());
                        mGpxPoint.save_in_context(getApplicationContext());
                        getSupportActionBar().setSubtitle(mGpxPoint.getName());
                    }
                })
                .setNegativeButton(R.string.btn_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void dialogMapDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);

        builder
                .setMessage(R.string.map_delete_confirm)
                .setPositiveButton(R.string.btn_confirm_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mGpxPoint.delete_in_db(getApplicationContext());
                        mGpxPoint.delete_in_context(getApplicationContext());
                        mGpxPoint = null;
                        mUrl = null;

                        Intent i = new Intent();
                        i.setClass(getBaseContext(), MapActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getBaseContext().startActivity(i);

                    }
                })
                .setNegativeButton(R.string.btn_confirm_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( BuildConfig.DEBUG ) Log.d(TAG, "requestCode:" + requestCode + " resultCode:" + resultCode);

        // check whether the result is ok
        switch ( requestCode ) {
            case RESULT_PICK_GPX:
                switch ( resultCode ) {
                    case Activity.RESULT_OK:
                        // Retour de ListActivity
//                        GpxPoint gpxPoint = (GpxPoint) data.getSerializableExtra("gpxPoint");
//                        if ( gpxPoint != null ) {
//                            mGpxPoint = gpxPoint;
//                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                mUrl = null;
                break;
        }
    }

    /**
     * dialogSelectContact : Sélection d'un n° de téléphone pour envoyer un SMS teu ou suila
     * @param action
     */
    private String mTelephone = "";
    private final String ACTION_SMS_TEOU = "SMS_TEOU";
    private final String ACTION_SMS_SUILA = "SMS_SUILA";
    protected void dialogSelectContact(final String action, final String message) {
        JSONObject jsonHistoriqueTelephone;

        // recup téléphone par défaut
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String lastTelephone = myPrefs.getString("telephone", "");
        mTelephone = lastTelephone;

        /**
         * récupération de l'historique
         */
        String historique = myPrefs.getString("historique", null);
        if ( historique != null ) {
            try {
                jsonHistoriqueTelephone = new JSONObject(historique);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                jsonHistoriqueTelephone = new JSONObject();
            }
        } else {
            jsonHistoriqueTelephone = new JSONObject();
        }

        final String categories[] = new String[jsonHistoriqueTelephone.length()];
        int iPositionSelected = 0;
        try {
                Iterator<String> keys = jsonHistoriqueTelephone.keys();
                int icount = 0;
                while (keys.hasNext()) {
                    String telephoneName = (String)jsonHistoriqueTelephone.get(keys.next());
                    String str[] = telephoneName.split("[\\(\\),\\.\\- ]");
                    String telephone = str[0].replaceAll(" ", "");
                    categories[icount] =  telephoneName;
                    if ( lastTelephone.equalsIgnoreCase(telephone)) {
                        iPositionSelected = icount;
                    }
                    icount++;
                }
                if ( icount == 1 ) {
                    mTelephone = categories[0];
                }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        /**
         * Traitement de la DialogBox
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapActivity.this);
        alertDialogBuilder.setSingleChoiceItems(categories, iPositionSelected, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if ( BuildConfig.DEBUG ) Log.d(TAG, "item " + item);
                String telephoneName = categories[item];
                String str[] = telephoneName.split("[\\(\\),\\.\\- ]");
                mTelephone = str[0].replaceAll(" ", "");
                if( !mTelephone.isEmpty() )
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }});
        alertDialogBuilder
                .setCancelable(false)
                .setTitle(message)
                .setPositiveButton(R.string.btn_return, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // enregistrement du dernier contact utilisé dans les préférences
                        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putString("telephone", mTelephone);
                        editor.commit();

                        if (action.equals(ACTION_SMS_TEOU)) {
                            SmsSender mySms = new SmsSender();
                            mySms.sendSMS(mTelephone, "TEOU", getApplicationContext());

                            getSupportActionBar().setSubtitle("");
                            displayUrl(Ja.getAssetsPath(URL_PATIENTER));

                        } else if (action.equals(ACTION_SMS_SUILA)) {
                            SmsSender mySms = new SmsSender();
                            mySms.sendSMS(mTelephone, "SUILA " + mGpxPoint.getUrl(), getApplicationContext());
                        }

                        if (BuildConfig.DEBUG) Log.d(TAG, "SMS TEOU");
                    }
                })
                .setNegativeButton(R.string.btn_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        alert.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.cancel();
                }
                return true;
            }
        });

        if(mTelephone.isEmpty() )
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    protected void dialogSelectRoute() {

        // recup route par défaut
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mRouteNumber = myPrefs.getInt("route", GpxPoint.MAP_POINT);

        final String categories[] = getResources().getStringArray(R.array.route);

        /**
         * Traitement de la DialogBox
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapActivity.this);
        alertDialogBuilder.setSingleChoiceItems(categories, mRouteNumber,
                new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                if ( BuildConfig.DEBUG ) Log.d(TAG, "item " + item);
                mRouteNumber = item;
               ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });

        alertDialogBuilder
                .setCancelable(false)
                .setTitle(getString(R.string.action_route))
                .setPositiveButton(R.string.btn_return, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        // enregistrement du dernier contact utilisé dans les préférences
                        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putInt("route", mRouteNumber);
                        editor.commit();

                        displayUrl(Ja.getAssetsPath(URL_PATIENTER_LOCAL));

                        Toast.makeText(getApplicationContext()
                                , getApplicationContext().getString(R.string.route_message_wait)
                                , Toast.LENGTH_LONG).show();

                        // Appel du service demande de position trajet
                        Intent intent = new Intent("TEOU_MESSAGE");
                        intent.putExtra("message", "REQ_POSITION_TRAJET");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        if (BuildConfig.DEBUG) Log.d(TAG, "SMS TEOU");
                    }
                })
                .setNegativeButton(R.string.btn_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        alert.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.cancel();
                }
                return true;
            }
        });

        if(mRouteNumber == GpxPoint.MAP_POINT )
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

}
