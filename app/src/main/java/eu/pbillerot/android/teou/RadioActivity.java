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

public class RadioActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
        ,AbsListView.MultiChoiceModeListener {
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

        mListView.setOnItemClickListener(this);

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setMultiChoiceModeListener(this);

        if (BuildConfig.DEBUG) Log.d(TAG, ".onStart");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (BuildConfig.DEBUG) Log.d(TAG, ".onActionItemClicked");

        switch (item.getItemId()) {
            case R.id.menu_lieu_rename:
                // Appel du service
                Intent intent = new Intent("TEOU_MESSAGE");
                intent.putExtra("message", "PLAY " + mRadioItem.getRadio_url());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                mode.finish();
                return true;
            case R.id.menu_lieu_delete:
                // Appel du service
                Intent it = new Intent("TEOU_MESSAGE");
                it.putExtra("message", "STOP");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(it);
                mode.finish();
                return true;

            default:
                mode.finish();
                return false;
        }
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, ".onPrepareActionMode");
        return false;
    }
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, ".onCreateActionMode");
        mode.getMenuInflater().inflate(R.menu.menu_lieu, menu);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (BuildConfig.DEBUG) Log.d(TAG, ".onDestroyActionMode");
        mAdapter.removeSelection();
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (BuildConfig.DEBUG) Log.d(TAG, ".onItemCheckedStateChanged");
        mAdapter.toggleSelection(position);
        int icount = mListView.getCheckedItemCount();
        if ( icount > 1) {
            mode.setTitle(icount + " " + getString(R.string.selecteds));
            mode.getMenu().findItem(R.id.menu_lieu_rename).setVisible(false);
        } else {
            SparseBooleanArray selected = mAdapter.getSelectedIds();
            for (int i = 0; i < selected.size(); i++){
                if (selected.valueAt(i)) {
                    mRadioItem = mAdapter.getItem(selected.keyAt(i));
                }
            }
            mode.setTitle(mRadioItem.getRadio_name());
            mode.getMenu().findItem(R.id.menu_lieu_rename).setVisible(true);
        }
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
            mAdapter.clear();
            for (int i=0; i < radioItems.size(); i++) {
                mAdapter.add(radioItems.get(i));
            }
            // actualisation de la vue
            mAdapter.notifyDataSetChanged();

        }
    }

}
