package com.example.multipleimageconverter.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.multipleimageconverter.R;

import java.util.ArrayList;

public class Ready_To_Share extends AppCompatActivity {
    AppCompatImageView imageView2;
    ArrayList<Uri> filterListFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_to_share);
        imageView2=findViewById(R.id.imageView2);

        Bundle bundle = getIntent().getExtras();
//        String message = bundle.getString("mycroppedlist");

        filterListFinal = bundle.getParcelableArrayList("myfilterlist");
        Glide.with(getApplicationContext())
                .load(filterListFinal.get(0))
                .into(imageView2);

    }
}