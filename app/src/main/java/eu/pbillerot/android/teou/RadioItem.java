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
public class RadioItem implements Serializable {
    private static final String TAG = RadioItem.class.getName();

    public long id;
    public String radio_name;
    public String radio_url;

    private XmlPullParserFactory mXmlFactoryObject;

    public long getId() {
        return id;
    }

    public String getRadio_name() {
        return radio_name;
    }

    public String getRadio_url() {
        return radio_url;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setRadio_name(String radio_name) {
        this.radio_name = radio_name;
    }

    public void setRadio_url(String radio_url) {
        this.radio_url = radio_url;
    }

    public RadioItem() {}

    public RadioItem(int id, String radio_name, String radio_url) {
        this.id = id;
        this.radio_name = radio_name;
        this.radio_url = radio_url;
    }

    public RadioItem(int id, String radio_name) {
        this.id = id;
        this.radio_name = radio_name;
    }

    public static ArrayList<RadioItem> parseXML(XmlPullParser parser) {
        int event;
        String text=null;

        ArrayList<RadioItem> radioItems = null;
        RadioItem radioItem = null;

        try {
            event = parser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name = null;

                switch (event){
                    case XmlPullParser.START_DOCUMENT:
                        radioItems = new ArrayList();
                        break;

                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if ( name.equalsIgnoreCase("radio" ) ) {
                            radioItem = new RadioItem();
                        } else if (radioItem != null) {
                            if ( name.equalsIgnoreCase("name") ){
                                radioItem.radio_name = parser.nextText();
                            } else if (name.equalsIgnoreCase("url") ) {
                                radioItem.radio_url = parser.nextText();
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if ( name.equalsIgnoreCase("radio") && radioItem != null){
                            radioItems.add(radioItem);
                            radioItem = null;
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return radioItems;
    }

    public void fetchXML(String url) {
        // Url : "https://pbillerot.github.io/memodoc/radios.xml";
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

                    ArrayList<RadioItem> radioItems = null;
                    radioItems = parseXML(myparser);
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
