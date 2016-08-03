package eu.pbillerot.android.teou;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";
    private final String URL_ACCUEIL = "file:///android_asset/accueil.html";
    private final String URL_GUIDE = "file:///android_asset/guide.html";
    private final String URL_PATIENTER = "file:///android_asset/patienter.html";
    private final String URL_PATIENTER_AFFICHAGE = "file:///android_asset/patienter_affichage.html";
    private final String URL_PATIENTER_LOCAL = "file:///android_asset/patienter_local.html";


    private WebView mWebView;

    private PositionReceiver mPositionReceiver = null;

    private String mTelephone = "";

    private GpxPoint mGpxPoint;
    private String mUrl;

    private final int RESULT_PICK_CONTACT = 1;
    private final int RESULT_PICK_GPX = 2;
    EditText mEditTextTelephone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorOrange));
        //toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        // Récupération d'un gpxPoint éventuellement
        mGpxPoint = (GpxPoint) this.getIntent().getSerializableExtra("gpxPoint");
        mUrl = this.getIntent().getStringExtra("url");

        FloatingActionButton fab_locate = (FloatingActionButton) findViewById(R.id.fab_locate);
        fab_locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportActionBar().setSubtitle("");
                displayUrl(URL_PATIENTER_LOCAL);
                // Appel du service demande de position
                Intent intent = new Intent("TEOU_MESSAGE");
                intent.putExtra("message", "REQ_POSITION");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        });

        // Par défaut le bouton SUILA ne sera pas affiché
        // Il sera affiché onStart si présence d'une map
        FloatingActionButton fab_suila = (FloatingActionButton) findViewById(R.id.fab_suila);
        fab_suila.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSmsSuila();
            }
        });

        FloatingActionButton fab_teou = (FloatingActionButton) findViewById(R.id.fab_teou);
        fab_teou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSmsTeou();
            }
        });

        if (BuildConfig.DEBUG) Log.d(TAG, ".onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        // Activity being restarted from stopped state

        // Démarrage du service
        if (!isMyServiceRunning(ServiceTeou.class)) {
            Intent i = new Intent(this.getApplicationContext(), ServiceTeou.class);
            this.getApplicationContext().startService(i);
        }

        // enregistrement de l'écouteur des positions
        mPositionReceiver = new PositionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("POSITION_RECEIVER");
        registerReceiver(mPositionReceiver, intentFilter);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new MyBrowser());
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        if (mGpxPoint == null) {
            getSupportActionBar().setSubtitle("");
            if (mUrl == null) {
                this.displayUrl(URL_ACCUEIL);
            } else {
                this.displayUrl(mUrl);
            }
        } else {
            getSupportActionBar().setSubtitle(mGpxPoint.getName());
            this.displayUrl(mGpxPoint.getUrl());
        }

        // affichage du bouton SUILA si Map affichée
        FloatingActionButton fab_suila = (FloatingActionButton) findViewById(R.id.fab_suila);
        if (mUrl != null && !mUrl.matches("(.*)asset(.*)")) {
            fab_suila.show();
        } else {
            fab_suila.hide();
        }

        if (BuildConfig.DEBUG) Log.d(TAG, ".onStart");
    }

    public void displayUrl(String url) {
        mUrl = url;
        mWebView.loadUrl(url);
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
                menu.findItem(R.id.menu_lieu_rename).setEnabled(false);
                menu.findItem(R.id.menu_lieu_delete).setEnabled(false);
            } else {
                menu.findItem(R.id.menu_lieu_rename).setEnabled(true);
                menu.findItem(R.id.menu_lieu_delete).setEnabled(true);
            }
        } else {
            menu.findItem(R.id.menu_lieu_rename).setEnabled(false);
            menu.findItem(R.id.menu_lieu_delete).setEnabled(false);
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
                // Appel de l'activité Liste des lieux
                Intent i = new Intent();
                i.setClass(getBaseContext(), ListActivity.class);
                startActivityForResult(i, RESULT_PICK_GPX);
                return true;


            case R.id.menu_lieu_rename:
                dialogLieuRename();
                return true;

            case R.id.menu_lieu_delete:
                dialogLieuDelete();
                return true;

            case R.id.action_help:
                getSupportActionBar().setSubtitle("");
                this.displayUrl(URL_GUIDE);
                FloatingActionButton fab_suila = (FloatingActionButton) findViewById(R.id.fab_suila);
                fab_suila.hide();

                return true;

            case R.id.action_quitter:
                if (isMyServiceRunning(ServiceTeou.class)) {
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
        // maj du point et url
        this.getIntent().putExtra("gpxPoint", mGpxPoint);
        this.getIntent().putExtra("url", mUrl);

        // Arrêt msgReceiver
        unregisterReceiver(mPositionReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        if (BuildConfig.DEBUG) Log.d(TAG, ".onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        if (BuildConfig.DEBUG) Log.d(TAG, ".onRestart");
    }

    private class PositionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mGpxPoint = (GpxPoint) intent.getSerializableExtra("gpxPoint");

            if (BuildConfig.DEBUG) Log.d(TAG, ".PostionReceiver.onReceive " + mGpxPoint.getUrl());
            getSupportActionBar().setSubtitle(mGpxPoint.getName());
            displayUrl(mGpxPoint.getUrl());
        }
    }

    protected void dialogSmsTeou() {
        // recup téléphone par défaut
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mTelephone = myPrefs.getString("telephone", null);

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MapActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_telephone, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapActivity.this);
        alertDialogBuilder.setView(promptView);

        mEditTextTelephone = (EditText) promptView.findViewById(R.id.editTextTelephone);
        mEditTextTelephone.setText(mTelephone);

        // setup a dialog window
        alertDialogBuilder
                .setMessage(R.string.telephone_textView)
                .setPositiveButton(R.string.btn_return, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mTelephone = mEditTextTelephone.getText().toString();
                        // enregistrement du favori dans les préférences
                        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putString("telephone", mTelephone);
                        editor.commit();

                        if (mTelephone != null) {
                            SmsSender mySms = new SmsSender();
                            mySms.sendSMS(mTelephone, "TEOU", getApplicationContext());

                            if (BuildConfig.DEBUG) Log.d(TAG, "SMS TEOU");
                        }
                        getSupportActionBar().setSubtitle("");
                        displayUrl(URL_PATIENTER);
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

    protected void dialogSmsSuila() {
        // recup téléphone par défaut
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mTelephone = myPrefs.getString("telephone", null);

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MapActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_telephone, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapActivity.this);
        alertDialogBuilder.setView(promptView);

        mEditTextTelephone = (EditText) promptView.findViewById(R.id.editTextTelephone);
        mEditTextTelephone.setText(mTelephone);


        // setup a dialog window
        alertDialogBuilder
                .setMessage(R.string.message_lieu_send)
                .setPositiveButton(R.string.btn_return, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mTelephone = mEditTextTelephone.getText().toString();
                        // enregistrement du favori dans les préférences
                        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putString("telephone", mTelephone);
                        editor.commit();

                        if (mTelephone != null) {
                            SmsSender mySms = new SmsSender();
                            mySms.sendSMS(mTelephone, "SUILA " + mGpxPoint.getUrl(), getApplicationContext());

                            if (BuildConfig.DEBUG) Log.d(TAG, "SMS SUILA...");
                        }
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

    protected void dialogLieuRename() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MapActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_lieu, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.editTextFavori);
        editText.setText(mGpxPoint.getName());

        // setup a dialog window
        alertDialogBuilder
                .setMessage(R.string.action_lieu_rename)
                .setPositiveButton(R.string.btn_return, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mGpxPoint.setName(editText.getText().toString());
                        // enregistrement du point dans la base
                        GpxDataSource gpxDataSource = new GpxDataSource(getApplicationContext());
                        gpxDataSource.open();
                        gpxDataSource.updateGpx(mGpxPoint);
                        gpxDataSource.close();
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

    private void dialogLieuDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);

        builder
                .setMessage(R.string.lieu_delete_confirm)
                .setPositiveButton(R.string.btn_confirm_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        GpxDataSource gpxDataSource = new GpxDataSource(getApplicationContext());
                        gpxDataSource.open();
                        gpxDataSource.deleteGpx(mGpxPoint);
                        gpxDataSource.close();
                        getSupportActionBar().setSubtitle("");
                        mGpxPoint = null;
                        mUrl = null;
                        displayUrl(URL_ACCUEIL);
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

        //if ( BuildConfig.DEBUG ) Log.d(TAG, "requestCode:" + requestCode + " resultCode:" + resultCode);

        // check whether the result is ok
        if (resultCode == Activity.RESULT_OK) {
            Cursor cursor = null;
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    // retour de la sélection d'un n° de téléphone dans CONTACTS
                    try {
                        Uri uri = data.getData();
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String telephone = cursor.getString(phoneIndex);
                        mEditTextTelephone.setText(telephone);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case RESULT_PICK_GPX:
                    // Retour de ListActivity
                    mUrl = null;
                    mGpxPoint = (GpxPoint) data.getSerializableExtra("gpxPoint");
                    if (mGpxPoint != null) {
                        getSupportActionBar().setSubtitle(mGpxPoint.getName());
                        displayUrl(mGpxPoint.getUrl());
                    }
                    break;
            }
        } else {
            //if ( BuildConfig.DEBUG ) Log.e(TAG, "Failed to pick contact");
        }
    }

}
