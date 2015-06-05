package ca.maibach.pickleboat.googlehelpers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import ca.maibach.pickleboat.Utility;

//TODO: CONSIDER REPLACING WITH:
//https://github.com/akexorcist/Android-GoogleDirectionAndPlaceLibrary/blob/master/library/src/main/java/app/akexorcist/gdaplibrary/GooglePlaceSearch.java

public class PlaceProvider extends ContentProvider {
    private static String TAG = "PickleBoat: " + "PlaceProvider";

//TODO:  extends SearchRecentSuggestionsProvider {
    //todo: create city setting
    private static LatLng sPosition;
    private static int RADIUS_PARAM = 50000;
    private static String LANG_PARAM = "en";
    private static String TYPE_PARAM = "name&address";

    private static String COMPONENTS_PARAM;

    private static String PLACES_API_KEY = Utility.PLACES_API_KEY;

    private static String AUTHORITY = Utility.CONTENT_AUTHORITY;

    public static final Uri SEARCH_URI = Uri.parse("content://" + AUTHORITY + "/search");

    public static final Uri DETAILS_URI = Uri.parse("content://" + AUTHORITY + "/details");

    //public static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    private static final int SEARCH = 1;
    private static final int SUGGESTIONS = 2;
    private static final int DETAILS = 3;
    private static final int NEARBY = 4;
    // Defines a set of uris allowed with this content provider

