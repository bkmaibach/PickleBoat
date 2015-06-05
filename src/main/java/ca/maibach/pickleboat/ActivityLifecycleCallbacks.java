package ca.maibach.pickleboat;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by keith on 30/04/15.
 */
public interface ActivityLifecycleCallbacks{
    public void onActivityStopped(Activity activity);
    public void onActivityStarted(Activity activity);
    public void onActivitySaveInstanceState(Activity activity, Bundle outState);
    public void onActivityResumed(Activity activity);
    public void onActivityPaused(Activity activity);
    public void onActivityDestroyed(Activity activity);
    public void onActivityCreated(Activity activity, Bundle savedInstanceState);
}