package com.example.stridor_app;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UserCard extends Card {
    private String id;
    private String name;
    private String gender;
    private String dob;

    public UserCard(String id, String name, String gender, String dob) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.dob = dob;
    }

    @Override
    public void setView(View row){
        TextView uid = row.findViewById(R.id.user_uid);
        TextView name = row.findViewById(R.id.user_name);
        TextView agender = row.findViewById(R.id.user_agender);

        uid.setText("UID: " + this.id);
        name.setText(this.name);
        agender.setText(this.gender + ", DOB: " + this.dob);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }
}
