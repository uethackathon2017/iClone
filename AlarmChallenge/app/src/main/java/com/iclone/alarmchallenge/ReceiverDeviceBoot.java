package com.iClone.AlarmChallenge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReceiverDeviceBoot extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    
    
    
    
    
    if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
      if (!intent.getData().getSchemeSpecificPart().equals(context.getPackageName())) {
        return;
      }
    }
    Intent i = new Intent(context, AlarmClockService.class);
    i.putExtra(AlarmClockService.COMMAND_EXTRA, AlarmClockService.COMMAND_DEVICE_BOOT);
    context.startService(i);
  }

}
