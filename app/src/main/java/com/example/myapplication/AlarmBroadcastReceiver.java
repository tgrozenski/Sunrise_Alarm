package com.example.myapplication;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    long unix;
    boolean alarm_state;

    public void onReceive(Context context, Intent intent) {

        Log.d("PEEPEE", "" + unix + " " + alarm_state);

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //Send a notification to inform user that app needs to be open again in order to use the alarm
            Intent myintent = new Intent(context.getApplicationContext(), NotificationService.class);
            context.startService(myintent);
        }
        else {

            unix = intent.getExtras().getLong("unix");
            alarm_state = intent.getExtras().getBoolean("alarm_state");

            Intent service = new Intent(context.getApplicationContext(), AlarmService.class);
            PendingIntent pendingintent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, service, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmmanager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            //cancel any previous alarm set
            alarmmanager.cancel(pendingintent);

            if (alarm_state) {
                Log.d("ALARM", "time to mili:" + unix + " Current Time" + System.currentTimeMillis());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmmanager.canScheduleExactAlarms()) {
                        alarmmanager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, unix, pendingintent);
                        Log.d("ALARM", "Alarm Has been Scheduled for: " + unix);
                    }
                    else {
                        Log.d("ERROR", "App does not have permission to schedule exact alarms");
                    }
                }
            }
            else {
                alarmmanager.cancel(pendingintent);
            }
        }

    }

}
