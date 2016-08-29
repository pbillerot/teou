package eu.pbillerot.android.teou;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by billerot on 15/07/16.
 * Gestionnaire d'affichage des items dans une view
 */
public class RadioAdapter extends ArrayAdapter<RadioItem> {
    private static final String TAG = "RadioAdapter";

    int mSelectedIndex = -1;

    // Implémentation du listener
    private MyToggleListener mMyToggleListener;

    public void setOnEventMyToggleListener(MyToggleListener listener) {
        mMyToggleListener = listener;
    }
    public interface MyToggleListener
    {
        public void onItemMyToggleStateChanged(int position, boolean isChecked);
        // pass view as argument or whatever you want.
    }
    public void setSelectedIndex(int index) {
        mSelectedIndex = index;
    }
    public int getSelected() {
        return mSelectedIndex;
    }

    //GpxPoints est la liste des models à afficher
    public RadioAdapter(Context context, List<RadioItem> radioItems) {
        super(context, 0, radioItems);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.radio_item, parent, false);
        }

        final View view = convertView;

        final RadioViewHolder viewHolder;
        if(convertView.getTag() == null){
            viewHolder = new RadioViewHolder();
            viewHolder.radio_name = (TextView) convertView.findViewById(R.id.radio_name);
            viewHolder.radio_url = (TextView) convertView.findViewById(R.id.radio_url);
            viewHolder.radio_toggle = (ToggleButton) convertView.findViewById(R.id.radio_toggle);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (RadioViewHolder) convertView.getTag();
        }

        if ( mSelectedIndex == position ) {
           viewHolder.radio_toggle.setChecked(true);
        } else {
            viewHolder.radio_toggle.setChecked(false);
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        RadioItem radioItem = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.radio_name.setText(radioItem.getRadio_name());
        viewHolder.radio_url.setText(radioItem.getRadio_url());

        viewHolder.radio_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (BuildConfig.DEBUG) Log.d(TAG, ".onClick " + position);

                if ( mSelectedIndex == position ) {
                    mMyToggleListener.onItemMyToggleStateChanged(position, false);
                } else {
                    mMyToggleListener.onItemMyToggleStateChanged(position, true);
                }
            }
        });

        return convertView;
    }

    private class RadioViewHolder{
        public TextView radio_name;
        public TextView radio_url;
        public ToggleButton radio_toggle;
    }

}
