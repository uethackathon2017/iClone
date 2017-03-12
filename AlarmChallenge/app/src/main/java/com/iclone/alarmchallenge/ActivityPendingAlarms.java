

package com.iClone.AlarmChallenge;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public final class ActivityPendingAlarms extends AppCompatActivity {
  boolean connected;
  private ListView listView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppSettings.setTheme(getBaseContext(), ActivityPendingAlarms.this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.pending_alarms);

    setTitle(R.string.pending_alarms);

    connected = false;
    listView = (ListView) findViewById(R.id.pending_alarm_list);
  }

  @Override
  protected void onResume() {
    super.onResume();
    final Intent i = new Intent(getApplicationContext(), AlarmClockService.class);
    if (!bindService(i, connection, Service.BIND_AUTO_CREATE)) {
      throw new IllegalStateException("Unable to bind to AlarmClockService.");
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (connected) {
      unbindService(connection);
    }
  }

  private final ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      connected = true;
      AlarmClockInterface clock = AlarmClockInterface.Stub.asInterface(service);
      try {
        ArrayAdapter<AlarmTime> adapter = new ArrayAdapter<>(
            getApplicationContext(), R.layout.pending_alarms_item, clock.pendingAlarmTimes());
        listView.setAdapter(adapter);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      connected = false;
    }
  };
}
