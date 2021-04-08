package com.example.stridor_app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;


public class RecordingCard extends Card{

    private Context context;
    private String time_stamp;
    private String media;
    private String uriPath;
    private String task;
    private String duration;
    private String rec_id;
    private String uid;

    public RecordingCard(Context context,String uid,String time_stamp, String media, String uriPath, String task, String duration, String rec_id) {
        this.context = context;
        this.uid = uid;
        this.time_stamp = time_stamp;
        this.media = media;
        this.uriPath = uriPath;
        this.task = task;
        this.duration = duration;
        this.rec_id = rec_id;
    }

    @Override
    public void setView(View row){
        ImageView media_img = (ImageView) row.findViewById(R.id.media_img);
        if(media.equalsIgnoreCase("VIDEO")){
            media_img.setBackgroundResource(R.drawable.ic_baseline_videocam_24);
        }else{
            media_img.setBackgroundResource(R.drawable.ic_baseline_audiotrack_24);
        }
        TextView uripath = row.findViewById(R.id.uripath);
        uripath.setText(this.rec_id);
        uripath.setTextSize(12);
//        if(this.uriPath.length() > 15){
//            uripath.setTextSize(5);
//        }
        TextView task = row.findViewById(R.id.task);
        task.setText(this.task);
        TextView durationView = row.findViewById(R.id.duration);
        durationView.setText(this.duration);

        ImageButton del_btn = (ImageButton)row.findViewById(R.id.del_rec);
        del_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("")
                        .setMessage("Are you sure you want to delete this recording?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBManager.deleteRecording(uid,time_stamp);
                            }
                        }).show();
            }
        });
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getUriPath() {
        return uriPath;
    }

    public void setUriPath(String uriPath) {
        this.uriPath = uriPath;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}
