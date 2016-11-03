package com.myexample.ringtoneswap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

public class PhoneVerification
		extends AppCompatActivity {

	// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
	private static final String TWITTER_KEY    = "G46BtHLQIlx16buRCBDaEU4kE";
	private static final String TWITTER_SECRET = "C21g5ncGknr2VOJbFKTFLi3VhqGAQ5xuxnutc6SdZnKkU5Nwtk";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());
		setContentView(R.layout.activity_phone_verification);
		final SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHAREDPREF_RINGTONESWAP, MODE_PRIVATE);
		if (!TextUtils.isEmpty(sharedPreferences.getString(Consts.PREF_PHONE_NUMBER, null))) {
			MainActivity.start(this);
			finish();
			return;
		}

		DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
		digitsButton.setCallback(new AuthCallback() {
			@Override
			public void success(DigitsSession session, String phoneNumber) {
				SharedPreferences.Editor editor = getSharedPreferences(Consts.SHAREDPREF_RINGTONESWAP, MODE_PRIVATE).edit();
				editor.putString(Consts.PREF_PHONE_NUMBER, phoneNumber);
				editor.apply();
				MainActivity.start(PhoneVerification.this);
				finish();
				Toast.makeText(getApplicationContext(), "Authentication successful for " + phoneNumber, Toast.LENGTH_LONG)
				     .show();
			}

			@Override
			public void failure(DigitsException exception) {
				Log.e("Digits", "Sign in with Digits failure", exception);
			}
		});
	}
}
