package com.programme.logcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LogcatReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Kiểm tra sự kiện là khi ứng dụng được cài đặt
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            System.out.println(intent.getAction());
            // Bắt đầu Foreground Service
            Intent serviceIntent = new Intent(context, LogcatService.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            context.startForegroundService(serviceIntent);
        }
//        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
//            Intent activityIntent = new Intent(context, MainActivity.class);
//            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//            context.startActivity(activityIntent);
//        }
    }
}