package ca.maibach.pickleboat.authentication;


import android.accounts.AccountManager;
import android.app.Activity;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.authentication.AuthenticationManager;
import com.firebase.client.core.Context;
import com.firebase.client.core.PersistentConnection;
import com.firebase.client.core.Repo;
import com.firebase.client.core.RepoInfo;

import org.json.JSONObject;

import ca.maibach.pickleboat.R;
import ca.maibach.pickleboat.Utility;


/**
 * Created with IntelliJ IDEA.
 * User: Udini
 * Date: 20/03/13
 * Time: 18:11
 */
public class FirebaseHelper{//} extends AuthenticationManager {
    private static final String TAG = "PickleBoat: " + "Auth";

    public static final String FIREBASE_URL = "https://pickleboat.firebaseio.com/";
    public static final String ACCOUNT_TYPE = "pickleboat.firebaseIO.com";

    //public static final String ACCOUNT_BUNDLE_KEY = "account_key";

    public static final String GUEST_AUTHTOKEN_TYPE = "guest_authtoken";
    public static final String CAPTAIN_AUTHTOKEN_TYPE = "captain_authtoken";
    public static final String ADMIN_AUTHTOKEN_TYPE = "admin_authtoken";


    public static final String ANONYMOUS_AUTH_PROVIDER = "anonymous";
    public static final String PASSWORD_AUTH_PROVIDER = "password";


}

/*    public FirebaseHelper(Context context, Repo repo, RepoInfo repoInfo, PersistentConnection connection) {
        super(context, repo, repoInfo, connection);

    }*/



/*

    static{
        sFirebase = new Firebase(FIREBASE_URL);

        sFirebase.addAuthStateListener(new Firebase.AuthStateListener() {

            @Override
            public void onAuthStateChanged(AuthData authData) {
                if(authData != null) {
                    mToken = authData.getToken();
                    if (mToken != null && authData.getExpires() <= System.currentTimeMillis() / 1000) {
                        String errorMsg = Utility.getApplicationContext().getString(R.string.msg_session_expired);
                        Utility.showErrorDialog(errorMsg);
                    }
                }
            }
        });
    }


    public static Firebase getFirebase(){
        return sFirebase;
    }


    public static JSONObject getStopJsonObject(String authToken) {}

    }
    */
