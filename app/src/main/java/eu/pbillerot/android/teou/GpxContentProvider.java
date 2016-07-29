package eu.pbillerot.android.teou;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Enregistrement de points GPS
 */
public class GpxContentProvider extends ContentProvider {
    private static final String TAG = "GpxContentProvider";

    static final String PROVIDER_NAME = "eu.pbillerot.android.teou";
    static final String URL = "content://" + PROVIDER_NAME + "/favoris";
    static final Uri CONTENT_URI = Uri.parse(URL);

    private GpxSqliteHelper gpxSqliteHelper;

    public GpxContentProvider() {
    }

    private long getId(Uri uri) {
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment != null) {
            try {
                return Long.parseLong(lastPathSegment);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        long id = getId(uri);
        SQLiteDatabase db = gpxSqliteHelper.getWritableDatabase();
        try {
            if (id < 0)
                return db.delete(
                        GpxSqliteHelper.TABLE_NAME,
                        selection, selectionArgs);
            else
                return db.delete(
                        GpxSqliteHelper.TABLE_NAME,
                        GpxSqliteHelper.COL_ID + "=" + id, selectionArgs);
        } finally {
            db.close();
        }
    }

    @Override
    public String getType(Uri uri) {
        return GpxContentProvider.PROVIDER_NAME;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = gpxSqliteHelper.getWritableDatabase();
        try {
            long id = db.insertOrThrow(GpxSqliteHelper.TABLE_NAME, null, values);

            if (id == -1) {
                throw new RuntimeException(String.format(
                        "%s : Failed to insert [%s] for unknown reasons.", TAG, values, uri));
            } else {
                return ContentUris.withAppendedId(uri, id);
            }
        } finally {
            db.close();
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        gpxSqliteHelper = new GpxSqliteHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        SQLiteDatabase db = gpxSqliteHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        long id = getId(uri);
        SQLiteDatabase db = gpxSqliteHelper.getReadableDatabase();
        if (id < 0) {
            return  db.query(GpxSqliteHelper.TABLE_NAME,
                    projection, selection, selectionArgs, null, null,
                    sortOrder);
        } else {
            return  db.query(GpxSqliteHelper.TABLE_NAME,
                    projection, GpxSqliteHelper.COL_ID + "=" + id, null, null, null,
                    null);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        long id = getId(uri);
        SQLiteDatabase db = gpxSqliteHelper.getWritableDatabase();

        try {
            if (id < 0)
                return db.update(
                        GpxSqliteHelper.TABLE_NAME,
                        values, selection, selectionArgs);
            else
                return db.update(
                        GpxSqliteHelper.TABLE_NAME,
                        values, GpxSqliteHelper.COL_ID + "=" + id, null);
        } finally {
            db.close();
        }
    }

}
