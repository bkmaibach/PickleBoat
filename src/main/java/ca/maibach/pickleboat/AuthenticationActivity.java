package ca.maibach.pickleboat;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import java.io.IOException;

import ca.maibach.pickleboat.authentication.FirebaseHelper;
import ca.maibach.pickleboat.data.SyncAdapter;

/**
 * Created by keith on 15/04/15.
 */
public class AuthenticationActivity extends AccountAuthenticatorActivity {
    private static String TAG = "PickleBoat: " + AuthenticationActivity.class.getSimpleName();

    final String accountType = FirebaseHelper.ACCOUNT_TYPE;
    final String authType = FirebaseHelper.GUEST_AUTHTOKEN_TYPE;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.loading_layout);

        new LoadingScreenTask().execute();


        firebase.addAuthStateListener(new Firebase.AuthStateListener() {

            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData.getToken() != null) {
                    AuthenticationActivity.this.setResult(AccountAuthenticatorActivity.RESULT_OK);

                }
            }
        });


    }

    private class LoadingScreenTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            final Account availableAccounts[] = AccountManager.get(AuthenticationActivity.this)
                    .getAccountsByType(accountType);

            if (availableAccounts.length == 0) {
                getGuestAccess();

            } else {
                try {
                    AccountManager.get(AuthenticationActivity.this).blockingGetAuthToken(availableAccounts[0], authType, true);
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }

    private void getGuestAccess() {

        AccountManager.get(this).getAuthTokenByFeatures(accountType, authType, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle result = null;
                        try {
                            String authToken;
                            result = future.getResult();
                            authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                            if (authToken != null) {
                                String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);

                                Account account = new Account(accountName, accountType);
                                SyncAdapter.enableSync(account);
                                SyncAdapter.setAutoSyncOn(account);


                                Log.d(TAG, "GetTokenForAccount Bundle is " + result + "\n" + "account: " + account.toString());
                            }

                        } catch (AuthenticatorException e) {
                            e.printStackTrace();
                            //Log.d(TAG, e.getMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                            //Log.d(TAG, e.getMessage());
                        } catch (OperationCanceledException e) {
                            e.printStackTrace();
                            //Log.d(TAG, e.getMessage());
                        }
                    }
                }, null);
    }


}
/*

    private void getTokenForAccountCreateIfNeeded(final String accountType, String authType) {
        //Log.d(TAG, "getTokenForAccountCreateIfNeeded");

        AccountManager.get(this).getAuthTokenByFeatures(accountType, authType, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle result = null;
                        try {
                            result = future.getResult();
                            authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                            if (authToken != null) {
                                String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                                Account account = new Account(accountName, AccountGeneral.ACCOUNT_TYPE);
                                CaptainSyncAdapter.enableSync(account);
                                Log.v(TAG, "periodic syncs before: " + Integer.toString(ContentResolver.getPeriodicSyncs(account, AUTHORITY).size()));
                                ContentResolver.removePeriodicSync(account, AUTHORITY, Bundle.EMPTY);

                                ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, 60);
                                CaptainSyncAdapter.setAutoSyncOn(account);
                                Log.v(TAG, "periodic syncs after: " + Integer.toString(ContentResolver.getPeriodicSyncs(account, AUTHORITY).size()));

                                onAccountReady(account);
                            }
                            //Log.d(TAG, "GetTokenForAccount Bundle is " + result + "\n" + "account: " + account.toString());
                        } catch (AuthenticatorException e) {
                            e.printStackTrace();
                            //Log.d(TAG, e.getMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                            //Log.d(TAG, e.getMessage());
                        } catch (OperationCanceledException e) {
                            e.printStackTrace();
                            //Log.d(TAG, e.getMessage());
                        }
                    }
                }, null);
    }

    private void showAccountPicker(final String authTokenType, final boolean invalidate) {
        //todo, disallow cancelling by touching off the dialog
        //Log.d(TAG, "showAccountPicker");

        final Account availableAccounts[] = AccountManager.get(this).getAccountsByType(sAccountType);

        if (availableAccounts.length == 0) {
            Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show();
        } else {
            String name[] = new String[availableAccounts.length];
            for (int i = 0; i < availableAccounts.length; i++) {
                name[i] = availableAccounts[i].name;
                // Account picker
                new AlertDialog.Builder(this).setTitle("Select Account").setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, name), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (invalidate)
                            invalidateAuthToken(availableAccounts[which], authTokenType);
                        else
                            getExistingAccountAuthToken(availableAccounts[which], authTokenType);
                    }
                }).show();
            }
        }
    }

    private void getExistingAccountAuthToken(final Account account, String authTokenType) {
        //Log.d(TAG, "getExistingAccountAuthToken");

        final AccountManagerFuture<Bundle> future = AccountManager.get(this).getAuthToken(account, authTokenType, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    if(account != null){
                        onAccountReady(account);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }
        }).start();
    }

    private void invalidateAuthToken(final Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = AccountManager.get(this).getAuthToken(account, authTokenType, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    AccountManager.get(AuthenticationActivity.this).invalidateAuthToken(account.type, authtoken);
                    //Log.d(TAG, "invalidateAuthToken: " + account.name + " invalidated");

                    //showMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        }).start();
    }*/





