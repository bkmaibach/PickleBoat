package ca.maibach.pickleboat.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import ca.maibach.pickleboat.Utility;


public class PickleContract {

    public static final String AUTHORITY = Utility.CONTENT_AUTHORITY;

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_BOAT = "boats";
    public static final String PATH_STOP = "stops";

    public static final class StopEntry implements BaseColumns {

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + AUTHORITY + "/" + PATH_STOP;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + AUTHORITY + "/" + PATH_STOP;
        public static final String TABLE_NAME = "stop";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STOP).build();
        public static final String KEY_ZONE_SETTING = "zone_setting";
        public static final String KEY_NAME = "name";
        public static final String KEY_STOP_NUMBER = "stop_number";
        public static final String KEY_LATLNG = "latlng";
        public static final String KEY_NEXT_BOAT = "next_boat";
        public static final String KEY_EST_ARRIVAL_TIME = "est_arrival";
        public static final String[] STOP_KEYS = {
                StopEntry.KEY_ZONE_SETTING,
                StopEntry.KEY_NAME,
                StopEntry.KEY_STOP_NUMBER,
                StopEntry.KEY_LATLNG,
                StopEntry.KEY_NEXT_BOAT,
                StopEntry.KEY_EST_ARRIVAL_TIME,

        };
        public static final int COL_NUM_ID = 0;
        public static final int COL_NUM_ZONE_SETTING = 1;
        public static final int COL_NUM_NAME = 2;
        public static final int COL_NUM_STOP_NUMBER = 3;
        public static final int COL_NUM_LATLNG = 4;
        public static final int COL_NUM_NEXT_ARRIVAL_BOAT = 5;
        public static final int COL_NUM_NEXT_ARRIVAL_TIME = 6;

        public static Uri buildZoneUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildBoatByZoneUri(String zoneSetting) {

            return CONTENT_URI.buildUpon().appendPath(zoneSetting).build();
        }


    }

    public static final class BoatEntry implements BaseColumns {


        public static final int STATUS_IN_TRANSIT = 3;
        public static final int STATUS_PATROLLING = 2;
        public static final int STATUS_STOPPED = 1;
        public static final int STATUS_UNAVAILABLE = 0;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOAT).build();

        public static final String TABLE_NAME = "boat";


        public static final String KEY_BOAT_NUMBER = "boat_number";
        public static final String KEY_ZONE_SETTING = "zone_setting";
        public static final String KEY_LAST_STOP = "last_stop";
        public static final String KEY_NEXT_STOP = "next_stop";
        public static final String KEY_STATUS = "status";


        public static final String[] BOAT_KEYS = {
                BoatEntry.TABLE_NAME + "." + BoatEntry._ID,
                BoatEntry.KEY_BOAT_NUMBER,
                BoatEntry.KEY_ZONE_SETTING,
                BoatEntry.KEY_LAST_STOP,
                BoatEntry.KEY_NEXT_STOP,
                BoatEntry.KEY_STATUS

        };

        public static final int COL_NUM_ID = 0;
        public static final int COL_NUM_BOAT_NUMBER = 1;
        public static final int COL_NUM_ZONE_SETTING = 2;
        public static final int COL_NUM_LAST_STOP = 3;
        public static final int COL_NUM_NEXT_STOP = 4;
        public static final int COL_NUM_STATUS = 5;

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + AUTHORITY + "/" + PATH_BOAT;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + AUTHORITY + "/" + PATH_BOAT;


        public static Uri buildBoatByZoneUri(String zoneSetting) {

            return CONTENT_URI.buildUpon().appendPath(zoneSetting).build();
        }

        public static Uri buildBoatByZoneStopUri(String zoneSetting, String nextStop) {

            return CONTENT_URI.buildUpon().appendPath(zoneSetting)
                    .appendQueryParameter(BoatEntry.KEY_NEXT_STOP, nextStop).build();
        }


    }


}