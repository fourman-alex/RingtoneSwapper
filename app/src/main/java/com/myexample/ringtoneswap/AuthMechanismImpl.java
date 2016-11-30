package com.myexample.ringtoneswap;

import android.support.annotation.NonNull;
import android.util.Log;

import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static com.myexample.ringtoneswap.Consts.TAG;

public class AuthMechanismImpl
		implements AuthMechanism {

	@Override
	public void authenticate(final AuthCallback authCallback) {
		final com.digits.sdk.android.AuthCallback digitsAuthCallback = new com.digits.sdk.android.AuthCallback() {
			@Override
			public void success(DigitsSession session, final String selfPhoneNumber) {

				Log.d(TAG, "success: Digits authentication successful for phone number: " + selfPhoneNumber);

				//sign in to firebase
				FirebaseAuth.getInstance()
				            .signInAnonymously()
				            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					            @Override
					            public void onComplete(@NonNull Task<AuthResult> task) {
						            if (task.isSuccessful()) {
							            Log.d(TAG, "onComplete: Firebase authentication is successful");
							            authCallback.onSuccess(selfPhoneNumber);
						            }
						            else {
							            Log.e(TAG, "onComplete: Firebase authentication has failed");
							            authCallback.onFailure();
						            }
					            }
				            });
			}

			@Override
			public void failure(DigitsException exception) {
				Log.e(TAG, "Sign in with Digits failure", exception);
				authCallback.onFailure();
			}
		};

		AuthConfig.Builder digitsAuthConfigBuilder = new AuthConfig.Builder().withAuthCallBack(digitsAuthCallback);
		Digits.authenticate(digitsAuthConfigBuilder.build());

	}
}
