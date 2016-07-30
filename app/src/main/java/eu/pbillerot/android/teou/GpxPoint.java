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
    public String tel;
    public double lat;
    public double lon;
    public double ele;
    public String time;


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTel() {
        return tel;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getEle() {
        return ele;
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

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setEle(double ele) {
        this.ele = ele;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public GpxPoint() {}

    public GpxPoint(int id, String name, String tel, double lat, double lon, double ele) {
        this.id = id;
        this.name = name;
        this.tel = tel;

        this.lat = lat;
        this.lon = lon;
        this.ele = ele;
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
    public String getUrl() {

        //return URL_OSM + "lat=" + lat + "&lon=" + lon + "&ele=" + ele;
        return URL_OSM + "mlat=" + lat + "&mlon=" + lon + "&zoom=16#map=16/" + lat + "/" + lon;
    }
}
