package com.example.stridor_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import java.io.ByteArrayOutputStream;

/**
 * Base class for Card UI element in listview
 */
public class Card {

    /**
     * constructor
     */
    public void card(){
    }

    /**
     * Set UI view with values
     * @param row
     */
    public void setView(View row){
    }

    /**
     * convert from bitmap to byte array
      */
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    /** convert from byte array to bitmap
     *
     * @param image
     * @return
     */
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
