package com.example.multipleimageconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.jsibbold.zoomage.ZoomageView;

public class ImageDetailActivity extends AppCompatActivity {
    ZoomageView imageView;
    String filePathJpg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        imageView=findViewById(R.id.imageView);
        Bundle Extras = getIntent().getExtras();
        if (Extras != null) {

            filePathJpg = Extras.getString("imagesDetail");
        }
           imageView.setImageURI(Uri.parse(filePathJpg));
    }
}