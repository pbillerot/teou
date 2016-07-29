package eu.pbillerot.android.teou;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by billerot on 15/07/16.
 */
public class GpxDataSource {
    // Database fields
    private SQLiteDatabase db;
    private GpxSqliteHelper gpxSqliteHelper;

    public String[] all_columns = {
            gpxSqliteHelper.COL_ID,
            gpxSqliteHelper.COL_NAME,
            gpxSqliteHelper.COL_TIME,
            gpxSqliteHelper.COL_TEL,
            gpxSqliteHelper.COL_LAT,
            gpxSqliteHelper.COL_LON,
            gpxSqliteHelper.COL_ELE
    };
    public String[] all_columns_update = {
            gpxSqliteHelper.COL_ID,
            gpxSqliteHelper.COL_NAME,
            gpxSqliteHelper.COL_TIME,
            gpxSqliteHelper.COL_TEL,
            gpxSqliteHelper.COL_LAT,
            gpxSqliteHelper.COL_LON,
            gpxSqliteHelper.COL_ELE
    };


    public GpxDataSource(Context context) {
        gpxSqliteHelper = new GpxSqliteHelper(context);
    }

    public void open() throws SQLException {
        db = gpxSqliteHelper.getWritableDatabase();
    }

    public void close() {
        gpxSqliteHelper.close();
    }

    public long createGpx(GpxPoint gpxPoint) {
        ContentValues values = new ContentValues();
        values.put(GpxSqliteHelper.COL_NAME, gpxPoint.getName());
        values.put(GpxSqliteHelper.COL_TEL, gpxPoint.getTel());
        values.put(GpxSqliteHelper.COL_TIME, gpxPoint.getTime());
        values.put(GpxSqliteHelper.COL_LAT, gpxPoint.getLat());
        values.put(GpxSqliteHelper.COL_LON, gpxPoint.getLon());
        values.put(GpxSqliteHelper.COL_ELE, gpxPoint.getEle());
        long insertId = db.insert(GpxSqliteHelper.TABLE_NAME, null,
                values);
        return insertId;
    }

    public void updateGpx(GpxPoint gpxPoint){
        ContentValues values = new ContentValues();
        values.put(GpxSqliteHelper.COL_NAME, gpxPoint.getName());
        values.put(GpxSqliteHelper.COL_TEL, gpxPoint.getTel());
        values.put(GpxSqliteHelper.COL_TIME, gpxPoint.getTime());
        values.put(GpxSqliteHelper.COL_LAT, gpxPoint.getLat());
        values.put(GpxSqliteHelper.COL_LON, gpxPoint.getLon());
        values.put(GpxSqliteHelper.COL_ELE, gpxPoint.getEle());

        db.update(GpxSqliteHelper.TABLE_NAME, values,
                GpxSqliteHelper.COL_ID + " = ? ",
                new String[] { String.valueOf(gpxPoint.getId()) });
    }

    public void deleteGpx(GpxPoint gpxPoint) {
        long id = gpxPoint.getId();
        db.delete(GpxSqliteHelper.TABLE_NAME,
                GpxSqliteHelper.COL_ID + " = " + id, null);
    }

    public List<GpxPoint> getAllGpxPoint() {
        List<GpxPoint> gpxPoints = new ArrayList<GpxPoint>();

        Cursor cursor = db.query(GpxSqliteHelper.TABLE_NAME,
                all_columns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            GpxPoint gpxPoint = cursorToGpx(cursor);
            gpxPoints.add(gpxPoint);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return gpxPoints;
    }

    private GpxPoint cursorToGpx(Cursor cursor) {
        GpxPoint gpxPoint = new GpxPoint();
        gpxPoint.setId(cursor.getLong(0));
        gpxPoint.setName(cursor.getString(1));
        gpxPoint.setTime(cursor.getString(2));
        gpxPoint.setTel(cursor.getString(3));
        gpxPoint.setLat(cursor.getDouble(4));
        gpxPoint.setLon(cursor.getDouble(5));
        gpxPoint.setEle(cursor.getDouble(6));
        return gpxPoint;
    }
}
