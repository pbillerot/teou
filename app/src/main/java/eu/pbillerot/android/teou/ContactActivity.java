package eu.pbillerot.android.teou;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ContactActivity extends AppCompatActivity {
    private static final String TAG = "ContactActivity";


    ListView mListView;
    ArrayAdapter mArrayAdapter;
    List<String> mContactList = new ArrayList<String>();
    JSONObject mJsonContact = new JSONObject();

    private final int RESULT_PICK_CONTACT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onCreate");

        setContentView(R.layout.activity_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.contact_activity_name);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fabAjout = (FloatingActionButton) findViewById(R.id.fab_contact_ajout);
        fabAjout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[] perms = {Manifest.permission.READ_CONTACTS};
                ActivityCompat.requestPermissions(ContactActivity.this, perms, RESULT_PICK_CONTACT);
                // result in onRequestPermissionsResult

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        // Activity being restarted from stopped state

        // récupération des contacts
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String historique = myPrefs.getString("historique", null);
        mContactList = new ArrayList<String>();

        if ( historique != null ) {
            try {
                mJsonContact = new JSONObject(historique);
                Iterator<String> keys = mJsonContact.keys();
                while (keys.hasNext()) {
                    mContactList.add((String)mJsonContact.get(keys.next()));
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        mListView = (ListView) findViewById(R.id.contact_list_view);
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, mContactList);
        mListView.setAdapter(mArrayAdapter);
        // Listener pour gérer le simple clic
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parent.getItemAtPosition(position);
                boolean isSelection = mListView.getCheckedItemCount() > 0 ? true: false;
                mListView.setItemChecked(position, !isSelection);
            }

        });
        // Listener pour gérer la sélection multiple
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener()
        {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                if ( BuildConfig.DEBUG ) Log.d(TAG, ".onItemCheckedStateChanged");
                int icount = mListView.getCheckedItemCount();
                if ( icount > 1) {
                    mode.setTitle(icount + " " + getString(R.string.selecteds));
                } else {
                    mode.setTitle(icount + " " + getString(R.string.selected));
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if ( BuildConfig.DEBUG ) Log.d(TAG, ".onActionItemClicked");
                switch (item.getItemId()) {
                    case R.id.menu_contact_delete:

                        // Mise à jour de l'objet json mJsonRadio

                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selected = mListView.getCheckedItemPositions();
                        // Captures all selected ids with a loop
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i) == true) {
                                String contact = (String) mListView.getItemAtPosition(selected.keyAt(i));
                                String str[] = contact.split("[ ]");
                                String telephone = str[0].replaceAll(" ", "");

                                mJsonContact.remove(telephone);

                                if ( BuildConfig.DEBUG ) Log.d(TAG, "Suppression de " + contact);
                            }
                        }

                        // Mise à jour des contacts dans les préférences
                        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putString("historique", mJsonContact.toString());
                        editor.commit();

                        // Mise à jour de la liste qui sera réaffichée par notifyDataSetChanged
                        try {
                            mContactList.clear();
                            Iterator<String> keys = mJsonContact.keys();
                            while (keys.hasNext()) {
                                mContactList.add((String)mJsonContact.get(keys.next()));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }

                        mArrayAdapter.notifyDataSetChanged();

                        // Close CAB
                        mode.finish();

                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_contact, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                //mArrayAdapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        if ( BuildConfig.DEBUG ) Log.d(TAG, ".onStart");
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
                        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        String telephone = cursor.getString(phoneIndex)
                                .replaceAll(" ", "")
                                .replaceAll("-", "");
                        String name = cursor.getString(nameIndex);

                        // Mise à jour de l'objet json
                        mJsonContact.put(telephone, telephone + " " + name);

                        // Mise à jour des contacts dans les préférences
                        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putString("historique", mJsonContact.toString());
                        editor.commit();

                        // Mise à jour de la liste qui sera réaffichée par notifyDataSetChanged
                        try {
                            mContactList.clear();
                            Iterator<String> keys = mJsonContact.keys();
                            while (keys.hasNext()) {
                                mContactList.add((String)mJsonContact.get(keys.next()));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }

                        mArrayAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } else {
            //if ( BuildConfig.DEBUG ) Log.e(TAG, "Failed to pick contact");
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
    public void onRequestPermissionsResult(int resultCode,
                                           String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent("TEOU_MESSAGE");

            switch (resultCode) {
                case RESULT_PICK_CONTACT:
                    // appel du carnet d adresse pour récupérer le n° de téléphone du contact
                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);

                    break;
            }
        } else {

            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            Toast.makeText(ContactActivity.this, "Permission denied to " + permissions[0], Toast.LENGTH_LONG).show();
        }
    }

}
