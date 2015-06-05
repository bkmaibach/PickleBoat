package ca.maibach.pickleboat;

import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;

import java.util.HashMap;

import ca.maibach.pickleboat.data.PickleContract.BoatEntry;
import ca.maibach.pickleboat.data.PickleContract.StopEntry;
import ca.maibach.pickleboat.googlehelpers.GoogleDirection;


public class StopFragment extends Fragment implements
        Preference.OnPreferenceChangeListener,
        AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static String TAG = "PickleBoat: " + "CaptainFragment";

    public static final int STOP_LOADER_ID = 3;

    public static final String POSITION_PARAM_KEY = "zone_key";
    public static final String SPINNER_SELECTION_KEY = "selection_key";

    private Spinner mSpinner;
    private String selectionKey;
    private StopAdapter mStopAdapter;
    private LatLng mLatLng;

    private HashMap<String, Integer> selectionMap;

    private StopCallbacks mStopCallbacks;

    public void setPosition(LatLng mLatLng) {
        this.mLatLng = mLatLng;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        mStopAdapter = new StopAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_dock, container, false);

        mSpinner = (Spinner) rootView.findViewById(R.id.driver_spinner);
        mSpinner.setAdapter(mStopAdapter);
        mSpinner.setOnItemSelectedListener(this);

        return rootView;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;

        //todo: restart loader if the preference changes
        //if(preference.hasKey(R.id.??))
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        Log.d(TAG, "onLoadFinished: cursor size: " + Integer.toString(c.getCount()));
        Log.d(TAG, "onLoadFinished: mLatLng: " + mLatLng.toString());

        LatLng pos;
        float[] results = new float[]{0};
        float lowDistance = 0;
        String bestSelectionKey = selectionKey;
        selectionMap = new HashMap<String, Integer>();


        while(c.moveToNext()) {

            String sKey =  c.getString(StopEntry.COL_NUM_ID);
            selectionMap.put(sKey, c.getPosition());

            pos = Utility.stringParamToLatLng(c.getString(StopEntry.COL_NUM_LATLNG));
            Log.d(TAG, "onLoadFinished: pos: " + pos.toString());

            //onLoadDriver(pos, false,)
            Location.distanceBetween(pos.latitude, pos.longitude,
                    mLatLng.latitude, mLatLng.longitude, results);

            if (results[0] < lowDistance || c.isFirst()) {
                lowDistance = results[0];
                bestSelectionKey = sKey;
                Log.d(TAG, "onLoadFinished: lowDistance: " + Float.toString(lowDistance));
            }

        }

        mStopAdapter.changeCursor(c);

        if(c.getCount() != 0){
            if(selectionKey == null) {
                selectionKey = bestSelectionKey;
                mSpinner.setSelection(selectionMap.get(selectionKey));
            }else{
                mSpinner.setSelection(selectionMap.get(selectionKey));
            }
        }

        mStopCallbacks.onStopsLoaded(c);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cLoader = null;
        if(id == STOP_LOADER_ID && args != null) {

            mLatLng = Utility.stringParamToLatLng(args.getString(POSITION_PARAM_KEY));

            String sortOrder = BoatEntry._ID + " ASC";

            String zoneSetting = Utility.getZoneSetting(getActivity());

            Log.d(TAG, "onCreateLoader: zoneSetting = " + zoneSetting);

            Uri driverUri = StopEntry.buildBoatByZoneUri(zoneSetting);

            cLoader = new CursorLoader(
                    getActivity(),
                    driverUri,
                    StopEntry.STOP_KEYS,
                    null,
                    null,
                    sortOrder
            );
        }
        return cLoader;
    }




    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ////Log.d(TAG, "onLoaderReset: id = " + Integer.toString(loader.getId()));

        //mStopAdapter.swapCursor(null);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SPINNER_SELECTION_KEY, selectionMap.get(selectionKey));
        outState.putString(POSITION_PARAM_KEY, Utility.latLngToStringParam(mLatLng));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if(savedInstanceState != null){

            getLoaderManager().initLoader(STOP_LOADER_ID, savedInstanceState, this);
            mSpinner.setSelection(savedInstanceState.getInt(SPINNER_SELECTION_KEY));
        }

    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected");

        Cursor c = mStopAdapter.getCursor();
        c.moveToPosition(position);
        LatLng stopLatlng = Utility.stringParamToLatLng(c.getString(StopEntry.COL_NUM_LATLNG));

        //may need to be parent.
        getView().findViewById(R.id.progressbar).setVisibility(View.VISIBLE);


        final GoogleDirection gDirection = new GoogleDirection(getActivity());

        String mode = Utility.getModeSetting();
        gDirection.request(mLatLng, stopLatlng, Utility.getModeSetting());
        gDirection.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            @Override
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                Log.d(TAG, "onResponse");

                String duration = Integer.toString(gDirection.getTotalDurationValue(doc) / 60);

                TextView durationView = (TextView) getView().findViewById(R.id.duration_textview);

                durationView.setText(duration);
                getView().findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
            }
        });
        if(mStopCallbacks != null){
            mStopCallbacks.onStopSelected(c, position);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface StopCallbacks {
        public void onStopsLoaded(Cursor c);

        public void onStopSelected(Cursor c, int position);
    }

    public void setStopCallbacks(StopCallbacks listener){
        mStopCallbacks = listener;

    }

}
