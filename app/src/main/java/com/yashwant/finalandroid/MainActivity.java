package com.yashwant.finalandroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.sql.DriverManager.println;

public class MainActivity extends AppCompatActivity
        implements SensorEventListener {


    private SensorManager mSensorManager;


    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;
    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];

    private ImageView mSpotTop;
    private ImageView mSpotBottom;
    private ImageView mSpotLeft;
    private ImageView mSpotRight;

    float azimuth;
    float pitch;
    float roll;
    // Very small values for the accelerometer (on all three axes) should
    // be interpreted as 0. This value is the amount of acceptable
    // non-zero drift.
    private static final float VALUE_DRIFT = 0.05f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lock the orientation to portrait (for now)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // Get accelerometer and magnetometer sensors from the sensor manager.
        // The getDefaultSensor() method returns null if the sensor
        // is not available on the device.
        mSensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);
        mSpotTop = (ImageView) findViewById(R.id.spot_top);
        mSpotBottom = (ImageView) findViewById(R.id.spot_bottom);
        mSpotLeft = (ImageView) findViewById(R.id.spot_left);
        mSpotRight = (ImageView) findViewById(R.id.spot_right);
    }

    /**
     * Listeners for the sensors are registered in this callback so that
     * they can be unregistered in onStop().
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Listeners for the sensors are registered in this callback and
        // can be unregistered in onStop().
        //
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        // (SENSOR_DELAY_NORMAL).
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorMagnetometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unregister all sensor listeners in this callback so they don't
        // continue to use resources when the app is stopped.
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int sensorType = sensorEvent.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = sensorEvent.values.clone();
                break;
            default:
                return;
        }
        float[] rotationMatrix = new float[9];
        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                null, mAccelerometerData, mMagnetometerData);
        float orientationValues[] = new float[3];
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);
        }
        azimuth = orientationValues[0];
         pitch = orientationValues[1];
         roll = orientationValues[2];

        if(pitch == 0 && roll == 0 && azimuth==0)
        {

            sendNow();

        }
        if (Math.abs(pitch) < VALUE_DRIFT) {
            pitch = 0;
        }
        if (Math.abs(roll) < VALUE_DRIFT) {
            roll = 0;
        }
        mSpotTop.setAlpha(0f);
        mSpotBottom.setAlpha(0f);
        mSpotLeft.setAlpha(0f);
        mSpotRight.setAlpha(0f);
        if (pitch > 0) {
            mSpotBottom.setAlpha(pitch);
        } else {
            mSpotTop.setAlpha(Math.abs(pitch));
        }
        if (roll > 0) {
            mSpotLeft.setAlpha(roll);
        } else {
            mSpotRight.setAlpha(Math.abs(roll));
        }

    }

    /**
     * Must be implemented to satisfy the SensorEventListener interface;
     * unused in this app.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void sendNow(View view) {




     //   FirebaseDatabase ref = FirebaseDatabase.getInstance();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        Map map = new HashMap();
        map.put("timestamp", ServerValue.TIMESTAMP);

        map.put("x-Value" , azimuth);
        map.put("y",pitch);
        map.put("z",roll);
        ref.child("yourNode").updateChildren(map);


      String a = "y=" +azimuth + "x =" + pitch + "Z = " + roll;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"1")
                .setSmallIcon(R.drawable.spot)
                .setContentTitle("Data Sent")
                .setContentText(a)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
         notificationManager.notify(1, builder.build());

    }
    public void sendNow() {




        //   FirebaseDatabase ref = FirebaseDatabase.getInstance();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        Map map = new HashMap();
        map.put("timestamp", ServerValue.TIMESTAMP);

        map.put("x-Value" , azimuth);
        map.put("y",pitch);
        map.put("z",roll);
        ref.child("NewNode").updateChildren(map);


    }

    }

