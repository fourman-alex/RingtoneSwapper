package com.myexample.ringtoneswap;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.io.FileNotFoundException;

public class MainActivity
		extends AppCompatActivity {

	private static final int CONTACT_CHOOSER_ACTIVITY_CODE = 1;
	private static final int FILE_CHOOSER_ACTIVITY_CODE    = 0;

	public static final int PERMISSION_WRITE_CONTACTS = 0;
	private String mPhoneNumber;

	public static void start(Context context) {
		Intent starter = new Intent(context, MainActivity.class);
		context.startActivity(starter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Button getContactBtn = (Button) findViewById(R.id.getContactBtn);

		FirebaseHelper.getInstance()
		              .init(new OnCompleteListener<AuthResult>() {
			              @Override
			              public void onComplete(@NonNull Task<AuthResult> task) {
				              FirebaseHelper.getInstance()
				                            .registerToRingtoneUpdates(getSelfPhoneNumber(), MainActivity.this);
				              getContactBtn.setEnabled(true);
			              }
		              });

		//check for needed permissions
		requestPermissions();

		getContactBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				startPickContactActivityForResult();
			}
		});

	}

	private void requestPermissions() {
		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

			if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_CONTACTS)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage("We need to access your contacts so that we can set ringtones to your friends");
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						requestPermissions();
					}
				});
				builder.show();
			}
			else {
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSION_WRITE_CONTACTS);
			}
		}
	}

	private String getSelfPhoneNumber() {

		String phoneNumber = getSharedPreferences(Consts.SHAREDPREF_RINGTONESWAP, MODE_PRIVATE).getString(Consts.PREF_PHONE_NUMBER, null);
		if (TextUtils.isEmpty(phoneNumber)) {
			throw new RuntimeException("Phone number is missing");
		}
		return phoneNumber;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_WRITE_CONTACTS: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					getSelfPhoneNumber();

				}
				else {

					// TODO: 03-Nov-16 disable the functionality?
					Toast.makeText(this, "We need the phone state permission to know your phone number", Toast.LENGTH_SHORT)
					     .show();
				}
			}

		}
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
						FirebaseHelper.getInstance()
						              .uploadRingtone(getSelfPhoneNumber(), mPhoneNumber, getContentResolver().openInputStream(data.getData()));
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

}
