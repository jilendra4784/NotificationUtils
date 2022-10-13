package com.example.notificationutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DismissNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("notification", " notification cleared ");
       NotificationUtil.handleMediaPlayer(context,false,0);
    }
}
