package com.flashlight.remote;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.*;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        com.google.firebase.database.FirebaseDatabase.getInstance()
                                .getReference("token")
                                .setValue(token);
                    }
                });

        PeriodicWorkRequest keepAlive = new PeriodicWorkRequest.Builder(
                KeepAliveWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "keepAlive",
                ExistingPeriodicWorkPolicy.REPLACE,
                keepAlive
        );

        finish();
    }
}
