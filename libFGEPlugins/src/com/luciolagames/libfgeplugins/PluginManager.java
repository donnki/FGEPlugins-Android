package com.luciolagames.libfgeplugins;

import org.cocos2dx.lib.Cocos2dxActivity;

import android.content.Intent;

public class PluginManager {
	private static String TAG = "LuciolaGames";
	public static Class invokeClass;
	
	protected GooglePlayIABPlugin mIABPlugin = null;
	protected GooglePlayGameServicePlugin mGameServicePlugin = null;
	protected AdmobPlugin mAdmobPlugin = null;
	protected CommonHelper mHelper = null;
	
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


	protected TalkingGameStatisticPlugin mStatisticPlugin = null;
	
	public PluginManager(Cocos2dxActivity context, Class cls) {
		invokeClass = cls;
		mHelper = new CommonHelper(context);
		mIABPlugin = new GooglePlayIABPlugin(context);
		mGameServicePlugin = new GooglePlayGameServicePlugin(context);
		mAdmobPlugin = new AdmobPlugin(context);
		mStatisticPlugin = new TalkingGameStatisticPlugin(context);
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
