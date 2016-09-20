package eu.pbillerot.android.teou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

public class ListActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener
        ,AbsListView.MultiChoiceModeListener {
    private static final String TAG = "ListActivity";

    // bdd
    private GpxDataSource mGpxDataSource;
    // selected
    GpxPoint mGpxPoint = null;

    private String mTelephone = "";

    ListView mListView;
    GpxPointAdapter mAdapter;

    private final int RESULT_RETOUR_MAP_ACTIVITY = 1;

    private final int RESULT_PICK_CONTACT = 1;
    EditText mEditTextTelephone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, ".onCreate");

        setContentView(R.layout.activity_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorOrange));
        //toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.action_map_list);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        // Activity being restarted from stopped state

        mListView = (ListView) findViewById(R.id.id_list_view);

        // ouverture d'une connexion avec la bdd
        mGpxDataSource = new GpxDataSource(this);
        mGpxDataSource.open();
        List<GpxPoint> gpxPoints = mGpxDataSource.getAllGpxPoint();
        mGpxDataSource.close();

        mAdapter = new GpxPointAdapter(this, gpxPoints);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);

        if (BuildConfig.DEBUG) Log.d(TAG, ".onStart");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // récupération du GpxPoint sélectionné
        mGpxPoint = (GpxPoint) parent.getItemAtPosition(position);
        mGpxPoint.save_in_context(getApplicationContext());

        // Retour à MapActivity
        Intent returnIntent = new Intent();
        returnIntent.putExtra("gpxPoint", mGpxPoint);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) Log.d(TAG, ".onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    private void dialogMapRename() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_map, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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

    private void deleteMap() {
        GpxDataSource gpxDataSource = new GpxDataSource(getApplicationContext());
        gpxDataSource.open();

        SparseBooleanArray selected = mAdapter.getSelectedIds();
        for (int i = 0; i < selected.size(); i++){
            if (selected.valueAt(i)) {
                GpxPoint gpxPoint = mAdapter.getItem(selected.keyAt(i));
                gpxDataSource.deleteGpx(gpxPoint);
            }
        }

        // Rechargement de l'adapter
        mAdapter.clear();
        List<GpxPoint> listGpxPoints = gpxDataSource.getAllGpxPoint();
        for (int i=0; i < listGpxPoints.size(); i++) {
            mAdapter.add(listGpxPoints.get(i));
        }

        gpxDataSource.close();

        // actualisation de la vue
        mAdapter.notifyDataSetChanged();
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
            case R.id.menu_item_rename:
                dialogMapRename();
                mode.finish();
                return true;
            case R.id.menu_item_delete:
                deleteMap();
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
        mode.getMenuInflater().inflate(R.menu.menu_map_item, menu);
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
            mode.getMenu().findItem(R.id.menu_item_rename).setVisible(false);
        } else {
            SparseBooleanArray selected = mAdapter.getSelectedIds();
            for (int i = 0; i < selected.size(); i++){
                if (selected.valueAt(i)) {
                    mGpxPoint = mAdapter.getItem(selected.keyAt(i));
                }
            }
            mode.setTitle(mGpxPoint.getName());
            mode.getMenu().findItem(R.id.menu_item_rename).setVisible(true);
        }
    }

}