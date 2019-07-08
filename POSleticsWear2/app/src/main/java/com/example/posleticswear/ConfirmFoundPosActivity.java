package com.example.posleticswear;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;

public class ConfirmFoundPosActivity extends WearableActivity {

    private Pos toConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_found_pos);

        Intent intent = getIntent();
        //TODO get Pos from Intent


        // Enables Always-on
        setAmbientEnabled();
    }

    public void approvePos(View view){
        NetworkSingleton.getInstance(this.getApplicationContext()).upvotePos(toConfirm.getId());
        startActivity(new Intent(this, MainActivity.class));
    }
    public void disprovePos(View view){
        NetworkSingleton.getInstance(this.getApplicationContext()).downvotePos(toConfirm.getId());
        startActivity(new Intent(this, MainActivity.class));
    }
}
