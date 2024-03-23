package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CombinedVibration;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.VibratorManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmService extends Service {
    private MediaPlayer mediaplayer;
    private VibratorManager vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaplayer = MediaPlayer.create(this, R.raw.queen_sound);
        mediaplayer.setLooping(true);
        mediaplayer.start();
        Log.d("MEDIA", "Alarm should be ringing now!");

        vibrator = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        //Do it the other way
        vibrator.vibrate(CombinedVibration.createParallel(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)));
        vibrator.getDefaultVibrator();

        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Log.d("CATS", "Notifs are enabled");
        } else {
            Log.d("CATS", "Notifs are not enabled");
        }

        notify_user();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

      notify_user();

        return START_STICKY;
    }

    public void notify_user() {

        //set intent for alarm screen from notification
        Intent intent1 = new Intent(getApplicationContext(), AlarmScreen.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, PendingIntent.FLAG_IMMUTABLE);

        //build Channel
        String description = "This is an alarm channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        String CHANNEL_ID = "DefaultId";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
        channel.setDescription(description);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ERROR", "Permission not granted");
        }
        //NotificationManager notificationManager = this.getSystemService(NotificationManager.class);

        //build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.alarm_icon)
                .setContentTitle("Alarm is Ringing!")
                .setContentText("Click to Stop Alarm Sound!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaplayer.stop();
        vibrator.cancel();


    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
