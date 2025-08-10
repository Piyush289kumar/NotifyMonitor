package com.example.notifymonitor;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // We'll create this layout next

        Button btnGrantAccess = findViewById(R.id.btnGrantAccess);

        btnGrantAccess.setOnClickListener(v -> {
            // Open Notification Listener Settings so user can enable our service
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });
    }
}
