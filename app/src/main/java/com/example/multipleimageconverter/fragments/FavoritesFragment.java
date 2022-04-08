
package com.example.multipleimageconverter.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multipleimageconverter.activity.DisplayActivity;
import com.example.multipleimageconverter.activity.ImageToPDF;
import com.example.multipleimageconverter.adapter.MainAdapter;
import com.example.multipleimageconverter.recyclerview.MainRecycleViewAdapter;
import com.example.multipleimageconverter.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mikhaellopez.circularprogressbar.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FavoritesFragment extends Fragment implements MainRecycleViewAdapter.OnItemClickListener {
    private static final int Merge_Request_CODE = 42;
    private RecyclerView recyclerView2;
    List<File> pdfArrayList;
    ProgressDialog progressDialog;
    LinearLayoutManager layoutManager;
    List<File> items = null;
    ProgressBar progressBar;
    public static int REQUEST_PERMISSIONS = 1;
    boolean boolean_permission;
    File dir;
    private MainAdapter mAdapter;
    private FavoritesFragment.ActionModeCallback actionModeCallback;
    private android.view.ActionMode actionMode;
    private static final int RQS_OPEN_DOCUMENT_TREE_ALL = 43;
    private BottomSheetDialog mBottomSheetDialog;
    private FavoritesFragment currentActivity;
    private static final int RQS_OPEN_DOCUMENT_TREE = 24;
    private File selectedFile;
    private boolean rotate;
    Boolean isScrolling = false;
    Dialog ocrProgressdialog;
    LinearLayoutCompat dcment;
    private MainAdapter.OnItemClickListener mOnItemClickListener;
    CharSequence search = "";
    //    private CircularProgressBar progressBar;
    private TextView progressBarPercentage;
    private TextView progressBarCount;
    private SharedPreferences mSharedPreferences;
    AppCompatImageView imagesFromDocument;
    EditText searchView;
    int currentItems, totalItems, scrollOutItems;
    AppCompatImageView homefromDocuments;
    AppCompatImageView imagefromDoc;
    boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);


//        progressDialogDment();
        recyclerView2 = view.findViewById(R.id.rv2);

        searchView = view.findViewById(R.id.search_bar2);

        pdfArrayList = new ArrayList<>();
        new PdfAsyncClass().execute();
        //displaypdf();
        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        pdfArrayList = new ArrayList<>();

        pdfArrayList.addAll(getfile(Environment.getExternalStorageDirectory()));

        mAdapter = new MainAdapter(getActivity(), pdfArrayList, mOnItemClickListener);
        recyclerView2.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
              public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                  mAdapter.getFilter().filter(charSequence);
                  search = charSequence;
    //                return false;
                  mAdapter.notifyDataSetChanged();

    //

              }

              @Override
              public void afterTextChanged(Editable editable) {
              }

          }


        );
//        .setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ImageDesignDocument.this, DesignFirstActivity.class);
//                startActivity(intent);
//            }
//        });
//        setSupportActionBar(toolbar);


        CheckStoragePermission();

        LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView2.setLayoutManager(mLayoutManager);

//        recyclerView = (RecyclerViewEmptySupport) findViewById(R.id.mainRecycleView);
//        recyclerView2.setEmptyView(findViewById(R.id.toDoEmptyView));
//        recyclerView2.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView2.setHasFixedSize(true);


