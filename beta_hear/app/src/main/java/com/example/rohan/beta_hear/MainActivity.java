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

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    int number_frequencies = 8;
    int number_intensities = 15;
    Button play;
    Button heard_it;
    ProgressBar intensity_progressbar;
    ProgressBar frequency_progressbar;
    SeekBar duration_bar; // for duration
    AudioTrack audioTrack;
    TextView textView; // to display the intensity level at which the user hears the sound
    PlayTask playTask;

    private void initViews() {
        setContentView(R.layout.activity_main);
        play = findViewById(R.id.button_play1);
        heard_it = findViewById(R.id.button_hear1);
        duration_bar = findViewById(R.id.seekBar);
        intensity_progressbar = findViewById(R.id.pb1);
        intensity_progressbar.setMax(number_intensities);
        frequency_progressbar = findViewById(R.id.pb2);
        frequency_progressbar.setMax(number_frequencies);
        textView = findViewById(R.id.textView1);
        textView.setText("Decibels");
        duration_bar.setMax(200);
        duration_bar.setProgress(180);
        play.setOnClickListener(this);
        play.setText("500 Hz");
        heard_it.setOnClickListener(this);
}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_play1:
                /*if(playTask.isCancelled())
                    playTask = new PlayTask();*/
                playTask = new PlayTask();
                int freq = frequency_progressbar.getProgress();
                playTask.execute(freq);
                break;
            case R.id.button_hear1:
                playTask.cancel(true);

        }
    }
    private class PlayTask extends AsyncTask <Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... freq) {
            for (int i = 0; i <= intensity_progressbar.getMax(); i++) {
                double secs = (duration_bar.getProgress() / 100) + 0.2;
                publishProgress(freq[0]);
                try {
                    Thread.sleep((long) (secs * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isCancelled())
                    break;
            }
            return intensity_progressbar.getProgress();   //for the onCancelled()
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
        private int getFrequency(int progress) {
            int frequency;
            switch (progress) {
                case 0:
                    frequency = 100;
                    break;
                case 1:
                    frequency = 200;
                    break;
                case 2:
                    frequency = 400;
                    break;
                case 3:
                    frequency = 800;
                    break;
                case 4:
                    frequency = 1600;
                    break;
                case 5:
                    frequency = 3200;
                    break;
                case 6:
                    frequency = 6400;
                    break;
                case 7:
                    frequency = 12800;
                    break;
                default:
                    frequency = 3200;
            }
            return frequency;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            double duration = duration_bar.getProgress() / 100;
            int intensity = getIntensity(intensity_progressbar.getProgress());
            int frequency = getFrequency(values[0]);
            PlayAudioTrack(frequency, duration, intensity);
            intensity_progressbar.incrementProgressBy(1);
        }
        @Override
        protected void onCancelled(Integer result) { // to play when the heard button is clicked
            frequency_progressbar.incrementProgressBy(1);
            int intensity = getIntensity(result);
            textView.setText(String.valueOf(intensity));
            textView.append("dB");
            intensity_progressbar.setProgress(0);
            int frequency = getFrequency(frequency_progressbar.getProgress());
            String freq_text = String.valueOf(frequency);
            freq_text += " Hz";
            play.setText(freq_text);
        }
    }
    public void PlayAudioTrack(int frequency, double duration, int intensity) {
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
                buffer[s] = (float) (amplitude * Math.sin(2.0 * Math.PI * frequency * frameCounter / sampleRate));
            }
            audioTrack.write(buffer, 0, bufsize, AudioTrack.WRITE_BLOCKING);
            audioTrack.play();
        }
        audioTrack.stop();
        audioTrack.release();

    }
}
