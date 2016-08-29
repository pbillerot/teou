package eu.pbillerot.android.teou;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class RadioActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, RadioAdapter.MyToggleListener {
    private static final String TAG = "RadioActivity";

    private static final String mUrlXml = "https://pbillerot.github.io/memodoc/radios.xml";

    ListView mListView;
    RadioAdapter mAdapter;

    RadioItem mRadioItem = null;
    ArrayList<RadioItem> mRadioItems = new ArrayList<RadioItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onCreate");

        setContentView(R.layout.activity_radio);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.radio_activity_name);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        // Activity being restarted from stopped state

        mListView = (ListView) findViewById(R.id.radio_list_view);

        mAdapter = new RadioAdapter(this, mRadioItems);
        mListView.setAdapter(mAdapter);

        new LoadXmlAsyncTask().execute(mUrlXml);

        // Clic sur un item
        mListView.setOnItemClickListener(this);

        // Clic sur le radio_toggle
        mAdapter.setOnEventMyToggleListener(this);

        if (BuildConfig.DEBUG) Log.d(TAG, ".onStart");
    }

    // setOnItemCheckMyRadioListener
    @Override
    public void onItemMyToggleStateChanged(int position, boolean isChecked) {
        //if (BuildConfig.DEBUG) Log.d(TAG, ".onItemMyRadioStateChanged " + position + " " + isChecked);

        mRadioItem = (RadioItem) mListView.getItemAtPosition(position);
        //if (BuildConfig.DEBUG) Log.d(TAG, mRadioItem.getRadio_name() + " " + isChecked);

        if ( isChecked == true ) {
            mAdapter.setSelectedIndex(position);
        } else {
            mAdapter.setSelectedIndex(-1);
        }
        mAdapter.notifyDataSetChanged();

        if ( isChecked ) {
            Intent intent = new Intent("TEOU_MESSAGE");
            intent.putExtra("message", "PLAY " + mRadioItem.getRadio_url());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } else {
            Intent it = new Intent("TEOU_MESSAGE");
            it.putExtra("message", "STOP");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(it);
        }

    }


    // setOnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (BuildConfig.DEBUG) Log.d(TAG, ".onItemClick");
        // récupération de la radio séléctionnée
        mRadioItem = (RadioItem) parent.getItemAtPosition(position);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, ".onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        // When the user hits back before the Activity has completed loading
        // Set the resultCode to Activity.RESULT_CANCELED
        // to indicate a failure
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        if (BuildConfig.DEBUG) Log.d(TAG, ".onDestroy finishing: " + isFinishing());
    }

    /**
     * Chargement en asynchrone du fichier XML des radios
     */
    private class LoadXmlAsyncTask extends AsyncTask<String, Void, ArrayList<RadioItem>> {

        /**
         * Récup du paramètre fourni
         * new LoadXmlAsyncTask().execute(mUrlXml);
         * @param arg_url
         * @return ArrayList<RadioItem>
         */
        protected ArrayList<RadioItem> doInBackground(String... arg_url) {
            // Some long-running task like downloading an image.
            // ... code shown above to send request and retrieve string builder
            ArrayList<RadioItem> radioItems = new ArrayList<RadioItem>();
            try {
                if (BuildConfig.DEBUG) Log.d(TAG, "LoadXmlAsyncTask.doInBackground " + arg_url[0]);
                URL url = new URL(arg_url[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                InputStream stream = conn.getInputStream();
                XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
                XmlPullParser parser = xmlFactoryObject.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(stream, null);

                radioItems = RadioItem.parseXML(parser);

                stream.close();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return radioItems;
        }

        // On récupère le résulat de doInBackground
        protected void onPostExecute(ArrayList<RadioItem> radioItems) {
            // This method is executed in the UIThread
            // with access to the result of the long running task

            // Rechargement de l'adapter
            // avec recherche de la station radio en cours
            SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String play_url = myPrefs.getString("play_url", "");
            int i_play = -1;
            mAdapter.clear();
            for (int i=0; i < radioItems.size(); i++) {
                RadioItem radioItem = (RadioItem)radioItems.get(i);
                mAdapter.add(radioItem);
                if ( play_url.equalsIgnoreCase(radioItem.getRadio_url())) {
                    i_play = i;
                }
            }
            mAdapter.setSelectedIndex(i_play);
            // actualisation de la vue
            mAdapter.notifyDataSetChanged();

        }
    }

}
