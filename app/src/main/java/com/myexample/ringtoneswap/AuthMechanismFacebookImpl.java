package com.myexample.ringtoneswap;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;

class AuthMechanismFacebookImpl implements AuthMechanism {
    private Activity activity;

    public AuthMechanismFacebookImpl(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void authenticate(AuthCallback authCallback) {
        phoneLogin(activity);
    }

    public static int APP_REQUEST_CODE = 99;

    public void phoneLogin(final Activity activity) {
        final Intent intent = new Intent(activity, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        activity.startActivityForResult(intent, APP_REQUEST_CODE);
    }

    public void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        if (requestCode != APP_REQUEST_CODE) {
            return;
        }

        final String toastMessage;
        final AccountKitLoginResult loginResult = AccountKit.loginResultWithIntent(data);
        if (loginResult == null || loginResult.wasCancelled()) {
            toastMessage = "Login Cancelled";
        } else if (loginResult.getError() != null) {
            toastMessage = loginResult.getError().getErrorType().getMessage();
        } else {
            final AccessToken accessToken = loginResult.getAccessToken();
            final long tokenRefreshIntervalInSeconds =
                    loginResult.getTokenRefreshIntervalInSeconds();
            if (accessToken != null) {
                toastMessage = "Success:" + accessToken.getAccountId()
                        + tokenRefreshIntervalInSeconds;
            } else {
                toastMessage = "Unknown response type";
            }
        }

        Toast.makeText(
                activity,
                toastMessage,
                Toast.LENGTH_LONG)
                .show();
    }
}
