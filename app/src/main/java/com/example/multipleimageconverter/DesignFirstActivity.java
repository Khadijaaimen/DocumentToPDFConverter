package com.example.multipleimageconverter;

import static android.content.ContentValues.TAG;

import static com.example.multipleimageconverter.R.drawable.*;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.mikhaellopez.circularprogressbar.BuildConfig;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DesignFirstActivity extends AppCompatActivity {
    private static final int Merge_Request_CODE = 42;
    private RecyclerViewEmptySupport recyclerView;
    List<File> pdfArrayList;
    List<File> items = null;
    RelativeLayout scanner_ly;
    RelativeLayout qr_ly;
    private MainAdapter mAdapter;
    private static final int PICK_FROM_GALLERY = 46;
    ImageView testingImages;
//    private DesignFirstActivity.ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private static final int RQS_OPEN_DOCUMENT_TREE_ALL = 43;
    private BottomSheetDialog mBottomSheetDialog;
    private DesignFirstActivity currentActivity;
    private static final int RQS_OPEN_DOCUMENT_TREE = 24;
    private static final int REQUEST_CODE_CHOOSE = 97;
    private File selectedFile;
    private boolean rotate;
    RelativeLayout images;
    Dialog ocrProgressdialog;
    RelativeLayout dcment;
    private MainAdapter.OnItemClickListener mOnItemClickListener;
    CharSequence search = "";
    private CircularProgressBar progressBar;
    private TextView progressBarPercentage;
    private TextView progressBarCount;
    AppCompatImageView imageFromMain;
    private SharedPreferences mSharedPreferences;
    EditText searchView;
    AppCompatImageView documentActivity;
    ImageView  scanbtn;
    RelativeLayout idcard_ly;
//    File file;
//    File fileFolder;
    List<Uri> files;
    Fragment fragment ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_design_pdf_creator);
//        FragmentManager fm = getSupportFragmentManager();
//        fragment = fm.findFragmentByTag("myFragmentTag");
//        if (fragment == null) {
//            FragmentTransaction ft = fm.beginTransaction();
//            fragment =new HomeFragment();
//            ft.add(android.R.id.content,fragment,"myFragmentTag");
//            ft.commit();
//        }

     /*   scanner_ly=findViewById(R.id.scanner_ly);
        scanner_ly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartMergeActivity("CameraActivity");
            }
        });
        idcard_ly=findViewById(R.id.idcard_ly);
        idcard_ly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartMergeActivityForMultiple("CaptureMultiple");
            }
        });

        qr_ly=findViewById(R.id.qr_ly);
        qr_ly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(DesignFirstActivity.this,QRcode.class);
                startActivity(intent);
            }
        });*/
//        recyclerView = findViewById(R.id.rv);
       /* scanbtn=findViewById(R.id.scanbtn);

        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ScannerView.class));
            }
        });*/


//        Toolbar toolbar = findViewById(R.id.toolbar);
//        testingImages=findViewById(R.id.testingImages);


//        items = new ArrayList<File>();
//        pdfArrayList = new ArrayList<>();
//        files = new ArrayList<Uri>();

//        imageFromMain = findViewById(R.id.imageFromMain);
//        imageFromMain.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(DesignFirstActivity.this, DesignImages.class);
//                startActivity(intent);
//
//
//
///*                Matisse.from(DesignFirstActivity.this)
//
//                        .choose(MimeType.ofImage())
//                        .capture(false)
//                        .countable(true)
//                        .maxSelectable(20)
//                        .forResult(REQUEST_CODE_CHOOSE);*/
//
//            }
//        });
//        dcment = findViewById(R.id.dcment);
//        searchView = findViewById(R.id.search_bar);
//        images = findViewById(R.id.images);
//        documentActivity = findViewById(R.id.DocumentActivity);
//        documentActivity.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(DesignFirstActivity.this, ImageDesignDocument.class);
//                documentActivity.setImageResource(doc1);
//
//
//                startActivity(intent);
//            }
//        });
/*        images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartMergeActivityForAllImages("ImagesSearch");

        *//*        Matisse.from(DesignFirstActivity.this)

                        .choose(MimeType.ofImage())
                        .capture(false)
                        .countable(true)
                        .maxSelectable(20)
                        .forResult(REQUEST_CODE_CHOOSE);*//*
            }
        });*/


        /*displaypdf();*/

