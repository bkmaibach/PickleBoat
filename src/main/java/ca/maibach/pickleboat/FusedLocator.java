package ca.maibach.pickleboat;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by keith on 02/04/15.
 */
public class FusedLocator implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    protected static final long INTERVAL = 1000 * 30;
    protected static final long FASTEST_INTERVAL = 1000 * 5;
    protected static final long ONE_MIN = 1000 * 60;
    protected static final long REFRESH_TIME = ONE_MIN * 5;
    protected static final float MINIMUM_ACCURACY = 50.0f;
    protected static String TAG = "PickleBoat: " + "FusedLocator";
    protected LocationSourceCallbacks mListener;
    protected Context mContext;

    protected LocationRequest locationRequest = null;
    protected GoogleApiClient googleApiClient;
    protected Location mLocation;
    protected FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

    public FusedLocator(Context context) {

        mContext = context;
    }


    public Location getLocation() {
        return mLocation;
    }

    public LatLng getLatLng() {
        double latitude = getLocation().getLatitude();
        double longitude = getLocation().getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        return latLng;
    }

    public void getConnectedLocationSourceAsync(LocationSourceCallbacks listener) {
        Log.d(TAG, "getConnectedLocationSourceAsync");

        googleApiClient = new GoogleApiClient.Builder(mContext, this, this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        googleApiClient.connect();
        mListener = listener;
    }


    public Boolean isConnected() {
        if (googleApiClient != null) {
            return googleApiClient.isConnected();
        } else {
            return false;
        }

    }

    public void disconnect() {
        fusedLocationProviderApi.removeLocationUpdates(googleApiClient, this);
        googleApiClient.disconnect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        Location currentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);
        if (currentLocation != null && currentLocation.getTime() > REFRESH_TIME) {
            mLocation = currentLocation;

        } else {
            fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
        mListener.onLocationSourceReady(getLatLng());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection suspended!");

        googleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed!");
    }

    @Override
    public void onLocationChanged(Location location) {

        mLocation = location;
    }

    public LocationSource getLocationSourceIfReady() {
        if (googleApiClient.isConnected()) {
            return (LocationSource) googleApiClient;
        } else {
            return null;
        }
    }

    public interface LocationSourceCallbacks {
        void onLocationSourceReady(LatLng position);
    }


}
