package com.myexample.ringtoneswap;

interface AuthMechanism {

	void authenticate(AuthCallback authCallback);

	interface AuthCallback {

		void onSuccess(String selfPhoneNumber);

		void onFailure();
	}
}
