package eu.pbillerot.android.teou;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AudioActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AudioAdapter.MyToggleListener {
    private static final String TAG = "AudioActivity";

    ListView mListView;
    AudioAdapter mAdapter;

    AudioItem mAudioItem = null;
    ArrayList<AudioItem> mAudioItems = new ArrayList<AudioItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onCreate");

        setContentView(R.layout.activity_audio);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.audio_activity_name);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        // Activity being restarted from stopped state

        mListView = (ListView) findViewById(R.id.audio_list_view);

        mAdapter = new AudioAdapter(this, mAudioItems);
        mListView.setAdapter(mAdapter);

        // Flux audio décrits dans un fichier XML
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String path = myPrefs.getString("pref_audio_xml_text", "");
        if ( path.isEmpty() )
        {
            path = "https://pbillerot.github.io/memodoc/audio.xml";
            SharedPreferences.Editor editor = myPrefs.edit();
            editor.putString("pref_audio_xml_text", path);
            editor.commit();
        }
        if ( ! path.startsWith("http")) {
            File extStore = Environment.getExternalStorageDirectory();
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path);
            path = file.toString();
        } else {
            if ( ! Ja.isConnected(getApplicationContext()) ) {
                Toast.makeText(getApplicationContext()
                        , getApplicationContext().getString(R.string.message_not_connected)
                        , Toast.LENGTH_SHORT).show();
            }
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "flux audio : " + path);
        new LoadXmlAsyncTask().execute(path);

        // Clic sur un item
        mListView.setOnItemClickListener(this);

        // Clic sur le audio_toggle
        mAdapter.setOnEventMyToggleListener(this);

        if (BuildConfig.DEBUG) Log.d(TAG, ".onStart");
    }

    // setOnItemCheckMyAudioListener
    @Override
    public void onItemMyToggleStateChanged(int position, boolean isChecked) {
        //if (BuildConfig.DEBUG) Log.d(TAG, ".onItemMyAudioStateChanged " + position + " " + isChecked);

        mAudioItem = (AudioItem) mListView.getItemAtPosition(position);
        //if (BuildConfig.DEBUG) Log.d(TAG, mAudioItem.getAudio_name() + " " + isChecked);

        if ( isChecked == true ) {
            mAdapter.setSelectedIndex(position);
        } else {
            mAdapter.setSelectedIndex(-1);
        }
        mAdapter.notifyDataSetChanged();

        if ( isChecked ) {
            Intent intent = new Intent("TEOU_MESSAGE");
            intent.putExtra("message", "PLAY " + mAudioItem.getAudio_url());
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
        // récupération de la audio séléctionnée
        mAudioItem = (AudioItem) parent.getItemAtPosition(position);

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
     * Chargement en asynchrone du fichier XML des audios
     */
    private class LoadXmlAsyncTask extends AsyncTask<String, Void, ArrayList<AudioItem>> {

        /**
         * Récup du paramètre fourni
         * new LoadXmlAsyncTask().execute(mUrlXml);
         * @param arg_url
         * @return ArrayList<AudioItem>
         */
        protected ArrayList<AudioItem> doInBackground(String... arg_url) {
            // Some long-running task like downloading an image.
            // ... code shown above to send request and retrieve string builder
            ArrayList<AudioItem> audioItems = new ArrayList<AudioItem>();
            try {
                if (BuildConfig.DEBUG) Log.d(TAG, "LoadXmlAsyncTask.doInBackground " + arg_url[0]);
                InputStream stream;
                if ( arg_url[0].toString().startsWith("http")) {
                    URL url = new URL(arg_url[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    stream = conn.getInputStream();
                } else {
                    stream = new FileInputStream(new File(arg_url[0]));
                }

                XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
                XmlPullParser parser = xmlFactoryObject.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(stream, null);

                audioItems = AudioItem.parseXML(parser);

                stream.close();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return audioItems;
        }

        // On récupère le résulat de doInBackground
        protected void onPostExecute(ArrayList<AudioItem> audioItems) {
            // This method is executed in the UIThread
            // with access to the result of the long running task

            // Rechargement de l'adapter
            // avec recherche de la station audio en cours
            SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String play_url = myPrefs.getString("play_url", "");
            int i_play = -1;
            mAdapter.clear();
            for (int i = 0; i < audioItems.size(); i++) {
                AudioItem audioItem = (AudioItem) audioItems.get(i);
                mAdapter.add(audioItem);
                if ( play_url.equalsIgnoreCase(audioItem.getAudio_url())) {
                    i_play = i;
                }
            }
            mAdapter.setSelectedIndex(i_play);
            // actualisation de la vue
            mAdapter.notifyDataSetChanged();

        }
    }

}
