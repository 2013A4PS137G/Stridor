package com.example.stridor_app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 *  Gives user all the information about the application and purpose
 *  TODO: XML to be filled with content
 *  Asks for Consent if it not given and then displays content
 *
 */
public class InfoActivity extends AppCompatActivity implements View.OnClickListener {

    Button view_users_btn;

    /**
     * Executes when page is opened
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean agreed = sharedPreferences.getBoolean("agreed", false);
        if (!agreed) {
            new AlertDialog.Builder(this)
                    .setTitle("Informed consent")
                    .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("agreed", true);
                            editor.commit();
                        }
                    })
                    .setMessage("The records of this study will be kept confidential. Only the researchers associated with this study will be given access to these records." +
                            "I have read the above information and I consent to take part in the study")
                    .show();
        }
        view_users_btn = findViewById(R.id.view_users);
        view_users_btn.setOnClickListener(this);
    }

    /**
     * Handles all button clicks
     * Currently, only view_users_btn
     * @param v
     */
    @Override
    public void onClick(View v) {
        // Move to Users Activity when clicked
        Intent i = new Intent(InfoActivity.this,UsersActivity.class);
        startActivity(i);
    }
}