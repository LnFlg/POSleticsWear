package com.example.posleticswear;

import androidx.appcompat.app.AppCompatActivity;
import android.support.wearable.activity.WearableActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class DiscoveryActivity extends WearableActivity {

    private Pos toDiscover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        //TODO
        TextView hashtag1= findViewById("");
        hashtag1.setText();

        Intent intent = getIntent();


    }
}
