package eu.pbillerot.android.teou;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by billerot on 15/07/16.
 * Gestionnaire d'affichage des items dans une view
 */
public class GpxPointAdapter extends ArrayAdapter<GpxPoint> {
    private static final String TAG = "GpxPointAdapter";

    private SparseBooleanArray mSelectedItemsIds;

    //GpxPoints est la liste des models à afficher
    public GpxPointAdapter(Context context, List<GpxPoint> gpxPoints) {
        super(context, 0, gpxPoints);
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.gpx_item,parent, false);
        }

        final View view = convertView;

        final GpxPointViewHolder viewHolder;
        if(convertView.getTag() == null){
            viewHolder = new GpxPointViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.gpx_name);
            viewHolder.tel = (TextView) convertView.findViewById(R.id.gpx_tel);
            viewHolder.time = (TextView) convertView.findViewById(R.id.gpx_time);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.gpx_icon);
            viewHolder.checkbox = (CheckBox) (CheckBox) convertView.findViewById(R.id.id_checkBox);
            //viewHolder.distance = (ImageView) convertView.findViewById(R.id.id_distance);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GpxPointViewHolder) convertView.getTag();
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        GpxPoint gpxPoint = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.name.setText(gpxPoint.getName());
        viewHolder.tel.setText(gpxPoint.getTelephon());
        viewHolder.time.setText(gpxPoint.getTimeView());

        viewHolder.checkbox.setChecked(false);
        if ( mSelectedItemsIds.get(position) ) {
            convertView.setActivated(true);
            viewHolder.checkbox.setChecked(true);
        } else {
            convertView.setActivated(false);
            viewHolder.checkbox.setChecked(false);
        }
        viewHolder.checkbox.setFocusable(false);

        viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) Log.d(TAG, ".onClick " + viewHolder.checkbox.isChecked());
                if (viewHolder.checkbox.isChecked())
                    ((ListView)parent).setItemChecked(position, true);
                else
                    ((ListView)parent).setItemChecked(position, false);
            }
        });

//        viewHolder.distance.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (BuildConfig.DEBUG) Log.d(TAG, ".onClick distance");
//                if ( v.getTag(R.id.id_distance) != null ) {
//                    viewHolder.distance.setImageResource(R.drawable.teou_distance);
//                    v.setTag(R.id.id_distance, false);
//
//                } else {
//                    viewHolder.distance.setImageResource(R.drawable.teou_distance_green);
//                    v.setTag(R.id.id_distance, true);
//                }
//            }
//        });

        //change background color if list item is selected
        //convertView.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4: Color.TRANSPARENT);

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

    private class GpxPointViewHolder{
        public TextView name;
        public TextView tel;
        public TextView time;
        public ImageView icon;
        public ImageView distance;
        public CheckBox checkbox;
    }

}
