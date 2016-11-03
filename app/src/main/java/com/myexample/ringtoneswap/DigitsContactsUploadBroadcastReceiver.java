package com.myexample.ringtoneswap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.digits.sdk.android.ContactsUploadService;
import com.digits.sdk.android.models.ContactsUploadResult;

public class DigitsContactsUploadBroadcastReceiver
		extends BroadcastReceiver {

	public DigitsContactsUploadBroadcastReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ContactsUploadService.UPLOAD_COMPLETE.equals(intent.getAction())) {
			ContactsUploadResult result = intent.getParcelableExtra(ContactsUploadService.UPLOAD_COMPLETE_EXTRA);
			// Post success notification
		}
		else {
			Log.d(Consts.TAG, "contacts upload failed");
			// Post failure notification
		}
	}
}
