package com.iclone.alarmchallenge;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.triggertrap.seekarc.SeekArc;

public class TimerActivity extends AppCompatActivity {
    SeekArc seekArc;
    TextView timer;
    Button start;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        seekArc = (SeekArc) findViewById(R.id.seekArc);
        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                if (b) {
                    int temp = 60 * i / 100;
                    timer.setText("" + temp);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                timer.setText("Start");
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });
        timer = (TextView) findViewById(R.id.timer_text);
        start = (Button) findViewById(R.id.button_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = timer.getText().toString();
                final int temp = Integer.parseInt(s);
                new CountDownTimer(temp*1000, 1) {
                    public void onTick(long millisUntilFinished) {
                        timer.setText(millisUntilFinished/1000+"");
                        seekArc.setTouchInSide(false);
                    }
                    public void onFinish() {
                        timer.setText("Done!");
                    }
                }.start();
                seekArc.setTouchInSide(true);
            }
        });
    }
}
