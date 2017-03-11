package com.iclone.alarmchallenge;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;

public class Countdown extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);
        textView = (TextView) findViewById(R.id.timer_text1);
        Bundle extras = getIntent().getExtras();
        int timer = 0;
        if (extras != null) {
            timer = extras.getInt("Timer");
            // and get whatever type user account id is
        }
        textView.setText(timer+"");
        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                textView.setText(millisUntilFinished/1000+"");
            }

            public void onFinish() {
                textView.setText("Done!");
            }
        }.start();
    }
}
