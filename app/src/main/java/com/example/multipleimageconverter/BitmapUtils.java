package com.example.multipleimageconverter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.loader.content.CursorLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class BitmapUtils  {
    public  static Bitmap GetFromAssetFolder(Context context, URI imageName) {
        InputStream its;
        try{
            AssetManager assetManager=context.getAssets();
            its=assetManager.open(String.valueOf(imageName));
            return BitmapFactory.decodeStream(its);
        }catch (IOException e){
            Log.e("", "Exception: " + e.getMessage());
            return null;
        }


    }

    public static Bitmap getBitmapFromGallery(Context context, Uri path) {
//        String[] filePathColumn = {MediaStore.Images.Media.DATA};
      String[] filePathColumn= {Environment.getExternalStorageDirectory().getAbsolutePath() +".MultiImageConverter",null};

        Cursor cursor = context.getContentResolver().query(path, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndexOrThrow(Environment.getExternalStorageDirectory().getAbsolutePath() +".MultiImageConverter");
        String picturePath = cursor.getString(columnIndex);
        return BitmapFactory.decodeFile(picturePath);
    }




    public static final String insertImage(ContentResolver cr,
                                           Bitmap source,
                                           String title,
                                           String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }


}

