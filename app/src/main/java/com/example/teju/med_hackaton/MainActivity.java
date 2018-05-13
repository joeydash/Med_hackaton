package com.example.teju.med_hackaton;

import android.Manifest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


import com.intentfilter.androidpermissions.PermissionManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

import static java.util.Collections.singleton;

public class MainActivity extends AppCompatActivity implements SensorEventListener  {
    private PermissionManager permissionManager;
    private Context context;
    private TextView tv_sound_intensity;
    private CircularProgressIndicator circularProgress;
    private MediaRecorder mRecorder = null;
    private Vibrator vibrator;
    private final Timer timer = new Timer();
    private  final Handler handler = new Handler();
    SensorManager sensorManager;
    private double ax,ay,az;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        permissionManager = PermissionManager.getInstance(context);
        getAllPermission();


        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        tv_sound_intensity = findViewById(R.id.tv_sound_intensity);
        circularProgress = findViewById(R.id.circular_progress);




        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        initiateMichrophone();
        initiateProgressbar();



        startWork();

    }

    private void getAllPermission() {
        permissionManager.checkPermissions(singleton(Manifest.permission.RECORD_AUDIO), new PermissionManager.PermissionRequestListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(context, "Record Audio Permissions Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(context, "Record Audio Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        });
        permissionManager.checkPermissions(singleton(Manifest.permission.VIBRATE), new PermissionManager.PermissionRequestListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(context, "Vibrate Permissions Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(context, "Vibrate Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initiateProgressbar() {
        circularProgress.setMaxProgress(32000);
        circularProgress.setCurrentProgress(0);
    }


    @Override
    protected void onStop() {
        super.onStop();
        stop();
        timer.cancel();
        timer.purge();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            ax=event.values[0];
            ay=event.values[1];
            az=event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    public void startWork(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {

                    public void run() {
                        double sound_intensity = getAmplitude();
                        if (sound_intensity>10000.0){
                            vibrate();
                        }
                        circularProgress.setCurrentProgress((int) sound_intensity);
                        String viewData = String.valueOf(sound_intensity)+"\n"+ax+"\n"+ay+"\n"+az;

                        tv_sound_intensity.setText(viewData);
                    }
                });
            }
        };

        timer.schedule(timerTask, 0, 100);
    }

    public void vibrate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            vibrator.vibrate(500);
        }
    }

    public void initiateMichrophone(){
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void start() throws IOException {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
            mRecorder.start();
        }
    }
    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }
    public double getAmplitude() {
        if (mRecorder != null)

            return mRecorder.getMaxAmplitude();
        else
            return 0;

    }
}
