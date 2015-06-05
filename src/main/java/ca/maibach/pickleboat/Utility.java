package ca.maibach.pickleboat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import ca.maibach.pickleboat.googlehelpers.GoogleDirection;

/**
 * Created by keith on 15/03/15.
 */
public class Utility {
    public static final String FIREBASE_BASE_URL = "https://h2otaxi.firebaseio.com/";
    private static final String TAG = "PickleBoat: " + "Utility";
    public static Context sContext;

    public static String CONTENT_AUTHORITY = "ca.maibach.pickleboat.PickleProvider";

    public static String PLACES_API_KEY = "AIzaSyAaq4JhND9dx6BKx67lAmlRxJuA4-MHeN4";
    private static Locale mLocale = Locale.CANADA;

    public static void setAndroidContext(Context context) {
        sContext = context;

        if (CONTENT_AUTHORITY != sContext.getString(R.string.pickleboat_content_authority) ||
                PLACES_API_KEY != sContext.getString(R.string.places_api_key) ||
                FIREBASE_BASE_URL != sContext.getString(R.string.firebase_url)) {
            throw new IllegalStateException("Utility constants improperly initialized");
        }

    }

    public static String getString(int resId) {
        return sContext.getString(resId);
    }

    public static Context getApplicationContext() {
        return sContext;
    }

    public static void quickToast(String msg) {

        Toast toast = Toast.makeText(sContext, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Show errors to users
     */
    public static void showErrorDialog(String message) {
        new AlertDialog.Builder(sContext)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    public static String getDescription(LatLng position, Context context) {
        try {
            Geocoder geocoder = new Geocoder(context, mLocale);
            List<Address> addressList = geocoder.getFromLocation(position.latitude, position.longitude, 1);

            Address address = addressList.get(0);

            return address.getAddressLine(0);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCountryCode(LatLng position, Context context) {
        try {
            Geocoder geocoder = new Geocoder(context, mLocale);
            List<Address> addressList = geocoder.getFromLocation(position.latitude, position.longitude, 1);

            Address address = addressList.get(0);
            return address.getCountryCode();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getZoneId(LatLng position, Context context) {
        try {
            Geocoder geocoder = new Geocoder(context, mLocale);
            List<Address> addressList = geocoder.getFromLocation(position.latitude, position.longitude, 1);

            Address address = addressList.get(0);
            return address.getLocality();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Address getAddress(LatLng position, Context context) {
        try {
            //todo: get locale from preference
            Geocoder geocoder = new Geocoder(context, mLocale);
            List<Address> addressList = geocoder.getFromLocation(position.latitude, position.longitude, 1);

            Address address = addressList.get(0);

            return address;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * A method to download json data from url
     */
    public static String downloadUrl(String urlString) throws IOException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonStr = null;
        URL url = new URL(urlString);


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
                //Log.v("driverService", "Closing connection");
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
        return jsonStr;
    }

    public static String getZoneSetting(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String retString = prefs.getString(context.getString(R.string.zone_setting_key), context.getString(R.string.zone_setting_default));

        return retString;
    }


    public static void setZoneSetting(String zone, Context context) {

    }

    public static String getModeSetting() {
        //TODO
        return GoogleDirection.MODE_WALKING;
    }

    public static void setModeSetting(String mode, Context context) {

    }

    public static LatLng stringParamToLatLng(String stringExtra) {
        //Log.d(TAG, "stringParamToLatLng: stringExtra = " + stringExtra);

        String[] coords = stringExtra.split(",");

        return new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
    }

    public static String latLngToStringParam(LatLng pickup) {
        //Log.d(TAG, "stringParamToLatLng: stringExtra = " + stringExtra);
        String latitude = Double.toString(pickup.latitude);
        String longitude = Double.toString(pickup.longitude);

        return latitude + "," + longitude;
    }


}