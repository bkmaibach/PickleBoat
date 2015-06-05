package ca.maibach.pickleboat;

/**
 * Created by keith on 31/03/15.
 *//*
public abstract class SyncActivity extends ActionBarActivity {
    private static String TAG = "PickleBoat: " + SyncActivity.class.getSimpleName();

    public static final String AUTHORITY = CaptainContract.CONTENT_AUTHORITY;

    public static final String KEY_SAVED_ACCOUNT = "saved_account";
    public static final String KEY_SAVED_AUTHTOKEN = "saved_authtoken";

    protected Account mConnectedAccount = null;

    protected String mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ////Log.d(TAG, "onCreate");
        //todo: prevent reauthorization on screen rotation
        super.onCreate(savedInstanceState);

        mConnectedAccount = getIntent().getParcelableExtra(AuthenticationActivity.PICKLEBOAT_ACCOUNT_KEY);

*//*        if (mConnectedAccount == null){
            getAuthorizedAccount();
        }*//*
    }



    SyncStatusObserver syncObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(final int which) {
            String status;
            //Log.d(TAG, "periodicSyncs: " + ContentResolver.getPeriodicSyncs(mConnectedAccount, AUTHORITY).toString());

            if (ContentResolver.isSyncActive(mConnectedAccount, AUTHORITY)){
                status = "Sync active";
            }

            else if (ContentResolver.isSyncPending(mConnectedAccount, AUTHORITY)){
                status = "Sync pending";
            }

            else{
                status = "Sync idle";
            }


            //Log.d(TAG, "refreshSyncStatus: " + status);
        }
    };

    Object handleSyncObserver;
    @Override
    public void onResume() {
        super.onResume();
        handleSyncObserver = ContentResolver.addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE |
                        ContentResolver.SYNC_OBSERVER_TYPE_PENDING, syncObserver);

    }

    @Override
    public void onPause() {
        if (handleSyncObserver != null) {
            ContentResolver.removeStatusChangeListener(handleSyncObserver);
        }

        super.onPause();
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }





    protected void syncNewZone(String newZoneId){
        Log.d(TAG, "syncNewZone: newZoneId: " + newZoneId);
        if(mConnectedAccount != null) {

            AccountManager.get(this).setUserData(mConnectedAccount,
                    FirebaseHelper.USERDATA_PICKUP_ZONE_ID,
                    newZoneId);


            requestSyncOnce();
        }
    }

    private void requestSyncOnce(){
        Log.d(TAG, "requestSyncOnce");

        if (ContentResolver.isSyncPending(mConnectedAccount, AUTHORITY) || ContentResolver.isSyncActive(mConnectedAccount, AUTHORITY)) {
            Log.d(TAG, "sync pending or active");
            for (int i = 0; i < ContentResolver.getCurrentSyncs().size(); i++) {
                Log.v(TAG, "current syncs before cancel: " + ContentResolver.getCurrentSyncs().get(0).toString());
            }

            ContentResolver.cancelSync(mConnectedAccount, AUTHORITY);
            for (int i = 0; i < ContentResolver.getCurrentSyncs().size(); i++) {
                Log.v(TAG, "current syncs after cancel: " + ContentResolver.getCurrentSyncs().get(0).toString());
            }
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(mConnectedAccount, AUTHORITY, bundle);
    }



}*/