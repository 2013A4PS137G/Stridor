package com.example.stridor_app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.io.File;
import java.io.IOException;

/**
 * Activity to help user record audio; check permissions, controls for record and pause, playback for recorded audio
 */
public class RecordAudioActivity extends AppCompatActivity implements View.OnClickListener{

    private SeekBar prog_bar;
    private TextView timer_text;
    private CountDownTimer pr_timer;
    private MyRecorder recobj = null;
    ImageButton rec_btn,stop_btn;
    Button play_btn,pause_btn,reset_btn;
    Button continue_btn;
    Boolean isPlaying,isPaused;
    Boolean showPlayback;
    int songPos;
    MediaPlayer m;
    Group group;
    private static String audioFile = null;
    SpinKitView spinKitView;

    /**
     * Set UI view variables in onCreate
     */
    void setViewVars(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        spinKitView = findViewById(R.id.spin_kit);
        spinKitView.setVisibility(View.INVISIBLE);

        prog_bar = findViewById(R.id.progressbar);
        prog_bar.setProgress(0);
        prog_bar.setMax(60000);
        prog_bar.setEnabled(false);

        rec_btn = findViewById(R.id.rec_btn);
        rec_btn.setOnClickListener(this);
        rec_btn.setEnabled(true);

        stop_btn = findViewById(R.id.stop_btn);
        stop_btn.setOnClickListener(this);
        stop_btn.setColorFilter(Color.WHITE);
        stop_btn.setEnabled(false);

        play_btn = findViewById(R.id.play_btn);
        play_btn.setOnClickListener(this);

        pause_btn = findViewById(R.id.pause_btn);
        pause_btn.setOnClickListener(this);
        pause_btn.setEnabled(false);

//        reset_btn = findViewById(R.id.reset_btn);
//        reset_btn.setOnClickListener(this);

        continue_btn = findViewById(R.id.done_btn);
        continue_btn.setOnClickListener(this);

        group=(Group)findViewById(R.id.group);
        group.setVisibility(View.INVISIBLE);
    }

//    toggleEnableDisable()