//        recyclerView2.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                if (dy > 0) { //check for scroll down
//                    visibleItemCount = mLayoutManager.getChildCount();
//                    totalItemCount = mLayoutManager.getItemCount();
//                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
//
//                    if (loading) {
//                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
//                            loading = false;
//                            Log.v("...", "Last Item Wow !");
//                            // Do pagination.. i.e. fetch new data
//
//                            loading = true;
//                        }
//                    }
//                }
//            }
//        });
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            CheckStoragePermission();
        }

        CreateDataSource();
        actionModeCallback = new FavoritesFragment.ActionModeCallback();
        currentActivity = this;
        InitBottomSheetProgress();
        return view;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void StartMergeActivity(String message) {
        Intent intent = new Intent(getActivity(), ImageToPDF.class);
        intent.putExtra("ActivityAction", message);
        startActivityForResult(intent, Merge_Request_CODE);
    }

    @Override
    public void onBackPressed() {
/*        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Boolean doNotDis = mSharedPreferences.getBoolean("doNotDis", false);
            if (doNotDis) {
                finish();
            } else {
                ShowRatingDialog();
            }
        }*/
        Intent intent = new Intent(getActivity(), HomeFragment.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

  /*  private void ShowRatingDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Rating With 5 Star")
                .setMessage("If you enjoy using this app, would you mind taking a moment to rate it? it won't take more than a minute, Thank you for your support !")

                .setPositiveButton("RATE NOW", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            startActivity(goToMarket);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                        }
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();


    }*/

    AlertDialog ratingAlertDialog = null;

    private void ShowRatingDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.rate_dialog, null);
//        dialogBuilder.setView(dialogView);
//        final AppCompatCheckBox donotDis = dialogView.findViewById(R.id.donotdis);
//        Button rateNow = dialogView.findViewById(R.id.bt_rateNow);
//        rateNow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
//                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
//                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
//                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//                try {
//                    startActivity(goToMarket);
//                } catch (android.content.ActivityNotFoundException anfe) {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
//                }
//            }
//        });
//        Button exit = dialogView.findViewById(R.id.bt_exit);
//        exit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (donotDis.isChecked())
//                    mSharedPreferences.edit().putBoolean("doNotDis", true).commit();
//                if (ratingAlertDialog != null) {
//                    ratingAlertDialog.dismiss();
//                }
//                finish();
//            }
//        });

        ratingAlertDialog = dialogBuilder.create();
        ratingAlertDialog.show();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == Merge_Request_CODE && resultCode == RESULT_OK) {
            if (result != null) {
                CreateDataSource();
                mAdapter.notifyItemInserted(items.size() - 1);
            }
        }
        if (resultCode == RESULT_OK && requestCode == RQS_OPEN_DOCUMENT_TREE) {
            if (result != null) {
                Uri uriTree = result.getData();
                DocumentFile documentFile = DocumentFile.fromTreeUri(getActivity(), uriTree);
                if (selectedFile != null) {
                    DocumentFile newFile = documentFile.createFile("application/pdf", selectedFile.getName());
                    try {
                        copy(selectedFile, newFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    selectedFile = null;
                    if (mBottomSheetDialog != null)
                        mBottomSheetDialog.dismiss();
                    Toast toast = Toast.makeText(getActivity(), "Copy files to: " + documentFile.getName(), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
        if (resultCode == RESULT_OK && requestCode == RQS_OPEN_DOCUMENT_TREE_ALL) {
            if (result != null) {
                List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
                ArrayList<Uri> files = new ArrayList<Uri>();
                Uri uriTree = result.getData();
                DocumentFile documentFile = DocumentFile.fromTreeUri(getActivity(), uriTree);
                for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                    File file = items.get(i);
                    DocumentFile newFile = documentFile.createFile("application/pdf", file.getName());
                    try {
                        copy(file, newFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (actionMode != null)
                    actionMode.finish();

                Toast toast = Toast.makeText(getActivity(), "Copy files to: " + documentFile.getName(), Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    private void CheckStoragePermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Storage Permission");
                alertDialog.setMessage("Storage permission is required in order to " +
                        "provide Image to PDF feature, please enable permission in app settings");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                                startActivity(i);
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }
    }

    public void copy(File selectedFile, DocumentFile newFile) throws IOException {
        try {
            OutputStream out = getActivity().getContentResolver().openOutputStream(newFile.getUri());
            FileInputStream in = new FileInputStream(selectedFile.getPath());
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void CreateDataSource() {

        items = new ArrayList<File>();

        File root = getActivity().getFilesDir();
        File myDir = new File(root + "/MultiImageConverter");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        File[] files = myDir.listFiles();

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                long result = file2.lastModified() - file1.lastModified();
                if (result < 0) {
                    return -1;
                } else if (result > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        for (int i = 0; i < files.length; i++) {
            items.add(files[i]);
        }

        //set data and list adapter
        mAdapter = new MainAdapter(getActivity(), pdfArrayList, mOnItemClickListener);
        mAdapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, File value, int position) {
                if (mAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(position);
                } else {
                    showBottomSheetDialog(value);
                }
            }

            @Override
            public void onItemLongClick(View view, File obj, int pos) {
                enableActionMode(pos);
            }

        });

        recyclerView2.setAdapter(mAdapter);
    }

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void deleteItems() {
        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            File file = pdfArrayList.get(selectedItemPositions.get(i));
            file.delete();
            mAdapter.removeData(selectedItemPositions.get(i));
            mAdapter.notifyDataSetChanged();
        }


    }

    //    private void enableActionMode(int position) {
//        if (actionMode == null) {
//            actionMode = startSupportActionMode(actionModeCallback);
//        }
//        toggleSelection(position);
//    }
    private void enableActionMode(int position) {
        if (actionMode == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                actionMode = getActivity().startActionMode(new android.view.ActionMode.Callback2() {
                    @Override
                    public boolean onCreateActionMode(android.view.ActionMode actionMode, Menu menu) {
                        actionMode.getMenuInflater().inflate(R.menu.menu_mainactionmode, menu);
                        return true;
                        //                    return false;
                    }

                    @Override
                    public boolean onPrepareActionMode(android.view.ActionMode actionMode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(android.view.ActionMode actionMode, MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (id == R.id.action_delete) {
                            showCustomDeleteAllDialog(mBottomSheetDialog.onWindowStartingSupportActionMode((ActionMode.Callback) actionMode));
                            return true;
                        }
                        if (id == R.id.select_all) {
                            selectAll();
                            return true;
                        }
                        if (id == R.id.action_share) {
                            shareAll();
                            return true;
                        }
                        //            if (id == R.id.action_copyTo) {
                        //                copyToAll();
                        //                return true;
                        //            }
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(android.view.ActionMode actionMode) {
                        mAdapter.clearSelections();
                        actionMode = null;

                    }
                }, position);/*startActionMode(actionModeCallback);*/
            }
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

    private void selectAll() {
        mAdapter.selectAll();
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    // TODO : INterface
//    @Override
//    public void onItemClick(View view, File value, int position) {
////        Intent intent=new Intent(MainActivity.this,DisplayActivity.class);
////        File file = items.get(position).getAbsoluteFile();
////        Uri uriToSend = Uri.fromFile(file);
////
////        intent.putExtra("file_path","" + uriToSend);
////        startActivity(intent);
//
//
//    }

//    @Override
//    public void onItemLongClick(View view, File obj, int pos) {
//
//    }

    @Override
    public void onItemClick(View view, File value, int position) {

    }

    @Override
    public void onItemLongClick(View view, File obj, int pos) {

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
                showCustomDeleteAllDialog(mode);
                return true;
            }
            if (id == R.id.select_all) {
                selectAll();
                return true;
            }
            if (id == R.id.action_share) {
                shareAll();
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

    public void showCustomDeleteAllDialog(final ActionMode mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure want to delete the selected files?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItems();
                mode.finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void shareAll() {
        Intent target = ShareCompat.IntentBuilder.from(getActivity()).getIntent();
        target.setAction(Intent.ACTION_SEND_MULTIPLE);
        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
        ArrayList<Uri> files = new ArrayList<Uri>();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            File file = pdfArrayList.get(selectedItemPositions.get(i));
            Uri contentUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
            files.add(contentUri);
        }
        target.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        target.setType("application/pdf");
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (target.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(target);
        }
        actionMode.finish();
    }


    private void copyToAll() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, RQS_OPEN_DOCUMENT_TREE_ALL);
    }
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    private void showBottomSheetDialog(final File currentFile) {
        final View view = getLayoutInflater().inflate(R.layout.sheet_list, null);

        ((View) view.findViewById(R.id.lyt_email)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                Uri contentUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", currentFile);
                Intent target = new Intent(Intent.ACTION_SEND);
                target.setType("text/plain");
                target.putExtra(Intent.EXTRA_STREAM, contentUri);
                target.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                startActivity(Intent.createChooser(target, "Send via Email..."));
            }
        });

        ((View) view.findViewById(R.id.lyt_share)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                Uri contentUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", currentFile);
                Intent target = ShareCompat.IntentBuilder.from(getActivity()).setStream(contentUri).getIntent();
                target.setData(contentUri);
                target.setType("application/pdf");
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (target.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(target);
                }
            }
        });

        ((View) view.findViewById(R.id.lyt_rename)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                showCustomRenameDialog(currentFile);

            }
        });

        ((View) view.findViewById(R.id.lyt_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                showCustomDeleteDialog(currentFile);

            }
        });

        ((View) view.findViewById(R.id.lyt_copyTo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, RQS_OPEN_DOCUMENT_TREE);
                selectedFile = currentFile;
            }
        });

//        ((View) view.findViewById(R.id.lyt_ocr)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mBottomSheetDialog.dismiss();
//                PDFOCRAsync pdfocrAsync = new PDFOCRAsync(currentFile, currentActivity);
//                try {
//                    pdfocrAsync.openRenderer();
//                    pdfocrAsync.execute();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });

        ((View) view.findViewById(R.id.lyt_openFile)).setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                Intent intent = new Intent(getActivity(), DisplayActivity.class);
                File file = currentFile.getAbsoluteFile();
                Uri uriToSend = Uri.fromFile(currentFile.getAbsoluteFile());

                intent.putExtra("file_path", "" + uriToSend);
                startActivity(intent);


//                Intent target = new Intent(Intent.ACTION_VIEW);
//                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", currentFile);
//                target.setDataAndType(contentUri, "application/pdf");
//                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                Intent intent = Intent.createChooser(target, "Open File");
//                try {
//                    startActivity(intent);
//                } catch (ActivityNotFoundException e) {
//                    //Snackbar.make(mCoordLayout, "Install PDF reader application.", Snackbar.LENGTH_LONG).show();
//                }

            }
        });
        mBottomSheetDialog = new BottomSheetDialog(getActivity());
        mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });
    }

    public void showCustomRenameDialog(final File currentFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.rename_layout, null);
        builder.setView(view);
        final EditText editText = (EditText) view.findViewById(R.id.renameEditText2);
        editText.setText(currentFile.getName());
        builder.setTitle("Rename");
        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                File root = Environment.getExternalStorageDirectory();
                File file = new File(root, editText.getText().toString());
                currentFile.renameTo(file);
                dialog.dismiss();
                CreateDataSource();
                mAdapter.notifyItemInserted(pdfArrayList.size() - 1);
                mAdapter.notifyDataSetChanged();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showCustomDeleteDialog(final File currentFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure want to delete this file?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                currentFile.delete();
                CreateDataSource();
                mAdapter.notifyItemInserted(pdfArrayList.size() - 1);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogAbout() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
//        dialog.setContentView(R.layout.dialog_about);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
//
//        ((Button) dialog.findViewById(R.id.bt_privcy)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDialogPrivacy();
//            }
//        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

//    private void showDialogPrivacy() {
//        final Dialog dialog = new Dialog(this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
//        dialog.setContentView(R.layout.privacy_layout);
//        dialog.setCancelable(true);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(dialog.getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//
//        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        WebView webView = (WebView) dialog.findViewById(R.id.privacy_webview);
//        webView.setVerticalScrollBarEnabled(true);
//        webView.loadUrl("file:///android_asset/Index.html");
//
//        dialog.getWindow().setAttributes(lp);
//        dialog.show();
//    }

    private void InitBottomSheetProgress() {

        ocrProgressdialog = new Dialog(getActivity());
        ocrProgressdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ocrProgressdialog.setContentView(R.layout.progressdialog);
        ocrProgressdialog.setCancelable(false);
        ocrProgressdialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(ocrProgressdialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

//        progressBar = (CircularProgressBar) ocrProgressdialog.findViewById(R.id.circularProgressBar);
//        progressBarPercentage = (TextView) ocrProgressdialog.findViewById(R.id.progressPercentage);
        //  progressBarCount = (TextView) ocrProgressdialog.findViewById(R.id.progressCount);

        ocrProgressdialog.getWindow().setAttributes(lp);
    }

    public void showBottomSheet(int size) {
        ocrProgressdialog.show();
//        this.progressBar.setProgressMax(size);
        this.progressBar.setProgress(0);
    }

    public void setProgress(int progress, int total) {
        this.progressBar.setProgress(progress);
        // this.progressBarCount.setText(progress + "/" + total);
        int percentage = (progress * 100) / total;
        this.progressBarPercentage.setText(percentage + "%");
    }

    public void runPostExecution(java.io.File file) {
        ocrProgressdialog.dismiss();
        progressBarPercentage.setText("0%");
        this.progressBar.setProgress(0);
        showOCRSuccessDialog(file);
    }

    public void showOCRSuccessDialog(final java.io.File outputFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Success");
        builder.setMessage("OCRed PDF Created Successfully at " + outputFile.getAbsolutePath());
        builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

//                Intent target = new Intent(Intent.ACTION_VIEW);
//                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", outputFile);
//                target.setDataAndType(contentUri, "application/pdf");
//                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

//                Intent intent=new Intent(MainActivity.this,DisplayActivity.class);
//                intent.putExtra("file_path"+"_"+items);


//                Intent intent = Intent.createChooser(target, "Open File");
//                try {
//                    startActivity(intent);
//                } catch (ActivityNotFoundException e) {
//                    //Snackbar.make(mCoordLayout, "Install PDF reader application.", Snackbar.LENGTH_LONG).show();
//                }
            }
        });
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    getfile(listFile[i]);

                } else {

                    boolean booleanpdf = false;
                    if (listFile[i].getName().endsWith(".pdf")) {

                        for (int j = 0; j < pdfArrayList.size(); j++) {
                            if (pdfArrayList.get(j).getName().equals(listFile[i].getName())) {
                                booleanpdf = true;
                            } else {

                            }
                        }

                        if (booleanpdf) {
                            booleanpdf = false;
                        } else {
                            pdfArrayList.add(listFile[i]);

                        }
                    }
                }
            }
        }
        return (ArrayList<File>) pdfArrayList;
    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;

            getfile(dir);

            mAdapter = new MainAdapter(getActivity(), pdfArrayList, mOnItemClickListener);
            recyclerView2.setAdapter(mAdapter);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                boolean_permission = true;
                getfile(dir);

                mAdapter = new MainAdapter(getActivity(), pdfArrayList, mOnItemClickListener);
                recyclerView2.setAdapter(mAdapter);

            } else {
                Toast.makeText(getActivity(), "Please allow the permission", Toast.LENGTH_LONG).show();

            }
        }

    }


