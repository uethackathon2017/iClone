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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MathActivity extends AppCompatActivity {

    public int counter = 0;
    public int result = 0;
    ArrayList<Button> buttons = new ArrayList<Button>();
    TextView questionText;
    TextView answerText;
    HashMap<Integer, String> hashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }
        questionText = (TextView) findViewById(R.id.math_text);
        answerText = (TextView) findViewById(R.id.result_text);
        hashMap = new HashMap<Integer, String>();
        hashMap.put(0,"+");
        hashMap.put(1,"-");
        hashMap.put(2,"x");
        hashMap.put(3,"/");
        buttons.clear();
        setButtons();
        createOperator();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
                        if (counter >= 2) {
                            Intent intent = new Intent(MathActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            createOperator();
                            answerText.setText("");
                        }
                        counter += 1;
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

    public void createOperator() {
        Random rd = new Random();
        int first = rd.nextInt(100);
        int second = rd.nextInt(100);
        int operator = rd.nextInt(4);
        switch (operator) {
            case 0: {
                result = first + second;
                break;
            }
            case 1: {
                if (first < second) {
                    int temp = first;
                    first = second;
                    second = temp;
                }
                result = first - second;
                break;
            }
            case 2: {
                result = first*second;
                break;
            }
            case 3: {
                while (first%second != 0) {
                    first = rd.nextInt(100);
                    second = rd.nextInt(100);
                }
                result = first/second;
                break;
            }
        }
        questionText.setText(first+hashMap.get(operator)+second);
    }
}
