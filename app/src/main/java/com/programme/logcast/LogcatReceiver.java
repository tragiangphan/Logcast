package com.programme.logcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LogcatReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Kiểm tra sự kiện là khi ứng dụng được cài đặt
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            // Bắt đầu Foreground Service
            Intent serviceIntent = new Intent(context, LogcatService.class);
            context.startForegroundService(serviceIntent);
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }
}