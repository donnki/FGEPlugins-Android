package com.luciolagames.libfgeplugins;

//import org.cocos2dx.lib.Cocos2dxActivity;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class PluginManager {
	public static final String TAG = "FGEPlugins";
	public static Class invokeClass;
	
	protected GooglePlayIABPlugin mIABPlugin = null;
	protected GooglePlayGameServicePlugin mGameServicePlugin = null;
	protected GoogleGAStatisticPlugin mGAPlugin = null;
	protected AdmobPlugin mAdmobPlugin = null;
	protected CommonHelper mHelper = null;
	protected TalkingGameStatisticPlugin mStatisticPlugin = null;
	protected GoogleTagManagerPlugin mTagManagerPlugin = null;
	
	public PluginManager(Activity context, Class cls) {
		invokeClass = cls;
		
		if(context.getString(R.string.commonEnabled).equalsIgnoreCase("true")){
			Log.d(TAG, "init CommonHelper");
			mHelper = new CommonHelper(context);
		}
		if(context.getString(R.string.gaEnabled).equalsIgnoreCase("true")){
			Log.d(TAG, "init GoogleGAStatisticPlugin");
			mGAPlugin = new GoogleGAStatisticPlugin(context);
		}
		
		if(context.getString(R.string.iabEnabled).equalsIgnoreCase("true")){
			Log.d(TAG, "init GooglePlayIABPlugin");
			mIABPlugin = new GooglePlayIABPlugin(context);
		}
		
		if(context.getString(R.string.gameServiceEnabled).equalsIgnoreCase("true")){
			Log.d(TAG, "init GooglePlayGameServicePlugin");
			mGameServicePlugin = new GooglePlayGameServicePlugin(context);
		}
		
		if(context.getString(R.string.admobEnabled).equalsIgnoreCase("true")){
			Log.d(TAG, "init AdmobPlugin");
			mAdmobPlugin = new AdmobPlugin(context);
		}
		
		if(context.getString(R.string.talkingdataEnabled).equalsIgnoreCase("true")){
			Log.d(TAG, "init TalkingGameStatisticPlugin");
			mStatisticPlugin = new TalkingGameStatisticPlugin(context);
		}
		
		if(context.getString(R.string.tagManagerEnabled).equalsIgnoreCase("true")){
			Log.d(TAG, "init GoogleTagManagerPlugin");
			mTagManagerPlugin = new GoogleTagManagerPlugin(context);
		}
	}
	
	
	public GooglePlayIABPlugin getIABPlugin() {
		return mIABPlugin;
	}

	public void setIABPlugin(GooglePlayIABPlugin mIABPlugin) {
		this.mIABPlugin = mIABPlugin;
	}

	public GooglePlayGameServicePlugin getGameServicePlugin() {
		return mGameServicePlugin;
	}

	public void setGameServicePlugin(GooglePlayGameServicePlugin mGameServicePlugin) {
		this.mGameServicePlugin = mGameServicePlugin;
	}

	public AdmobPlugin getAdmobPlugin() {
		return mAdmobPlugin;
	}

	public void setAdmobPlugin(AdmobPlugin mAdmobPlugin) {
		this.mAdmobPlugin = mAdmobPlugin;
	}

	public CommonHelper getHelper() {
		return mHelper;
	}

	public void setHelper(CommonHelper mHelper) {
		this.mHelper = mHelper;
	}

	public TalkingGameStatisticPlugin getStatisticPlugin() {
		return mStatisticPlugin;
	}

	public void setStatisticPlugin(TalkingGameStatisticPlugin mStatisticPlugin) {
		this.mStatisticPlugin = mStatisticPlugin;
	}
	
	public GoogleTagManagerPlugin getTagManagerPlugin() {
		return mTagManagerPlugin;
	}

	public void setStatisticPlugin(GoogleTagManagerPlugin mTagManagerPlugin) {
		this.mTagManagerPlugin = mTagManagerPlugin;
	}

    public void onStart() {
        if(mGameServicePlugin != null){
        	mGameServicePlugin.onStart();
        }

    }

    public void onStop() {
        if(mGameServicePlugin != null){
        	mGameServicePlugin.onStop();
        }
    }
    
    public void onDestroy() {
        if (mIABPlugin != null){
        	mIABPlugin.onDestroy(); 
        }
        if (mGAPlugin != null){
        	mGAPlugin.onDestroy();
        }
    }
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (mGameServicePlugin != null){
        	mGameServicePlugin.onActivityResult(requestCode, resultCode, intent);
        }
        if (mIABPlugin != null){
        	mIABPlugin.onActivityResult(requestCode, resultCode, intent);
        }
    }
    
    public void onResume(){
		if(mStatisticPlugin != null){
			mStatisticPlugin.onResume();
		}
	}
	
    public void onPause(){
		if(mStatisticPlugin != null){
			mStatisticPlugin.onPause();
		}
	}
    
    
    public void onRestart(){
    	
    }
}
