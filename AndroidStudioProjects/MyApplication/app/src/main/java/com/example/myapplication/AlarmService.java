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
        mediaplayer = MediaPlayer.create(this, R.raw.rooster_sound);
        mediaplayer.setLooping(true);
        mediaplayer.start();
        Log.d("MEDIA", "Alarm should be ringing now!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibrator = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator.getDefaultVibrator();
        } else {
            //Do it the other way
            //vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE);

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //set intent for alarm screen from notification
        Intent intent1 = new Intent(getApplicationContext(), AlarmScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, PendingIntent.FLAG_IMMUTABLE);

        //build Channel
        String description = "This is an alarm channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        String CHANNEL_ID = "DefaultId";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription(description);
        }
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

        //notify
        NotificationManagerCompat.from(this).notify(1, builder.build());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaplayer.stop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibrator.cancel();
        }
        else {
            //do the other way
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
