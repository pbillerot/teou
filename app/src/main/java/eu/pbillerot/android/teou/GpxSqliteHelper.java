package eu.pbillerot.android.teou;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by billerot on 15/07/16.
 */
public class GpxSqliteHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "teou.sqlite";
    static final String TABLE_NAME = "gpx";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " name TEXT NULL, " +
                    " tel TEXT NULL, " +
                    " lat FLOAT NOT NULL, " +
                    " lon FLOAT NOT NULL, " +
                    " ele FLOAT NULL, " +
                    " time TEXT NOT NULL);";

    static final String COL_ID = "_id";
    static final String COL_NAME = "name";
    static final String COL_TIME = "time";
    static final String COL_TEL = "tel";
    static final String COL_LAT = "lat";
    static final String COL_LON = "lon";
    static final String COL_ELE = "ele";

    GpxSqliteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_DB_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +  TABLE_NAME);
        onCreate(db);
    }
}