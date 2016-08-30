package eu.pbillerot.android.teou;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

/**
 * Created by billerot on 15/07/16.
 * Gestionnaire d'affichage des items dans une view
 */
public class AudioAdapter extends ArrayAdapter<AudioItem> {
    private static final String TAG = "AudioAdapter";

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
    public AudioAdapter(Context context, List<AudioItem> audioItems) {
        super(context, 0, audioItems);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.audio_item, parent, false);
        }

        final View view = convertView;

        final RadioViewHolder viewHolder;
        if(convertView.getTag() == null){
            viewHolder = new RadioViewHolder();
            viewHolder.audio_name = (TextView) convertView.findViewById(R.id.audio_name);
            viewHolder.audio_url = (TextView) convertView.findViewById(R.id.audio_url);
            viewHolder.audio_toggle = (ToggleButton) convertView.findViewById(R.id.audio_toggle);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (RadioViewHolder) convertView.getTag();
        }

        if ( mSelectedIndex == position ) {
           viewHolder.audio_toggle.setChecked(true);
        } else {
            viewHolder.audio_toggle.setChecked(false);
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        AudioItem audioItem = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.audio_name.setText(audioItem.getAudio_name());
        viewHolder.audio_url.setText(audioItem.getAudio_url());

        viewHolder.audio_toggle.setOnClickListener(new View.OnClickListener() {
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
        public TextView audio_name;
        public TextView audio_url;
        public ToggleButton audio_toggle;
    }

}
