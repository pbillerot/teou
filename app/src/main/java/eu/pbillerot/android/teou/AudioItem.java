package eu.pbillerot.android.teou;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by billerot on 13/07/16.
 */
public class AudioItem implements Serializable {
    private static final String TAG = AudioItem.class.getName();

    public long id;
    public String audio_name;
    public String audio_url;
    private boolean selected;

    private XmlPullParserFactory mXmlFactoryObject;

    public long getId() {
        return id;
    }

    public String getAudio_name() {
        return audio_name;
    }

    public String getAudio_url() {
        return audio_url;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAudio_name(String audio_name) {
        this.audio_name = audio_name;
    }

    public void setAudio_url(String audio_url) {
        this.audio_url = audio_url;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public AudioItem() {}

    public AudioItem(int id, String audio_name, String audio_url) {
        this.id = id;
        this.audio_name = audio_name;
        this.audio_url = audio_url;
    }

    public AudioItem(int id, String audio_name) {
        this.id = id;
        this.audio_name = audio_name;
    }

    public static ArrayList<AudioItem> parseXML(XmlPullParser parser) {
        int event;
        String text=null;

        ArrayList<AudioItem> audioItems = null;
        AudioItem audioItem = null;

        try {
            event = parser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name = null;

                switch (event){
                    case XmlPullParser.START_DOCUMENT:
                        audioItems = new ArrayList();
                        break;

                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if ( name.equalsIgnoreCase("audio" ) ) {
                            audioItem = new AudioItem();
                        } else if (audioItem != null) {
                            if ( name.equalsIgnoreCase("name") ){
                                audioItem.audio_name = parser.nextText();
                            } else if (name.equalsIgnoreCase("url") ) {
                                audioItem.audio_url = parser.nextText();
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if ( name.equalsIgnoreCase("audio") && audioItem != null){
                            audioItems.add(audioItem);
                            audioItem = null;
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return audioItems;
    }

    public void fetchXML(String url) {
        // Url : "https://pbillerot.github.io/memodoc/audio.xml";
        final String sUrl = url;
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(sUrl);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream stream = conn.getInputStream();
                    mXmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = mXmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    ArrayList<AudioItem> audioItems = null;
                    audioItems = parseXML(myparser);
                    stream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
