package com.example.stridor_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;

import net.cr0wd.snackalert.SnackAlert;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class VideoCaptureActivity extends AppCompatActivity implements View.OnClickListener {

    private CardArrayAdapter cardArrayAdapter;
    private ListView listView;
    private PopupWindow popupWindow;
    private TextView noRec;
    private Button recBtn;
    private String uid;
    private String name;
    private String gender;
    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
            dispatchTakeVideoIntent();
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.

            new AlertDialog.Builder(VideoCaptureActivity.this)
                    .setTitle("")
                    .setMessage("Grant READ_EXTERNAL_STORAGE permission in settings to access this function")
                    .setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Whatever...
                        }
                    }).show();
        }
    });

    private String task;
    private String rec_id;
    private String dob;
    private int height;
    private int weight;

    static final int REQUEST_VIDEO_CAPTURE = 1;

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void check_permission_and_take_video(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            dispatchTakeVideoIntent();
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.

            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            String uriPath = Utils.getPathFromURI(this,videoUri);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, videoUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = Long.parseLong(time)/1000;
            retriever.release();

            addRecording("VIDEO",uriPath,duration);
        }
    }

    private void showVideo(Uri videoUri){
        Intent i = new Intent(VideoCaptureActivity.this,VideoShowActivity.class);
        i.putExtra("videoUri",videoUri);
        startActivity(i);
    }

    private void showAudio(Uri audioUri){
//        Intent i = new Intent(VideoCaptureActivity.this,AudioShowActivity.class);
//        i.putExtra("audioUri",audioUri);
//        startActivity(i);

//        String mime = getContentResolver().getType(audioUri);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(audioUri, "application/pdf");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(intent);
        }
        catch (ActivityNotFoundException e) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle extras = getIntent().getExtras();
        this.uid = extras.getString("ID");

        noRec = findViewById(R.id.norec);
        listView = (ListView) findViewById(R.id.card_listView);
        listView.addHeaderView(new View(this));
        listView.addFooterView(new View(this));

        recBtn = findViewById(R.id.buttonRecord);
        recBtn.setOnClickListener(this);
        Button delete_user = findViewById(R.id.buttonDelete);

        setMetaData();
        delete_user.setOnClickListener(this);

        populateData();
    }

    public void setMetaData() {
        try {
            DBManager.fetchUser(this.uid, new DBManager.DataSnapshotCallback() {
                @Override
                public void onSuccess(DataSnapshot value) {
                    HashMap<String, Object> res = (HashMap<String, Object>) value.getValue();
                    name = (String) res.get("NAME");
                    gender = (String) res.get("GENDER");
                    dob = (String) res.get("DOB");
                    height = ((Long)res.get("HEIGHT")).intValue();
                    weight = ((Long)res.get("WEIGHT")).intValue();

                    ((TextView)findViewById(R.id.name)).setText(name);
                    ((TextView)findViewById(R.id.gender)).setText(gender);
                    ((TextView)findViewById(R.id.age)).setText(dob);
                    ((TextView)findViewById(R.id.height)).setText(height + " cm");
                    ((TextView)findViewById(R.id.weight)).setText(weight + " Kg");
                }
            });

            if(!isOnline()){
                new AlertDialog.Builder(this)
                        .setTitle("No Internet connection")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setMessage("some features on this page may not work properly without an active internet connection")
                        .show();
            }

        }catch (Exception e){

        }
    }

    public void populateData(){
        try{

            DBManager.fetchRecordings(this.uid, new DBManager.DataSnapshotCallback() {
                @Override
                public void onSuccess(DataSnapshot value) {
                    cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), R.layout.list_item_recording_card);

                    for(DataSnapshot it : value.getChildren()){
                        HashMap<String, Object> res = (HashMap<String, Object>) it.getValue();
                        String ts = it.getKey();
                        String media = (String) res.get("MEDIA");
                        String path = (String) res.get("PATH");
                        String task = (String) res.get("TASK");
                        String duration = (String) res.get("DURATION");
                        String rec_id = (String) res.get("REC_ID");
                        RecordingCard card = new RecordingCard(VideoCaptureActivity.this,uid,ts, media,path,task,duration, rec_id);
                        cardArrayAdapter.add(card);
                    }

                    if(cardArrayAdapter.getCount() == 0){
                        noRec.setVisibility(View.VISIBLE);
                    }else{
                        noRec.setVisibility(View.GONE);
                    }

                    listView.setAdapter(cardArrayAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            RecordingCard card = (RecordingCard) parent.getAdapter().getItem(position);
                            if(card.getMedia().equalsIgnoreCase("AUDIO")){
                                showAudio(Uri.parse(card.getUriPath()));
                            }else{
                                showVideo(Uri.parse(card.getUriPath()));
                            }
                        }
                    });

                }
            });

        }catch (Exception e){
            noRec.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.buttonRecord:
                onButtonShowPopupWindowClick(v);
                break;
            case R.id.buttonDelete:
                new AlertDialog.Builder(VideoCaptureActivity.this)
                        .setTitle("")
                        .setMessage("Are you sure you want to delete this user and all of its data?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBManager.deleteUser(uid);
                                finish();
                                Intent i = new Intent(VideoCaptureActivity.this,MainActivity.class);
                                startActivity(i);
                            }
                        }).show();
                break;
        }
    }

    public void onButtonShowPopupWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.select_task_popup, null);

        int w = LinearLayout.LayoutParams.MATCH_PARENT;
        int h = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, w, h, focusable);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        w = size.x;
        h = size.y;
        popupWindow.setWidth(w);
        popupWindow.setHeight((int)(h*0.5));

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

        TextView cancel_btn = popupView.findViewById(R.id.cancel_button);
        TextView proceed_btn = popupView.findViewById(R.id.next_button);

        proceed_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rec_id = ((EditText)popupView.findViewById(R.id.rec_id_edit_text)).getText().toString();
                if(rec_id == null || rec_id.length() == 0){
                    new AlertDialog.Builder(VideoCaptureActivity.this)
                            .setTitle("Form error")
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setMessage("Enter a valid Recording ID")
                            .show();
                }else {
                    setRecID(rec_id);
                    Spinner task_dropdown = popupView.findViewById(R.id.task_dropdown);
                    setTask(task_dropdown.getSelectedItem().toString().toUpperCase());
                    Spinner media_dropdown = popupView.findViewById(R.id.media_dropdown);
                    if (media_dropdown.getSelectedItem().toString().equalsIgnoreCase("AUDIO")) {
                        Intent i = new Intent(VideoCaptureActivity.this, RecordAudioActivity.class);
                        startActivity(i);
                    } else {
                        check_permission_and_take_video();
                    }
                    popupWindow.dismiss();
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

    }

    public void setTask(String task) {
        this.task = task;
    }
    public void setRecID(String rec_id) {
        this.rec_id = rec_id;
    }

    void addRecording(String media,String file_path,long duration){
        String duration_str =  String.format("%d min, %d sec",
                TimeUnit.SECONDS.toMinutes(duration),
                TimeUnit.SECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration))
        );

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String time_stamp = sdf.format(now);

        DBManager.insertRecording(this.uid,time_stamp,media,file_path,this.task,duration_str,this.rec_id);

        View vv = findViewById(android.R.id.content).getRootView();
        SnackAlert.longSuccess(vv, "Added new recording!");
    }


    protected void onNewIntent (Intent intent){
        super.onNewIntent(intent);
        try {
            if(intent.hasExtra("audio_file_path")){
                String audio_path = intent.getStringExtra("audio_file_path");
                long duration = intent.getLongExtra("duration",0);
                addRecording("AUDIO",audio_path,duration);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}