package com.myexample.ringtoneswap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

	public static final int PERMISSION_READ_PHONE_STATE = 0;
	private String mPhoneNumber;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FirebaseHelper.getInstance()
		              .registerToRingtoneUpdates(getSelfPhoneNumber());

		Button getContactBtn = (Button) findViewById(R.id.getContactBtn);
		getContactBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startPickContactActivityForResult();
			}
		});

		Button fileBtn = (Button) findViewById(R.id.getFileBtn);
		fileBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startPickFileActivityForResult();
			}
		});

		Button getSelfNumberBtn = (Button) findViewById(R.id.getSelfNumberBtn);
		getSelfNumberBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Here, thisActivity is the current activity
				if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

					ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_PHONE_STATE);
				}
				else {

					getSelfPhoneNumber();
				}
			}
		});
	}

	private String getSelfPhoneNumber() {
		String selfPhoneNumber = "+9720546206083";
		Toast.makeText(this, selfPhoneNumber, Toast.LENGTH_SHORT)
		     .show();
		return selfPhoneNumber;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_READ_PHONE_STATE: {
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
					FirebaseHelper.getInstance()
					              .init(new OnCompleteListener<AuthResult>() {
						              @Override
						              public void onComplete(@NonNull Task<AuthResult> task) {
							              if (task.isSuccessful()) {
								              try {
									              FirebaseHelper.getInstance()
									                            .uploadRingtone(getSelfPhoneNumber(), mPhoneNumber, getContentResolver().openInputStream(data.getData()));
								              } catch (FileNotFoundException e) {
									              e.printStackTrace();
								              }
							              }
						              }
					              });
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
