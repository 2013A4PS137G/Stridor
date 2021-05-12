package com.example.stridor_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Used in v1 with local DB not used in current version
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Information
    static final String DB_NAME = "STRIDOR.DB";

    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_USERS_TABLE = "create table USERS(ID TEXT PRIMARY KEY, NAME TEXT NOT NULL,MOBILE INTEGER, GENDER TEXT NOT NULL, AGE INTEGER NOT NULL," +
            " HEIGHT INTEGER NOT NULL, WEIGHT INTEGER NOT NULL, ILLNESS TEXT);";
    private static final String CREATE_RECORDINGS_TABLE = "create table RECORDINGS(ID TEXT NOT NULL, TYPE TEXT, PATH TEXT PRIMARY KEY, TASK TEXT NOT NULL, DURATION TEXT);";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_RECORDINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS USERS");
        db.execSQL("DROP TABLE IF EXISTS RECORDINGS");
        onCreate(db);
    }
}