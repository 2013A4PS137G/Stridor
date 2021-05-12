package com.example.stridor_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * TODO: Open with button click in SettingsActivity - to show/edit/save user metadata
 */
public class UserDetailsActivity extends AppCompatActivity {

    /**
     * Executes when page is opened
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
    }
}