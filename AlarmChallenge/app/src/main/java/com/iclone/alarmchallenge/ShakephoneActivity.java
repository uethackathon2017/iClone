package com.iClone.AlarmChallenge;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.tbouron.shakedetector.library.ShakeDetector;


public class ShakephoneActivity extends AppCompatActivity{
    TextView text;
    int counter = 0;

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
        setContentView(R.layout.activity_shakephone);
        final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        text = (TextView) findViewById(R.id.shake_text);
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
        final Button snoozeButton = (Button) findViewById(R.id.sleep_button_shake);
        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyService.acknowledgeCurrentNotification(snoozeMinutes);

                finish();
            }
        });
        ShakeDetector.create(this, new ShakeDetector.OnShakeListener() {
            @Override
            public void OnShake() {
                counter += 10;
                text.setText(counter+"");
                if (counter >= 100) {
                    notifyService.acknowledgeCurrentNotification(0);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}

