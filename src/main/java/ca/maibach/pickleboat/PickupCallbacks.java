package ca.maibach.pickleboat;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by keith on 14/04/15.
 */
public interface PickupCallbacks {
    public void onPickupDetailsReceived(LatLng position, String description);
    public void onInitialized(LatLng position, String description);
}
