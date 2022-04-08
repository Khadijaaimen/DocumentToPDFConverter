package com.example.multipleimageconverter.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.multipleimageconverter.misc.ScanReader;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerView extends AppCompatActivity implements ZXingScannerView.ResultHandler
{
    ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView=new ZXingScannerView(this);
        setContentView(scannerView);

/*        Dexter.this(getApplicationContext())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check*//*();*/
    }

    @Override
    public void handleResult(Result rawResult) {
        ScanReader.scantext.setText(rawResult.getText());
//        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }
}