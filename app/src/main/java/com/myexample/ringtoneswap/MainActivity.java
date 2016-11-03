package com.myexample.ringtoneswap;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity
		extends AppCompatActivity {

	private static final int CONTACT_CHOOSER_ACTIVITY_CODE = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button getContactBtn = (Button) findViewById(R.id.getContactBtn);
		getContactBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startPickContactActivityForResult();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case CONTACT_CHOOSER_ACTIVITY_CODE:
				if (resultCode == RESULT_OK) {
					Uri contactData = data.getData();
					String[] PROJECTION = {ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER};
					Cursor cursor = getContentResolver().query(contactData, PROJECTION, null, null, null);
					if (cursor != null) {
						while (cursor.moveToNext()) {
							String number = cursor.getString(0);
							Toast.makeText(this, "phone number - " + number, Toast.LENGTH_SHORT)
							     .show();
						}
						cursor.close();
					}
				}
				break;
		}
	}


	private void startPickContactActivityForResult() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, CONTACT_CHOOSER_ACTIVITY_CODE);
	}
}
