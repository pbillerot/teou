package eu.pbillerot.android.teou;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by billerot on 13/07/16.
 */
public class GpxPoint implements Serializable {
    private static final String TAG = GpxPoint.class.getName();

    public final static int MAP_POINT = 9;
    public final static int MAP_FOOT = 0;
    public final static int MAP_BICYCLE = 1;
    public final static int MAP_CAR = 2;

    //public static final String URL_OSM = "http://osmand.net/go?z=15&";
    public static final String URL_OSM = "http://www.openstreetmap.org/";

    public long id = -1;
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

    public GpxPoint(Context context) {
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = myPrefs.getString("GPXPOINT", null);

        try {
            if ( json != null ) {
                JSONObject jsonObject = new JSONObject(json);

                setId(jsonObject.getLong("id"));
                setName(jsonObject.getString("name"));
                setTelephon(jsonObject.getString("telephon"));
                setUrl(jsonObject.getString("url"));
                setTime(jsonObject.getString("time"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public GpxPoint(int id, String name, String telephon, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.telephon = telephon;

        //return URL_OSM + "lat=" + lat + "&lon=" + lon + "&ele=" + ele; // osmand
        this.url = URL_OSM + "?mlat=" + latitude + "&mlon=" + longitude + "&zoom=16#map=16/" + latitude + "/" + longitude;
        setTime();
    }

    public void setCalculTrajet(Context context) {
        /**
         * url route
         * http://www.openstreetmap.org/directions?engine=<engine>&route=<lat_1>%2C<lon_1>%3B<lat_2>%2C<lon_2>
         * engine
         *     mapzen_foot
         *     graphhopper_foot
         *     mapzen_bicycle
         *     graphhopper_bicycle
         *     mapzen_car
         *     osrm_car
         */
        // récupération du point d'arrivée dans le contexte
        GpxPoint gpxPoint_Arrivee = new GpxPoint(context);

        // choix engine
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String engine;
        switch (myPrefs.getInt("route", GpxPoint.MAP_FOOT)) {
            case GpxPoint.MAP_FOOT:
                engine = "graphhopper_foot";
                break;
            case GpxPoint.MAP_BICYCLE:
                engine = "graphhopper_bicycle";
                break;
            case GpxPoint.MAP_CAR:
                engine = "osrm_car";
                break;
            default:
                engine = "graphhopper_foot";
                break;
        }

        Uri uri_start = Uri.parse(getUrl());
        Uri uri_end = Uri.parse(gpxPoint_Arrivee.getUrl());
        this.url = URL_OSM + "directions?engine=" + engine + "&route=" + uri_start.getQueryParameter("mlat")
                + "%2C" + uri_start.getQueryParameter("mlon")
                + "%3B" + uri_end.getQueryParameter("mlat")
                + "%2C" + uri_end.getQueryParameter("mlon")
                + "&mlat=" + uri_end.getQueryParameter("mlat")
                + "&mlon=" + uri_end.getQueryParameter("mlon")
        ;

        this.setName(gpxPoint_Arrivee.getName());

    }

    public int getTypeMap() {
        int type = GpxPoint.MAP_POINT;

        if ( getUrl().matches("(.*)foot(.*)") )
            type = GpxPoint.MAP_FOOT;
        if ( getUrl().matches("(.*)bicycle(.*)") )
            type = GpxPoint.MAP_BICYCLE;
        if ( getUrl().matches("(.*)car(.*)") )
            type = GpxPoint.MAP_CAR;

        return type;
    }


    public GpxPoint(int id, String name, String telephon, String url) {
        this.id = id;
        this.name = name;
        this.telephon = telephon;

        //return URL_OSM + "lat=" + lat + "&lon=" + lon + "&ele=" + ele; // osmand
        this.url = url;
        setTime();
    }

    public GpxPoint(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);

            setId(jsonObject.getLong("id"));
            setName(jsonObject.getString("name"));
            setTelephon(jsonObject.getString("telephon"));
            setUrl(jsonObject.getString("url"));
            setTime(jsonObject.getString("time"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String toJSON() {
        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("id", getId());
            jsonObject.put("name", getName());
            jsonObject.put("telephon", getTelephon());
            jsonObject.put("url", getUrl());
            jsonObject.put("time", getTime());

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
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

    public void insert_in_db(Context context) {
        // sauvegarde du point
        GpxDataSource gpxDataSource = new GpxDataSource(context);
        gpxDataSource.open();
        long indexId = gpxDataSource.createGpx(this);
        gpxDataSource.close();
        this.setId(indexId);
    }

    public void save_in_db(Context context) {
        // enregistrement de la carte dans la base
        if ( getId() == -1 ) {
            insert_in_db(context);
        } else {
            GpxDataSource gpxDataSource = new GpxDataSource(context);
            gpxDataSource.open();
            gpxDataSource.updateGpx(this);
            gpxDataSource.close();
        }

    }

    public void delete_in_db(Context context) {
        GpxDataSource gpxDataSource = new GpxDataSource(context);
        gpxDataSource.open();
        gpxDataSource.deleteGpx(this);
        gpxDataSource.close();
    }

    public void save_in_context(Context context) {
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putString("GPXPOINT", this.toJSON());
        editor.commit();
    }

    public void delete_in_context(Context context) {
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.remove("GPXPOINT");
        editor.commit();
    }

}

