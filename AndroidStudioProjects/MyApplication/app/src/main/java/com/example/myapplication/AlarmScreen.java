package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibratorManager;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmScreen extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_screen_activity);

        MediaPlayer mediaplayer = new MediaPlayer();
        VibratorManager vibrator = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        Button stop_alarm = (Button) findViewById(R.id.stop_sound);
        stop_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(AlarmScreen.this,AlarmService.class));
                Intent myint = new Intent(AlarmScreen.this, MainActivity.class);
                startActivity(myint);
            }
        });

    }
}
