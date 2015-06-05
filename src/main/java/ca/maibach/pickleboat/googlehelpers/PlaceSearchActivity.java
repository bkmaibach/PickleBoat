package ca.maibach.pickleboat.googlehelpers;

import android.animation.LayoutTransition;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SearchView;

import com.google.android.gms.maps.model.LatLng;

import ca.maibach.pickleboat.R;

//TODO cleanups: close connections, ondestroy,

public abstract class PlaceSearchActivity extends ActionBarActivity implements

         LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnClickListener,
        SearchView.OnCloseListener {

    private static String TAG = "PickleBoat: " + "MainActivity";


    private SearchView mSearchView;
    static private LayoutParams sOpenParams;
    static private LayoutParams sClosedParams;


    public void setLocationBias(LatLng initialPosition) {
        //Log.d(TAG, "setLocationBias");
        if(initialPosition == null){
            //Log.d(TAG, "initialPosition is null!");
        }

        PlaceProvider.biasLocation(initialPosition);
        //todo: why is this causing network timeout?
        //PlaceProvider.setCountryComponent(Utility.getCountryCode(initialPosition, getBaseContext()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log(TAG, "onCreate");

    }

    protected void initLayout(RelativeLayout layout){

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        layout.setLayoutTransition(layoutTransition);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) findViewById(R.id.location_searchview);
        sClosedParams = (LayoutParams) mSearchView.getLayoutParams();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnSearchClickListener(this);
        mSearchView.setOnCloseListener(this);
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
    public void onClick(View v) {
        //TODO: initialize without copy constructor for better compatibility
        LayoutParams openParams = new LayoutParams(sClosedParams);
        openParams.height = LayoutParams.WRAP_CONTENT;
        openParams.width = LayoutParams.MATCH_PARENT;
        //openParams.removeRule(RelativeLayout.ALIGN_TOP);
        //openParams.removeRule(RelativeLayout.ALIGN_RIGHT);
        openParams.addRule(RelativeLayout.ALIGN_LEFT);
        openParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        openParams.setMargins(0, 0, 0, 0);

        if (mSearchView.getLayoutParams() != openParams) {
            mSearchView.setLayoutParams(openParams);
        }
    }


    @Override
    public boolean onClose() {
        if (mSearchView.getLayoutParams() != sClosedParams) {
            if (sOpenParams == null) {
                sOpenParams = (LayoutParams) mSearchView.getLayoutParams();
            }
            mSearchView.setLayoutParams(sClosedParams);
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (mSearchView.getQuery() != "") {
            mSearchView.setQuery("", false);
            mSearchView.setIconified(true);
        }
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            //Log(TAG, "ACTION_SEARCH");
            onActionSearch(intent.getStringExtra(SearchManager.QUERY));
        } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            //Log(TAG, "ACTION_VIEW");
            onActionView(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
        }
    }

    private void onActionSearch(String query) {
        //Log(TAG, "onActionSearch");
        Bundle data = new Bundle();
        data.putString("query", query);
        getSupportLoaderManager().restartLoader(0, data, this);
    }

    private void onActionView(String placeId) {
        //Log(TAG, "onActionView");
        Bundle data = new Bundle();
        data.putString("query", placeId);
        getSupportLoaderManager().restartLoader(1, data, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle query) {
        //Log.d(TAG, "onCreateLoader: " + Integer.toString(id));
        CursorLoader cLoader = null;
        if (id == 0)
            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.SEARCH_URI, null, null, new String[]{query.getString("query")}, null);
        else if (id == 1)
            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.DETAILS_URI, null, null, new String[]{query.getString("query")}, null);
        return cLoader;

    }

    public abstract void onDetailsLoaded(LatLng position, String description);


    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        //Log(TAG, "onLoadFinished");
        if (c.moveToFirst()) {
            LatLng position = new LatLng(Double.parseDouble(c.getString(1)),
                    Double.parseDouble(c.getString(2)));

            String description = c.getString(0);
            //todo: make this an interface
            onDetailsLoaded(position, description);
        }
        else {
            Log.e(TAG, "cursor is empty!");
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        //Log(TAG, "onLoaderReset");
    }

}
