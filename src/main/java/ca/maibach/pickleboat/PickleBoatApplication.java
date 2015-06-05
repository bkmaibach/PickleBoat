package ca.maibach.pickleboat;

import android.app.Application;
import android.content.Intent;

import ca.maibach.pickleboat.authentication.PickleboatService;


/**
 * Created by keith on 23/03/15.
 */
public class PickleBoatApplication extends Application {


    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */


    @Override
    public void onCreate() {
        super.onCreate();
        Utility.setAndroidContext(this);
        Intent intent = new Intent(this, PickleboatService.class);

        startService();
    }


}
