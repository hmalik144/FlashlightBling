package com.example.h_mal.flashlightbling;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;

public class MainActivity extends AppCompatActivity {

    ToggleButton flashLight;
    private static final int CAMERA_REQUEST = 50;
    public static boolean flashLightStatus = false;

    private SensorManager sm;
    private SensorEventListener SensorEventListener;
    private float acelVal;
    private float acelLast;
    private float shake;
    public static boolean hasCameraFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flashLight = (ToggleButton) findViewById(R.id.Flashlight);
        flashLight.setEnabled(false);
        shake = 0.00f;
        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;

        final RelativeLayout rel = (RelativeLayout) findViewById(R.id.rel);
        rel.setBackgroundColor(Color.BLACK);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        SensorEventListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                acelLast = acelVal;
                acelVal = (float) Math.sqrt((double) (x * x + y * y + z * z));
                float delta = acelVal - acelLast;
                shake = shake * 0.9f + delta;
                if (shake > 12) {
                    if (hasCameraFlash) {
                        if (flashLightStatus)
                            flashLightOff();
                        else
                            flashLightOn();
                    } else {
                        Toast.makeText(MainActivity.this, "No flash available on your device",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sm.registerListener(SensorEventListener, sm.getDefaultSensor(TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);


        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);

        hasCameraFlash = getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        flashLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasCameraFlash) {
                    if (!flashLight.isChecked()) {
                        rel.setBackgroundColor(Color.WHITE);
                        sm.unregisterListener(SensorEventListener);

                    }else {
                        rel.setBackgroundColor(Color.BLACK);
                        sm.registerListener(SensorEventListener, sm.getDefaultSensor(TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No flash available on your device",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            flashLightStatus = true;
        } catch (CameraAccessException e) {
        }
    }

    public void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            flashLightStatus = false;
        } catch (CameraAccessException e) {
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    flashLight.setEnabled(true);
                    Toast.makeText(this, "Flashlight activated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied for the Camera", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
