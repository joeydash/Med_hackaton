package com.example.teju.med_hackaton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView tv_sound_intensity;
    private MediaRecorder mRecorder = null;
    private Vibrator v;
    private final Timer timer = new Timer();
    private  final Handler handler = new Handler();
    GraphView graph;
    LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tv_sound_intensity = findViewById(R.id.tv_sound_intensity);

        initiateMichrophone();

        startWork();
        startGraph();





    }


    @Override
    protected void onStop() {
        super.onStop();
        stop();
        timer.cancel();
        timer.purge();

    }
    public void startGraph(){
        graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });
        graph.addSeries(series);
    }


    public void startWork(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {

                    public void run() {
                        tv_sound_intensity.setText(String.valueOf(getAmplitude()));
//                        vibrate();
                    }
                });
            }
        };

        timer.schedule(timerTask, 0, 100);
    }

    public void vibrate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(500);
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
