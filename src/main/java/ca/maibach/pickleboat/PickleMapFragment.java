package ca.maibach.pickleboat;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.w3c.dom.Document;

import ca.maibach.pickleboat.googlehelpers.GoogleDirection;

/**
 * Created by keith on 14/04/15.
 */
public class PickleMapFragment extends Fragment implements
        FusedLocator.LocationSourceCallbacks,
        GoogleMap.OnMapClickListener {
    private static String TAG = "PickleBoat: " + "PickleMapFragment";
    //Member classes
    protected GoogleMap mGoogleMap;
    private Marker mPickupMarker;
    private PickupInfoWindowAdapter mInfoWindowAdapter;
    private SupportMapFragment mMapFragment;
    private FusedLocator mFusedLocator;
    private Polyline mPolyLine = null;

    private LatLng previewedPickup = null;
    private String previewDescription;

    private PickupCallbacks mListener = null;

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        setUpMapIfNeeded();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");


        return inflater.inflate(R.layout.fragment_pickup, container, false);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        FragmentManager fm = getChildFragmentManager();
        mMapFragment = ((SupportMapFragment) fm.findFragmentById(R.id.map_container));
        if (mMapFragment == null) {
            mMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mMapFragment).commit();
        }

        mFusedLocator = new FusedLocator(getActivity());

    }


    @Override
    public void onStart() {
        super.onStart();
        if (!isGooglePlayServicesAvailable()) {
            //todo: generate some error message
            getActivity().finish();
        }
        mFusedLocator.getConnectedLocationSourceAsync(this);
    }

    @Override
    public void onStop() {
        mFusedLocator.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (previewedPickup != null) {
            outState.putString("saved_pickup_preview", Utility.latLngToStringParam(previewedPickup));
            outState.putString("saved_preview_description", previewDescription);
        }

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {

        }
    }

    public void setUpMapIfNeeded() {
        Log.d(TAG, "setUpMapIfNeeded: " + (mGoogleMap == null));

        if (mGoogleMap == null) {
            mGoogleMap = mMapFragment.getMap();

            if (mGoogleMap != null) {
                setUpMap();
            }
        }
    }

    public void setUpMap() {
        Log.d(TAG, "setUpMap");
        mGoogleMap.setPadding(0, 110, 0, 0);
        mGoogleMap.setLocationSource(mFusedLocator.getLocationSourceIfReady());
        mInfoWindowAdapter = new PickupInfoWindowAdapter();
        mGoogleMap.setInfoWindowAdapter(mInfoWindowAdapter);
        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setMyLocationEnabled(true);

    }

    @Override
    public void onLocationSourceReady(LatLng position) {
        Log.d(TAG, "onLocationSourceReady");

        if (previewedPickup == null) {
            setUpMapIfNeeded();

            if (mListener != null) {
                mListener.onInitialized(position, null);
            }

        }
    }

    public void drawPickupRoute(LatLng toPickup) {
        if (previewedPickup != null) {
            GoogleDirection gDirection = new GoogleDirection(getActivity());
            gDirection.request(toPickup, previewedPickup, GoogleDirection.MODE_BICYCLING);
            gDirection.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                @Override
                public void onResponse(String status, Document doc, GoogleDirection gd) {
                    if (mPolyLine != null) {
                        mPolyLine.remove();
                    }
                    mPolyLine = mGoogleMap.addPolyline(gd.getPolyline(doc, 1, Color.DKGRAY));
                }
            });

        }


    }

    @Override
    public void onMapClick(LatLng latLng) {
        //todo: implements nearby search
        String description = Utility.getAddress(latLng, getActivity()).getThoroughfare();

        if (mListener != null) {
            mListener.onPickupDetailsReceived(latLng, description);
        }
    }

    public void previewPickup(LatLng pickup, String description) {
        Log.d(TAG, "previewPickup: mPickup = " + pickup.toString());

        previewedPickup = pickup;
        previewDescription = description;
        CameraUpdate cameraPosition = CameraUpdateFactory.newLatLngZoom(pickup, 12);
        mGoogleMap.animateCamera(cameraPosition);

        if (mPickupMarker == null) {
            mPickupMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.pickup_info_window_title))
                    .position(pickup)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

        mPickupMarker.setPosition(pickup);
        mPickupMarker.setSnippet(description);
        mPickupMarker.showInfoWindow();
    }

    public void markStop(LatLng coords, String name, int stopNumber) {
        Log.d(TAG, "markStop");
        MarkerOptions markerOptions = new MarkerOptions()
                .title(name)
                .snippet(Integer.toString(stopNumber))
                .position(coords)
                .flat(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mGoogleMap.addMarker(markerOptions);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 0).show();
            return false;
        }
    }

    public boolean isConnected() {
        return mFusedLocator.isConnected();
    }

    public void setPickupCallbacks(PickupCallbacks listener) {
        mListener = listener;
    }

    private class PickupInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View view;

        public PickupInfoWindowAdapter() {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.pickup_info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            if (mPickupMarker != null && mPickupMarker.isInfoWindowShown()) {
                mPickupMarker.hideInfoWindow();
                mPickupMarker.showInfoWindow();
            }
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            mPickupMarker = marker;

            final String title = marker.getTitle();
            final TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }

            final String snippet = marker.getSnippet();
            final TextView snippetUi = ((TextView) view
                    .findViewById(R.id.snippet_textview));
            if (snippet != null) {
                snippetUi.setText(snippet);
            } else {
                snippetUi.setText("");
            }

            return view;
        }
    }


}
