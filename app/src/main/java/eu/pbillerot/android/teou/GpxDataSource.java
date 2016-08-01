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
            gpxSqliteHelper.COL_TELEPHON,
            gpxSqliteHelper.COL_URL,
    };
    public String[] all_columns_update = {
            gpxSqliteHelper.COL_ID,
            gpxSqliteHelper.COL_NAME,
            gpxSqliteHelper.COL_TIME,
            gpxSqliteHelper.COL_TELEPHON,
            gpxSqliteHelper.COL_URL,
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
        values.put(GpxSqliteHelper.COL_TELEPHON, gpxPoint.getTelephon());
        values.put(GpxSqliteHelper.COL_TIME, gpxPoint.getTime());
        values.put(GpxSqliteHelper.COL_URL, gpxPoint.getUrl());
        long insertId = db.insert(GpxSqliteHelper.TABLE_NAME, null,
                values);
        return insertId;
    }

    public void updateGpx(GpxPoint gpxPoint){
        ContentValues values = new ContentValues();
        values.put(GpxSqliteHelper.COL_NAME, gpxPoint.getName());
        values.put(GpxSqliteHelper.COL_TELEPHON, gpxPoint.getTelephon());
        values.put(GpxSqliteHelper.COL_TIME, gpxPoint.getTime());
        values.put(GpxSqliteHelper.COL_URL, gpxPoint.getUrl());

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
        gpxPoint.setTelephon(cursor.getString(3));
        gpxPoint.setUrl(cursor.getString(4));
        return gpxPoint;
    }
}
