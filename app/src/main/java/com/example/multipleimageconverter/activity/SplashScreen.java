package com.example.multipleimageconverter.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.example.multipleimageconverter.R;

import java.io.File;

public class SplashScreen extends AppCompatActivity {

    private final Context context = this;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable onNext = this::onNext;
    TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

/*        File file = getFileStreamPath(titleText + ".html");

        JSONObject root = new JSONObject();
        JSONArray arr = new JSONArray();
        root.put("key",(Object)arr);*/
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(onNext, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        File file = new File(Environment.getExternalStoragePublicDirectory("").getAbsolutePath() + "/MultiImageConverter");
//         ();
        if (!file.exists()) {
            file.mkdirs();
        }
        handler.removeCallbacks(onNext);
    }

    private void onNext() {
        startActivity(new Intent(context, MainActivity.class));
        finish();
    }
}