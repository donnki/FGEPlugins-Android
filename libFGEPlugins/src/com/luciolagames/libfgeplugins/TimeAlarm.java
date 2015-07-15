package com.luciolagames.libfgeplugins;

import com.luciolagames.libfgeplugins.R;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class TimeAlarm extends BroadcastReceiver {

     NotificationManager nm;

     @Override
     public void onReceive(Context context, Intent intent) {
         nm = (NotificationManager) context
             .getSystemService(Context.NOTIFICATION_SERVICE);
         String  from = intent.getStringExtra("from");
         String  message = intent.getStringExtra("message");
         
         
         PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
             new Intent(context, PluginManager.invokeClass), 0);
         Notification notif = new Notification(R.drawable.ic_launcher,
        		 message, System.currentTimeMillis());
         notif.setLatestEventInfo(context, from, message, contentIntent);
         nm.notify(1, notif);
    }
}