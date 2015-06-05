package ca.maibach.pickleboat.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ca.maibach.pickleboat.Utility;
import ca.maibach.pickleboat.data.PickleContract.StopEntry;

import ca.maibach.pickleboat.authentication.FirebaseHelper;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static String TAG = "PickleBoat: " + "CaptainSyncAdapter";

    public static final String AUTHORITY = Utility.CONTENT_AUTHORITY;

    private final String ZONE_KEY = "zones";
    private final String STOPS_KEY = "stops";

    int MOCK_TIMESTAMP = 1428796738;



    private AccountManager mAccountManager;
    private Context mContext = null;

    private static final String zone_path = "zones";
    private static final String stops_path = "stops";


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        //Log.d(TAG, "syncAdapter binder: " + getSyncAdapterBinder().toString());
        //mContentResolver = context.getContentResolver();
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {




        try {


            String authToken = mAccountManager.blockingGetAuthToken(account, FirebaseHelper.GUEST_AUTHTOKEN_TYPE, true);


            String zoneSetting = Utility.getZoneSetting(mContext);



            long zoneId;

            // First, check if the location with this city name exists in the db
            try{
            provider.delete(
                    StopEntry.CONTENT_URI,
                    null,
                    null);


            } catch(Exception e) {
                Log.e(TAG, "error in deleting database for refresh");
            }

            try{
                ContentValues stopValues;
                JSONObject stop;

                String name;
                int stopNumber;
                String latLng;


                for(int i = 0; i < stops.length(); i++){
                    stopValues = new ContentValues();
                    stop = stops.getJSONObject(i);

                    name = stop.getString(StopEntry.KEY_NAME);
                    stopNumber = stop.getInt(StopEntry.KEY_STOP_NUMBER);
                    latLng = stop.getString(StopEntry.KEY_LATLNG);

                    stopValues.put(StopEntry.KEY_ZONE_SETTING, zoneSetting);
                    stopValues.put(StopEntry.KEY_NAME, name);
                    stopValues.put(StopEntry.KEY_STOP_NUMBER, stopNumber);
                    stopValues.put(StopEntry.KEY_LATLNG, latLng);

                    provider.insert(StopEntry.CONTENT_URI, stopValues);
                }



            }catch(JSONException e){
                Log.e(TAG, "JSONException");
            }

        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            syncResult.stats.numIoExceptions++;
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            syncResult.stats.numAuthExceptions++;
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getStopJson(String authToken) {
        //todo: utilize authtoken in retriving drivers
        String jsonStr = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        URL url = null;


        //TODO: implement http server
            final String zone_setting = Utility.getZoneSetting(mContext);

            Uri builtUri = Uri.parse(Utility.FIREBASE_BASE_URL).buildUpon()
                    .appendPath(zone_path).appendPath(zone_setting)
                    .appendPath(stops_path)
                    .build();


        Firebase ref = new Firebase(builtUri.toString());
        // Attach an listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
        try {

            // Create the request to OpendriverMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.

                Log.e(TAG, "inputStream == null, cannot read data!");
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                Log.e(TAG, "Stream was empty. No point in parsing!");
            }
            jsonStr = buffer.toString();
            ////Log.d(TAG, jsonStr);

        } catch (IOException e) {
            Log.e(TAG, "Couldn't successfully get the driver data");
            // If the code didn't successfully get the driver data, there's no point in attemtping
            // to parse it.
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    ////Log.d(TAG, "Error closing stream", e);
                }
            }
        }
        Log.v(TAG, "CaptainSyncAdapter > driver jsonStr = " + jsonStr);
        return jsonStr;
    }

    public static boolean setAutoSyncOn(Account account){
        Log.d(TAG, "setAutoSyncOn");
        if (!ContentResolver.getSyncAutomatically(account, AUTHORITY)){
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
            return true;
        }else{
            Log.d(TAG, "...but was already on");
            return false;
        }
    }
    public static boolean setAutoSyncOff(Account account){
        Log.d(TAG, "setAutoSyncOff");
        if (ContentResolver.getSyncAutomatically(account, AUTHORITY)){
            ContentResolver.setSyncAutomatically(account, AUTHORITY, false);
            return true;
        }else{
            Log.d(TAG, "...but was already off");
            return false;
        }
    }

    public static boolean enableSync(Account account){
        Log.d(TAG, "enableSync");
        if (ContentResolver.getIsSyncable(account, AUTHORITY) != 1){
            ContentResolver.setIsSyncable(account, AUTHORITY, 1);
            return true;
        }else{
            Log.d(TAG, "...but was already enabled");
            return false;
        }
    }

    public static boolean disableSync(Account account){
        Log.d(TAG, "disableSync");
        if (ContentResolver.getIsSyncable(account, AUTHORITY) != 0){
            ContentResolver.setIsSyncable(account, AUTHORITY, 0);
            return true;
        }else{
            Log.d(TAG, "...but was already disabled");
            return false;
        }
    }

}
