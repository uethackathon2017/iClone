package com.iclone.alarmchallenge;

import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class RewriteWord extends AppCompatActivity {
    char[] word = new char[3];
    int[] answer = new int[]{-1, -1, -1};
    int counter = 0;
    ArrayList<Button> buttons = new ArrayList<Button>();
    TextView textView;
    TextView answerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewrite_word);
        textView = (TextView) findViewById(R.id.rewrite_text);
        answerText = (TextView) findViewById(R.id.answer_text);
        buttons.clear();
        setButtons();
        createWord();
        createButton();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
                    if (temp.length() < 2) {
                        answerText.setText(temp + "" + btnTemp.getText().toString());
                    } else if (temp.length() == 2) {
                        answerText.setText(temp + "" + btnTemp.getText().toString());
                        String s1 = "Rewrite: " + answerText.getText().toString();
                        String s2 = textView.getText().toString();
                        if (s1.equals(s2)) {
                            if (counter >= 2) {
                                Intent intent = new Intent(RewriteWord.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                createWord();
                                createButton();
                                answerText.setText("");
                                counter += 1;
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
        for (int i = 0; i < 3; i++) {
            word[i] = (char) (rd.nextInt(26) + 65);
        }
        textView.setText("Rewrite: " + word[0] + "" + word[1] + "" + word[2]);
    }

    public void createButton() {
        for (int i = 0; i < 10; i++) {
            Random rd = new Random();
            char c = (char) (rd.nextInt(26) + 65);
            buttons.get(i).setText(c + "");
        }
        for (int i = 0; i < 3; i++) {
            Random rd = new Random();
            int r;
            do {
                r = rd.nextInt(10);
            } while (r == answer[0] || r == answer[1] || r == answer[2]);
            answer[i] = r;
            buttons.get(r).setText(word[i] + "");
        }
    }
}
