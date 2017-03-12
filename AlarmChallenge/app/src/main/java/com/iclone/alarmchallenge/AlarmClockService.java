

package com.iClone.AlarmChallenge;

import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.apache.commons.lang3.text.StrSubstitutor;

public final class AlarmClockService extends Service {
  public final static String COMMAND_EXTRA = "command";
  public final static int COMMAND_UNKNOWN = 1;
  public final static int COMMAND_NOTIFICATION_REFRESH = 2;
  public final static int COMMAND_DEVICE_BOOT = 3;
  public final static int COMMAND_TIMEZONE_CHANGE = 4;

  public final static int NOTIFICATION_BAR_ID = 69;

  private DbAccessor db;
  private PendingAlarmList pendingAlarms;

  @Override
  public void onCreate() {
    super.onCreate();
    
    
    
    if (getPackageManager().checkPermission(
        "android.permission.WRITE_EXTERNAL_STORAGE", getPackageName()) ==
          PackageManager.PERMISSION_GRANTED) {
      Thread.setDefaultUncaughtExceptionHandler(
          new LoggingUncaughtExceptionHandler(
                  Environment.getExternalStorageDirectory().getPath()));
    }

    
    db = new DbAccessor(getApplicationContext());
    pendingAlarms = new PendingAlarmList(getApplicationContext());

    
    for (Long alarmId : db.getEnabledAlarms()) {
      if (pendingAlarms.pendingTime(alarmId) != null) {
        continue;
      }




        AlarmTime alarmTime = null;

        AlarmInfo info = db.readAlarmInfo(alarmId);

        if (info != null) {
            alarmTime = info.getTime();
        }

      pendingAlarms.put(alarmId, alarmTime);
    }

    ReceiverNotificationRefresh.startRefreshing(getApplicationContext());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    handleStart(intent);
    return START_STICKY;
  }

  private void handleStart(Intent intent) {
    if (intent != null && intent.hasExtra(COMMAND_EXTRA)) {
      Bundle extras = intent.getExtras();
      int command = extras.getInt(COMMAND_EXTRA, COMMAND_UNKNOWN);

      final Handler handler = new Handler();
      final Runnable maybeShutdown = new Runnable() {
        @Override
        public void run() {
          if (pendingAlarms.size() == 0) {
            stopSelf();
          }
        }
      };

      switch (command) {
        case COMMAND_NOTIFICATION_REFRESH:
          refreshNotification();
          handler.post(maybeShutdown);
          break;
        case COMMAND_DEVICE_BOOT:
          fixPersistentSettings();
          handler.post(maybeShutdown);
          break;
        case COMMAND_TIMEZONE_CHANGE:



          for (long alarmId : pendingAlarms.pendingAlarms()) {
            scheduleAlarm(alarmId);



          }
          handler.post(maybeShutdown);
          break;
        default:
          throw new IllegalArgumentException("Unknown service command.");
      }
    }
  }

  private void refreshNotification() {
      String resolvedString = getString(R.string.no_pending_alarms);

      AlarmTime nextTime = pendingAlarms.nextAlarmTime();

      if (nextTime != null) {
          Map<String, String> values = new HashMap<>();

          values.put("t", nextTime.localizedString(getApplicationContext()));

          values.put("c", nextTime.timeUntilString(getApplicationContext()));

          String templateString = AppSettings.getNotificationTemplate(
                  getApplicationContext());

          StrSubstitutor sub = new StrSubstitutor(values);

          resolvedString = sub.replace(templateString);
      }

    
    final Intent notificationIntent = new Intent(this, ActivityAlarmClock.class);
    final PendingIntent launch = PendingIntent.getActivity(this, 0,
        notificationIntent, 0);

    Context c = getApplicationContext();

      NotificationCompat.Builder builder = new NotificationCompat.Builder(
              getApplicationContext());

      Notification notification = builder
              .setContentIntent(launch)
              .setSmallIcon(R.drawable.ic_stat_notify_alarm)
              .setContentTitle(getString(R.string.app_name))
              .setContentText(resolvedString)
              .setColor(ContextCompat.getColor(getApplicationContext(),
                      R.color.notification_color))
              .build();
      notification.flags |= Notification.FLAG_ONGOING_EVENT;

    final NotificationManager manager =
      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (pendingAlarms.size() > 0 && AppSettings.displayNotificationIcon(c)) {
      manager.notify(NOTIFICATION_BAR_ID, notification);
    } else {
      manager.cancel(NOTIFICATION_BAR_ID);
    }

    setSystemAlarmStringOnLockScreen(getApplicationContext(), nextTime);
  }

