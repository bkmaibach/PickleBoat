package ca.maibach.pickleboat.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ca.maibach.pickleboat.data.PickleContract.BoatEntry;
import ca.maibach.pickleboat.data.PickleContract.StopEntry;

public class PickleDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "driver.db";
    private static final int DATABASE_VERSION = 1;
    private static String TAG = "PickleBoat: " + "CaptainDbHelper";

    public PickleDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Log.d(TAG, "onCreate");
        if (!sqLiteDatabase.isDatabaseIntegrityOk()) {
            //Log.d(TAG, "Database integrity is not ok!");
        }

        final String SQL_CREATE_DRIVER_TABLE = "CREATE TABLE " + PickleContract.BoatEntry.TABLE_NAME + " (" +
                PickleContract.BoatEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PickleContract.BoatEntry.KEY_BOAT_NUMBER + " STRING NOT NULL, " +
                PickleContract.BoatEntry.KEY_ZONE_SETTING + " STRING NOT NULL, " +
                PickleContract.BoatEntry.KEY_LAST_STOP + " INTEGER NOT NULL, " +
                PickleContract.BoatEntry.KEY_NEXT_STOP + " INTEGER NOT NULL, " +
                PickleContract.BoatEntry.KEY_STATUS + " INTEGER NOT NULL, " +


                " FOREIGN KEY (" + PickleContract.BoatEntry.KEY_NEXT_STOP + ") REFERENCES " +
                StopEntry.TABLE_NAME + " (" + StopEntry._ID + "), " +

                " FOREIGN KEY (" + PickleContract.BoatEntry.KEY_LAST_STOP + ") REFERENCES " +
                StopEntry.TABLE_NAME + " (" + StopEntry._ID + "), " +

                "UNIQUE (" + PickleContract.BoatEntry.KEY_ZONE_SETTING + ", " + PickleContract.BoatEntry.KEY_BOAT_NUMBER + ") ON CONFLICT REPLACE" + " );";



        final String SQL_CREATE_ZONE_TABLE = "CREATE TABLE " + StopEntry.TABLE_NAME + " (" +
                StopEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StopEntry.KEY_ZONE_SETTING + " STRING NOT NULL, " +
                StopEntry.KEY_STOP_NUMBER + " INTEGER NOT NULL, " +
                StopEntry.KEY_LATLNG + " STRING NOT NULL" +
                StopEntry.KEY_NEXT_BOAT + "INTEGER" +
                StopEntry.KEY_EST_ARRIVAL_TIME + " INTEGER";




        Log.v("SQL_CREATE_DRIVER_TABLE", SQL_CREATE_DRIVER_TABLE);
        Log.v("SQL_CREATE_ZONE_TABLE", SQL_CREATE_ZONE_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_DRIVER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ZONE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BoatEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StopEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }

}