    // Obtain browser key from https://code.google.com/apis/console
    String mKey = "key=" + PLACES_API_KEY;

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);

    }

    // Defines a set of uris allowed with this content provider
    private static final UriMatcher mUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // URI for "Go" button
        uriMatcher.addURI(AUTHORITY, "search", SEARCH );

        // URI for suggestions in Search Dialog
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,SUGGESTIONS);

        // URI for Details
        uriMatcher.addURI(AUTHORITY, "details", DETAILS);

        // URI for Nearby search
        uriMatcher.addURI(AUTHORITY, "nearby", NEARBY);

        return uriMatcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor c = null;

        AutocompleteJSONParser parser;
        DetailsJSONParser detailsParser;
        NearbyJSONParser nearbyParser;

        String jsonString = "";
        String jsonPlaceDetails = "";
        String jsonPlaceNearby = "";

        List<HashMap<String, String>> list = null;
        List<HashMap<String, String>> detailsList = null;

        MatrixCursor mCursor = null;

        switch(mUriMatcher.match(uri)){
            case SEARCH:
                // Defining a cursor object with columns description, lat and lng
                mCursor = new MatrixCursor(new String[] { "description","lat","lng" });

                // Create a parser object to parse places in JSON format
                parser = new AutocompleteJSONParser();

                // Create a parser object to parse place details in JSON format
                detailsParser = new DetailsJSONParser();

                // Get Places from Google Places API
                jsonString = getTextSearch(selectionArgs);
                try {
                    // Parse the places ( JSON => List )
                    list = parser.parse(new JSONObject(jsonString));

                    // Finding latitude and longitude for each places using Google Places Details API
                    for(int i=0;i<list.size();i++){
                        HashMap<String, String> hMap = (HashMap<String, String>) list.get(i);

                        detailsParser =new DetailsJSONParser();

                        // Get Place details
                        jsonPlaceDetails = getDetails(hMap.get("reference"));

                        // Parse the details ( JSON => List )
                        detailsList = detailsParser.parse(new JSONObject(jsonPlaceDetails));

                        // Creating cursor object with places
                        for(int j=0;j<detailsList.size();j++){
                            HashMap<String, String> hMapDetails = detailsList.get(j);

                            // Adding place details to cursor
                            mCursor.addRow(new String[]{ hMap.get("description") , hMapDetails.get("lat") , hMapDetails.get("lng") });
                        }

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ////Log.d(TAG, "search cursor: " + mCursor.toString());
                c = mCursor;
                break;

            case SUGGESTIONS :

                // Defining a cursor object with columns id, SUGGEST_COLUMN_TEXT_1, SUGGEST_COLUMN_INTENT_EXTRA_DATA
                mCursor = new MatrixCursor(new String[] { "_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA } );

                // Creating a parser object to parse places in JSON format
                parser = new AutocompleteJSONParser();

                // Get Places from Google Places API
                jsonString = getAutocomplete(selectionArgs);

                try {
                    // Parse the places ( JSON => List )
                    list = parser.parse(new JSONObject(jsonString));

                    // Creating cursor object with places
                    for(int i=0;i<list.size();i++){
                        HashMap<String, String> hMap = (HashMap<String, String>) list.get(i);
                        //Log.v(TAG,  hMap.get("description"));
                        // Adding place details to cursor
                        mCursor.addRow(new String[] { Integer.toString(i), hMap.get("description"), hMap.get("place_id") });
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ////Log.d(TAG, "suggestions cursor: " + mCursor.toString());
                c = mCursor;
                break;

            case DETAILS :
                //Log.v(TAG, "case DETAILS");
                // Defining a cursor object with columns description, lat and lng
                mCursor = new MatrixCursor(new String[] { "thoroughfare","lat","lng", "formatted_address" });

                detailsParser = new DetailsJSONParser();
                jsonPlaceDetails = getDetails(selectionArgs[0]);
                try {
                    detailsList = detailsParser.parse(new JSONObject(jsonPlaceDetails));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                for (int j = 0; j < detailsList.size(); j++) {
                    HashMap<String, String> hMapDetails = detailsList.get(j);
                    mCursor.addRow(new String[]{hMapDetails.get("thoroughfare"), hMapDetails.get("lat"), hMapDetails.get("lng"), hMapDetails.get("formatted_address")});
                }



                ////Log.d(TAG, "details cursor: " + mCursor.toString());
                c = mCursor;
                break;

            case NEARBY:
                mCursor = new MatrixCursor(new String[] { "thoroughfare","lat","lng" });
                nearbyParser = new NearbyJSONParser();
                jsonPlaceNearby = getNearby(selectionArgs[0], selectionArgs[1], selectionArgs[2]);

                try {
                    list = nearbyParser.parse(new JSONObject(jsonPlaceNearby));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                for (int j = 0; j < detailsList.size(); j++) {
                    HashMap<String, String> hMapNearby = list.get(j);
                    mCursor.addRow(new String[]{hMapNearby.get("thoroughfare"), hMapNearby.get("lat"), hMapNearby.get("lng")});
                }



                break;

        }
        ////Log.d(TAG, "returning cursor: " + c.toString());
        return c;
    }

    public static void biasLocation(LatLng biasPosition){
        ////Log.d(TAG, "biasLocation: " + biasPosition.toString());
        sPosition = biasPosition;
    }

    public static void setCountryComponent(String countryCode){
        COMPONENTS_PARAM = "country:" + countryCode;
    }


    @Override
    public boolean onCreate() {
        //setupSuggestions(AUTHORITY, MODE);
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            ////Log.d(TAG, e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getAutocomplete(String[] params){
        ////Log.d(TAG, "getAutocomplete");
        // For storing data from web service
        String data = "";
        String url = getAutocompleteUrl(params[0]);
        try{
            // Fetching the data from web service in background
            data = downloadUrl(url);
        }catch(Exception e){
            ////Log.d("Background Task",e.toString());
        }
        return data;
    }
    private String getAutocompleteUrl(String qry){

        try {
            qry = "input=" + URLEncoder.encode(qry, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String location = "location="
                + Double.toString(sPosition.latitude) + ","
                + Double.toString(sPosition.longitude);

        String radius = "radius=" + RADIUS_PARAM;

        // place type to be searched
        String types = "types=geocode";

        String language = "language="+LANG_PARAM;

        String components = "components="+COMPONENTS_PARAM;

        // Building the parameters to the web service
        String parameters = qry+"&"+language+"&"+types+"&"+location+"&"+radius+"&"+components+"&"+mKey;

        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

        ////Log.d(TAG, "autocomplete url: " + url);

        return url;
    }

    private String getTextSearch(String[] params){
        ////Log.d(TAG, "getTextSearch");
        // For storing data from web service
        String data = "";
        String url = getTextSearchUrl(params[0]);
        try{
            // Fetching the data from web service in background
            data = downloadUrl(url);
        }catch(Exception e){
            ////Log.d("Background Task",e.toString());
        }
        //Log.v(TAG, "getTextSearch data: " + data);

        return data;
    }

    private String getTextSearchUrl(String qry){

        try {
            qry = "input=" + URLEncoder.encode(qry, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String location = "location="
                + Double.toString(sPosition.latitude) + ","
                + Double.toString(sPosition.longitude);

        String radius = "radius=" + RADIUS_PARAM;

        // place type to be searched
        String types = "types=geocode";

        String language = "language="+LANG_PARAM;

        String components = "components="+COMPONENTS_PARAM;

        // Building the parameters to the web service
        String parameters = qry+"&"+language+"&"+types+"&"+location+"&"+radius+"&"+components+"&"+mKey;

        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/"+output+"?"+parameters;

        ////Log.d(TAG, "textsearch url: " + url);

        return url;
    }

    private String getDetails(String reference) {
        String data = "";
        String url = getDetailsUrl(reference);
        try {
            data = downloadUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.v(TAG, "getDetails" + " data: " + data);
        return data;
    }
    private String getDetailsUrl(String place_id){

        // reference of place
        //NOTE that placeid is the form of the query parameter, and place_id is the form within a json output
        String placeId = "placeid=" + place_id;

        String language = "language="+LANG_PARAM;

        // Building the parameters to the web service
        String parameters = placeId+"&"+language+"&"+mKey;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters;
        ////Log.d(TAG, "details url: " + url);

        return url;
    }

    private String getNearby(String lat, String lng, String radius){
        ////Log.d(TAG, "getNearby");

        if(Double.parseDouble(radius) >= 50000){
            radius = "50000";
        };

        String data = "";

        String url = getNearbyUrl(lat, lng, radius);
        try{
            // Fetching the data from web service in background
            data = downloadUrl(url);
        }catch(Exception e){
            ////Log.d("Background Task",e.toString());
        }
        //Log.v(TAG, "getNearby data: " + data);

        return data;
    }

    private String getNearbyUrl(String lat, String lng, String radiusParam){

        String location = "location="+lat+","+lng;
        String radius = "radius="+radiusParam;
        String language = "language="+LANG_PARAM;

        // Building the parameters to the web service
        String parameters = location+"&"+radius+"&"+language+"&"+mKey;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters;
        ////Log.d(TAG, "details url: " + url);

        return url;
    }





}