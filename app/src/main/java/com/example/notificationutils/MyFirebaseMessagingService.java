
package com.example.notificationutils;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = "MsgFirebaseService";
    public static final String TITLE = "title";
    public static final String DES = "description";
    public static final String URL = "url";
    public static final String TITLE_COLOR = "titleColor";
    public static final String DES_COLOR = "descriptionColor";


    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        try {
            Log.i("notification", "onMessageReceived:  called");

            if (NotificationUtil.isAppRunning(this.getApplicationContext())) return;

            if (remoteMessage.getNotification() != null) {
                Log.i("notification", "onMessageReceived: " + "getDefaultNotificationMethod called");
                NotificationUtil.getInstance().getDefaultNotification(this.getApplicationContext(), remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
            } else {
                Log.i("notification", "onMessageReceived:  getDataMethod called");
                final Map<String, String> params = (Map<String, String>) remoteMessage.getData();
                final JSONObject object = new JSONObject((Map) params);
                final String title = object.isNull("title") ? "" : object.getString("title");
                final String description = object.isNull("description") ? "" : object.getString("description");
                final String titleColor = object.isNull("titleColor") ? "" : object.getString("titleColor");
                final String descriptionColor = object.isNull("descriptionColor") ? "" : object.getString("descriptionColor");
                final String url = object.isNull("url") ? "" : object.getString("url");

                if (!TextUtils.isEmpty((CharSequence) title) && !TextUtils.isEmpty((CharSequence) description)) {
                    NotificationUtil.getInstance().getNotification(this.getApplicationContext(), title, description, url, titleColor, descriptionColor, remoteMessage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
