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
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class RewriteWord extends AppCompatActivity {
    char[] word;
    int[] answer;
    int number;
    int counter = 0;
    ArrayList<Button> buttons = new ArrayList<Button>();
    TextView textView;
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
        setContentView(R.layout.activity_rewrite_word);
        number = 3;  
        word = new char[number];
        answer = new int[number];
        for (int i = 0; i < number; i++) {
            answer[i] = -1;
        }
        textView = (TextView) findViewById(R.id.rewrite_text);
        answerText = (TextView) findViewById(R.id.answer_text);
        textProcess = (TextView) findViewById(R.id.textView_process);
        buttons.clear();
        setButtons();
        createWord();
        createButton();
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
        final Button snoozeButton = (Button) findViewById(R.id.sleep_button);
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
        return;
    }


    public void setButtons() {
        Button btn = (Button) findViewById(R.id.btn0);
        buttons.add(btn);
        Button btn1 = (Button) findViewById(R.id.btn1);
        buttons.add(btn1);
        Button btn2 = (Button) findViewById(R.id.btn2);
        buttons.add(btn2);
        Button btn3 = (Button) findViewById(R.id.btn3);
        buttons.add(btn3);
        Button btn4 = (Button) findViewById(R.id.btn4);
        buttons.add(btn4);
        Button btn5 = (Button) findViewById(R.id.btn5);
        buttons.add(btn5);
        Button btn6 = (Button) findViewById(R.id.btn6);
        buttons.add(btn6);
        Button btn7 = (Button) findViewById(R.id.btn7);
        buttons.add(btn7);
        Button btn8 = (Button) findViewById(R.id.btn8);
        buttons.add(btn8);
        Button btn9 = (Button) findViewById(R.id.btn9);
        buttons.add(btn9);
        for (int i = 0; i < buttons.size(); i++) {
            final Button btnTemp = buttons.get(i);
            btnTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String temp = answerText.getText().toString();
                    if (temp.length() < number-1) {
                        answerText.setText(temp + "" + btnTemp.getText().toString());
                    } else if (temp.length() == number-1) {
                        answerText.setText(temp + "" + btnTemp.getText().toString());
                        String s1 = "Rewrite: " + answerText.getText().toString();
                        String s2 = textView.getText().toString();
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
                                        createWord();
                                        createButton();
                                        answerText.setText("");
                                        answerText.setTextColor(Color.WHITE);
                                    }
                                }.start();
                                counter += 1;
                                textProcess.setText((counter+1) + "/3");
                            }
                        } else {
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
        ImageButton delete = (ImageButton) findViewById(R.id.delete_button);
        delete.setOnClickListener(new View.OnClickListener() {
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

    public void createWord() {
        Random rd = new Random();
        textView.setText("Rewrite: ");
        for (int i = 0; i < number; i++) {
            word[i] = (char) (rd.nextInt(26) + 65);
            String temp = textView.getText().toString();
            textView.setText(temp+word[i]);
        }
    }

    public void createButton() {
        for (int i = 0; i < 10; i++) {
            Random rd = new Random();
            char c = (char) (rd.nextInt(26) + 65);
            buttons.get(i).setText(c + "");
        }
        for (int i = 0; i < number; i++) {
            Random rd = new Random();
            int r;
            boolean valid;
            do {
                valid = true;
                r = rd.nextInt(10);
                for (int j = 0; j < number; j++) {
                    if (r == answer[j]) {
                        valid = false;
                    }
                }
            } while (!valid);
            answer[i] = r;
            buttons.get(r).setText(word[i] + "");
        }
    }
}
