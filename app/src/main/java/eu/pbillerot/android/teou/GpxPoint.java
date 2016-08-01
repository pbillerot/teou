package eu.pbillerot.android.teou;

import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by billerot on 13/07/16.
 */
public class GpxPoint implements Serializable {
    private static final String TAG = GpxPoint.class.getName();

    //public static final String URL_OSM = "http://osmand.net/go?z=15&";
    public static final String URL_OSM = "http://www.openstreetmap.org/?";

    public long id;
    public String name;
    public String telephon;
    public String url;
    public String time;


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTelephon() {
        return telephon;
    }

    public String getUrl() {
        return url;
    }

    public String getTime() {
        return time;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTelephon(String telephon) {
        this.telephon = telephon;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public GpxPoint() {}

    public GpxPoint(int id, String name, String telephon, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.telephon = telephon;

        //return URL_OSM + "lat=" + lat + "&lon=" + lon + "&ele=" + ele; // osmand
        this.url = URL_OSM + "mlat=" + latitude + "&mlon=" + longitude + "&zoom=16#map=16/" + latitude + "/" + longitude;
        setTime();
    }

    public GpxPoint(int id, String name, String telephon, String url) {
        this.id = id;
        this.name = name;
        this.telephon = telephon;

        //return URL_OSM + "lat=" + lat + "&lon=" + lon + "&ele=" + ele; // osmand
        this.url = url;
        setTime();
    }

    public void setTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss'Z'");
        this.time = formater.format(cal.getTime());
    }

    public String getTimeView() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss'Z'");
        try {
            Date date = sdf.parse(time);
            sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
            return sdf.format(date);
        } catch (Exception e) {
            if ( BuildConfig.DEBUG ) Log.e(TAG, e.toString());
        }
        return time;
    }
}