//        searchView.addTextChangedListener(new TextWatcher() {
//                                              @Override
//                                              public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                                              }
//
//                                              @Override
//                                              public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                                                  mAdapter.getFilter().filter(charSequence);
//                                                  search = charSequence;
////                return false;
//                                                  mAdapter.notifyDataSetChanged();
//
////
//
//                                              }
//
//                                              @Override
//                                              public void afterTextChanged(Editable editable) {
//                                              }
//
//                                          }


//        );
//        dcment.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(DesignFirstActivity.this, ImageDesignDocument.class);
//                startActivity(intent);
//            }
//        });
//        setSupportActionBar(toolbar);
//        CheckStoragePermission();

//        RelativeLayout maddCameraFAB = findViewById(R.id.mainaddCameraFAB);
//        RelativeLayout maddFilesFAB = findViewById(R.id.mainaddFilesFAB);
//        mSharedPreferences = getSharedPreferences("configuration", MODE_PRIVATE);
////        ViewAnimation.initShowOut(maddCameraFAB);
//
//        maddFilesFAB.setOnClickListener(v -> StartMergeActivity("FileSearch"));
//
//        maddCameraFAB.setOnClickListener(v -> StartMergeActivity("CameraActivity"));
//
//        CheckStoragePermission();
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
////        drawer.addDrawerListener(toggle);
////        toggle.syncState();
////        navigationView.setNavigationItemSelectedListener(this);
//
////        recyclerView = (RecyclerViewEmptySupport) findViewById(R.id.mainRecycleView);
////        recyclerView.setEmptyView(findViewById(R.id.toDoEmptyView));
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setHasFixedSize(true);
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//            CheckStoragePermission();
//        }
//
//        CreateDataSource();
//        actionModeCallback = new DesignFirstActivity.ActionModeCallback();
//        currentActivity = this;
//        InitBottomSheetProgress();

    }

