package com.example.multipleimageconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

public class QRcode extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    boolean CameraPermission = false;
    final int CAMERA_PERM = 1;
    Dialog MyDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        askPermission();
        if (CameraPermission) {

            scannerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mCodeScanner.startPreview();

                }
            });

            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull @NotNull Result result) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myCustomAlertDialog(result);
//                            Toast.makeText(QRcode.this, result.getText(), Toast.LENGTH_LONG).show();

                        }
                    });

                }
            });

        }
    }

    private void askPermission() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(QRcode.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM);


            } else {

                mCodeScanner.startPreview();
                CameraPermission = true;
            }

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {

        if (requestCode == CAMERA_PERM) {


            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                mCodeScanner.startPreview();
                CameraPermission = true;
            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                    new AlertDialog.Builder(this)
                            .setTitle("Permission")
                            .setMessage("Please provide the camera permission for using all the features of the app")
                            .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    ActivityCompat.requestPermissions(QRcode.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM);

                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    }).create().show();

                } else {

                    new AlertDialog.Builder(this)
                            .setTitle("Permission")
                            .setMessage("You have denied some permission. Allow all permission at [Settings] > [Permissions]")
                            .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", getPackageName(), null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();


                                }
                            }).setNegativeButton("No, Exit app", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            finish();

                        }
                    }).create().show();


                }

            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        if (CameraPermission) {
            mCodeScanner.releaseResources();
        }

        super.onPause();
    }

    public void myCustomAlertDialog(Result result) {
        MyDialog = new Dialog(QRcode.this);
        MyDialog.setContentView(R.layout.customdialogueqrcode);
        MyDialog.setTitle("QR Code");

        AppCompatButton _dialogQrShare = MyDialog.findViewById(R.id._dialogQrsave);
        AppCompatButton qrcopy = MyDialog.findViewById(R.id.qrcopy);
        TextView _dialogIVQR = MyDialog.findViewById(R.id._dialogIVQR);

        _dialogQrShare.setEnabled(true);
        qrcopy.setEnabled(true);

        _dialogIVQR.setText(result.getText());
        qrcopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", _dialogIVQR.getText());
                manager.setPrimaryClip(clipData);
                ClipData pasteData = manager.getPrimaryClip();
                ClipData.Item item = pasteData.getItemAt(0);
                String paste = item.getText().toString();
                MyDialog.dismiss();
            }
        });

        _dialogQrShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = _dialogIVQR.getText().toString();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, s);
                startActivity(Intent.createChooser(sharingIntent, "Share text via"));
            }
        });

        if (!MyDialog.isShowing())
            MyDialog.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(QRcode.this, MainActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppPreferences.setButtonCLicked(getApplication(), false);
    }
}
