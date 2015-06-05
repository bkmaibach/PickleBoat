package ca.maibach.pickleboat;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by keith on 14/04/15.
 */
public interface PickupCallbacks {
    void onPickupDetailsReceived(LatLng position, String description);

    void onInitialized(LatLng position, String description);
}
