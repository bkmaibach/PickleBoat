package ca.maibach.pickleboat;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.firebase.client.Firebase;

import ca.maibach.pickleboat.data.PickleContract;
import ca.maibach.pickleboat.data.PickleContract.BoatEntry;
import ca.maibach.pickleboat.data.PickleContract.StopEntry;
import ca.maibach.pickleboat.data.PickleDbHelper;

/**
 * Created by keith on 28/02/15.
 */
public class PickleProvider extends ContentProvider {

    public static final String AUTHORITY = Utility.CONTENT_AUTHORITY;
    private static final String zonePath = "zones";
    private static final String boatPath = PickleContract.PATH_BOAT;
    private static final String stopPath = PickleContract.PATH_STOP;
    private static final int BOAT = 1;
    private static final int BOAT_BY_ZONE = 2;
    private static final int BOAT_BY_ZONE_STOP = 3;
    private static final int STOP = 5;
    private static final int STOP_BY_ZONE = 6;
    private static final int STOP_ID = 7;
    // Defines a set of uris allowed with this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sBoatByNextStopQueryBuilder;
    private static final String sBoatZoneSelection =
            StopEntry.TABLE_NAME + "." + BoatEntry.KEY_ZONE_SETTING + " = ? ";
    private static final String sBoatZoneStopSelection =
            StopEntry.TABLE_NAME + "." + BoatEntry.KEY_ZONE_SETTING + " = ? " + "AND" +
                    StopEntry.TABLE_NAME + "." + StopEntry._ID + " = ? ";
    private static final String sStopZoneSelection =
            StopEntry.TABLE_NAME + "." + StopEntry.KEY_ZONE_SETTING;
    private static String TAG = "PickleBoat: " + "CaptainProvider";

    static {
        sBoatByNextStopQueryBuilder = new SQLiteQueryBuilder();
        sBoatByNextStopQueryBuilder.setTables(
                BoatEntry.TABLE_NAME + " INNER JOIN " +
                        PickleContract.StopEntry.TABLE_NAME +
                        " ON " + PickleContract.BoatEntry.TABLE_NAME +
                        "." + BoatEntry.KEY_NEXT_STOP +
                        " = " + PickleContract.StopEntry.TABLE_NAME +
                        "." + StopEntry._ID);
    }

