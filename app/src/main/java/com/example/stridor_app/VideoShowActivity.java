package com.example.stridor_app;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoShowActivity extends AppCompatActivity {
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_show);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        videoView = findViewById(R.id.videoView);
        Bundle extras = getIntent().getExtras();
        Uri videoUri = (Uri) extras.get("videoUri");
        videoView.setVideoURI(videoUri);
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        videoView.start();
    }
}