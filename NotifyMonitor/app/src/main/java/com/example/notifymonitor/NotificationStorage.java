package com.example.notifymonitor;

import java.util.ArrayList;
import java.util.List;

public class NotificationStorage {
    private static final List<String> notifications = new ArrayList<>();

    public static void add(String notification) {
        notifications.add(0, notification); // newest first
    }

    public static List<String> getAll() {
        return new ArrayList<>(notifications);
    }
}