    private PickleDbHelper mOpenHelper;
    private Firebase mFirebase;

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, boatPath, BOAT);
        uriMatcher.addURI(AUTHORITY, boatPath + "/*", BOAT_BY_ZONE);
        uriMatcher.addURI(AUTHORITY, boatPath + "/*/*", BOAT_BY_ZONE_STOP);

        uriMatcher.addURI(AUTHORITY, stopPath, STOP);
        uriMatcher.addURI(AUTHORITY, stopPath + "/*", STOP_BY_ZONE);
        uriMatcher.addURI(AUTHORITY, stopPath + "/*" + "/#", STOP_ID);

        return uriMatcher;
    }

    public static String getZoneSettingFromUri(Uri uri) {
        return getZoneSettingFromUri(uri);
    }

    public static String getStopParameterFromUri(Uri uri) {
        return uri.getQueryParameter(BoatEntry.KEY_NEXT_STOP);
    }

    @Override
    public String getType(Uri uri) {
        ////Log.v(TAG, "getType");
        ////Log.v(TAG, uri.toString());
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case BOAT:
                return BoatEntry.CONTENT_TYPE;

            case BOAT_BY_ZONE:
                return BoatEntry.CONTENT_TYPE;

            case BOAT_BY_ZONE_STOP:
                return BoatEntry.CONTENT_TYPE;

            case STOP:
                return StopEntry.CONTENT_TYPE;

            case STOP_BY_ZONE:
                return StopEntry.CONTENT_TYPE;

            case STOP_ID:
                return StopEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri for getType: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        ////Log.v(TAG, "onCreate");
        mOpenHelper = new PickleDbHelper(getContext());

        mOpenHelper.onUpgrade(mOpenHelper.getWritableDatabase(), 2, 3);


        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //Log.v(TAG, "query: " + uri.toString());
        Cursor retCursor = null;

        String[] dbArgs;
        String dbSelection;

        String zoneSetting;
        String nextStop;

        switch (sUriMatcher.match(uri)) {


            case BOAT:

                retCursor = mOpenHelper.getReadableDatabase().query(
                        BoatEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;


            case BOAT_BY_ZONE:

                zoneSetting = getZoneSettingFromUri(uri);

                dbSelection = sBoatZoneSelection;
                dbArgs = new String[]{zoneSetting};

                retCursor = mOpenHelper.getReadableDatabase().query(
                        BoatEntry.TABLE_NAME,
                        projection,
                        dbSelection,
                        dbArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case BOAT_BY_ZONE_STOP:

                zoneSetting = getZoneSettingFromUri(uri);
                nextStop = getStopParameterFromUri(uri);

                dbSelection = sBoatZoneStopSelection;
                dbArgs = new String[]{zoneSetting, nextStop};

                retCursor = mOpenHelper.getReadableDatabase().query(
                        BoatEntry.TABLE_NAME,
                        projection,
                        dbSelection,
                        dbArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case STOP_BY_ZONE:
                zoneSetting = getZoneSettingFromUri(uri);

                dbSelection = sStopZoneSelection;
                dbArgs = new String[]{zoneSetting};

                retCursor = mOpenHelper.getReadableDatabase().query(
                        BoatEntry.TABLE_NAME,
                        projection,
                        dbSelection,
                        dbArgs,
                        null,
                        null,
                        sortOrder
                );
                break;


            case STOP_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        StopEntry.TABLE_NAME,
                        projection,
                        StopEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        if (retCursor == null) {
            Log.e(TAG, "retCursor is null!");
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //Log.v(TAG, "insert: " + uri.toString());


        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case BOAT:

                long boatId = db.insertWithOnConflict(BoatEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (boatId > 0)
                    returnUri = BoatEntry.CONTENT_URI;
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            case STOP:

                long stopId = db.insertWithOnConflict(StopEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (stopId > 0)
                    returnUri = StopEntry.buildZoneUri(stopId);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null, false);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //Log.v(TAG, "delete: " + uri.toString());
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case BOAT:
                rowsDeleted = db.delete(PickleContract.BoatEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case STOP:
                rowsDeleted = db.delete(PickleContract.StopEntry.TABLE_NAME, selection, selectionArgs);
                break;


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (null == selection || 0 != rowsDeleted) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //Log.v(TAG, "update: " + uri.toString());

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;


        switch (match) {
            case BOAT:

                rowsUpdated = db.update(PickleContract.BoatEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case STOP:
                rowsUpdated = db.update(PickleContract.StopEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (0 != rowsUpdated) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        //Log.v(TAG, "bulkInsert");
        //Log.v(TAG, uri.toString());
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOAT:
                ////Log.v(TAG, "bulkInsert > case BOAT");

                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(PickleContract.BoatEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (-1 != _id) {
                            returnCount++;
                        }

                    }
                    ////Log.v(TAG, "bulkInsert > returnCount = " + Integer.toString(returnCount));
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null, false);

                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }

    }

    /**
     * Implement this to support canonicalization of URIs that refer to your
     * content provider.  A canonical URI is one that can be transported across
     * devices, backup/restore, and other contexts, and still be able to refer
     * to the same data item.  Typically this is implemented by adding query
     * params to the URI allowing the content provider to verify that an incoming
     * canonical URI references the same data as it was originally intended for and,
     * if it doesn't, to find that data (if it exists) in the current environment.
     * <p/>
     * <p>For example, if the content provider holds people and a normal URI in it
     * is created with a row index into that people database, the cananical representation
     * may have an additional query param at the end which specifies the name of the
     * person it is intended for.  Later calls into the provider with that URI will look
     * up the row of that URI's base index and, if it doesn't match or its entry's
     * name doesn't match the name in the query param, perform a query on its database
     * to find the correct row to operate on.</p>
     * <p/>
     * <p>If you implement support for canonical URIs, <b>all</b> incoming calls with
     * URIs (including this one) must perform this verification and recovery of any
     * canonical URIs they receive.  In addition, you must also implement
     * {@link #uncanonicalize} to strip the canonicalization of any of these URIs.</p>
     * <p/>
     * <p>The default implementation of this method returns null, indicating that
     * canonical URIs are not supported.</p>
     *
     * @param url The Uri to canonicalize.
     * @return Return the canonical representation of <var>url</var>, or null if
     * canonicalization of that Uri is not supported.
     */
    //TODO: canonicalize
    @Override
    public Uri canonicalize(Uri url) {
        return super.canonicalize(url);
    }

}

