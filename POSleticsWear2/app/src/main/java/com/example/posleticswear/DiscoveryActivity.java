package com.example.posleticswear;

import androidx.appcompat.app.AppCompatActivity;
import android.support.wearable.activity.WearableActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class DiscoveryActivity extends WearableActivity {

    private Pos toDiscover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        // Intent zurück in Pos überführen
        Intent i = getIntent();
        ArrayList <String> hashtagList = i.getStringArrayListExtra("hashtagList");

        Pos pos = new Pos(i.getExtras().getDouble("lat"),i.getExtras().getDouble("long"),
                i.getExtras().getInt("id"),i.getExtras().getInt("upvotes") );
        pos.setHighestHashtags(hashtagList);


        TextView hashtag1= (TextView) findViewById(R.id.textView_Hashtag_1);
        hashtag1.setText(pos.getHighestHashtags().get(0));

        TextView hashtag2= (TextView) findViewById(R.id.textView_Hashtag_2);
        hashtag1.setText(pos.getHighestHashtags().get(1));


        // ToDo
        //Distanzmarkeirung richtig setzen
    }
}
