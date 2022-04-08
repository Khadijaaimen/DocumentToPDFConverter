package com.example.multipleimageconverter.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.multipleimageconverter.recyclerview.MainRecycleViewAdapter;
import com.example.multipleimageconverter.R;
import com.example.multipleimageconverter.adapter.MainAdapter;
import com.example.multipleimageconverter.fragments.FavoritesFragment;
import com.example.multipleimageconverter.fragments.HomeFragment;
import com.example.multipleimageconverter.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainRecycleViewAdapter.OnItemClickListener {
    private MainActivity.ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private MainAdapter mAdapter;
    boolean CameraPermission = false;
    final int CAMERA_PERM = 1;
    //    private CircularProgressBar progressBar;
    private TextView progressBarPercentage;
    ProgressDialog progressDialog;
    Dialog ocrProgressdialog;
    Fragment selectedFragment = null;
    ProgressBar progressbbbbar;
    ProgressDialog progressdialog;
    int a = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedFragment = new Fragment();
//        askPermission();
//
//        progressDialog = new ProgressDialog( this );
//        progressDialog.setCancelable( false );
//        progressDialog.setMessage( "Loading Files...." );*/
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_favorites:
                            progressDialogDment();
                            break;
                        case R.id.nav_search:
                            selectedFragment = new SearchFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

    private void progressDialogDment() {

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.show();
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.black);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                progressDialog.dismiss();

            }
        }, 10000);

        selectedFragment=new FavoritesFragment();



    }

    @Override
    public void onItemClick(View view, File value, int position) {

    }

    @Override
    public void onItemLongClick(View view, File obj, int pos) {
        enableActionMode(pos);
        Toast.makeText(this, "item clicked :" + pos, Toast.LENGTH_SHORT).show();
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }


    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        // ItemTouchHelperClass.isItemSwipe = false;
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_mainactionmode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete) {
//                showCustomDeleteAllDialog(mode);
                return true;
            }
            if (id == R.id.select_all) {
//                selectAll();
                return true;
            }
            if (id == R.id.action_share) {
//                shareAll();
                return true;
            }
//            if (id == R.id.action_copyTo) {
//                copyToAll();
//                return true;
//            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            actionMode = null;
        }

    }

    private void askPermission(){

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ){

                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM);


            }else {

//                mCodeScanner.startPreview();
                CameraPermission = true;
            }

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {

        if (requestCode == CAMERA_PERM){


            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

//                mCodeScanner.startPreview();
                CameraPermission = true;
            }else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){

                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Permission")
                            .setMessage("Please provide the camera permission for using all the features of the app")
                            .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM);

                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    }).create().show();

                }else {

                    new AlertDialog.Builder(this)
                            .setTitle("Permission")
                            .setMessage("You have denied some permission. Allow all permission at [Settings] > [Permissions]")
                            .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package",getPackageName(),null));
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
        if (CameraPermission){
//            mCodeScanner.releaseResources();
        }

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}


