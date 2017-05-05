package com.myexample.ringtoneswap;

import android.app.Activity;
import android.content.Context;

interface AuthMechanism {

	void authenticate(AuthCallback authCallback);

	interface AuthCallback {

		void onSuccess(String selfPhoneNumber);

		void onFailure();
	}
}
