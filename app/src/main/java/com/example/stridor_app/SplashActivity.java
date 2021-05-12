package com.example.stridor_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.FirebaseDatabase;

/**
 *  Launcher Activity.
 *  Displays App Logo on screen for 3 seconds and then moves on to Info Activity.
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Executes when page is opened
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try
        {
            // Hide toolbar for fullscreen activity
            this.getSupportActionBar().hide();

            // Firebase Database- for data to persist ie., change on device when it changes on the server
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        catch (NullPointerException e){}

        Thread mythread = new Thread() {
            @Override
            public void run() {
                try {

                    // wait 3 seconds
                    sleep(3000);

                    //open info activity
                    Intent i = new Intent(SplashActivity.this, InfoActivity.class);
                    startActivity(i);
                    finish();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        mythread.start();
    }


    /**
     * Executes on pressing virtual back button
      */
    @Override
    public void onBackPressed() {
        //To not allow user to go back by pressing back virtual button
        moveTaskToBack(false);
    }

}