//    public void StartMergeActivity(String message) {
//        Intent intent = new Intent(getApplicationContext(), ImageToPDF.class);
//        intent.putExtra("ActivityAction", message);
//        startActivityForResult(intent, Merge_Request_CODE);
//
//    }
//
//    public void StartMergeActivityForAllImages(String message) {
//        Intent intent = new Intent(getApplicationContext(), ImageToPDF.class);
//        intent.putExtra("ActivityAction", message);
//        startActivityForResult(intent, Merge_Request_CODE);
//    }
//
//    public void StartMergeActivityForMultiple(String message) {
//        Intent intent = new Intent(getApplicationContext(), ImageToPDF.class);
//        intent.putExtra("ActivityAction", message);
//        startActivityForResult(intent, Merge_Request_CODE);
//    }
//
//    @Override
//    public void onBackPressed() {
///*        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            Boolean doNotDis = mSharedPreferences.getBoolean("doNotDis", false);
//            if (doNotDis) {
//                finish();
//            } else {
//                ShowRatingDialog();
//            }
//        }*/
//        finishAffinity();
//    }
//
//    AlertDialog ratingAlertDialog = null;
//
//    private void ShowRatingDialog() {
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = this.getLayoutInflater();
//
//        ratingAlertDialog = dialogBuilder.create();
//        ratingAlertDialog.show();
//
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.sort_menu, menu);
//        mainMenuItem = menu.findItem(R.id.fileSort);
//        return true;
//    }
//
//    private MenuItem mainMenuItem;
//    private boolean isChecked = false;
//    Comparator<File> comparator = null;
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.nameSort:
//                mainMenuItem.setTitle("Name");
//                comparator = FileComparator.getNameComparator();
//                FileComparator.isDescending = isChecked;
//                sortFiles(comparator);
//                return true;
//            case R.id.modifiedSort:
//                mainMenuItem.setTitle("Modified");
//                comparator = FileComparator.getLastModifiedComparator();
//                FileComparator.isDescending = isChecked;
//                sortFiles(comparator);
//                return true;
//            case R.id.sizeSort:
//                mainMenuItem.setTitle("Size");
//                comparator = FileComparator.getSizeComparator();
//                FileComparator.isDescending = isChecked;
//                sortFiles(comparator);
//                return true;
//            case R.id.ordering:
//                isChecked = !isChecked;
//                if (isChecked) {
//                    item.setIcon(ic_baseline_keyboard_arrow_up_24);
//                } else {
//                    item.setIcon(ic_baseline_keyboard_arrow_down_24);
//                }
//                if (comparator != null) {
//                    FileComparator.isDescending = isChecked;
//                    sortFiles(comparator);
//                } else {
//                    comparator = FileComparator.getLastModifiedComparator();
//                    FileComparator.isDescending = isChecked;
//                    sortFiles(comparator);
//                }
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    private void sortFiles(Comparator<File> comparator) {
//        Collections.sort(mAdapter.items, comparator);
//        mAdapter.notifyDataSetChanged();
//    }
//
//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.home) {
//            // Handle the camera action
//        }
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent result) {
//        super.onActivityResult(requestCode, resultCode, result);
//
//        if (requestCode == Merge_Request_CODE && resultCode == Activity.RESULT_OK) {
//            if (result != null) {
//
//                Uri uri = result.getData();
//                Log.i("tazaUri", "Taza uri we got is : " + uri);
//
//                CreateDataSource();
//                mAdapter.notifyItemInserted(pdfArrayList.size() - 1);
//            }
//        }
//
//        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
//
//
//            files = Matisse.obtainResult(result);
//            Log.d("Matisse", "mSelected: " + files);
//
///*
//            Toast.makeText(this, "Pakistan Zindabad", Toast.LENGTH_SHORT).show();
//*/
//            Log.i("uriparse", "uri parse we got is :" + files.get(0));
//
//            Intent intent = new Intent(DesignFirstActivity.this, CroppedActivity.class);
//            intent.putExtra("imageUri", files.toString());
//            startActivity(intent);
//
//            Log.i("listSize", "onActivityResult: files size " + files.size());
//        }
//
//        if (resultCode == RESULT_OK && requestCode == RQS_OPEN_DOCUMENT_TREE) {
//            if (result != null) {
//                Uri uriTree = result.getData();
//                DocumentFile documentFile = DocumentFile.fromTreeUri(this, uriTree);
//                if (selectedFile != null) {
//                    DocumentFile newFile = documentFile.createFile("application/pdf", selectedFile.getName());
//                    try {
//                        copy(selectedFile, newFile);
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    selectedFile = null;
//                    if (mBottomSheetDialog != null)
//                        mBottomSheetDialog.dismiss();
//                    Toast toast = Toast.makeText(this, "Copy files to: " + documentFile.getName(), Toast.LENGTH_LONG);
//                    toast.show();
//                }
//            }
//        }
//        if (resultCode == RESULT_OK && requestCode == RQS_OPEN_DOCUMENT_TREE_ALL) {
//            if (result != null) {
//                List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
//                ArrayList<Uri> files = new ArrayList<Uri>();
//                Uri uriTree = result.getData();
//                DocumentFile documentFile = DocumentFile.fromTreeUri(this, uriTree);
//                for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
//                    File file = items.get(i);
//                    DocumentFile newFile = documentFile.createFile("application/pdf", file.getName());
//                    try {
//                        copy(file, newFile);
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (actionMode != null)
//                    actionMode.finish();
//
//                Toast toast = Toast.makeText(this, "Copy files to: " + documentFile.getName(), Toast.LENGTH_LONG);
//                toast.show();
//            }
//        }
//    }
//
//    private void CheckStoragePermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//                alertDialog.setTitle("Storage Permission");
//                alertDialog.setMessage("Storage permission is required in order to " +
//                        "provide Image to PDF feature, please enable permission in app settings");
//                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Settings",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
//                                startActivity(i);
//                                dialog.dismiss();
//                            }
//                        });
//                alertDialog.show();
//            } else {
//                // No explanation needed; request the permission
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        2);
//            }
//        }
//    }
//
//    public void copy(File selectedFile, DocumentFile newFile) throws IOException {
//        try {
//            OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
//            FileInputStream in = new FileInputStream(selectedFile.getPath());
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = in.read(buffer)) != -1) {
//                out.write(buffer, 0, read);
//            }
//            in.close();
//            out.flush();
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void CreateDataSource() {
//
//        File root = getFilesDir();
//        File myDir = new File(root + "/MultiImageConverter");
//        if (!myDir.exists()) {
//            myDir.mkdirs();
//        }
//        File[] files = myDir.listFiles();
//
//        Arrays.sort(files, (file1, file2) -> {
//            long result = file2.lastModified() - file1.lastModified();
//            if (result < 0) {
//                return -1;
//            } else if (result > 0) {
//                return 1;
//            } else {
//                return 0;
//            }
//        });
//
//        for (int i = 0; i < files.length; i++) {
//            items.add(files[i]);
//        }
//
//        //set data and list adapter
//        mAdapter = new MainAdapter(this, pdfArrayList, mOnItemClickListener);
//        mAdapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, File value, int position) {
//                if (mAdapter.getSelectedItemCount() > 0) {
//                    enableActionMode(position);
//                } else {
//                    showBottomSheetDialog(value);
//                }
//            }
//
//            @Override
//            public void onItemLongClick(View view, File obj, int pos) {
//                enableActionMode(pos);
//            }
//
//        });
//
//        recyclerView.setAdapter(mAdapter);
//    }
//
//    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//    private void deleteItems() {
//        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
//        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
//            File file = pdfArrayList.get(selectedItemPositions.get(i));
//            file.delete();
//            mAdapter.removeData(selectedItemPositions.get(i));
//            mAdapter.notifyDataSetChanged();
//        }
//
//
//    }
//
//    private void enableActionMode(int position) {
//        if (actionMode == null) {
//            actionMode = startSupportActionMode(actionModeCallback);
//        }
//        toggleSelection(position);
//    }
//
//    private void toggleSelection(int position) {
//        mAdapter.toggleSelection(position);
//        // ItemTouchHelperClass.isItemSwipe = false;
//        int count = mAdapter.getSelectedItemCount();
//
//        if (count == 0) {
//            actionMode.finish();
//        } else {
//            actionMode.setTitle(String.valueOf(count));
//            actionMode.invalidate();
//        }
//    }
//
//    private void selectAll() {
//        mAdapter.selectAll();
//        int count = mAdapter.getSelectedItemCount();
//
//        if (count == 0) {
//            actionMode.finish();
//        } else {
//            actionMode.setTitle(String.valueOf(count));
//            actionMode.invalidate();
//        }
//    }
//
//    @Override
//    public void onItemClick(View view, File value, int position) {
//
//    }
//
//    @Override
//    public void onItemLongClick(View view, File obj, int pos) {
//
//    }
//
//    private class ActionModeCallback implements ActionMode.Callback {
//        @Override
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            mode.getMenuInflater().inflate(R.menu.menu_mainactionmode, menu);
//            return true;
//        }
//
//        @Override
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//            return false;
//        }
//
//        @Override
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            int id = item.getItemId();
//            if (id == R.id.action_delete) {
//                showCustomDeleteAllDialog(mode);
//                return true;
//            }
//            if (id == R.id.select_all) {
//                selectAll();
//                return true;
//            }
//            if (id == R.id.action_share) {
//                shareAll();
//                return true;
//            }
////            if (id == R.id.action_copyTo) {
////                copyToAll();
////                return true;
////            }
//            return false;
//        }
//
//        @Override
//        public void onDestroyActionMode(ActionMode mode) {
//            mAdapter.clearSelections();
//            actionMode = null;
//        }
//
//    }
//
//    public void showCustomDeleteAllDialog(final ActionMode mode) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Are you sure want to delete the selected files?");
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                deleteItems();
//                mode.finish();
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//
//            }
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//
//
///*    Uri contentUri = FileProvider.getUriForFile(getApplicationContext(),
//            getApplicationContext().getPackageName() + ".provider", currentFile);
//    Intent target = ShareCompat.IntentBuilder.from(currentActivity).setStream(contentUri).getIntent();
//                target.setData(contentUri);
//                target.setType("application/pdf");
//                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                if (target.resolveActivity(getPackageManager()) != null) {
//        startActivity(target);
//    }*/
//
//
//    private void shareAll() {
//        Intent target = ShareCompat.IntentBuilder.from(this).getIntent();
//        target.setAction(Intent.ACTION_SEND_MULTIPLE);
//        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
//        ArrayList<Uri> files = new ArrayList<Uri>();
//        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
//            File file = pdfArrayList.get(selectedItemPositions.get(i));
//            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
//            files.add(contentUri);
//        }
//        target.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
//        target.setType("application/pdf");
//        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        if (target.resolveActivity(getPackageManager()) != null) {
//            startActivity(target);
//        }
//        actionMode.finish();
//    }
//
///*    private void shareAll() {
//        Intent target = ShareCompat.IntentBuilder.from(this).getIntent();
//        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
//        if (selectedItemPositions.get(mAdapter.getSelectedItemCount()) <= 1){
//         target.setAction(Intent.ACTION_SEND);
//            File file = pdfArrayList.get(mAdapter.getSelectedItemCount());
//            Uri contentUri = FileProvider
//                    .getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
//            target.setData(contentUri);
//            target.setType("application/pdf");
//            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(target);
//        }else {
//
//            target.setAction(Intent.ACTION_SEND_MULTIPLE);
//            ArrayList<Uri> files = new ArrayList<Uri>();
//            for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
//                File file = pdfArrayList.get(i);
//                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
//                files.add(contentUri);
//            }
//            target.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
//            target.setType("application/pdf");
//            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            if (target.resolveActivity(getPackageManager()) != null) {
//                startActivity(target);
//            }
//        }
//
//        actionMode.finish();
//    }*/
//
//
//    private void copyToAll() {
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        startActivityForResult(intent, RQS_OPEN_DOCUMENT_TREE_ALL);
//    }
////>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//
//    private void showBottomSheetDialog(final File currentFile) {
//        final View view = getLayoutInflater().inflate(R.layout.sheet_list, null);
//
//        ((View) view.findViewById(R.id.lyt_email)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mBottomSheetDialog.dismiss();
//                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", currentFile);
//                Intent target = new Intent(Intent.ACTION_SEND);
//                target.setType("text/plain");
//                target.putExtra(Intent.EXTRA_STREAM, contentUri);
//                target.putExtra(Intent.EXTRA_SUBJECT, "Subject");
//                startActivity(Intent.createChooser(target, "Send via Email..."));
//            }
//        });
//
//        ((View) view.findViewById(R.id.lyt_share)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mBottomSheetDialog.dismiss();
//                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", currentFile);
//                Intent target = ShareCompat.IntentBuilder.from(currentActivity).setStream(contentUri).getIntent();
//                target.setData(contentUri);
//                target.setType("application/pdf");
//                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                if (target.resolveActivity(getPackageManager()) != null) {
//                    startActivity(target);
//                }
//            }
//        });
//
//        ((View) view.findViewById(R.id.lyt_rename)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mBottomSheetDialog.dismiss();
//                showCustomRenameDialog(currentFile);
//
//            }
//        });
//
//        ((View) view.findViewById(R.id.lyt_delete)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mBottomSheetDialog.dismiss();
//                showCustomDeleteDialog(currentFile);
//
//            }
//        });
//
///*        ((View) view.findViewById(R.id.lyt_copyTo)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                startActivityForResult(intent, RQS_OPEN_DOCUMENT_TREE);
//                selectedFile = currentFile;
//            }
//        });*/
//
////        ((View) view.findViewById(R.id.lyt_ocr)).setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                mBottomSheetDialog.dismiss();
////                PDFOCRAsync pdfocrAsync = new PDFOCRAsync(currentFile, currentActivity);
////                try {
////                    pdfocrAsync.openRenderer();
////                    pdfocrAsync.execute();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////
////            }
////        });
//
//        ((View) view.findViewById(R.id.lyt_openFile)).setOnClickListener(new View.OnClickListener() {
//
//
//            @Override
//            public void onClick(View view) {
//                mBottomSheetDialog.dismiss();
//                Intent intent = new Intent(DesignFirstActivity.this, DisplayActivity.class);
//                File file = currentFile.getAbsoluteFile();
//                Uri uriToSend = Uri.fromFile(currentFile.getAbsoluteFile());
//
//                intent.putExtra("file_path", "" + uriToSend);
//                startActivity(intent);
//
//
////                Intent target = new Intent(Intent.ACTION_VIEW);
////                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", currentFile);
////                target.setDataAndType(contentUri, "application/pdf");
////                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
////                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
////
////                Intent intent = Intent.createChooser(target, "Open File");
////                try {
////                    startActivity(intent);
////                } catch (ActivityNotFoundException e) {
////                    //Snackbar.make(mCoordLayout, "Install PDF reader application.", Snackbar.LENGTH_LONG).show();
////                }
//
//            }
//        });
//        mBottomSheetDialog = new BottomSheetDialog(this);
//        mBottomSheetDialog.setContentView(view);
//
//        mBottomSheetDialog.show();
//        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                mBottomSheetDialog = null;
//            }
//        });
//    }
//
//    //:TODO Rename 717 yahan ana hy :
//
//    public void showCustomRenameDialog(File currentFile) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = this.getLayoutInflater();
//        View view = inflater.inflate(R.layout.rename_layout, null);
//        builder.setView(view);
//        final EditText editText = (EditText) view.findViewById(R.id.renameEditText2);
//        editText.setText(currentFile.getName());
//        builder.setTitle("Rename");
//        builder.setPositiveButton("Rename", (dialog, id) -> {
//
////            File root = (File) getFilesDir();
//            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MultiImageConverter");
//            File file = new File(root, editText.getText().toString());
////            File file = new File(root + "/MultiImageConverter", editText.getText().toString());
//            currentFile.renameTo(file);
////                file.renameTo(currentFile);
//            dialog.dismiss();
//            CreateDataSource();
//            mAdapter.notifyItemInserted(pdfArrayList.size() - 1);
//            mAdapter.notifyDataSetChanged();
//            // FIXME: 17/02/2022  Notify kr dy yahan
//
//        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//
//            }
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//        mAdapter.notifyDataSetChanged();
//    }
//
//    public void showCustomDeleteDialog(final File currentFile) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Are you sure want to delete this file?");
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                currentFile.delete();
//                CreateDataSource();
//                mAdapter.notifyItemInserted(pdfArrayList.size() - 1);
//
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//
//            }
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//        mAdapter.notifyDataSetChanged();
//    }
//
//    private void showDialogAbout() {
//        final Dialog dialog = new Dialog(this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
////        dialog.setContentView(R.layout.dialog_about);
//        dialog.setCancelable(true);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(dialog.getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//
//        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
////
////        ((Button) dialog.findViewById(R.id.bt_privcy)).setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                showDialogPrivacy();
////            }
////        });
//        dialog.show();
//        dialog.getWindow().setAttributes(lp);
//    }
//
////    private void showDialogPrivacy() {
////        final Dialog dialog = new Dialog(this);
////        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
////        dialog.setContentView(R.layout.privacy_layout);
////        dialog.setCancelable(true);
////        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
////
////        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
////        lp.copyFrom(dialog.getWindow().getAttributes());
////        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
////        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
////
////        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                dialog.dismiss();
////            }
////        });
////
////        WebView webView = (WebView) dialog.findViewById(R.id.privacy_webview);
////        webView.setVerticalScrollBarEnabled(true);
////        webView.loadUrl("file:///android_asset/Index.html");
////
////        dialog.getWindow().setAttributes(lp);
////        dialog.show();
////    }
//
//    private void InitBottomSheetProgress() {
//
//        ocrProgressdialog = new Dialog(this);
//        ocrProgressdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        ocrProgressdialog.setContentView(R.layout.progressdialog);
//        ocrProgressdialog.setCancelable(false);
//        ocrProgressdialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(ocrProgressdialog.getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//
//        progressBar = (CircularProgressBar) ocrProgressdialog.findViewById(R.id.circularProgressBar);
//        progressBarPercentage = (TextView) ocrProgressdialog.findViewById(R.id.progressPercentage);
//        //  progressBarCount = (TextView) ocrProgressdialog.findViewById(R.id.progressCount);
//
//        ocrProgressdialog.getWindow().setAttributes(lp);
//    }
//
//    public void showBottomSheet(int size) {
//        ocrProgressdialog.show();
//        this.progressBar.setProgressMax(size);
//        this.progressBar.setProgress(0);
//    }
//
//    public void setProgress(int progress, int total) {
//        this.progressBar.setProgress(progress);
//        // this.progressBarCount.setText(progress + "/" + total);
//        int percentage = (progress * 100) / total;
//        this.progressBarPercentage.setText(percentage + "%");
//    }
//
//    public void runPostExecution(java.io.File file) {
//        ocrProgressdialog.dismiss();
//        progressBarPercentage.setText("0%");
//        this.progressBar.setProgress(0);
//        showOCRSuccessDialog(file);
//    }
//
//    public void showOCRSuccessDialog(final java.io.File outputFile) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Success");
//        builder.setMessage("OCRed PDF Created Successfully at " + outputFile.getAbsolutePath());
//        builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//
////                Intent target = new Intent(Intent.ACTION_VIEW);
////                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", outputFile);
////                target.setDataAndType(contentUri, "application/pdf");
////                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
////                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
////                Intent intent=new Intent(MainActivity.this,DisplayActivity.class);
////                intent.putExtra("file_path"+"_"+items);
//
//
////                Intent intent = Intent.createChooser(target, "Open File");
////                try {
////                    startActivity(intent);
////                } catch (ActivityNotFoundException e) {
////                    //Snackbar.make(mCoordLayout, "Install PDF reader application.", Snackbar.LENGTH_LONG).show();
////                }
//            }
//        });
//        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//
//            }
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//
//    public ArrayList<File> findPdf(File file) {
//        ArrayList<File> arrayList = new ArrayList<>();
//            File[] files = file.listFiles();
//
//
//
////        if(file.exists() && !file.isHidden())
//       for (File singleFile : files) {
//
//        if (singleFile.isDirectory() && !singleFile.isHidden() ) {
//                arrayList.addAll(findPdf(singleFile));
//
//            } else {
//                if (singleFile.getName().endsWith(".pdf")) {
//                    arrayList.add(singleFile);
//                }
//
//
//            }
//        }
//
//        return arrayList;
//    }
//
//    public void displaypdf() {
////        Intent intent =new Intent(MainActivity.this,MainActivity.class);
////        startActivity(intent);
//
//
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
//        pdfArrayList = new ArrayList<>();
//
// File file =new File(Environment.getExternalStoragePublicDirectory("").getAbsolutePath() + "/MultiImageConverter");
////         ();
//if (!file.exists())
//{
//    file.mkdirs();
//}
//
//    pdfArrayList.addAll(findPdf(file));
//// pdfArrayList.addAll(findPdf(file));
//    mAdapter = new MainAdapter(this, pdfArrayList, mOnItemClickListener);
//    recyclerView.setAdapter(mAdapter);
//    mAdapter.notifyDataSetChanged();
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Intent intent = new Intent(DesignFirstActivity.this, DesignFirstActivity.class);
//        startActivity(intent);
//    }
//
//


}
