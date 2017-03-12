
package com.iClone.AlarmChallenge;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.simplealertdialog.SimpleAlertDialog;
import com.simplealertdialog.SimpleAlertDialogFragment;
import com.wdullaer.materialdatetimepicker.time.*;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.iClone.AlarmChallenge.ActivityAlarmSettings.EXPLAIN_READ_EXTERNAL_STORAGE;
import static com.iClone.AlarmChallenge.ActivityAlarmSettings.PERMISSION_NOT_GRANTED;
import static com.iClone.AlarmChallenge.ActivityAlarmSettings._requestReadExternalStoragePermission;


/**
 * This is the main Activity for the application.  It contains a ListView
 * for displaying all alarms, a simple clock, and a button for adding new
 * alarms.  The context menu allows the user to edit default settings.  Long-
 * clicking on the clock will trigger a dialog for enabling/disabling 'debug
 * mode.'
 */
public final class ActivityAlarmClock extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener,
        TimePickerDialog.OnTimeChangedListener, SimpleAlertDialog.OnItemClickListener {

    public static final int DELETE_CONFIRM = 1;
    public static final int DELETE_ALARM_CONFIRM = 2;

    public static final int ACTION_TEST_ALARM = 0;
    public static final int ACTION_PENDING_ALARMS = 1;

    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 0;
    private static final int REQUEST_CODE_TASK = 4;
    private static final int REQUEST_CODE_DIFF = 5;

    private TimePickerDialog picker;
    public static ActivityAlarmClock activityAlarmClock;

    private static AlarmClockServiceBinder service;
    private static NotificationServiceBinder notifyService;
    private DbAccessor db;
    private static AlarmAdapter adapter;
    private Cursor cursor;
    private Handler handler;
    private Runnable tickCallback;
    private static RecyclerView alarmList;
    private int mLastFirstVisiblePosition;

    
    boolean isCreatingNew;
    private static long alarmID;
    private static AlarmInfo changeInfo;
    private AlarmInfo originalInfo;
    private static AlarmSettings originalSettings;
    private static AlarmSettings settings;

    public static final int NAME_PICKER = 3;
    public static final int DOW_PICKER = 4;
    public static final int TONE_PICKER = 5;
    public static final int GAME_TYPE = 6;
    public static final int GAME_DIFFICULTY = 7;
    public static final int DELETE = 8;
    public static final int VIBRATE = 9;
    private static ProgressDialog progressDialog;

    @Override
    public void onItemClick(SimpleAlertDialog dialog, int requestCode, int which) {
        if(requestCode == REQUEST_CODE_TASK){
            //Toast.makeText(getBaseContext(),"Task-"+which+"",Toast.LENGTH_SHORT).show();
            settings.setGameType(which);
            changeInfo.setGameType(which);
        }
        if (requestCode == REQUEST_CODE_DIFF){
            //Toast.makeText(getBaseContext(),"Diff-"+which+"",Toast.LENGTH_SHORT).show();
            settings.setGameDiff(which);
            changeInfo.setGameDiff(which);
        }
        saveAlarmSettings(alarmID);
    }

    

    public enum SettingType {
        TIME,
        NAME,
        DAYS_OF_WEEK,
        TONE,
        VIBRATE,
        GAME_TYPE,
        GAME_DIFFICULTY,
        DELETE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.setMainActivityTheme(getBaseContext(),
                ActivityAlarmClock.this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.alarm_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        activityAlarmClock = this;

        
        service = new AlarmClockServiceBinder(getApplicationContext());

        db = new DbAccessor(getApplicationContext());

        handler = new Handler();

        
        
        alarmList = (RecyclerView) findViewById(R.id.alarm_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        alarmList.setLayoutManager(layoutManager);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCreatingNew = true;
                Calendar now = Calendar.getInstance();

                picker = TimePickerDialog.newInstance(
                        ActivityAlarmClock.this,
                        ActivityAlarmClock.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(ActivityAlarmClock.this)
                );

                if (AppSettings.isThemeDark(ActivityAlarmClock.this)) {
                    picker.setThemeDark(true);
                }

                picker.setAccentColor(AppSettings.getTimePickerColor(
                        ActivityAlarmClock.this));

                picker.vibrate(true);







                AlarmTime time = new AlarmTime(now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE), 0);

                picker.setTitle(time.timeUntilString(ActivityAlarmClock.this));

                picker.show(getFragmentManager(), "TimePickerDialog");
            }
        });

        
        
        tickCallback = new Runnable() {
            @Override
            public void run() {
                
                redraw();

                
                AlarmUtil.Interval interval = AlarmUtil.Interval.MINUTE;





                long next = AlarmUtil.millisTillNextInterval(interval);

                handler.postDelayed(tickCallback, next);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();

        service.bind();

        handler.post(tickCallback);

        requery();

        alarmList.getLayoutManager().scrollToPosition(mLastFirstVisiblePosition);

        notifyService = new NotificationServiceBinder(getApplicationContext());

        notifyService.bind();

        notifyService.call(new NotificationServiceBinder.ServiceCallback() {
            @Override
            public void run(NotificationServiceInterface service) {
                int count;

                try {
                    count = service.firingAlarmCount();
                } catch (RemoteException e) {
                    return;
                }

                if (count > 0) {
                    Intent notifyActivity = new Intent(getApplicationContext(),
                            ActivityAlarmNotification.class);

                    notifyActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(notifyActivity);
                }
            }
        });

        TimePickerDialog tpd = (TimePickerDialog) getFragmentManager().
                findFragmentByTag("TimePickerDialog");

        if (tpd != null) {
            picker = tpd;

            tpd.setOnTimeSetListener(this);

            tpd.setOnTimeChangedListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(tickCallback);

        service.unbind();

        if (notifyService != null) {
            notifyService.unbind();
        }

        mLastFirstVisiblePosition = ((LinearLayoutManager)
                alarmList.getLayoutManager()).
                findFirstCompletelyVisibleItemPosition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        db.closeConnections();

        activityAlarmClock = null;

        notifyService = null;

        cursor.close();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        progressDialog = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST) {
            if (permissions.length == 1 &&
                    permissions[0].equals(
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showProgressDialog();

                showDialogFragment(TONE_PICKER);
            } else {
                showDialogFragment(PERMISSION_NOT_GRANTED);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {






        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        if(isCreatingNew){
            AlarmTime time = new AlarmTime(hourOfDay, minute, second);

            service.createAlarm(time);

            requery();
        } else {
            final AlarmTime time = changeInfo.getTime();

            changeInfo.setTime(new AlarmTime(hourOfDay, minute, second, time.getDaysOfWeek()));

            saveAlarmSettings(alarmID);
            requery();
        }
    }

    @Override
    public void onTimeChanged(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        if(isCreatingNew){
            AlarmTime time = new AlarmTime(hourOfDay, minute, second);

            picker.setTitle(time.timeUntilString(this));
        } else {
            final AlarmTime infoTime = changeInfo.getTime();

            final AlarmTime time = new AlarmTime(hourOfDay, minute, second,
                    infoTime.getDaysOfWeek());

            picker.setTitle(time.timeUntilString(this));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                showDialogFragment(DELETE_CONFIRM);
                break;
            case R.id.action_default_settings:
                Intent alarm_settings = new Intent(getApplicationContext(),
                        ActivityAlarmSettings.class);

                alarm_settings.putExtra(ActivityAlarmSettings.EXTRAS_ALARM_ID,
                        AlarmSettings.DEFAULT_SETTINGS_ID);

                startActivity(alarm_settings);
                break;
            case R.id.action_app_settings:
                Intent app_settings = new Intent(getApplicationContext(),
                        ActivityAppSettings.class);

                startActivity(app_settings);
                break;
            case ACTION_TEST_ALARM:
                
                
                final Calendar testTime = Calendar.getInstance();

                testTime.add(Calendar.SECOND, 5);

                AlarmTime time = new AlarmTime(
                        testTime.get(Calendar.HOUR_OF_DAY),
                        testTime.get(Calendar.MINUTE),
                        testTime.get(Calendar.SECOND));

                service.createAlarm(time);

                requery();
                break;
            case ACTION_PENDING_ALARMS:
                
                startActivity(new Intent(getApplicationContext(),
                        ActivityPendingAlarms.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(ActivityAlarmClock.this,
                getString(R.string.loading),
                getString(R.string.please_wait), true, true);
    }

    private void showDialogFragment(int id) {
        DialogFragment dialog = new ActivityDialogFragment().newInstance(
                id);

        dialog.show(getFragmentManager(), "ActivityDialogFragment");
    }

    private void redraw() {
        
        adapter.notifyDataSetChanged();

        Calendar now = Calendar.getInstance();

        AlarmTime time = new AlarmTime(now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE), 0);

        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setTitle(
                time.localizedString(this));
    }

    private void requery() {
        cursor = db.readAlarmInfo();

        ArrayList<AlarmInfo> infos = new ArrayList<>();

        while (cursor.moveToNext()) {
            infos.add(new AlarmInfo(cursor));
        }

        adapter = new AlarmAdapter(infos, service, this);

        alarmList.setAdapter(adapter);

        setEmptyViewIfEmpty(this);
    }

    public static void setEmptyViewIfEmpty(Activity activity) {
        if (adapter.getItemCount() == 0) {
            activity.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

            alarmList.setVisibility(View.GONE);
        } else {
            activity.findViewById(R.id.empty_view).setVisibility(View.GONE);

            alarmList.setVisibility(View.VISIBLE);
        }
    }

    public static void removeItemFromList(Activity activity, long alarmId, int position) {
        service.deleteAlarm(alarmId);

        adapter.removeAt(position);

        setEmptyViewIfEmpty(activity);
    }

    public static class ActivityDialogFragment extends DialogFragment {

        public ActivityDialogFragment newInstance(int id) {
            ActivityDialogFragment fragment = new ActivityDialogFragment();

            Bundle args = new Bundle();

            args.putInt("id", id);

            fragment.setArguments(args);

            return fragment;
        }

        public ActivityDialogFragment newInstance(int id, AlarmInfo info,
                int position) {
            ActivityDialogFragment fragment = new ActivityDialogFragment();

            Bundle args = new Bundle();

            args.putInt("id", id);

            args.putLong("alarmId", info.getAlarmId());

            args.putInt("position", position);

            fragment.setArguments(args);

            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            switch (getArguments().getInt("id")) {
                case ActivityAlarmClock.DELETE_CONFIRM:
                    final AlertDialog.Builder deleteConfirmBuilder =
                            new AlertDialog.Builder(getActivity());

                    deleteConfirmBuilder.setTitle(R.string.delete_all);

                    deleteConfirmBuilder.setMessage(R.string.confirm_delete);

                    deleteConfirmBuilder.setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            service.deleteAllAlarms();

                            adapter.removeAll();

                            setEmptyViewIfEmpty(getActivity());

                            dismiss();
                        }
                    });

                    deleteConfirmBuilder.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });
                    return deleteConfirmBuilder.create();
                case ActivityAlarmClock.DELETE_ALARM_CONFIRM:
                    final AlertDialog.Builder deleteAlarmConfirmBuilder =
                            new AlertDialog.Builder(getActivity());

                    deleteAlarmConfirmBuilder.setTitle(R.string.delete);

                    deleteAlarmConfirmBuilder.setMessage(
                            R.string.confirm_delete);

                    deleteAlarmConfirmBuilder.setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    removeItemFromList(getActivity(),
                                            getArguments().getLong("alarmId"),
                                            getArguments().getInt("position"));
                                    service.deleteAlarm(alarmID);

                                    dismiss();
                                }
                            });

                    deleteAlarmConfirmBuilder.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dismiss();
                                }
                            });
                    return deleteAlarmConfirmBuilder.create();
                case NAME_PICKER:
                    final View nameView = View.inflate(getActivity(),
                            R.layout.name_settings_dialog, null);
                    final TextView label = (TextView) nameView.findViewById(R.id.name_label);
                    label.setText(changeInfo.getName());
                    final AlertDialog.Builder nameBuilder = new AlertDialog.Builder(getActivity());
                    nameBuilder.setTitle(R.string.alarm_label);
                    nameBuilder.setView(nameView);
                    nameBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            changeInfo.setName(label.getEditableText().toString());
                            activityAlarmClock.saveAlarmSettings(alarmID);
                            dismiss();
                        }
                    });
                    nameBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });
                    return nameBuilder.create();

                case DOW_PICKER:
                    final AlertDialog.Builder dowBuilder = new AlertDialog.Builder(getActivity());
                    dowBuilder.setTitle(R.string.scheduled_days);
                    dowBuilder.setMultiChoiceItems(
                            changeInfo.getTime().getDaysOfWeek().names(getActivity()),
                            changeInfo.getTime().getDaysOfWeek().bitmask(),
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    if (isChecked) {
                                        changeInfo.getTime().getDaysOfWeek().addDay(Week.Day.values()[which]);
                                    } else {
                                        changeInfo.getTime().getDaysOfWeek().removeDay(Week.Day.values()[which]);
                                    }
                                    activityAlarmClock.saveAlarmSettings(alarmID);
                                }
                            });
                    dowBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });
                    return dowBuilder.create();

                case TONE_PICKER:
                    MediaPickerDialog mediaPicker = new MediaPickerDialog(getActivity());
                    mediaPicker.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            if (progressDialog != null) {
                                progressDialog.dismiss();

                                progressDialog = null;
                            }
                        }
                    });
                    mediaPicker.setPickListener(new MediaPickerDialog.OnMediaPickListener() {
                        @Override
                        public void onMediaPick(String name, Uri media) {
                            if (name.length() == 0) {
                                name = getString(R.string.unknown_name);
                            }
                            settings.setTone(media, name);
                            activityAlarmClock.saveAlarmSettings(alarmID);
                        }
                    });
                    return mediaPicker;


                default:
                    return super.onCreateDialog(savedInstanceState);
            }
        }

    }

    public void ChangeAlarmTime(long _alarmID,SettingType type){
        this.alarmID = _alarmID;
        originalInfo = db.readAlarmInfo(alarmID);
        if(originalInfo != null)
            changeInfo = new AlarmInfo(originalInfo);
        originalSettings = db.readAlarmSettings(alarmID);
        settings = new AlarmSettings(originalSettings);
        isCreatingNew = false;
        switch (type){
            case TIME:
                final AlarmTime time = changeInfo.getTime();

                Calendar c = time.calendar();

                picker = TimePickerDialog.newInstance(
                        ActivityAlarmClock.this,
                        ActivityAlarmClock.this,
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(ActivityAlarmClock.this)
                );

                if (AppSettings.isThemeDark(ActivityAlarmClock.this)) {
                    picker.setThemeDark(true);
                }

                picker.setAccentColor(AppSettings.getTimePickerColor(
                        ActivityAlarmClock.this));

                picker.vibrate(true);







                picker.setTitle(time.timeUntilString(ActivityAlarmClock.this));

                picker.show(getFragmentManager(), "TimePickerDialog");
                break;
            case TONE:
                if (ContextCompat.checkSelfPermission(ActivityAlarmClock.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    
                    
                    final Uri currentTone= settings.getTone();
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION | RingtoneManager.TYPE_RINGTONE);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    startActivityForResult(intent, TONE_PICKER);
                } else {
                    requestReadExternalStoragePermission();
                }
                break;
            case NAME:
                showDialogFragment(NAME_PICKER);
                break;

            case DAYS_OF_WEEK:
                showDialogFragment(DOW_PICKER);
                break;
            case VIBRATE:
                break;
            case DELETE:
                showDialogFragment(DELETE_ALARM_CONFIRM);
                break;
            case GAME_TYPE:
                new SimpleAlertDialogFragment.Builder()
                        .setTitle("Choose one")
                        .setItems(R.array.task, new int[]{
                                R.drawable.ic_math,
                                R.drawable.ic_word,
                                R.drawable.ic_shake})
                        .setRequestCode(REQUEST_CODE_TASK)
                        .create().show(getFragmentManager(), "dialog");
                break;
            case GAME_DIFFICULTY:
                new SimpleAlertDialogFragment.Builder()
                        .setTitle("Choose one")
                        .setItems(R.array.diff, new int[]{
                                R.drawable.ic_diff_easy,
                                R.drawable.ic_diff_medium,
                                R.drawable.ic_diff_hard})
                        .setRequestCode(REQUEST_CODE_DIFF)
                        .create().show(getFragmentManager(), "dialog");
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TONE_PICKER){
            if(resultCode == RESULT_OK){

                Uri uriTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                settings.setTone(uriTone,"Name");
                saveAlarmSettings(alarmID);
                
            }
        }
    }

    public void saveAlarmSettings(long alarmId) {
        
        if (originalInfo != null && !originalInfo.equals(changeInfo)) {
            db.writeAlarmInfo(alarmId, changeInfo);

            
            
            
            
            if (!originalInfo.getTime().equals(changeInfo.getTime())) {
                service.scheduleAlarm(alarmId);
            }
        }

        
        if (!originalSettings.equals(settings)) {
            db.writeAlarmSettings(alarmId, settings);
        }
        requery();
    }

    public void requestReadExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showDialogFragment(EXPLAIN_READ_EXTERNAL_STORAGE);
        } else {
            _requestReadExternalStoragePermission(this);
        }
    }

    /**
     * A helper interface to encapsulate the data displayed in the list view of
     * this activity.  Consists of a setting name, a setting value, and a type.
     * The type is used to trigger the appropriate action from the onClick
     * handler.
     */
    private abstract class Setting {
        public abstract String name();
        public abstract String value();
        public abstract ActivityAlarmClock.SettingType type();
    }

    /**
     * This adapter populates the settings_items view with the data encapsulated
     * in the individual Setting objects.
     */
    private final class SettingsAdapter extends ArrayAdapter<ActivityAlarmClock.Setting> {

        List<ActivityAlarmClock.Setting> settingsObjects;

        public SettingsAdapter(Context context, List<ActivityAlarmClock.Setting> settingsObjects) {
            super(context, 0, settingsObjects);

            this.settingsObjects = settingsObjects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ActivityAlarmClock.ViewHolder holder;

            LayoutInflater inflater = getLayoutInflater();

            if (settingsObjects.get(position).name().
                    equalsIgnoreCase(getString(R.string.vibrate))) {
                convertView = inflater.inflate(R.layout.vibrate_settings_item, parent,
                        false);
            } else {
                convertView = inflater.inflate(R.layout.settings_item, parent,
                        false);
            }

            holder = new ActivityAlarmClock.ViewHolder(convertView);

            convertView.setTag(holder);

            holder.populateFrom(settingsObjects.get(position));

            return(convertView);
        }

    }

    private class ViewHolder {

        private View row;

        ViewHolder(View row) {
            this.row = row;
        }

        void populateFrom(ActivityAlarmClock.Setting setting) {
            ((TextView) row.findViewById(R.id.setting_name)).
                    setText(setting.name());

            if (setting.name().equalsIgnoreCase(getString(R.string.vibrate))) {
                SwitchCompat vibrateSwitch = (SwitchCompat) row.findViewById(
                        R.id.setting_vibrate_sc);

                if (settings.getVibrate()) {
                    vibrateSwitch.setChecked(true);
                } else {
                    vibrateSwitch.setChecked(false);
                }

                vibrateSwitch.setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    settings.setVibrate(true);
                                } else {
                                    settings.setVibrate(false);
                                }
                            }
                        });
            } else {
                ((TextView) row.findViewById(R.id.setting_value)).
                        setText(setting.value());
            }
        }

    }

}
