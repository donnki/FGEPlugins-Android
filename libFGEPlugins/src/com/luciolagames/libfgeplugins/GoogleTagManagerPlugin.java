package com.luciolagames.libfgeplugins;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.DataLayer;
import com.google.android.gms.tagmanager.TagManager;
import com.google.android.gms.tagmanager.Container.FunctionCallMacroCallback;
import com.google.android.gms.tagmanager.Container.FunctionCallTagCallback;


public class GoogleTagManagerPlugin {
	private static GoogleTagManagerPlugin sInstance;
	protected Activity context;

	public static void refresh() {
		ContainerHolderSingleton.getContainerHolder().refresh();
	}

	public static void pushEvent(String eventKey) {
		DataLayer dataLayer = TagManager.getInstance(sInstance.context)
				.getDataLayer();
		dataLayer.push("event", eventKey);
		Log.i(PluginManager.TAG, "[TagManagerPlugin] push event: " + eventKey);
	}

	public static void pushEventWithParam(String eventName,
			Map<String, Object> data) {
		DataLayer dataLayer = TagManager.getInstance(sInstance.context)
				.getDataLayer();
		dataLayer.pushEvent(eventName, data);
	}
	
	public void init(){
		TagManager tagManager = TagManager.getInstance(context);

        // Modify the log level of the logger to print out not only
        // warning and error messages, but also verbose, debug, info messages.
        tagManager.setVerboseLoggingEnabled(true);

        PendingResult<ContainerHolder> pending =
                tagManager.loadContainerPreferNonDefault(context.getString(R.string.tmContainerID),
                R.raw.ga_tag_data);

        // The onResult method will be called as soon as one of the following happens:
        //     1. a saved container is loaded
        //     2. if there is no saved container, a network container is loaded
        //     3. the request times out. The example below uses a constant to manage the timeout period.
        pending.setResultCallback(new ResultCallback<ContainerHolder>() {
            @Override
            public void onResult(ContainerHolder containerHolder) {
                ContainerHolderSingleton.setContainerHolder(containerHolder);
                Container container = containerHolder.getContainer();
                if (!containerHolder.getStatus().isSuccess()) {
                    Log.e(PluginManager.TAG, "[TagManagerPlugin] failure loading container");
//                    displayErrorToUser("loadError");
                    return;
                }
                Log.i(PluginManager.TAG, "[TagManagerPlugin] load container success, now registerFunctionCallMacroCallback");
                ContainerHolderSingleton.setContainerHolder(containerHolder);
                ContainerLoadedCallback.registerCallbacksForContainer(container);
                containerHolder.setContainerAvailableListener(new ContainerLoadedCallback());
                
            }
        }, 2, TimeUnit.SECONDS);
	}
	public GoogleTagManagerPlugin(Activity context){
		this.context = context;
		sInstance = this;
		init();
	}
	
	private static class ContainerLoadedCallback implements ContainerHolder.ContainerAvailableListener {
        @Override
        public void onContainerAvailable(ContainerHolder containerHolder, String containerVersion) {
            // We load each container when it becomes available.
            Container container = containerHolder.getContainer();
            registerCallbacksForContainer(container);
        }

        public static void registerCallbacksForContainer(Container container) {
            // Register two custom function call macros to the container.
            container.registerFunctionCallMacroCallback("increment", new CustomMacroCallback());
            container.registerFunctionCallMacroCallback("mod", new CustomMacroCallback());
            // Register a custom function call tag to the container.
            container.registerFunctionCallTagCallback("custom_tag", new CustomTagCallback());
            container.registerFunctionCallTagCallback("test_event_1", new CustomTagCallback());
            container.registerFunctionCallTagCallback("test_event_2", new CustomTagCallback());
        }
    }

    private static class CustomMacroCallback implements FunctionCallMacroCallback {
        private int numCalls;

        @Override
        public Object getValue(String name, Map<String, Object> parameters) {
            if ("increment".equals(name)) {
                return ++numCalls;
            } else if ("mod".equals(name)) {
                return (Long) parameters.get("key1") % Integer.valueOf((String) parameters.get("key2"));
            } else {
                throw new IllegalArgumentException("Custom macro name: " + name + " is not supported.");
            }
        }
    }

    private static class CustomTagCallback implements FunctionCallTagCallback {
        @Override
        public void execute(String tagName, Map<String, Object> parameters) {
            // The code for firing this custom tag.
            Log.i(PluginManager.TAG, "[TagManagerPlugin] Custom function call tag :" + tagName + " is fired.");
        }
    }

   
}

/**
 * Singleton to hold the GTM Container (since it should be only created once
 * per run of the app).
 */
class ContainerHolderSingleton {
    private static ContainerHolder containerHolder;

    /**
     * Utility class; don't instantiate.
     */
    private ContainerHolderSingleton() {
    }

    public static ContainerHolder getContainerHolder() {
        return containerHolder;
    }

    public static void setContainerHolder(ContainerHolder c) {
        containerHolder = c;
    }
}
