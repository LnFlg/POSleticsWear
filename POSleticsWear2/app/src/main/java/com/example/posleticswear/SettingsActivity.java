package com.example.posleticswear;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends WearableActivity implements AdapterView.OnItemSelectedListener {



    public static ArrayAdapter<Integer> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        Spinner spinner = (Spinner) findViewById(R.id.spinner_users);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter<Integer>
                (this, android.R.layout.simple_spinner_item,
                        RuntimeData.getInstance().getUsers());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        NetworkSingleton.getInstance(this.getApplicationContext()).getAllPos();


        // Enables Always-on
        setAmbientEnabled();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        RuntimeData.getInstance().setUserId((Integer)parent.getItemAtPosition(pos));
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void getRoute(View view){
        NetworkSingleton.getInstance(this.getApplicationContext()).getRouteFromServer(RuntimeData.getInstance().getUserId());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (event.getRepeatCount() == 0) {
            if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    public ArrayAdapter<Integer> getAdapter() {
        return adapter;
    }

    public void setAdapter(ArrayAdapter<Integer> adapter) {
        this.adapter = adapter;
    }

}
