package com.iClone.AlarmChallenge;

interface NotificationServiceInterface {
  long currentAlarmId();
  int firingAlarmCount();
  float volume();
  void acknowledgeCurrentNotification(int snoozeMinutes);
}