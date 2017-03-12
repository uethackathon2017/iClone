package com.iClone.AlarmChallenge;

import android.content.Context;
import android.os.RemoteException;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ContentViewHolder> {

    private ArrayList<AlarmInfo> alarmInfos;
    private AlarmClockServiceBinder service;
    private Context mcontext;
    private TimePickerDialog picker;

    private int[] Images = {R.drawable.ic_math,R.drawable.ic_word,R.drawable.ic_shake};
    private int[] diff_im = {R.drawable.ic_diff_easy,R.drawable.ic_diff_medium,R.drawable.ic_diff_hard};

    public AlarmAdapter(ArrayList<AlarmInfo> alarmInfos,
            AlarmClockServiceBinder service, Context context) {
        this.alarmInfos = alarmInfos;
        this.service = service;
        this.mcontext = context;
    }

    public ArrayList<AlarmInfo> getAlarmInfos() {
        return alarmInfos;
    }

    public void removeAt(int position) {
        alarmInfos.remove(position);

        notifyItemRemoved(position);

        notifyItemRangeChanged(position, alarmInfos.size());
    }

    public void removeAll() {
        int size = alarmInfos.size();

        if (size > 0) {
            for (int i = 0; i < size; i++) {
                alarmInfos.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    @Override
    public void onBindViewHolder(final ContentViewHolder holder, int position) {
        final AlarmInfo info = alarmInfos.get(position);

        AlarmTime time = null;
        
        if (service.clock() != null) {
            try {
                time = service.clock().pendingAlarm(info.getAlarmId());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        
        if (time == null) {
            time = info.getTime();
        }

        String timeStr = time.localizedString(mcontext);

        String timeText = timeStr;

        holder.timeAlarm.setText(timeText);
        holder.icon_game.setImageResource(Images[info.getGameType()]);
        holder.game_ic.setImageResource(Images[info.getGameType()]);
        holder.diff_ic.setImageResource(diff_im[info.getGameDiff()]);

        

        holder.labelText.setText(info.getName());


        holder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

        holder.timeAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if(mcontext instanceof ActivityAlarmClock){
                    ((ActivityAlarmClock)mcontext).ChangeAlarmTime(info.getAlarmId(), ActivityAlarmClock.SettingType.TIME);
                }

            }
        });

        holder.Tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if(mcontext instanceof ActivityAlarmClock){
                    ((ActivityAlarmClock)mcontext).ChangeAlarmTime(info.getAlarmId(), ActivityAlarmClock.SettingType.TONE);
                }
            }
        });

        holder.Label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if(mcontext instanceof ActivityAlarmClock){
                    ((ActivityAlarmClock)mcontext).ChangeAlarmTime(info.getAlarmId(), ActivityAlarmClock.SettingType.NAME);
                }
            }
        });

        holder.Repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if(mcontext instanceof ActivityAlarmClock){
                    ((ActivityAlarmClock)mcontext).ChangeAlarmTime(info.getAlarmId(), ActivityAlarmClock.SettingType.DAYS_OF_WEEK);
                }
            }
        });

        holder.Game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mcontext instanceof ActivityAlarmClock){
                    ((ActivityAlarmClock)mcontext).ChangeAlarmTime(info.getAlarmId(), ActivityAlarmClock.SettingType.GAME_TYPE);
                }
            }
        });

        holder.Difficul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mcontext instanceof ActivityAlarmClock){
                    ((ActivityAlarmClock)mcontext).ChangeAlarmTime(info.getAlarmId(), ActivityAlarmClock.SettingType.GAME_DIFFICULTY);
                }
            }
        });

        holder.Vibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mcontext instanceof ActivityAlarmClock){
                    ((ActivityAlarmClock)mcontext).ChangeAlarmTime(info.getAlarmId(), ActivityAlarmClock.SettingType.VIBRATE);
                }
            }
        });

        holder.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mcontext instanceof ActivityAlarmClock){
                    ((ActivityAlarmClock)mcontext).ChangeAlarmTime(info.getAlarmId(), ActivityAlarmClock.SettingType.DELETE);
                }
            }
        });


        if (!info.getTime().getDaysOfWeek().equals(Week.NO_REPEATS)) {
            holder.repeatAlarm.setText(info.getTime().getDaysOfWeek().
                    toString(mcontext));
        }

        holder.on_Off.setChecked(info.enabled());
    }

    @Override
    public int getItemCount() {
        return alarmInfos.size();
    }

    @Override
    public ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_container, parent, false);

        return new ContentViewHolder(itemView);
    }

    public class ContentViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        protected TextView timeAlarm;
        protected TextView repeatAlarm;
        protected TextView labelText;
        protected CheckBox on_Off;
        protected LinearLayout Game;
        protected LinearLayout Difficul;
        protected LinearLayout Label;
        protected LinearLayout Repeat;
        protected LinearLayout Tone;
        protected LinearLayout Vibrate;
        protected LinearLayout Delete;
        protected SwipeLayout swipeLayout;
        protected ImageView icon_game;
        protected ImageView game_ic;
        protected ImageView diff_ic;

        public ContentViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            timeAlarm = (TextView) itemView.findViewById(R.id.textView_alarm_time);
            repeatAlarm = (TextView) itemView.findViewById(R.id.textView_alarm_days);
            labelText = (TextView) itemView.findViewById(R.id.label_text);
            on_Off = (CheckBox) itemView.findViewById(R.id.on_off);
            Game = (LinearLayout) itemView.findViewById(R.id.swipe_options_game_layout);
            Difficul = (LinearLayout) itemView.findViewById(R.id.swipe_options_difficulty_layout);
            Label = (LinearLayout) itemView.findViewById(R.id.swipe_options_label_layout);
            Repeat = (LinearLayout) itemView.findViewById(R.id.swipe_options_repeat_layout);
            Tone = (LinearLayout) itemView.findViewById(R.id.swipe_options_ringtone_layout);
            Vibrate = (LinearLayout) itemView.findViewById(R.id.swipe_options_vibrate_layout);
            Delete = (LinearLayout) itemView.findViewById(R.id.swipe_options_delete_layout);
            icon_game = (ImageView) itemView.findViewById(R.id.alarm_game_icon);
            game_ic = (ImageView) itemView.findViewById(R.id.swipe_options_game_icon) ;
            diff_ic = (ImageView) itemView.findViewById(R.id.swipe_options_difficulty_icon) ;

            on_Off.setOnCheckedChangeListener(new  CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final AlarmInfo info = alarmInfos.get(getAdapterPosition());

                    if (isChecked) {
                        info.setEnabled(true);

                        service.scheduleAlarm(info.getAlarmId());
                    } else {
                        info.setEnabled(false);

                        service.unscheduleAlarm(info.getAlarmId());
                    }
                }
            });

        }

        @Override
        public void onClick(View v) {
            Toast.makeText(mcontext,"Click on "+ v.getId()+"",Toast.LENGTH_SHORT);
        }

        @Override
        public boolean onLongClick(View v) {

            return true;
        }
    }

}
