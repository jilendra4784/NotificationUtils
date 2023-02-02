// 
// Decompiled by Procyon v0.5.36
// 

package com.example.notificationutils;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationUtil {
    private static final NotificationUtil ourInstance;
    static NotificationManager mManager;
    static MediaPlayer mediaPlayer = null;
    Notification customNotification;
    public static final int NOTIFICATION_ID=(int) System.currentTimeMillis();

    static NotificationUtil getInstance() {
        return NotificationUtil.ourInstance;
    }
    private boolean isValidColor(final String color) {
        return !TextUtils.isEmpty((CharSequence) color);
    }

    void getNotification(final Context context, final String title, final String desc, final String url, final String titleColor, final String desColor, RemoteMessage remoteMessage) {

        Bitmap bitmap;
        bitmap = ImageCacheManager.getInstance().getBitmapFromCache(url);
        if (bitmap != null) {
            createFullScreenNotification(context, title, desc, url, titleColor, desColor, bitmap);
        } else {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                try {
                    URL imgURL = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) imgURL.openConnection();
                    con.setDoInput(true);
                    con.connect();
                    int responseCode;
                    InputStream in;
                    Bitmap bmp = null;
                    responseCode = con.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.i("notification", "getNotification: " + HttpURLConnection.HTTP_OK);
                        in = con.getInputStream();
                        bmp = BitmapFactory.decodeStream(in);
                        in.close();
                        ImageCacheManager.getInstance().saveBitmapToCahche(url, bmp);
                        Bitmap finalBmp = bmp;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                createFullScreenNotification(context, title, desc, url, titleColor, desColor, finalBmp);
                            }
                        });

                    } else {
                        Log.i("notification", "image load failed: ");
                        createFullScreenNotification(context, title, desc, url, titleColor, desColor, null);
                    }

                } catch (Exception ex) {
                    Log.e("Exception", ex.toString());
                    createFullScreenNotification(context, title, desc, url, titleColor, desColor, null);
                }
            });
        }
    }

    void getDefaultNotification(final Context context, final String title, final String desc) {
        final String DEFAULT_NOTIFICATION_CHANNEL = context.getPackageName();
        this.createChannels(context, DEFAULT_NOTIFICATION_CHANNEL, "");
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.putExtra("PUSH_KEY", "0");
        final PendingIntent pendingIntent;

        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        final Notification customNotification = new NotificationCompat.Builder(context, DEFAULT_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle((CharSequence) title)
                .setAutoCancel(true)
                .setContentText((CharSequence) desc)
                .setContentIntent(pendingIntent).build();

        final int NOTIFICATION_ID = (int) System.currentTimeMillis();
        getManager(context).notify(NOTIFICATION_ID, customNotification);
    }

    public static NotificationManager getManager(final Context mContext) {
        if (mManager == null) {
            mManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }


    static {
        ourInstance = new NotificationUtil();
    }

    private void createFullScreenNotification(final Context context, String title, final String desc, final String url, final String titleColor, final String desColor, Bitmap bitmap) {
        String mStatus="";
        if(title.contains("|")){
            String[] mData =title.split("\\|");
            title=mData[0];
            mStatus=mData[1];
        }else {
            mStatus="0";
        }

        final String DEFAULT_NOTIFICATION_CHANNEL = context.getPackageName();
        this.createChannels(context, DEFAULT_NOTIFICATION_CHANNEL, desc);
        final Intent intent = new Intent(context, SPCBridge.class);
        intent.putExtra("PUSH_TITLE", title);
        intent.putExtra("PUSH_DES", desc);
        intent.putExtra("PUSH_IMAGE_URL", url);
        intent.putExtra("STATUS", mStatus);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        final PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.notification);

        remoteView.setTextViewText(R.id.notification_title, (CharSequence) title);
       // remoteView.setTextViewText(R.id.notification_desc, (CharSequence) desc);
        if (this.isValidColor(titleColor)) {
            remoteView.setTextColor(R.id.notification_title, Color.parseColor("#" + titleColor));
        }
        if (this.isValidColor(desColor)) {
            remoteView.setTextColor(R.id.notification_desc, Color.parseColor("#" + desColor));
        }

        remoteView.setImageViewResource(R.id.imvIcon, R.mipmap.icon);

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.bg);
        }
        try {
            if (desc.contains("simple_bg")) {
                remoteView.setInt(R.id.llyNotification, "setBackgroundResource", R.mipmap.bg);
                customNotification = new NotificationCompat.Builder(context, DEFAULT_NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.mipmap.icon)
                        .setContentTitle(title)
                        .setContentText(desc)
                        .setCustomContentView(remoteView)
                        .setContentIntent(pendingIntent)
                        .setDeleteIntent(createOnDismissedIntent(context, NOTIFICATION_ID))
                        .setAutoCancel(true)
                        .build();
            } else {
                handleMediaPlayer(context, true,-1);

                Log.i("notification", "SDK_VERSION: " + Build.VERSION.SDK_INT);
                if (Build.VERSION.SDK_INT >= 31) {

                    remoteView.setInt(R.id.llyNotification, "setBackgroundResource", R.mipmap.bg);
                    customNotification = new NotificationCompat.Builder(context, DEFAULT_NOTIFICATION_CHANNEL)
                            .setSmallIcon(R.mipmap.icon)
                            .setContentTitle(title)
                            .setContentText(desc)
                            .setFullScreenIntent(pendingIntent, true)
                            .setCustomHeadsUpContentView(remoteView)
                            .setDeleteIntent(createOnDismissedIntent(context, NOTIFICATION_ID))
                            .setAutoCancel(true)
                            .build();
                } else {
                    Log.i("notification", "in 30 1: ");
                    //remoteView.setImageViewBitmap(R.id.llyNotification, bitmap);
                    remoteView.setInt(R.id.llyNotification, "setBackgroundResource", R.mipmap.bg);
                    Log.i("notification", "in 30 2: ");
                    customNotification = new NotificationCompat.Builder(context, DEFAULT_NOTIFICATION_CHANNEL)
                            .setSmallIcon(R.mipmap.icon)
                            .setContentTitle(title)
                            .setContentText(desc)
                            .setCustomContentView(remoteView)
                            .setFullScreenIntent(pendingIntent, true)
                            .setDeleteIntent(createOnDismissedIntent(context, NOTIFICATION_ID))
                            .setAutoCancel(true)
                            .build();
                }
            }


        } catch (Exception ex) {
            Log.i("notification", "in Exception: ");
            //remoteView.setImageViewResource(R.id.llyNotification, R.mipmap.bg);
            remoteView.setInt(R.id.llyNotification, "setBackgroundResource", R.mipmap.bg);
            Log.i("notification", "in Exception 1: ");
            customNotification = new NotificationCompat.Builder(context, DEFAULT_NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.mipmap.icon)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .setCustomContentView(remoteView)
                    .setFullScreenIntent(pendingIntent, true)
                    .setDeleteIntent(createOnDismissedIntent(context, NOTIFICATION_ID))
                    .setAutoCancel(true)
                    .build();
            Log.i("notification", "EXception: " + ex.getStackTrace());

        }
        this.getManager(context).notify(NOTIFICATION_ID, customNotification);


    }

    public PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Log.e("notification", "Creating dismiss notification intent : ");
        Intent intent = new Intent(context, DismissNotificationReceiver.class);
        return PendingIntent.getBroadcast(context, notificationId, intent, FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createChannels(final Context context, final String DEFAULT_NOTIFICATION_CHANNEL, String desc) {
        final NotificationChannel androidChannel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            androidChannel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, (CharSequence) context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            androidChannel.setSound(null, null);
            androidChannel.enableVibration(true);
            this.getManager(context).createNotificationChannel(androidChannel);
        }
    }

    public static void handleMediaPlayer(Context context, boolean isSoundPlaying , int status) {
        Log.i("notification", "handleMediaPlayer:  isSoundPlaying :" + isSoundPlaying + "Status "+status);
        if (isSoundPlaying) {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                mediaPlayer = MediaPlayer.create(context, notification);
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                mediaPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isAppRunning(final Context context) {
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(context.getPackageName())) {
                Log.i("notification", "My App running in : Foreground " + context.getPackageName());
                return true;
            }
        }
        return false;
    }
}
