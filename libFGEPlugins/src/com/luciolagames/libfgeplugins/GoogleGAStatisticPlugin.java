package com.luciolagames.libfgeplugins;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.Container.FunctionCallMacroCallback;
import com.google.android.gms.tagmanager.Container.FunctionCallTagCallback;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;

public class GoogleGAStatisticPlugin {
	private Activity context;
	
	public GoogleGAStatisticPlugin(Activity context){
		this.context = context; 
		Tracker tracker = getTracker();
		Log.d(PluginManager.TAG, "[GAPlugin] GoogleGAStatisticPlugin init.");
        // All subsequent hits will be send with screen name = "main screen"
        tracker.setScreenName("main_game");

        tracker.send(new HitBuilders.EventBuilder()
               .setCategory("game")
               .setAction("onCreate")
               .setLabel("init")
               .build());
        

	}
	
	public synchronized Tracker getTracker() {
	      try {
	          final GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(context);
	          return googleAnalytics.newTracker(R.xml.analytics_global_config);
	      }catch(final Exception e){
	          Log.e(PluginManager.TAG, "[GAPlugin] Failed to initialize Google Analytics V4");
	      }

	      return null;
	 }
	
	public void onDestroy(){
		Log.d(PluginManager.TAG, "[GAPlugin] Destroying helper.");
		Tracker tracker = getTracker();

        // All subsequent hits will be send with screen name = "main screen"
        tracker.setScreenName("main_game");

        tracker.send(new HitBuilders.EventBuilder()
               .setCategory("game")
               .setAction("onDestroy")
               .setLabel("destroy")
               .build());
	}
	
	
}