    @SuppressWarnings("deprecation")
    public static void setSystemAlarmStringOnLockScreen(Context context,
            AlarmTime alarmTime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            String lockScreenText = AppSettings.lockScreenString(
                    context, alarmTime);

            if (lockScreenText != null) {
                Settings.System.putString(context.getContentResolver(),
                        Settings.System.NEXT_ALARM_FORMATTED, lockScreenText);
            }
        }
    }

  
  
  public void fixPersistentSettings() {
    final String badDebugName = "DEBUG_MODE\"";
    final String badNotificationName = "NOTFICATION_ICON";
    final String badLockScreenName = "LOCK_SCREEN\"";
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    Map<String, ?> prefNames = prefs.getAll();
    
    if (!prefNames.containsKey(badDebugName) &&
        !prefNames.containsKey(badNotificationName) &&
        !prefNames.containsKey(badLockScreenName)) {
      return;
    }
    Editor editor = prefs.edit();
    if (prefNames.containsKey(badDebugName)) {
      editor.putString(AppSettings.DEBUG_MODE, prefs.getString(badDebugName, null));
      editor.remove(badDebugName);
    }
    if (prefNames.containsKey(badNotificationName)){
      editor.putBoolean(AppSettings.NOTIFICATION_ICON, prefs.getBoolean(badNotificationName, true));
      editor.remove(badNotificationName);
    }
    if (prefNames.containsKey(badLockScreenName)) {
      editor.putString(AppSettings.LOCK_SCREEN, prefs.getString(badLockScreenName, null));
      editor.remove(badLockScreenName);
    }
    editor.apply();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    db.closeConnections();

    ReceiverNotificationRefresh.stopRefreshing(getApplicationContext());

    final NotificationManager manager =
      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.cancel(NOTIFICATION_BAR_ID);

      setSystemAlarmStringOnLockScreen(getApplicationContext(), null);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return new AlarmClockInterfaceStub(getApplicationContext(), this);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    
    
    
    
    if (pendingAlarms.size() == 0) {
      stopSelf();
      return false;
    }
    
    
    return true;
  }

  public AlarmTime pendingAlarm(long alarmId) {
    return pendingAlarms.pendingTime(alarmId);
  }

  public AlarmTime[] pendingAlarmTimes() {
    return pendingAlarms.pendingTimes();
  }

  public void createAlarm(AlarmTime time) {
    
    long alarmId = db.newAlarm(time);
    scheduleAlarm(alarmId);
  }

    public void deleteAlarm(long alarmId) {
        pendingAlarms.remove(alarmId);

        db.deleteAlarm(alarmId);

        refreshNotification();
    }

  public void deleteAllAlarms() {
    for (Long alarmId : db.getAllAlarms()) {
      deleteAlarm(alarmId);
    }
  }

  public void scheduleAlarm(long alarmId) {
    AlarmInfo info = db.readAlarmInfo(alarmId);
    if (info == null) {
      return;
    }
    
    pendingAlarms.put(alarmId, info.getTime());

    
    db.enableAlarm(alarmId, true);

    
    
    final Intent self = new Intent(getApplicationContext(), AlarmClockService.class);
    startService(self);

    refreshNotification();
  }

  public void acknowledgeAlarm(long alarmId) {
    AlarmInfo info = db.readAlarmInfo(alarmId);
    if (info == null) {
      return;
    }

    pendingAlarms.remove(alarmId);

    AlarmTime time = info.getTime();
    if (time.repeats()) {
      pendingAlarms.put(alarmId, time);
    } else {
      db.enableAlarm(alarmId, false);
    }
    refreshNotification();
  }

  public void dismissAlarm(long alarmId) {
    AlarmInfo info = db.readAlarmInfo(alarmId);
    if (info == null) {
      return;
    }

    pendingAlarms.remove(alarmId);
    db.enableAlarm(alarmId, false);

    refreshNotification();
  }

  public void snoozeAlarm(long alarmId) {
    snoozeAlarmFor(alarmId, db.readAlarmSettings(alarmId).getSnoozeMinutes());
  }

  public void snoozeAlarmFor(long alarmId, int minutes) {
    
    pendingAlarms.remove(alarmId);

    
    AlarmTime time = AlarmTime.snoozeInMillisUTC(minutes);

    
    pendingAlarms.put(alarmId, time);
    refreshNotification();
  }
}