package com.example.teju.med_hackaton;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView tv_sound_intensity;
    private MediaRecorder mRecorder = null;
    private Vibrator vibrator;
    private final Timer timer = new Timer();
    private  final Handler handler = new Handler();
    private int count = 40;
    private DataPoint[] values = new DataPoint[count];

    GraphView graph;
    LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tv_sound_intensity = findViewById(R.id.tv_sound_intensity);
        graph = findViewById(R.id.graph);

        initiateMichrophone();
        startWork();
        initiateGraphDataPoints();
        startGraph();
    }


    @Override
    protected void onStop() {
        super.onStop();
        stop();
        timer.cancel();
        timer.purge();

    }
    public void initiateGraphDataPoints(){
        for (int i=0; i<count; i++) {
            double x = 0.0;
            double y = 0.0;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
    }

    public void startGraph(){

        series = new LineGraphSeries<>(values);
        graph.addSeries(series);
    }
    public void updateGraph(double x,double y){
        DataPoint v = new DataPoint(x, y);
        for (int i=1; i<count; i++) {
            values[i-1] = values[i];
        }
        values[count-1] = v;
        series = new LineGraphSeries<>(values);
        graph.addSeries(series);
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
                        Long tsLong = System.currentTimeMillis()/1000;
                        updateGraph((double)tsLong,sound_intensity);
                        tv_sound_intensity.setText(String.valueOf(sound_intensity));
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
