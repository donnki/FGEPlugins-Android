package com.luciolagames.libfgeplugins;


import android.app.Activity;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class AdmobPlugin {
//	public static final String TAG = "GAME";
	
//	private static final String ADMOB_INTERSTITIAL = "ca-app-pub-2550536070025080/1660836858";
	
	protected static AdmobPlugin sInstance;
	
	
	protected Activity context;
	protected InterstitialAd mInterstitialAd;
	
	
	public AdmobPlugin(Activity activity) {
		this.context = activity;
		sInstance = this;
		init();
	}
	
	private void init(){
		mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(context.getString(R.string.admobInterstitialID));
        mInterstitialAd.setAdListener(new AdListener(){
        	@Override 
        	public void onAdClosed(){
        		Log.d(PluginManager.TAG, "[AdmobPlugin] on InterstitialAd Closed");
        		requestNewInterstitial();
        	}
        	
        	@Override
        	public void onAdOpened(){
        		Log.d(PluginManager.TAG, "[AdmobPlugin] on InterstitialAd opened");
        	}
        	
        	@Override
        	public void onAdLoaded(){
        		Log.d(PluginManager.TAG, "[AdmobPlugin] on InterstitialAd Loaded");
        	}
        	
        	@Override
        	public void onAdFailedToLoad(int result){
        		Log.d(PluginManager.TAG, "[AdmobPlugin] on InterstitialAd FailedToLoad");
        	}
        });
        requestNewInterstitial();
	}
	
	private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }
	
	protected void onShowInterstiticalAD(){
		context.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Log.d(PluginManager.TAG, "[AdmobPlugin] Show InterstitialAD, isLoaded: " + mInterstitialAd.isLoaded());
				if (mInterstitialAd.isLoaded()) {
		            mInterstitialAd.show();
		        } else {
		        	Log.d(PluginManager.TAG, "[AdmobPlugin] InterstiticalAD not loaded yet");
		        }
			}
		});
		
	}
	
	public static void showInterstitialAD(){
		sInstance.onShowInterstiticalAD();
		Log.d(PluginManager.TAG, "[AdmobPlugin] Show InterstitialAD, end");
	}
}
