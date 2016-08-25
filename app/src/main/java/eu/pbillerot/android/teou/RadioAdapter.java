package eu.pbillerot.android.teou;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by billerot on 15/07/16.
 * Gestionnaire d'affichage des items dans une view
 */
public class RadioAdapter extends ArrayAdapter<RadioItem> {
    private static final String TAG = "RadioAdapter";

    private SparseBooleanArray mSelectedItemsIds;

    //GpxPoints est la liste des models à afficher
    public RadioAdapter(Context context, List<RadioItem> radioItems) {
        super(context, 0, radioItems);
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.radio_item,parent, false);
        }

        final View view = convertView;

        final RadioViewHolder viewHolder;
        if(convertView.getTag() == null){
            viewHolder = new RadioViewHolder();
            viewHolder.radio_name = (TextView) convertView.findViewById(R.id.radio_name);
            viewHolder.radio_url = (TextView) convertView.findViewById(R.id.radio_url);
            viewHolder.radio_checkbox = (CheckBox) (CheckBox) convertView.findViewById(R.id.radio_checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (RadioViewHolder) convertView.getTag();
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        RadioItem radioItem = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.radio_name.setText(radioItem.getRadio_name());
        viewHolder.radio_url.setText(radioItem.getRadio_url());

        viewHolder.radio_checkbox.setChecked(false);
        if ( mSelectedItemsIds.get(position) ) {
            convertView.setActivated(true);
            viewHolder.radio_checkbox.setChecked(true);
        } else {
            convertView.setActivated(false);
            viewHolder.radio_checkbox.setChecked(false);
        }
        viewHolder.radio_checkbox.setFocusable(false);

        viewHolder.radio_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) Log.d(TAG, ".onClick " + viewHolder.radio_checkbox.isChecked());
                if (viewHolder.radio_checkbox.isChecked())
                    ((ListView)parent).setItemChecked(position, true);
                else
                    ((ListView)parent).setItemChecked(position, false);
            }
        });

        return convertView;
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void toggleSelection(int position)
    {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value)
    {
        if(value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();// mSelectedCount;
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    private class RadioViewHolder{
        public TextView radio_name;
        public TextView radio_url;
        public CheckBox radio_checkbox;
    }

}
