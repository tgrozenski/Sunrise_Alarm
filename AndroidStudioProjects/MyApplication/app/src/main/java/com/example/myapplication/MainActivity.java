package com.example.myapplication;

import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    double longe;
    int hour = 0;
    int minute = 0;
    int alarm_minute = 0;
    int alarm_hour = 0;
    long unix;
    boolean alarm_state = false;
    double lat;
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get permission for notification
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        //check permissions
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                        .RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_COARSE_LOCATION,false);
                    if (fineLocationGranted != null && fineLocationGranted) {
                        Log.d("LOCATION", "Precise location access granted");
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        Log.d("LOCATION", "Approximate location access granted");                            }
                    else {
                        Log.d("LOCATION", "No Location access granted");
                    }
                        }
                );
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
        get_location();

        //Timepickers
        NumberPicker before_picker = findViewById(R.id.before_selector);
        before_picker.setMinValue(0);
        before_picker.setMaxValue(30);

        NumberPicker after_picker = findViewById(R.id.after_selector);
        after_picker.setMinValue(0);
        after_picker.setMaxValue(30);

        //apply and reset buttons
        Button apply = findViewById(R.id.apply_button);
        TextView alarm_time = findViewById(R.id.alarm_time);

        apply.setOnClickListener(view -> {
            get_numberpicker();
            String text;
            if (alarm_minute >= 10) {
                text = alarm_hour + ":" + alarm_minute;
                }
            else {
                text = alarm_hour + ":" + "0" + alarm_minute;
                }
            alarm_time.setText(text);
            Log.d("BUTTON", "Applied" + alarm_hour + ":" + alarm_minute);
            unix = time_to_mili(alarm_hour, alarm_minute) + System.currentTimeMillis();
            Log.d("TIME", "unix: " + unix);

            if (alarm_state) {
                Toast.makeText(MainActivity.this, "Alarm set for: " + text, Toast.LENGTH_LONG).show();
                //send broadcast
                Intent broadcast = new Intent(MainActivity.this, AlarmBroadcastReceiver.class);
                broadcast.putExtra("unix", unix);
                broadcast.putExtra("alarm_state", alarm_state);
                sendBroadcast(broadcast);
                }

        });

        Button reset = findViewById(R.id.reset_button);
        reset.setOnClickListener(view -> {
            alarm_time.setText("0:00");
            alarm_minute = minute;
            alarm_hour = hour;
            unix = time_to_mili(alarm_hour, alarm_minute) + System.currentTimeMillis();
            Intent broadcast = new Intent(MainActivity.this, AlarmBroadcastReceiver.class);
            broadcast.putExtra("unix", unix);
            broadcast.putExtra("alarm_state", alarm_state);
            sendBroadcast(broadcast);
            Log.d("TIME", "Unix: " + unix);
        });


        //get location
        Button location_button = findViewById(R.id.location_button);
        location_button.setOnClickListener(view -> get_location());

        //get alarm_state
        androidx.appcompat.widget.SwitchCompat on_off =  findViewById(R.id.switch1);
        on_off.setOnClickListener(view -> {
             Intent broadcast = new Intent(MainActivity.this, AlarmBroadcastReceiver.class);
            if (on_off.isChecked()) {
                Toast.makeText(MainActivity.this, "Alarm is enabled for " + alarm_hour + ":" + alarm_minute, Toast.LENGTH_LONG).show();
                alarm_state = true;
                broadcast.putExtra("alarm_state", alarm_state);
                broadcast.putExtra("unix", unix);
            } else {
                Toast.makeText(MainActivity.this, "Alarm cancelled for " + alarm_hour + ":" + alarm_minute, Toast.LENGTH_LONG).show();
                alarm_state = false;
                broadcast.putExtra("alarm_state", alarm_state);
                broadcast.putExtra("unix", unix);
            }
            sendBroadcast(broadcast);
        });

        //check for location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            longe = location.getLongitude();
                            lat = location.getLatitude();
                            Log.d("LOCATION", "Location:" + lat + ":" + longe);
                        }
                        else {
                            Log.d("LOCATION", "Location is null");
                            Toast.makeText(getApplicationContext(), "No location found now, open Maps to ensure location services are enabled", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        Button button = findViewById(R.id.test_alarm);
        button.setOnClickListener(v -> {
            Intent intent1 = new Intent(MainActivity.this, AlarmService.class);
            startService(intent1);
        });
    }

    public void get_location() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.d("LOCATION", "Location:" + location);

                            //generate a sunset
                            String longitude = location.getLongitude() + "";
                            String lattitude = location.getLatitude() + "";
                            com.luckycatlabs.sunrisesunset.dto.Location sunrise_location = new com.luckycatlabs.sunrisesunset.dto.Location(lattitude, longitude);
                            TimeZone timezone = TimeZone.getDefault();
                            SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(sunrise_location, timezone);

                            //try to set tomorrow's date
                            Date dt = new Date();
                            Calendar c = Calendar.getInstance(timezone);
                            c.setTime(dt);
                            c.add(Calendar.DATE, 1);
                            dt = c.getTime();
                            c.setTime(dt);
                            String officialSunrise = calculator.getOfficialSunriseForDate(c);
                            Log.d("CALENDAR", c.getTime() + "");
                            Log.d("LOCATION",timezone + ":" + officialSunrise);

                            //Display Ui
                            TextView sunrise_time = findViewById(R.id.sunrise_time);
                            sunrise_time.setText(officialSunrise);
                            TextView timezone_display = findViewById(R.id.time_zone);
                            timezone_display.setText(timezone.getDisplayName());

                            //Set the global variables correctly
                            string_to_time(officialSunrise);
                            long alarm_mili =  time_to_mili(hour, minute);
                            unix = time_to_mili(hour, minute) + System.currentTimeMillis();
                            Log.d("TIME", hour + ":" + minute + ", " + alarm_mili);

                        }
                        else {
                            Log.d("LOCATION", "Location is null");

                        }
                    }
                });
    }
    protected void onRestart() {
        super.onRestart();

        Log.d("RESTART", "restart has been called " + System.currentTimeMillis());
        Button apply = findViewById(R.id.apply_button);
        //check if past alarm has gone off
        if (System.currentTimeMillis() > unix) {
            Log.d("RESTART", "Time has passed the previous alarm Current Time: " + System.currentTimeMillis());
            get_location();
            apply.performClick();
        }
        else {
            apply.performClick();
        }
    }

    protected void onStop() {
        super.onStop();
        Log.d("LIFECYCLE", "Onstop is called");
        Intent intent = new Intent(MainActivity.this, NotificationService.class);
        startService(intent);
    }

    //could be rewritten as a case switch
    public void string_to_time(String officialSunrise) {
        char[] arr = officialSunrise.toCharArray();

        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                hour += Character.getNumericValue(arr[i]) * 10;
            }
            else {
                hour += Character.getNumericValue(arr[i]);
            }
            alarm_hour = hour;
        }
        for (int i = 3; i < 5; i++) {
            if (i == 3) {
                minute += Character.getNumericValue(arr[i]) * 10;
            }
            else {
                minute += Character.getNumericValue(arr[i]);
            }
            alarm_minute = minute;
        }

    }
    public static long time_to_mili(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        long current = ((calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE)) * 60000;
        long timeinmil = ((long) hour * 60 + (long) minute) * 60000;
        Log.d("TIME", timeinmil + "<-time in milliseconds");
        Log.d("TIME", current + "<-current time");
        if (timeinmil > current) {
            return timeinmil - current;
        }
        else {
            return ((24 * 60) * 60000) - (current - timeinmil);
        }

    }

    public void get_numberpicker() {
        NumberPicker before_picker = findViewById(R.id.before_selector);
        if (before_picker.getValue()!=0) {
            alarm_minute -= before_picker.getValue();
            if (alarm_minute < 0) {
                alarm_minute = alarm_minute + 60;
                alarm_hour-=1;
            }
        }

        NumberPicker after_picker = findViewById(R.id.after_selector);
        if (after_picker.getValue()!=0) {
            alarm_minute += after_picker.getValue();
            if (alarm_minute > 59) {
                alarm_minute = alarm_minute - 60;
                alarm_hour+=1;
            }
        }
    }
}
