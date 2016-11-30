package com.myexample.ringtoneswap;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService
		extends FirebaseMessagingService {

	private static final String TAG = "MESSAGE";

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {

		Log.d(TAG, "From: " + remoteMessage.getFrom());

		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Log.d(TAG, "Message data payload: " + remoteMessage.getData());
			String pranker = remoteMessage.getData().get("pranker");
			final SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHAREDPREF_RINGTONESWAP, MODE_PRIVATE);
			String selfPhoneNumber = sharedPreferences.getString(Consts.PREF_PHONE_NUMBER, null);
			FirebaseHelper.getInstance().downloadAndSetRingtone(selfPhoneNumber, pranker, this);
		}

		super.onMessageReceived(remoteMessage);
	}
}
