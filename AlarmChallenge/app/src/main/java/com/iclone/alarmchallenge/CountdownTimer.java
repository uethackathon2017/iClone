package com.iclone.alarmchallenge;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;

public class CountdownTimer extends AppCompatActivity {
//    SeekArc seekArc;
//    TextView textView;
//
//    public CountdownTimer(int timer, int i) {
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_timer);
        /*
        Bundle bundle = getIntent().getExtras();
        final int timer = bundle.getInt("timer");
        textView = (TextView) findViewById(R.id.timer_text);
        seekArc = (SeekArc) findViewById(R.id.seekArc);
        seekArc.setProgress(timer);
        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                int temp = 60*i/100;
                textView.setText("" + temp);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });
        new CountDownTimer(timer*600, 0) {

            public void onTick(long millisUntilFinished) {
                int temp = seekArc.getProgress();
                seekArc.setProgress(temp - 1);
            }

            public void onFinish() {
                textView.setText("Done!");
            }
        }.start();
        */
    }
}
