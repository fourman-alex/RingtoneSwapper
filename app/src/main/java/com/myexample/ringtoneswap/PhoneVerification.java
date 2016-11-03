package com.myexample.ringtoneswap;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import java.io.FileNotFoundException;

import io.fabric.sdk.android.Fabric;


public class PhoneVerification
		extends AppCompatActivity {

	private static final int CONTACT_CHOOSER_ACTIVITY_CODE = 1;
	private static final int FILE_CHOOSER_ACTIVITY_CODE    = 0;

	public static final int PERMISSION_WRITE_CONTACTS = 0;

	// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
	private static final String TWITTER_KEY    = "G46BtHLQIlx16buRCBDaEU4kE";
	private static final String TWITTER_SECRET = "C21g5ncGknr2VOJbFKTFLi3VhqGAQ5xuxnutc6SdZnKkU5Nwtk";
	private String mPhoneNumber;
	private Button mDigitsButton;
	private Button mSelectContactBtn;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().withTheme(R.style.AppTheme)
		                                                                   .build());

		setContentView(R.layout.activity_phone_verification);
		mDigitsButton = (Button) findViewById(R.id.auth_button);
		mSelectContactBtn = (Button) findViewById(R.id.selectContact_btn);


		//init firebase
		FirebaseHelper.getInstance()
		              .init(new OnCompleteListener<AuthResult>() {
			              @Override
			              public void onComplete(@NonNull Task<AuthResult> task) {
				              if (task.isSuccessful()) {
					              mDigitsButton.setEnabled(true);
					              final SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHAREDPREF_RINGTONESWAP, MODE_PRIVATE);
					              String selfPhoneNumber = sharedPreferences.getString(Consts.PREF_PHONE_NUMBER, null);
					              if (!TextUtils.isEmpty(selfPhoneNumber)) {
						              requestPermissions(PhoneVerification.this);
					              }
				              }
				              else {
					              Snackbar.make(findViewById(R.id.activity_phone_verification), "Uh Oh...", Snackbar.LENGTH_LONG)
					                      .show();
				              }
			              }
		              });

		mSelectContactBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startPickContactActivityForResult();
			}
		});

		//		mDigitsButton.setAuthTheme(R.style.AppTheme);
		final AuthCallback authCallback = new AuthCallback() {
			@Override
			public void success(DigitsSession session, String selfPhoneNumber) {
				SharedPreferences.Editor editor = getSharedPreferences(Consts.SHAREDPREF_RINGTONESWAP, MODE_PRIVATE).edit();
				editor.putString(Consts.PREF_PHONE_NUMBER, selfPhoneNumber);
				editor.apply();
				mDigitsButton.setEnabled(false);
				requestPermissions(PhoneVerification.this);

				Snackbar.make(findViewById(R.id.activity_phone_verification), "Authentication successful for " + selfPhoneNumber, Snackbar.LENGTH_LONG)
				        .show();
			}

			@Override
			public void failure(DigitsException exception) {
				Log.e("Digits", "Sign in with Digits failure", exception);
			}
		};

		mDigitsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AuthConfig.Builder digitsAuthConfigBuilder = new AuthConfig.Builder().withAuthCallBack(authCallback);
				Digits.authenticate(digitsAuthConfigBuilder.build());

			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_WRITE_CONTACTS: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					onPermissionGranted();

				}
				else {

					// TODO: 03-Nov-16 disable the functionality?
					Toast.makeText(this, "We need the phone state permission to know your phone number", Toast.LENGTH_SHORT)
					     .show();
				}
			}

		}
	}

	private void onPermissionGranted() {
		mDigitsButton.setEnabled(false);
		mSelectContactBtn.setVisibility(View.VISIBLE);
		final SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHAREDPREF_RINGTONESWAP, MODE_PRIVATE);
		String selfPhoneNumber = sharedPreferences.getString(Consts.PREF_PHONE_NUMBER, null);
		FirebaseHelper.getInstance()
		              .registerToRingtoneUpdates(selfPhoneNumber, PhoneVerification.this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case CONTACT_CHOOSER_ACTIVITY_CODE:
				if (resultCode == RESULT_OK) {
					Uri contactData = data.getData();
					String[] PROJECTION = {ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER};
					Cursor cursor = getContentResolver().query(contactData, PROJECTION, null, null, null);
					if (cursor != null) {
						while (cursor.moveToNext()) {
							mPhoneNumber = cursor.getString(0);
							Toast.makeText(this, "phone number - " + mPhoneNumber, Toast.LENGTH_SHORT)
							     .show();
							startPickFileActivityForResult();
						}
						cursor.close();
					}
				}
				break;
			case FILE_CHOOSER_ACTIVITY_CODE:
				if (resultCode == RESULT_OK) {
					Toast.makeText(this, data.getDataString(), Toast.LENGTH_SHORT)
					     .show();
					try {
						String selfPhoneNumber = getSharedPreferences(Consts.SHAREDPREF_RINGTONESWAP, MODE_PRIVATE).getString(Consts.PREF_PHONE_NUMBER, null);
						FirebaseHelper.getInstance()
						              .uploadRingtone(selfPhoneNumber, mPhoneNumber, getContentResolver().openInputStream(data.getData()));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
		}
	}

	private void startPickContactActivityForResult() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, CONTACT_CHOOSER_ACTIVITY_CODE);
	}

	private void startPickFileActivityForResult() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setTypeAndNormalize("*/*");
		startActivityForResult(intent, FILE_CHOOSER_ACTIVITY_CODE);
	}

	private void requestPermissions(final Activity activity) {
		if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

			if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_CONTACTS)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage("We need to access your contacts so that we can set ringtones to your friends");
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						requestPermissions(activity);
					}
				});
				builder.show();
			}
			else {
				ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_CONTACTS}, PERMISSION_WRITE_CONTACTS);
			}
		}
		else {
			onPermissionGranted();
		}
	}

}
