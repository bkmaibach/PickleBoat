package ca.maibach.pickleboat.authentication;

/**
 * Created by keith on 21/03/15.
 */

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.firebase.client.Firebase;

import ca.maibach.pickleboat.Utility;

/**
 * A bound Service that instantiates the authenticator
 * when started.
 */
public class PickleboatService extends Service {
    public static final String ACTION_INIT_FIREBASE = "init_firebase";
    private static final Object lock = new Object();
    private static final String zone_path = "zones";
    private String TAG = "PickleBoat: " + "AuthenticatorService";
    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;
    private Firebase mFirebase;




    /* A reference to the Firebase */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() == ACTION_INIT_FIREBASE) {


            Firebase ref = new Firebase(builtUri.toString());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        //Log.d(TAG, "onCreate");


        synchronized (lock) {
            if (mAuthenticator == null) {
                mAuthenticator = new Authenticator(this);

            }
            if (mFirebase == null) {
                final String zone_setting = Utility.getZoneSetting(this);

                Uri builtUri = Uri.parse(Utility.FIREBASE_BASE_URL).buildUpon()
                        .appendPath(zone_path).appendPath(zone_setting)
                        .build();

                mFirebase = new Firebase(builtUri.toString());
            }
        }
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: intent action " + intent.getAction());
        IBinder binder = new IBinder()

        return mAuthenticator.getIBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onBind");
        return super.onUnbind(intent);
    }
}