package com.iclone.alarmchallenge;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AddAlarm extends AppCompatActivity {
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
       // getSupportActionBar().setTitle("Alarm");
        String[] day = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        listView = (ListView) findViewById(R.id.list_view);
        List<CustomLV> customLVs = new ArrayList<CustomLV>();
        for (int i = 0; i < day.length; i++) {
            CustomLV temp = new CustomLV();
            temp.textDay = day[i];
            customLVs.add(temp);
        }
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.custom_layout_listview, customLVs);
        listView.deferNotifyDataSetChanged();
        listView.setAdapter(customAdapter);
        final String str = "abc";
        for (int i = 0; i < listView.getChildCount(); i++) {
            View view = listView.getChildAt(i);
            TextView v = (TextView) view.findViewById(R.id.day_text);
            CheckBox box = (CheckBox) view.findViewById(R.id.checkbox);
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        
                    }
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        return true;
    }
}

class CustomLV {
    String textDay;
    public void  setTextDay(String textDay) {
        this.textDay = textDay;
    }

    public String getTextDay() {
        return textDay;
    }

    int idCheckbox;
    public int getIdCheckbox() {
        return idCheckbox;
    }

    public void setIdCheckbox(int idCheckbox) {
        this.idCheckbox = idCheckbox;
    }


}

class CustomAdapter extends ArrayAdapter {
    Context context;
    int resource;
    List<CustomLV> objects;
    public CustomAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);

        TextView day = (TextView) rowView.findViewById(R.id.day_text);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkbox);
        day.setText(objects.get(position).getTextDay());
        return rowView;
    }
}
