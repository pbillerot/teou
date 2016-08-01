package eu.pbillerot.android.teou;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by billerot on 15/07/16.
 * Gestionnaire d'affichage des items dans une view
 */
public class GpxPointAdapter extends ArrayAdapter<GpxPoint> {
    private static final String TAG = "GpxPointAdapter";

    //GpxPoints est la liste des models à afficher
    public GpxPointAdapter(Context context, List<GpxPoint> gpxPoints) {
        super(context, 0, gpxPoints);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.gpx_item,parent, false);
        }

        GpxPointViewHolder viewHolder = (GpxPointViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new GpxPointViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.gpx_name);
            viewHolder.tel = (TextView) convertView.findViewById(R.id.gpx_tel);
            viewHolder.time = (TextView) convertView.findViewById(R.id.gpx_time);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.gpx_icon);
            convertView.setTag(viewHolder);
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        GpxPoint gpxPoint = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.name.setText(gpxPoint.getName());
        viewHolder.tel.setText(gpxPoint.getTelephon());
        viewHolder.time.setText(gpxPoint.getTimeView());

        return convertView;
    }

    private class GpxPointViewHolder{
        public TextView name;
        public TextView tel;
        public TextView time;
        public ImageView icon;
    }

}
