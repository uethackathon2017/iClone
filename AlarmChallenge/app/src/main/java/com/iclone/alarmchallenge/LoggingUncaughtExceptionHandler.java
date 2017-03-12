
package com.iClone.AlarmChallenge;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

 
public final class LoggingUncaughtExceptionHandler implements UncaughtExceptionHandler {
  private String directory;
  private UncaughtExceptionHandler defaultHandler;

  public LoggingUncaughtExceptionHandler(String directory) {
    this.directory = directory;
    this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
  }

  @Override
  public void uncaughtException(Thread thread, Throwable ex) {
    try {
      String timestamp = new SimpleDateFormat("yyyyMMdd_kkmmss.SSSS", Locale.US)
              .format(Calendar.getInstance().getTime());
      String filename = timestamp + "-alarmclock.txt";

      Writer stacktrace = new StringWriter();
      ex.printStackTrace(new PrintWriter(stacktrace));

      BufferedWriter bos = new BufferedWriter(new FileWriter(directory + "/" + filename));
      bos.write(stacktrace.toString());
      bos.flush();
      bos.close();
      stacktrace.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      defaultHandler.uncaughtException(thread, ex);
    }
  }
}
