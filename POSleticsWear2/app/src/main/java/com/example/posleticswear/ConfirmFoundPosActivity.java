package com.example.posleticswear;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;

public class ConfirmFoundPosActivity extends WearableActivity {

    private int idToConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_found_pos);

        Intent intent = getIntent();
        idToConfirm=intent.getIntExtra("id",0);


        // Enables Always-on
        setAmbientEnabled();
    }

    public void approvePos(View view){
        NetworkSingleton.getInstance(this.getApplicationContext()).upvotePos(idToConfirm);
        startActivity(new Intent(this, MainActivity.class));
    }
    public void disprovePos(View view){
        NetworkSingleton.getInstance(this.getApplicationContext()).downvotePos(idToConfirm);
        startActivity(new Intent(this, MainActivity.class));
    }
}
