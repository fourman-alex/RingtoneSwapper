package com.myexample.ringtoneswap;


import com.digits.sdk.android.Digits;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

public class ApplicationInst
		extends android.app.Application {

	private static final String TWITTER_KEY    = "G46BtHLQIlx16buRCBDaEU4kE";
	private static final String TWITTER_SECRET = "C21g5ncGknr2VOJbFKTFLi3VhqGAQ5xuxnutc6SdZnKkU5Nwtk";

	@Override
	public void onCreate() {
		super.onCreate();

		//init digits
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().withTheme(R.style.AppTheme)
		                                                                   .build());
	}


}
