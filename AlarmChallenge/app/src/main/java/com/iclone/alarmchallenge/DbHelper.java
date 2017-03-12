

package com.iClone.AlarmChallenge;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class DbHelper extends SQLiteOpenHelper {
  public static final String DB_NAME = "alarmclock";
  public static final int DB_VERSION = 1;

  public static final String DB_TABLE_ALARMS = "alarms";
  public static final String ALARMS_COL__ID = "_id";
  public static final String ALARMS_COL_TIME = "time";
  public static final String ALARMS_COL_ENABLED = "enabled";
  public static final String ALARMS_COL_NAME = "name";
  public static final String ALARMS_COL_DAY_OF_WEEK = "dow";
  public static final String ALARMS_GAME_TYPE = "g_t";
  public static final String ALARMS_GAME_DIFFICULTY = "g_d";

  public static final String DB_TABLE_SETTINGS = "settings";
  public static final String SETTINGS_COL_ID = "id";
  public static final String SETTINGS_COL_TONE_URL = "tone_url";
  public static final String SETTINGS_COL_TONE_NAME = "tone_name";
  public static final String SETTINGS_COL_SNOOZE = "snooze";
  public static final String SETTINGS_COL_VIBRATE = "vibrate";
  public static final String SETTINGS_COL_VOLUME_STARTING = "vol_start";
  public static final String SETTINGS_COL_VOLUME_ENDING = "vol_end";
  public static final String SETTINGS_COL_VOLUME_TIME = "vol_time";
  public static final String SETTINGS_GAME_TYPE = "g_type";
  public static final String SETTINGS_GAME_DIFFICULTY = "g_diff";

  public DbHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    
    
    
    
    db.execSQL("CREATE TABLE " + DB_TABLE_ALARMS + " (" 
        + ALARMS_COL__ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + ALARMS_COL_NAME + " TEXT,"
        + ALARMS_COL_DAY_OF_WEEK + " UNSIGNED INTEGER (0, 127),"
        + ALARMS_COL_TIME + " UNSIGNED INTEGER (0, 86399),"
        + ALARMS_COL_ENABLED + " UNSIGNED INTEGER (0, 1),"
        + ALARMS_GAME_TYPE + " UNSIGNED INTEGER (0, 2),"
        + ALARMS_GAME_DIFFICULTY + " UNSIGNED INTEGER (0, 2))");
    
    
    
    db.execSQL("CREATE TABLE " + DB_TABLE_SETTINGS + " (" 
        + SETTINGS_COL_ID + " INTEGER PRIMARY KEY, "
        + SETTINGS_COL_TONE_URL + " TEXT,"
        + SETTINGS_COL_TONE_NAME + " TEXT,"
        + SETTINGS_COL_SNOOZE + " UNSIGNED INTEGER (1, 60),"
        + SETTINGS_COL_VIBRATE + " UNSIGNED INTEGER (0, 1),"
        + SETTINGS_COL_VOLUME_STARTING + " UNSIGNED INTEGER (1, 100),"
        + SETTINGS_COL_VOLUME_ENDING + " UNSIGNED INTEGER (1, 100),"
        + SETTINGS_COL_VOLUME_TIME + " UNSIGNED INTEGER (1, 60),"
        + SETTINGS_GAME_TYPE + " UNSIGNED_INTEGER (0, 2),"
        + SETTINGS_GAME_DIFFICULTY + " UNSIGNED_INTEGER (0, 2))");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }
}