    /**
     * Executes when page is opened
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        setViewVars();

        // Create a new Recorder object
        recobj = new MyRecorder(getCacheDir().toString());
        isPlaying = false;
        isPaused = false;
        showPlayback = false;

        // timer text to show progress
        timer_text = (TextView) findViewById(R.id.timer_text);
        // Create a Media player object for playback
        m = new MediaPlayer();

        runCountdownProgress(new FunctionCallback() {
            public void onFin() {
                stopStuff();
            }
        },0);

    }

    /**
     * Handles all button clicks
     * Currently - (Record) rec_btn , stop_btn (Playback) play_btn, pause_btn, done_btn
     * @param v
     */
    @Override
    public void onClick(View v) {
        boolean isRecording = recobj.isRecording();

        switch (v.getId()) {
            case R.id.rec_btn: {
                if(check_permission()) {
                    if (!isRecording) {
                        prog_bar.setProgress(0);
                        pr_timer.start();
                        rec_btn.setEnabled(false);
                        stop_btn.setEnabled(true);
                        stop_btn.setColorFilter(Color.RED);
                        recobj.startRecording();
                        spinKitView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            }
            case R.id.stop_btn: {
                if(isRecording){
                    stopStuff();
                }
                break;
            }
            case R.id.play_btn: {
                rec_btn.setEnabled(false);
                stop_btn.setColorFilter(Color.WHITE);
                stop_btn.setEnabled(false);
                play_btn.setEnabled(false);
                pause_btn.setEnabled(true);
                isPlaying = true;

                if(isPaused){
                    m.seekTo(songPos);
                    spinKitView.setVisibility(View.VISIBLE);
                    runCountdownProgress(new FunctionCallback() {
                        @Override
                        public void onFin() {
                            rec_btn.setEnabled(true);
                            play_btn.setEnabled(true);
                            pause_btn.setEnabled(false);

                            pr_timer.cancel();
                            prog_bar.setProgress(0);
                            timer_text.setText("00:00");
                            spinKitView.setVisibility(View.INVISIBLE);
                        }
                    },songPos);
                    pr_timer.start();
                    m.start();
                    isPaused = false;
                    isPlaying = true;
                }else{
                    spinKitView.setVisibility(View.VISIBLE);
                    runCountdownProgress(new FunctionCallback() {
                        @Override
                        public void onFin() {
                            pr_timer.cancel();
                            prog_bar.setProgress(0);
                            timer_text.setText("00:00");
                            spinKitView.setVisibility(View.INVISIBLE);
                        }
                    },0);

                    try {
                        m.setDataSource(audioFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        m.prepare();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    m.seekTo(0);
                    m.start();

                    // TODO
                    m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            m.reset();
                            isPlaying = false;
                            isPaused = false;
                            pr_timer.cancel();
                            spinKitView.setVisibility(View.INVISIBLE);
                            prog_bar.setProgress(0);
                            timer_text.setText("00:00");
                            rec_btn.setEnabled(true);
                            play_btn.setEnabled(true);
                            pause_btn.setEnabled(false);
                        }
                    });
                    pr_timer.start();
                }
                isPaused = false;
                break;
            }
            case R.id.pause_btn: {
                rec_btn.setEnabled(true);
                play_btn.setEnabled(true);
                pause_btn.setEnabled(false);
                songPos = m.getCurrentPosition();
                spinKitView.setVisibility(View.INVISIBLE);
                pr_timer.cancel();
                m.pause();
                isPaused = true;
                isPlaying = false;
                break;
            }

//            case R.id.reset_btn: {
//                try {
//                    m.reset();
//                    pr_timer.cancel();
//                }catch (Exception e){
//                }
//                isPaused = false;
//                isPlaying = false;
//                break;
//            }
            case R.id.done_btn: {
                proceed(audioFile,recobj.getDuration());
                break;
            }
        }
    }

    /**
     * Executes when recording is stopped
     */
    void stopStuff(){
        stop_btn.setEnabled(false);
        pr_timer.cancel();
        recobj.stopRecording();
        audioFile = recobj.getFilename();
        prog_bar.setProgress(0);
        rec_btn.setEnabled(true);
        stop_btn.setColorFilter(Color.WHITE);
        timer_text.setText("00:00");
        spinKitView.setVisibility(View.INVISIBLE);
        group.setVisibility(View.VISIBLE);
    }

    /**
     * Executes when user clicks on done button, sends intent back to user page with audio file path
     */
    private void proceed(String audioFile,long duration){
        spinKitView.setVisibility(View.INVISIBLE);
        Intent i = new Intent(RecordAudioActivity.this, UserActivity.class);
        i.putExtra("audio_file_path", audioFile);
        i.putExtra("duration",duration);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    /**
     * Runs a countdown timer from a start offset in millisecond to 60 seconds and callback function to execute during onFinish
     * To control progressbar
     */
    void runCountdownProgress(FunctionCallback func, long startDeltaMilli){
        long tot_time = 60000 - startDeltaMilli;
        pr_timer = new CountDownTimer(tot_time, 10) {

            public void onTick(long millisUntilFinished) {
                int prog_time = (int)(startDeltaMilli + tot_time - millisUntilFinished);
                prog_bar.setProgress(prog_time);
                int prog_time_sec = (int)(prog_time/1000);
                String time_text = "00:";
                if(prog_time_sec < 10)
                    time_text += "0";
                time_text += prog_time_sec;
                timer_text.setText(time_text);
                if(millisUntilFinished < 100){
                    timer_text.setText("01:00");
                    prog_bar.setProgress(60000);
                }
            }

            public void onFinish() {
                func.onFin();
            }
        };
    }

    /**
     * Utiliy Interface for runCountdownProgress
     */
    interface FunctionCallback{
        void onFin();
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.

        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.

            new AlertDialog.Builder(RecordAudioActivity.this)
                    .setTitle("")
                    .setMessage("Grant RECORD_AUDIO permission in settings to access this function")
                    .setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Whatever...
                        }
                    }).show();
        }
    });

    /**
     * Checks if permission is granted for audio record, else prompts user for permission.
     * Returns true if granted, false otherwise
     */
    private boolean check_permission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            return true;
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.

            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            return false;
        }
    }

}