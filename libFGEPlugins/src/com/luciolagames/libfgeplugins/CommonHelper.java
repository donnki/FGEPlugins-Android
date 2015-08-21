package com.luciolagames.libfgeplugins;

import java.util.Calendar;
import java.util.UUID;



import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CommonHelper {
	//	private static final String TAG = "GAME";
	private static CommonHelper sInstance;
	private static final int notifyID = 1001;
	
	private Activity context;
	
	public CommonHelper(Activity activity) {
		this.context = activity;
		sInstance = this;
	}
	

	/**
	 * 取本机唯一标识UDID 返回String
	 * */
	public static String getUDID(int i) {
		final TelephonyManager tm = (TelephonyManager) sInstance.context.getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice, tmSerial, tmPhone, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
				+ android.provider.Settings.Secure.getString(
						sInstance.context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
		UUID deviceUuid = new UUID(androidId.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String uniqueId = deviceUuid.toString();
		Log.d(PluginManager.TAG, "[CommonHelper] uuid=" + uniqueId);
		return uniqueId;
	}
	
	
	public static void addNotification(String title, String detail, int sec){
		Log.i(PluginManager.TAG, "[CommonHelper] addNotification: " + title + ", detail: " + detail + ", delayTime: " + sec);
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTimeInMillis(System.currentTimeMillis());
		 // Add defined amount of days to the date
		 calendar.add(Calendar.SECOND, sec);

		 // Retrieve alarm manager from the system
		 AlarmManager alarmManager = (AlarmManager)sInstance.context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		 // Every scheduled intent needs a different ID, else it is just executed once

		 // Prepare the intent which should be launched at the date
		 Intent intent = new Intent(sInstance.context, TimeAlarm.class);
		 intent.putExtra("from", title);
		 intent.putExtra("message", detail);

		 // Prepare the pending intent
		 PendingIntent pendingIntent = PendingIntent.getBroadcast(sInstance.context.getApplicationContext(), notifyID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		 // Register the alert in the system. You have the option to define if the device has to wake up on the alert or not
		 alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	}
	
}
