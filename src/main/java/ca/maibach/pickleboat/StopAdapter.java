package ca.maibach.pickleboat;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.maibach.pickleboat.data.PickleContract.StopEntry;

/**
 * Created by keith on 28/02/15.
 */
public class StopAdapter extends CursorAdapter {
    private static final String TAG = "PickleBoat: " + "driverAdapter";


    public StopAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.nameView.setText(cursor.getString(StopEntry.COL_NUM_NAME));
        viewHolder.numberView.setText(Integer.toString(cursor.getInt(StopEntry.COL_NUM_STOP_NUMBER)));

        int currentTime = (int) (System.currentTimeMillis()) / 1000;
        int arrivalMinutes = cursor.getInt(StopEntry.COL_NUM_NEXT_ARRIVAL_TIME) - currentTime;
        viewHolder.nameView.setText(Integer.toString(arrivalMinutes / 60));

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int layoutId = -1;
        layoutId = R.layout.stop_spinner_item;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        ////Log.d(TAG, Integer.toString(getCount()));
        return view;

    }

    @Override
    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {

        int layoutId = -1;
        layoutId = R.layout.stop_spinner_item;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        ////Log.d(TAG, Integer.toString(getCount()));
        return view;
    }


    @Override
    protected void onContentChanged() {
        super.onContentChanged();

    }


    public static class ViewHolder {

        public final TextView nameView;
        public final TextView numberView;
        public final TextView nextArrival;


        public ViewHolder(View view) {

            numberView = (TextView) view.findViewById(R.id.stop_address_textview);
            nameView = (TextView) view.findViewById(R.id.stop_name_textview);
            nextArrival = (TextView) view.findViewById(R.id.arrival_textview);
        }
    }
}
