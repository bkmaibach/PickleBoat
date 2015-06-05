package ca.maibach.pickleboat.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


/**
 * Created by keith on 14/03/15.
 */
//multiple syncadapters can share this file!
public class SyncService extends Service {
    private static final String TAG = "PickleBoat: SyncService";

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sDriverSyncAdapter = null;

    @Override
    public void onCreate() {
        //Log.d(TAG, "onCreate");

        synchronized (sSyncAdapterLock) {
            if (sDriverSyncAdapter == null) {
                sDriverSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: intent action " + intent.getAction());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: intent action " + intent.getAction());

        return sDriverSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
