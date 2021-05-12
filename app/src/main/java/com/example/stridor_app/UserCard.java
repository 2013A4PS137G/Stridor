package com.example.stridor_app;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * UI Card for a User in Users page
 */
public class UserCard extends Card {
    private String id;
    private String name;
    private String gender;
    private String dob;

    /**constructor
     *
     * @param id
     * @param name
     * @param gender
     * @param dob
     */
    public UserCard(String id, String name, String gender, String dob) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.dob = dob;
    }

    /**
     * Sets values in Card's view
     * @param row
     */
    @Override
    public void setView(View row){
        TextView uid = row.findViewById(R.id.user_uid);
        TextView name = row.findViewById(R.id.user_name);
        TextView agender = row.findViewById(R.id.user_agender);

        uid.setText("UID: " + this.id);
        name.setText(this.name);
        agender.setText(this.gender + ", DOB: " + this.dob);
    }

    /**
     * getter
     * @return
     */
    public String getId() {
        return id;
    }

    /**setter
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * getter
     * @return
     */
    public String getName() {
        return name;
    }

    /**setter
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter
     * @return
     */
    public String getGender() {
        return gender;
    }

    /**setter
     *
     * @param gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * getter
     * @return
     */
    public String getDob() {
        return dob;
    }

    /**setter
     *
     * @param dob
     */
    public void setDob(String dob) {
        this.dob = dob;
    }
}
