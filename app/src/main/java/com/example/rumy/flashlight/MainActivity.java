package com.example.rumy.flashlight;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private CameraManager mCameraManager;
    private String mCameraId;
    private ImageButton mTorchOnOffButton;
    private Boolean isTorchOn;
    private MediaPlayer mp;
    SensorManager sensorManager;
    Sensor sensor;
    TextView textLIGHT_available;
    TextView textLIGHT_reading;
    private boolean isDark = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FlashLightActivity", "onCreate()");
        setContentView(R.layout.activity_main);
        mTorchOnOffButton = (ImageButton) findViewById(R.id.button_on_off);
        isTorchOn = false;

        Boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {

            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error !!");
            alert.setMessage("Your device doesn't support flash light!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                    System.exit(0);
                }
            });
            alert.show();
            return;
        }else{
            FloatingActionButton fab_pref = findViewById(R.id.fab_pref);
            fab_pref.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent modifySettings = new Intent(MainActivity.this, SettingsActivity2.class);
                    startActivity(modifySettings);
                }
            });
        }


        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mTorchOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isTorchOn) {
                        //turnOffFlashLight();
                        isTorchOn = false;
                    } else {
                        //turnOnFlashLight();
                        isTorchOn = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        textLIGHT_available = (TextView) findViewById(R.id.LIGHT_available);
        textLIGHT_reading = (TextView) findViewById(R.id.LIGHT_reading);

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (sensor != null) {
            textLIGHT_available.setText("Sensor.TYPE_LIGHT Available");
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            textLIGHT_available.setText("Sensor.TYPE_LIGHT NOT Available");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            textLIGHT_reading.setText("LIGHT: " + event.values[0]);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            int valueDark=Integer.parseInt(sharedPrefs.getString("light_sensor", "100"));
            if (isTorchOn) {
                if (event.values[0] > valueDark ) {
                    isDark = false;
                    turnOffFlashLight();
                } else {
                    isDark = true;
                    turnOnFlashLight();
                }
                //turnOffFlashLight();
                //isTorchOn = false;
            }else {
                turnOffFlashLight();
            }
//                isTorchOn = true;
//            }
//            if (event.values[0] > valueDark ) {
//                isDark = false;
//            } else {
//                isDark = true;
//            }
        }
    }


    public void turnOnFlashLight() {

        try {
            if (isDark && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, true);
                //playOnOffSound();
                mTorchOnOffButton.setImageResource(R.drawable.on);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void turnOffFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, false);
                //playOnOffSound();
                mTorchOnOffButton.setImageResource(R.drawable.off);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playOnOffSound() {

        mp = MediaPlayer.create(MainActivity.this, R.raw.flash_sound);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }
        });
        mp.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isTorchOn) {
            turnOffFlashLight();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTorchOn) {
            turnOffFlashLight();
        }
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTorchOn) {
            turnOnFlashLight();
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
