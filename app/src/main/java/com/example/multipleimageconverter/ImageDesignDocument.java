package com.example.multipleimageconverter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mikhaellopez.circularprogressbar.BuildConfig;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

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

public class ImageDesignDocument extends AppCompatActivity implements MainRecycleViewAdapter.OnItemClickListener {
    private static final int Merge_Request_CODE = 42;
    private RecyclerView recyclerView2;
    List<File> pdfArrayList;
    List<File> items = null;
    private MainAdapter mAdapter;
    private ImageDesignDocument.ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private static final int RQS_OPEN_DOCUMENT_TREE_ALL = 43;
    private BottomSheetDialog mBottomSheetDialog;
    private ImageDesignDocument currentActivity;
    private static final int RQS_OPEN_DOCUMENT_TREE = 24;
    private File selectedFile;
    Dialog ocrProgressDialog;
    MainAdapter.OnItemClickListener mOnItemClickListener;
    CharSequence search = "";
    private CircularProgressBar progressBar;
    private TextView progressBarPercentage;
    EditText searchView;


    private void CheckStoragePermission() {
        if (ContextCompat.checkSelfPermission(ImageDesignDocument.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ImageDesignDocument.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog alertDialog = new AlertDialog.Builder(ImageDesignDocument.this).create();
                alertDialog.setTitle("Storage Permission");
                alertDialog.setMessage("Storage permission is required in order to " +
                        "provide Image to PDF feature, please enable permission in app settings");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                                startActivity(i);
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(ImageDesignDocument.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);

        CheckStoragePermission();

        if (ContextCompat.checkSelfPermission(ImageDesignDocument.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            CheckStoragePermission();
        }

        searchView = findViewById(R.id.search_bar2);
        pdfArrayList = new ArrayList<>();
        displaypdf();
        searchView.addTextChangedListener(new TextWatcher() {
              @Override
              public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

              }

              @Override
              public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                  mAdapter.getFilter().filter(charSequence);
                  search = charSequence;
                  mAdapter.notifyDataSetChanged();
              }

              @Override
              public void afterTextChanged(Editable editable) {
              }
          }
        );
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setHasFixedSize(true);

        CreateDataSource();
        actionModeCallback = new ImageDesignDocument.ActionModeCallback();
        currentActivity = this;
        InitBottomSheetProgress();

    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(ImageDesignDocument.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sort_menu, menu);
//        mainMenuItem = menu.findItem(R.id.fileSort);
        return true;
    }

    private MenuItem mainMenuItem;
    private boolean isChecked = false;
    Comparator<File> comparator = null;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.nameSort:
//                mainMenuItem.setTitle("Name");
                comparator = FileComparator.getNameComparator();
                FileComparator.isDescending = isChecked;
                sortFiles(comparator);
                return true;
            case R.id.modifiedSort:
//                mainMenuItem.setTitle("Modified");
                comparator = FileComparator.getLastModifiedComparator();
                FileComparator.isDescending = isChecked;
                sortFiles(comparator);
                return true;
            case R.id.sizeSort:
//                mainMenuItem.setTitle("Size");
                comparator = FileComparator.getSizeComparator();
                FileComparator.isDescending = isChecked;
                sortFiles(comparator);
                return true;
            case R.id.ordering:
                isChecked = !isChecked;
                if (isChecked) {
                    item.setIcon(R.drawable.ic_baseline_keyboard_arrow_up_24);
                } else {
                    item.setIcon(R.drawable.ic_baseline_keyboard_arrow_down_24);
                }
                if (comparator != null) {
                    FileComparator.isDescending = isChecked;
                    sortFiles(comparator);
                } else {
                    comparator = FileComparator.getLastModifiedComparator();
                    FileComparator.isDescending = isChecked;
                    sortFiles(comparator);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortFiles(Comparator<File> comparator) {
        Collections.sort(mAdapter.pdfArrayList, comparator);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == Merge_Request_CODE && resultCode == Activity.RESULT_OK) {
            if (result != null) {
                CreateDataSource();
                mAdapter.notifyItemInserted(items.size() - 1);
            }
        }
        if (resultCode == RESULT_OK && requestCode == RQS_OPEN_DOCUMENT_TREE) {
            if (result != null) {
                Uri uriTree = result.getData();
                DocumentFile documentFile = DocumentFile.fromTreeUri(this, uriTree);
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
                    Toast toast = Toast.makeText(this, "Copy files to: " + documentFile.getName(), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
        if (resultCode == RESULT_OK && requestCode == RQS_OPEN_DOCUMENT_TREE_ALL) {
            if (result != null) {
                List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
                ArrayList<Uri> files = new ArrayList<Uri>();
                Uri uriTree = result.getData();
                DocumentFile documentFile = DocumentFile.fromTreeUri(this, uriTree);
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

                Toast toast = Toast.makeText(this, "Copy files to: " + documentFile.getName(), Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    public void copy(File selectedFile, DocumentFile newFile) throws IOException {
        try {
            OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
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

        File root = getFilesDir();
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
        mAdapter = new MainAdapter(this, pdfArrayList, mOnItemClickListener);
        mAdapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, File value, int position) {
                if (mAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(position);
                } else {
                    showBottomSheetDialog(value);
                }
            }

//            @Override
//            public void onItemLongClick(View view, File obj, int pos) {
//                enableActionMode(pos);
//            }

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        Intent target = ShareCompat.IntentBuilder.from(this).getIntent();
        target.setAction(Intent.ACTION_SEND_MULTIPLE);
        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
        ArrayList<Uri> files = new ArrayList<Uri>();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            File file = pdfArrayList.get(selectedItemPositions.get(i));
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
            files.add(contentUri);
        }
        target.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        target.setType("application/pdf");
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (target.resolveActivity(getPackageManager()) != null) {
            startActivity(target);
        }
        actionMode.finish();
    }

    private void showBottomSheetDialog(final File currentFile) {
        final View view = getLayoutInflater().inflate(R.layout.sheet_list, null);

        ((View) view.findViewById(R.id.lyt_email)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", currentFile);
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
                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", currentFile);
                Intent target = ShareCompat.IntentBuilder.from(currentActivity).setStream(contentUri).getIntent();
                target.setData(contentUri);
                target.setType("application/pdf");
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (target.resolveActivity(getPackageManager()) != null) {
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

        ((View) view.findViewById(R.id.lyt_openFile)).setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                Intent intent = new Intent(ImageDesignDocument.this, DisplayActivity.class);
                File file = currentFile.getAbsoluteFile();
                Uri uriToSend = Uri.fromFile(currentFile.getAbsoluteFile());

                intent.putExtra("file_path", "" + uriToSend);
                startActivity(intent);
            }
        });
        mBottomSheetDialog = new BottomSheetDialog(this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.rename_layout, null);
        builder.setView(view);
        final EditText editText = (EditText) view.findViewById(R.id.renameEditText2);
        editText.setText(currentFile.getName());
        builder.setTitle("Rename");
        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                File root =Environment.getExternalStorageDirectory();
                File file = new File(root , editText.getText().toString());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void InitBottomSheetProgress() {

        ocrProgressDialog = new Dialog(this);
        ocrProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ocrProgressDialog.setContentView(R.layout.progressdialog);
        ocrProgressDialog.setCancelable(false);
        ocrProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(ocrProgressDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        progressBar = (CircularProgressBar) ocrProgressDialog.findViewById(R.id.circularProgressBar);
        progressBarPercentage = (TextView) ocrProgressDialog.findViewById(R.id.progressPercentage);

        ocrProgressDialog.getWindow().setAttributes(lp);
    }

    public void showBottomSheet(int size) {
        ocrProgressDialog.show();
        this.progressBar.setProgressMax(size);
        this.progressBar.setProgress(0);
    }

    public void setProgress(int progress, int total) {
        this.progressBar.setProgress(progress);
        // this.progressBarCount.setText(progress + "/" + total);
        int percentage = (progress * 100) / total;
        this.progressBarPercentage.setText(percentage + "%");
    }

    public void runPostExecution(java.io.File file) {
        ocrProgressDialog.dismiss();
        progressBarPercentage.setText("0%");
        this.progressBar.setProgress(0);
        showOCRSuccessDialog(file);
    }

    public void showOCRSuccessDialog(final java.io.File outputFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    public ArrayList<File> findPdf(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        for (File singleFile : files) {

            if (singleFile.isDirectory() && !singleFile.isHidden()) {

                arrayList.addAll(findPdf(singleFile));


            } else {
                if (singleFile.getName().endsWith(".pdf")) {
                    arrayList.add(singleFile);
                }


            }

        }

        return arrayList;
    }

    public void displaypdf() {
        recyclerView2 = findViewById(R.id.rv2);
        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(new GridLayoutManager(this, 1));
        pdfArrayList  = new ArrayList<>();
        pdfArrayList.addAll(findPdf(Environment.getExternalStorageDirectory()));
        mAdapter = new MainAdapter(this, pdfArrayList, mOnItemClickListener);
        recyclerView2.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

}
