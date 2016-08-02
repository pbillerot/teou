package eu.pbillerot.android.teou;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

public class ListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "ListActivity";

    // bdd
    private GpxDataSource mGpxDataSource;
    // selected
    GpxPoint mGpxPoint = null;

    private String mTelephone = "";

    ListView mListView;
    GpxPointAdapter mAdapter;
    int mAdapterPosition;

    private final int RESULT_RETOUR_MAP_ACTIVITY = 1 ;

    private final int RESULT_PICK_CONTACT = 1;
    EditText mEditTextTelephone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onCreate");

        setContentView(R.layout.activity_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorOrange));
        //toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.action_lieu_list);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        // Activity being restarted from stopped state

        mListView = (ListView) findViewById(R.id.gpx_list_view);

        // ouverture d'une connexion avec la bdd
        mGpxDataSource = new GpxDataSource(this);
        mGpxDataSource.open();
        List<GpxPoint> gpxPoints = mGpxDataSource.getAllGpxPoint();
        mGpxDataSource.close();

        mAdapter = new GpxPointAdapter(this, gpxPoints);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);

        registerForContextMenu(mListView);
        mListView.setOnCreateContextMenuListener(this);

        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onStart");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onCreateContextMenu ");
        MenuInflater inflater = getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        mAdapterPosition = info.position;
        mGpxPoint = (GpxPoint) mAdapter.getItem(info.position);
        menu.setHeaderTitle(mGpxPoint.getName());

        inflater.inflate(R.menu.menu_lieu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onContextItemSelected " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.menu_lieu_rename:
                dialogLieuRename();
                return true;
            case R.id.menu_lieu_delete:
                dialogLieuDelete();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // "menu_list" is the menubar of my actionbar menu
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onResume");
        //mGpxDataSource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onPause");
        //mGpxDataSource.close();
        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // récupération du GpxPoint séléctionné
        mGpxPoint = (GpxPoint) parent.getItemAtPosition(position);

        // Retour à MapActivity
        Intent returnIntent = new Intent();
        returnIntent.putExtra("gpxPoint", mGpxPoint);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    private void dialogLieuRename() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_lieu, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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

                        // actualisation de la vue
                        mAdapter.notifyDataSetChanged();

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setMessage("[" + mGpxPoint.getName() + "] " +  getString(R.string.lieu_delete_confirm))
                .setPositiveButton(R.string.btn_confirm_yes,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        GpxDataSource gpxDataSource = new GpxDataSource(getApplicationContext());
                        gpxDataSource.open();
                        gpxDataSource.deleteGpx(mGpxPoint);
                        gpxDataSource.close();
                        // actualisation de la vue
                        mGpxPoint = null;
                        mAdapter.remove(mAdapter.getItem(mAdapterPosition));
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.btn_confirm_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        // When the user hits back before the Activity has completed loading
        // Set the resultCode to Activity.RESULT_CANCELED
        // to indicate a failure
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}
