package com.example.multipleimageconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class ScanReader extends AppCompatActivity {
    public static TextView scantext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_reader);
        scantext=(TextView)findViewById(R.id.scantext);
    }
}