package ca.maibach.pickleboat;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by keith on 30/04/15.
 */
public interface ActivityLifecycleCallbacks {
    void onActivityStopped(Activity activity);

    void onActivityStarted(Activity activity);

    void onActivitySaveInstanceState(Activity activity, Bundle outState);

    void onActivityResumed(Activity activity);

    void onActivityPaused(Activity activity);

    void onActivityDestroyed(Activity activity);

    void onActivityCreated(Activity activity, Bundle savedInstanceState);
}