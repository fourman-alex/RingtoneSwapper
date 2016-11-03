package com.myexample.ringtoneswap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.digits.sdk.android.models.Contacts;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;

import io.fabric.sdk.android.Fabric;

public class MainActivity
		extends AppCompatActivity {

	// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
	private static final String TWITTER_KEY                          = "t2J09dxXqJaFujhY3pppmY8sh";
	private static final String TWITTER_SECRET                       = "5U7HaywQ1PKEDG8AUZ0xVJM2LFgGbNacjQanXtk9xpwMzvyGXh";
	public static final  int    CONTACT_UPLOAD_REQUEST               = 123;
	private static final int    MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());
		setContentView(R.layout.activity_main);

		DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
		digitsButton.setCallback(new AuthCallback() {
			@Override
			public void success(DigitsSession session, String phoneNumber) {
				// TODO: associate the session userID with your user model
				Toast.makeText(getApplicationContext(), "Authentication successful for " + phoneNumber, Toast.LENGTH_LONG)
				     .show();
			}

			@Override
			public void failure(DigitsException exception) {
				Log.d("Digits", "Sign in with Digits failure", exception);
			}
		});

		Button findFriendsBtn = (Button) findViewById(R.id.findFriendsBtn);
		findFriendsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// Here, thisActivity is the current activity
				if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

					ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);

					// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
					// app-defined int constant. The callback method gets the
					// result of the request.
				}
				else {
					//					Digits.uploadContacts();
					Digits.findFriends(new Callback<Contacts>() {
						@Override
						public void success(Result<Contacts> result) {
							Toast.makeText(MainActivity.this, "found friends?", Toast.LENGTH_SHORT)
							     .show();
						}

						@Override
						public void failure(TwitterException exception) {
							Toast.makeText(MainActivity.this, "OH OH!", Toast.LENGTH_SHORT)
							     .show();
						}
					});
				}

			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					Digits.uploadContacts(CONTACT_UPLOAD_REQUEST);

				}
				else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this, "Permission was denied", Toast.LENGTH_SHORT)
					     .show();
				}
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CONTACT_UPLOAD_REQUEST) {
			Toast.makeText(this, "stuff", Toast.LENGTH_SHORT)
			     .show();
		}
	}
}
