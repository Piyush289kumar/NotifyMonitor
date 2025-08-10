package com.example.notifymonitor;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationListenerService extends android.service.notification.NotificationListenerService {

    private final OkHttpClient client = new OkHttpClient();
    private static final String TAG = "NotifyMonitor";
    private static final String API_URL = "http://192.168.29.175:8000/api/notifications";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String packageName = sbn.getPackageName();
            Bundle extras = sbn.getNotification().extras;

            // Get app label
            PackageManager pm = getPackageManager();
            String appLabel = "";
            String appVersion = "";
            try {
                appLabel = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
                appVersion = pm.getPackageInfo(packageName, 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                appLabel = packageName;
                appVersion = "unknown";
            }

            // Get notification text fields
            String title = extras.getString("android.title", "");
            String text = extras.getString("android.text", "");
            CharSequence[] textLines = extras.getCharSequenceArray("android.textLines");
            String bigText = extras.getString("android.bigText", "");

            if (TextUtils.isEmpty(text) && textLines != null) {
                text = TextUtils.join("\n", textLines);
            }
            if (TextUtils.isEmpty(text) && !TextUtils.isEmpty(bigText)) {
                text = bigText;
            }

            // Get message type from category if available
            String messageType = "text";
            if (sbn.getNotification().category != null) {
                messageType = sbn.getNotification().category;
            }

            // Device & OS info
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceName = Build.MODEL;
            String osVersion = Build.VERSION.RELEASE;

            // Time formatting
            String deliveredAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date(sbn.getPostTime()));

            // Build JSON payload for Laravel
            JsonObject json = new JsonObject();

            // App info
            json.addProperty("app_name", appLabel);
            json.addProperty("app_identifier", packageName);
            json.addProperty("app_version", appVersion);

            // Content
            json.addProperty("title", title);
            json.addProperty("message", text);
            json.addProperty("message_type", messageType);

            // Status
            json.addProperty("delivered_at", deliveredAt);

            // Device info
            json.addProperty("device_id", deviceId);
            json.addProperty("device_name", deviceName);
            json.addProperty("os_version", osVersion);

            // Raw payload
            JsonObject raw = new JsonObject();
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                raw.addProperty(key, String.valueOf(value));
            }
            json.add("raw_payload", raw);

            // Debug log
            Log.d(TAG, "Sending to server: " + json.toString());

            // Send to Laravel API
            sendNotificationToServer(json.toString());

        } catch (Exception e) {
            Log.e(TAG, "Error processing notification: " + e.getMessage(), e);
        }
    }

    private void sendNotificationToServer(String jsonBody) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    Log.d(TAG, "Server response: " + response.body().string());
                } else {
                    Log.d(TAG, "Server response: empty body");
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to send notification: " + e.getMessage(), e);
            }
        }).start();
    }
}
