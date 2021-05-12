package com.example.stridor_app;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.InputFilter;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import net.cr0wd.snackalert.SnackAlert;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


/**
 * Displays all users recorded using the app with gender and uid; add user
 */
public class UsersActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private CardArrayAdapter cardArrayAdapter;
    private ListView listView;
    private PopupWindow popupWindow;
    private TextView noUser;
    final Calendar myCalendar = Calendar.getInstance();
    EditText dob;


    /**
     * Executes when page is opened
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show back navigation button in toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        noUser = findViewById(R.id.nouser);
        listView = (ListView) findViewById(R.id.card_listView);
        listView.addHeaderView(new View(this));
        listView.addFooterView(new View(this));

        Button nUserBtn = findViewById(R.id.fab);
        nUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonShowPopupWindowClick(view);
            }
        });

        // populates UI
        populateData();
    }

    /**
     * Fetches data from firebase database for list of users and populates it into the listview
     */
    public void populateData(){
        try{
            // Fetching users from firebase DB and perform operations
            DBManager.fetchUsers(new DBManager.DataSnapshotCallback() {
                @Override
                public void onSuccess(DataSnapshot value) {
                    cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), R.layout.list_item_user_card);
                    for(DataSnapshot it : value.getChildren()) {
                        HashMap<String, Object> res = (HashMap<String, Object>) it.getValue();
                        String uid = it.getKey();
                        String name = (String) res.get("NAME");
                        String gender = (String) res.get("GENDER");
                        String dob = (String)res.get("DOB");

                        //Creating a user card with user data and adding to list
                        UserCard card = new UserCard(uid, name, gender, dob);
                        cardArrayAdapter.add(card);
                    }

                    if(cardArrayAdapter.getCount() == 0){
                        noUser.setVisibility(View.VISIBLE);
                    }else{
                        noUser.setVisibility(View.GONE);
                    }

                    listView.setAdapter(cardArrayAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        // Opens User page Activity when User card is clicked
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            UserCard card = (UserCard) parent.getAdapter().getItem(position);
                            Intent i = new Intent(UsersActivity.this, UserActivity.class);
                            i.putExtra("ID",card.getId());
                            startActivity(i);
                        }
                    });

                }
            });

            // Displays a alert message if user is not connected to internet
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
            noUser.setVisibility(View.VISIBLE);
        }
    }


    /** Opens popup window when new user button is clicked.
     * Contains user metadata form fields to be filled by user which is then validated.
     * If valid - data is saved to firebase and users list is updated and success snackbar is shown
     * else error snackbar is shown
     * @param view
     */
    public void onButtonShowPopupWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.new_user_popup, null);

        final Spinner gender_dropdown = popupView.findViewById(R.id.gender_dropdown);
        gender_dropdown.setOnItemSelectedListener(this);


        int w =LinearLayout.LayoutParams.MATCH_PARENT;
        int h = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, w, h, focusable);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        w = size.x;
        h = size.y;
        popupWindow.setWidth(w);
        popupWindow.setHeight((int)(h*0.7));

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

        dob= (EditText) popupView.findViewById(R.id.dob);
        EditText mbl = popupView.findViewById(R.id.mobile_edit_text);
        EditText hh = popupView.findViewById(R.id.height_edit_text);
        EditText ww = popupView.findViewById(R.id.weight_edit_text);
        mbl.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "9999999999")});
        hh.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "999")});
        ww.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "999")});

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                DatePickerDialog dialog = new DatePickerDialog(UsersActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMaxDate(new Date().getTime());

                dialog.show();
            }
        });

        TextView cancel_btn = popupView.findViewById(R.id.cancel_button);
        TextView proceed_btn = popupView.findViewById(R.id.next_button);

        proceed_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String name = ((EditText)popupView.findViewById(R.id.name_edit_text)).getText().toString();
                    String mobile = ((EditText)popupView.findViewById(R.id.mobile_edit_text)).getText().toString();
                    String dob = ((EditText)popupView.findViewById(R.id.dob)).getText().toString();
                    int height = Integer.parseInt(((EditText)popupView.findViewById(R.id.height_edit_text)).getText().toString());
                    int weight = Integer.parseInt(((EditText)popupView.findViewById(R.id.weight_edit_text)).getText().toString());
                    String illness = ((EditText)popupView.findViewById(R.id.illness_edit_text)).getText().toString();
                    String gender = gender_dropdown.getSelectedItem().toString();

                    String err = validateUser(mobile,height,weight);
                    if(err != null){
                        new AlertDialog.Builder(UsersActivity.this)
                                .setTitle("Form error")
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setMessage(err)
                                .show();
                    }else{
                        DBManager.insertUser(name,mobile,gender,dob,height,weight,illness);
                        popupWindow.dismiss();
                        View vv = findViewById(android.R.id.content).getRootView();

                        SnackAlert.longSuccess(vv, "Added new user!");
                    }
                }catch (Exception e){
                    SnackAlert.longError(v, "Required fields have to be filled");
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

    /**
     * Utility function to validata user metadata
     * @param mobile
     * @param h
     * @param w
     * @return
     */
    String validateUser(String mobile, int h, int w){
        if(mobile.length() < 10){
            return "Enter a valid mobile number";
        }else
        if(h > 250){
            return "Enter valid height";
        }else if(w > 250){
            return "Enter valid weight";
        }
        return null;
    }

    /**
     * formats date to be displayed to dd/MM/yyyy format
     */
    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dob.setText(sdf.format(myCalendar.getTime()));
    }



//    popupView.setOnTouchListener(new View.OnTouchListener() {
//        int orgX, orgY;
//        int offsetX, offsetY;
//
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    orgX = (int) event.getX();
//                    orgY = (int) event.getY();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    offsetX = (int)event.getRawX() - orgX;
//                    offsetY = (int)event.getRawY() - orgY;
//                    popupWindow.update(0, offsetY, -1, -1, true);
//                    break;
//            }
//            return true;
//        }});

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

//    @Override
//    public void onBackPressed() {
//    }

    /**
     * called when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * utility function to check if device is online
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

}