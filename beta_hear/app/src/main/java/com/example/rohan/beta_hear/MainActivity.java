package com.example.rohan.beta_hear;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import static android.media.AudioFormat.CHANNEL_OUT_MONO;
import static android.media.AudioFormat.ENCODING_PCM_FLOAT;
import static android.media.AudioManager.AUDIO_SESSION_ID_GENERATE;
import static android.media.AudioManager.STREAM_MUSIC;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button play;
    Button heard_it;
    ProgressBar progressBar;
    SeekBar seekBar; // for duration
    AudioTrack audioTrack;
    TextView textView; // to display the intensity level at which the user hears the sound
    PlayTask playTask = new PlayTask();

    void initViews() {
        setContentView(R.layout.activity_main);
        play = findViewById(R.id.button1);
        heard_it = findViewById(R.id.button2);
        progressBar = findViewById(R.id.pb1);
        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView2);
        textView.setText("Decibels");
        progressBar.setMax(15);
        seekBar.setMax(200);
        seekBar.setProgress(180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        play.setOnClickListener(this);
        heard_it.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                int max = progressBar.getMax();
                playTask.execute(max);
                break;
            case R.id.button2:
                playTask.cancel(true);
        }
    }

    private class PlayTask extends AsyncTask <Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... max) {
            for (int i = 0; i <= max[0]; i++) {
                double secs = 2.1;
                publishProgress(i);
                try {
                    Thread.sleep((long) (secs * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isCancelled())
                    break;
            }
            return progressBar.getProgress();   //for the onCancelled()
        }

        private int getIntensity(int progress) { // return the intensity level in dB from the progress
            int intensity;
            switch (progress) {
                case 0:
                    intensity = -95;
                    break;
                case 1:
                    intensity = -85;
                    break;
                case 2:
                    intensity = -75;
                    break;
                case 3:
                    intensity = -70;
                    break;
                case 4:
                    intensity = -65;
                    break;
                case 5:
                    intensity = -60;
                    break;
                case 6:
                    intensity = -55;
                    break;
                case 7:
                    intensity = -50;
                    break;
                case 8:
                    intensity = -45;
                    break;
                case 9:
                    intensity = -40;
                    break;
                case 10:
                    intensity = -35;
                    break;
                case 11:
                    intensity = -30;
                    break;
                case 12:
                    intensity = -25;
                    break;
                case 13:
                    intensity = -20;
                    break;
                case 14:
                    intensity = -15;
                    break;
                case 15:
                    intensity = -10;
                    break;
                default:
                    intensity = -50;
                    break;
            }
            return intensity;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            double duration = seekBar.getProgress() / 100;
            int progress = progressBar.getProgress();
            int intensity = getIntensity(progress);
            PlayAudioTrack(500, duration, intensity);
            progressBar.incrementProgressBy(1);
        }

        @Override
        protected void onCancelled(Integer result) { // to play when the heard button is clicked
            int intensity = getIntensity(result);
            textView.setText(String.valueOf(intensity));
            textView.append("dB");
        }
    }

    public void PlayAudioTrack(int f, double duration, int intensity) {
        int sampleRate = 48000;        // Samples per second
        double numFrames = sampleRate * duration;
        double frameCounter = 0;
        double power = Math.pow(10, (intensity / 10));
        double amplitude = Math.pow(2 * power, 0.5);

        AudioAttributes aud_att = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setLegacyStreamType(STREAM_MUSIC)
                .build();
        AudioFormat aud_format = new AudioFormat.Builder()
                .setEncoding(ENCODING_PCM_FLOAT)
                .setSampleRate(sampleRate)
                .build();

        int bufsize = android.media.AudioTrack.getMinBufferSize(sampleRate, CHANNEL_OUT_MONO, ENCODING_PCM_FLOAT);

        audioTrack = new AudioTrack(aud_att, aud_format, bufsize, AudioTrack.MODE_STREAM, AUDIO_SESSION_ID_GENERATE);

        float[] buffer = new float[bufsize];

        while (frameCounter < numFrames) {
            double remaining = numFrames - frameCounter;
            int toWrite = (remaining > bufsize) ? bufsize : (int) remaining;

            for (int s = 0; s < toWrite; s++, frameCounter++) {
                buffer[s] = (float) (amplitude * Math.sin(2.0 * Math.PI * f * frameCounter / sampleRate));
            }
            audioTrack.write(buffer, 0, bufsize, AudioTrack.WRITE_BLOCKING);
            audioTrack.play();
        }
        audioTrack.stop();
        audioTrack.release();

    }
}
