package com.iClone.AlarmChallenge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MathActivity extends AppCompatActivity {
    public int level;
    public int counter = 0;
    public int result = 0;
    ArrayList<Button> buttons = new ArrayList<Button>();
    TextView questionText;
    TextView answerText;
    TextView textProcess;

    public static final String TIMEOUT_COMMAND = "timeout";

    public static final int TIMEOUT = 0;

    private NotificationServiceBinder notifyService;
    private DbAccessor db;
    private Handler handler;
    private Runnable timeTick;

    
    int snoozeMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.
                getDefaultSharedPreferences(getBaseContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math);
        level = 0; 
        questionText = (TextView) findViewById(R.id.math_text);
        answerText = (TextView) findViewById(R.id.result_text);
        textProcess = (TextView) findViewById(R.id.tv_math_process);
        buttons.clear();
        setButtons();
        createOperator(level);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        db = new DbAccessor(getApplicationContext());

        
        notifyService = new NotificationServiceBinder(getApplicationContext());

        notifyService.bind();

        
        handler = new Handler();

        timeTick = new Runnable() {
            @Override
            public void run() {
                notifyService.call(new NotificationServiceBinder.
                        ServiceCallback() {
                    @Override
                    public void run(NotificationServiceInterface service) {
                        try {
                            TextView volume = (TextView)
                                    findViewById(R.id.volume);

                            String volumeText = "Volume: " + service.volume();

                            volume.setText(volumeText);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                        long next = AlarmUtil.millisTillNextInterval(
                                AlarmUtil.Interval.SECOND);

                        handler.postDelayed(timeTick, next);
                    }
                });
            }
        };
        final Button snoozeButton = (Button) findViewById(R.id.sleep_button_math);
        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyService.acknowledgeCurrentNotification(snoozeMinutes);

                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    public void setButtons() {
        Button btn = (Button) findViewById(R.id.bt0);
        buttons.add(btn);
        Button btn1 = (Button) findViewById(R.id.bt1);
        buttons.add(btn1);
        Button btn2 = (Button) findViewById(R.id.bt2);
        buttons.add(btn2);
        Button btn3 = (Button) findViewById(R.id.bt3);
        buttons.add(btn3);
        Button btn4 = (Button) findViewById(R.id.bt4);
        buttons.add(btn4);
        Button btn5 = (Button) findViewById(R.id.bt5);
        buttons.add(btn5);
        Button btn6 = (Button) findViewById(R.id.bt6);
        buttons.add(btn6);
        Button btn7 = (Button) findViewById(R.id.bt7);
        buttons.add(btn7);
        Button btn8 = (Button) findViewById(R.id.bt8);
        buttons.add(btn8);
        Button btn9 = (Button) findViewById(R.id.bt9);
        buttons.add(btn9);
        for (int i = 0; i < buttons.size(); i++) {
            final Button btnTemp = buttons.get(i);
            btnTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String str = answerText.getText().toString();
                    answerText.setText(str + btnTemp.getText().toString());
                    String s1 = answerText.getText().toString();
                    String s2 = result+"";
                    if (s1.equals(s2)) {
                        answerText.setTextColor(Color.GREEN);
                        if (counter >= 2) {
                            notifyService.acknowledgeCurrentNotification(0);
                            finish();
                        } else {
                            new CountDownTimer(1000,1) {
                                @Override
                                public void onTick(long l) {

                                }

                                @Override
                                public void onFinish() {
                                    answerText.setTextColor(Color.WHITE);
                                    createOperator(level);
                                    answerText.setText("");
                                }
                            }.start();
                        }
                        textProcess.setText((counter+1)+"/3");
                        counter += 1;
                    } else {
                        if (s1.length() == s2.length()) {
                            answerText.setTextColor(Color.RED);
                            new CountDownTimer(1000,1) {
                                @Override
                                public void onTick(long l) {

                                }

                                @Override
                                public void onFinish() {
                                    answerText.setTextColor(Color.WHITE);
                                }
                            }.start();
                        }
                    }
                }
            });
        }
        ImageButton deleteButton = (ImageButton) findViewById(R.id.math_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = answerText.getText().toString();
                if (s.length() > 0) {
                    s = s.substring(0, s.length() - 1);
                    answerText.setText(s);
                }
            }
        });
    }

    public void createOperator(int level) {
        int first;
        int second;
        int third;
        Random rd = new Random();
        int r = 1;
        if (level == 0) {
            r = 9;
        } else if (level == 1) {
            r = 29;
        } else {
            r = 99;
        }
        first = rd.nextInt(r)+1;
        second = rd.nextInt(r)+1;
        third = rd.nextInt(8) + 2;
        result = (first + second)*third;
        questionText.setText("(" + first + " + " + second + ")" + " x " + third);
    }
}
