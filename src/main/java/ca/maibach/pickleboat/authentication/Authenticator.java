package ca.maibach.pickleboat.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import ca.maibach.pickleboat.R;
import ca.maibach.pickleboat.Utility;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;


/**
 * Created by keith on 21/03/15.
 */
public class Authenticator extends AbstractAccountAuthenticator {
    public static final String ACCOUNT_TYPE = FirebaseHelper.ACCOUNT_TYPE;
    public static final String AUTHTOKEN_TYPE_KEY = "authtoken_type_key";
    public static final String AUTHTOKEN_KEY = "authtoken_key";
    public static final String EXPIRY_KEY = "expiry_key";
    public static final String UID_KEY = "uid_key";
    public static final String FIREBASE_URL = FirebaseHelper.FIREBASE_URL;
    private static final String TAG = "PickleBoat: " + "Auth";
    private final Context mContext;


    // Simple constructor
    public Authenticator(Context context) {
        super(context);
        this.mContext = context;
    }


    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        try {
            //FirebaseHelper.getFirebase().createUser(email, pass, new ResultHandler(response));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                               String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "getAuthToken");

        final AccountManager am = AccountManager.get(mContext);
        String authToken = am.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(authToken)) {

            switch (authTokenType) {
                case FirebaseHelper.GUEST_AUTHTOKEN_TYPE:

                    try {

                        firebase.authAnonymously(
                                new AuthResultHandler("anonymous", response));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                case FirebaseHelper.CAPTAIN_AUTHTOKEN_TYPE:
                case FirebaseHelper.ADMIN_AUTHTOKEN_TYPE:

                    final String password = am.getPassword(account);
                    Log.d(TAG, "> re-authenticating with the existing password");
                    try {
                        FirebaseHelper.getFirebase().authAnonymously(
                                new AuthResultHandler("password", response));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            }
        } else {
            Log.d("TAG", "> peekAuthToken returned - " + authToken);

            Bundle bundle = new Bundle();
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
            bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            bundle.putString(AccountManager.KEY_AUTH_TOKEN_LABEL, getAuthTokenLabel(authTokenType));


        }
        return null;
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return s;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        //Log.d(TAG, "hasFeatures");
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    private class ResultHandler implements Firebase.ResultHandler {

        private final AccountAuthenticatorResponse mResponse;

        public ResultHandler(AccountAuthenticatorResponse response) {
            this.mResponse = response;
        }


        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(FirebaseError firebaseError) {
            String errorMsg = Utility.getString((R.string.msg_signup_fail));

        }
    }

    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;
        private final AccountAuthenticatorResponse mResponse;

        public AuthResultHandler(String provider, AccountAuthenticatorResponse response) {
            this.provider = provider;
            this.mResponse = response;
        }


        @Override
        public void onAuthenticated(AuthData authData) {
            Log.d(TAG, provider + " auth successful");
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, authData.getUid());
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, authData.getProvider());
            result.putString(AccountManager.KEY_AUTHTOKEN, authData.getToken());
            mResponse.onResult(result);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            Log.d(TAG, provider + " auth error: " + firebaseError.toString());
            mResponse.onError(firebaseError.getCode(), firebaseError.getMessage());
        }
    }






/*

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);

        String authToken = am.peekAuthToken(account, authTokenType);
        String userId = null; //User identifier, needed for creating ACL on our server-side

        ////Log.d(TAG, "> peekAuthToken returned - " + authToken);

        // Lets give another try to authenticate the user
        if (TextUtils.isEmpty(authToken)) {
            final String password = am.getPassword(account);
            if (password != null) {
                try {
                    ////Log.d(TAG, "> re-authenticating with the existing password");
                    User user = sServerAuthenticate.login(account.name, password, authTokenType);
                    if (user != null) {
                        authToken = user.getSessionToken();
                        userId = user.getObjectId();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(ACCOUNT_TYPE, account.type);
        intent.putExtra(AUTHTOKEN_TYPE, authTokenType);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;

        final Bundle bundle = new Bundle();

        return bundle;
    }
*/


}