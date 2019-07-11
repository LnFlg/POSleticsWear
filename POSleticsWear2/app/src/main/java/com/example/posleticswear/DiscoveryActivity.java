package com.example.posleticswear;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.wearable.activity.WearableActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Map;

public class DiscoveryActivity extends WearableActivity implements SensorEventListener {



    private SensorManager mSensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLoc;
    private TextView distanceTextView;
    private Pos pos;
    float currentDegree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);


        //Für Locationdata
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        // Intent zurück in Pos überführen
        Intent i = getIntent();
        ArrayList <String> hashtagList = i.getStringArrayListExtra("hashtagList");

        pos = new Pos(i.getExtras().getDouble("lat"),i.getExtras().getDouble("long"),
                i.getExtras().getInt("id"),i.getExtras().getInt("upvotes") );
        pos.setHighestHashtags(hashtagList);


        TextView hashtag1= (TextView) findViewById(R.id.textView_Hashtag_1);
        hashtag1.setText(pos.getHighestHashtags().get(0));

        TextView hashtag2= (TextView) findViewById(R.id.textView_Hashtag_2);
        hashtag1.setText(pos.getHighestHashtags().get(1));


        //Distanzmarkeirung richtig setzen
        distanceTextView= (TextView) findViewById(R.id.textView_distance);
        distanceTextView.setText(pos.getLoc().distanceTo(lastKnownLoc)+"m");
    }

    // Get readings from accelerometer and magnetometer.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!RuntimeData.getInstance().isDisableLocationServices()) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelerometerReading,
                        0, accelerometerReading.length);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);
            }

            updateLastKnown();
            updateOrientationAngles();


            if(lastKnownLoc.distanceTo(pos.getLoc())<=5.0){
                startActivity(new Intent(this, ConfirmFoundPosActivity.class).putExtra("id",pos.getId()));
            }

            //calc und set distanz, rotation
            distanceTextView.setText(lastKnownLoc.distanceTo(pos.getLoc()) + "m");


            //Button drehen
            float head = (float) Math.toDegrees(orientationAngles[0]);
            float bearTo = lastKnownLoc.bearingTo(pos.getLoc());

            //bearTo = The angle from true north to the destination location from the point we're your currently standing.
            //head = The angle that you've rotated your phone from true north.

            GeomagneticField geoField = new GeomagneticField(Double.valueOf(lastKnownLoc.getLatitude()).floatValue(), Double
                    .valueOf(lastKnownLoc.getLongitude()).floatValue(),
                    Double.valueOf(lastKnownLoc.getAltitude()).floatValue(),
                    System.currentTimeMillis());
            head -= geoField.getDeclination(); // converts magnetic north into true north

            if (bearTo < 0) {
                bearTo = bearTo + 360;
                //bearTo = -100 + 360  = 260;
            }

            float direction = bearTo - head;

            // If the direction is smaller than 0, add 360 to get the rotation clockwise.
            if (direction < 0) {
                direction = direction + 360;
            }

            RotateAnimation ra = new RotateAnimation(currentDegree, direction, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            ra.setDuration(210);
            ra.setFillAfter(true);

            findViewById(R.id.imageView_NavArrow).startAnimation(ra);

            currentDegree = direction;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    private void updateLastKnown() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                lastKnownLoc = location;
                            }
                        }
                    });
        } catch (SecurityException e) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    RuntimeData.LOCATION_PERMISSION_CODE);
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RuntimeData.LOCATION_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, do the
                    // location-related task
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        updateLastKnown();
                    }

                } else {RuntimeData.getInstance().setDisableLocationServices(true);}
                return;
            }

        }
    }
}