//    private void progressDialogDment() {
//
//        progressDialog=new ProgressDialog(getActivity());
//        progressDialog.show();
//        progressDialog.setContentView(R.layout.progress_dialog);
//        progressDialog.getWindow().setBackgroundDrawableResource(R.color.black);
////        progressDialog.dismiss();
//    }
//    public ArrayList<File> findPdf(File file) {
//        ArrayList<File> arrayList = new ArrayList<>();
//        File[] files = file.listFiles();
//        for (File singleFile : files) {
//
//            if (singleFile.isDirectory() && !singleFile.isHidden()) {
//
//                arrayList.addAll(findPdf(singleFile));
//
//
//            } else {
//                if (singleFile.getName().endsWith(".pdf")) {
//                    arrayList.add(singleFile);
//                }
//
//
//            }
//
//        }
//
//        return arrayList;
//    }
//
//    public void displaypdf() {
//        recyclerView2.setHasFixedSize(true);
//        recyclerView2.setLayoutManager(new GridLayoutManager(getActivity(), 1));
//        pdfArrayList = new ArrayList<>();
//
//        pdfArrayList.addAll(findPdf(Environment.getExternalStorageDirectory()));
//
//        mAdapter = new MainAdapter(getActivity(), pdfArrayList, mOnItemClickListener);
//        recyclerView2.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();
//    }
//
//    private void initAdapter() {
//
//        mAdapter = new MainAdapter(getActivity(),pdfArrayList,mOnItemClickListener);
//        recyclerView2.setAdapter(mAdapter);
//    }
//
//
//    private void initScrollListener() {
//        recyclerView2.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView2, int newState) {
//                super.onScrollStateChanged(recyclerView2, newState);
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView2, int dx, int dy) {
//                super.onScrolled(recyclerView2, dx, dy);
//
//                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView2.getLayoutManager();
//
//                if (!isLoading) {
//                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == pdfArrayList.size() - 1) {
//                        //bottom of list!
//                        loadMore();
//                        isLoading = true;
//                    }
//                }
//            }
//        });
//
//
//    }
//
//    private void loadMore() {
//        pdfArrayList.add(null);
//        mAdapter.notifyItemInserted(pdfArrayList.size() - 1);
//
//
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                pdfArrayList.remove(pdfArrayList.size() - 1);
//                int scrollPosition = pdfArrayList.size();
//                mAdapter.notifyItemRemoved(scrollPosition);
//                int currentSize = scrollPosition;
//                int nextLimit = currentSize + 10;
//
//                while (currentSize - 1 < nextLimit) {
//                    displaypdf();
//                    currentSize++;
//                }
//
//                mAdapter.notifyDataSetChanged();
//                isLoading = false;
//            }
//        }, 2000);
//
//
//    }
//
//    private void populateData() {
//        int i = 0;
//        while (i < 10) {
//            displaypdf();
//            i++;
//        }
}

class PdfAsyncClass extends AsyncTask<Void, Void, Void> {


    @Override
    protected Void doInBackground(Void... voids) {
//            displaypdf();
        return null;
    }


}

