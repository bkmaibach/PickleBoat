package ca.maibach.pickleboat;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.LatLng;

import ca.maibach.pickleboat.data.PickleContract.StopEntry;
import ca.maibach.pickleboat.googlehelpers.PlaceSearchActivity;

//TODO cleanups: close connections, ondestroy,

public class MainActivity extends PlaceSearchActivity implements
        StopFragment.StopCallbacks,
        PickupCallbacks{
    private static String TAG = "PickleBoat: " + "MainActivity";



    //Member Variables
    private LatLng mPickup;

    private PickleMapFragment mMapFragment;
    private StopFragment mDriverFragment;


    //Methods


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_pickup);

/*        //todo: investigate this
        syncNewZone(Utility.getZoneSetting(this));*/

        FragmentManager fm = getSupportFragmentManager();

        mMapFragment = (PickleMapFragment)fm.findFragmentById(R.id.pickup_fragment);

        mDriverFragment = (StopFragment)fm.findFragmentById(R.id.stop_fragment);

        mDriverFragment.setStopCallbacks(this);
        mMapFragment.setPickupCallbacks(this);


        super.initLayout((RelativeLayout) findViewById(R.id.pickup_layout));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //TODO: settings activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPickupDetailsReceived(LatLng position, String description) {
        Log.d(TAG, "onPickupDetailsReceived");

        mPickup = position;

        Bundle args = new Bundle();
        args.putString(StopFragment.POSITION_PARAM_KEY, Utility.latLngToStringParam(mPickup));

        //todo: show dialog asking to change zone if position is not in zone
        //todo: show place picker if description is null

        String setDescription;
        if (description != null) {
            setDescription = description;
        } else {
            setDescription = Utility.getDescription(position, this);
        }
        getSupportLoaderManager().restartLoader(StopFragment.STOP_LOADER_ID, args, mDriverFragment);

        mMapFragment.previewPickup(position, setDescription);
    }

    @Override
    public void onInitialized(LatLng position, String description) {
        if(mPickup == null){
            mPickup = position;
        }

        Bundle args = new Bundle();
        args.putString(StopFragment.POSITION_PARAM_KEY, Utility.latLngToStringParam(mPickup));

        //todo: show dialog asking to change zone if position is not in zone

        //mDriverFragment.set

        //todo: show place picker if description is null

        String setDescription;
        if (description != null) {
            setDescription = description;
        } else {
            setDescription = Utility.getDescription(position, this);
        }

        getSupportLoaderManager().initLoader(StopFragment.STOP_LOADER_ID, args, mDriverFragment);
        mMapFragment.previewPickup(position, setDescription);
    }



    @Override
    public void onDetailsLoaded(LatLng position, String description) {
        Log.d(TAG, "onDetailsLoaded");

        onPickupDetailsReceived(position, description);
    }


    @Override
    public void onStopsLoaded(Cursor c) {
        Log.d(TAG, "onStopsLoaded: rows = " + c.getCount());
        LatLng stopCoord;
        String stopName;
        int stopNumber;

        if (c.moveToFirst()) {

            do {
                Log.d(TAG, "row " + c.getPosition());
                stopCoord = Utility.stringParamToLatLng(c.getString(StopEntry.COL_NUM_LATLNG));
                stopName = c.getString(StopEntry.COL_NUM_NAME);
                stopNumber = c.getInt(StopEntry.COL_NUM_NAME);

                mMapFragment.markStop(stopCoord, stopName, stopNumber);
            } while (c.moveToNext());
        }
    }

    @Override
    public void onStopSelected(Cursor c, int position) {
        mMapFragment.drawPickupRoute(Utility.stringParamToLatLng
                (c.getString(StopEntry.COL_NUM_LATLNG)));
    }
}
