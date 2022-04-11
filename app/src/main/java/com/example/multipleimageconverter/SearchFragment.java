package com.example.multipleimageconverter;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.getExternalStoragePublicDirectory;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.AppCompatEditText;
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
import java.util.Objects;

public class SearchFragment extends Fragment implements MainRecycleViewAdapter.OnItemClickListener {
    private static final int Merge_Request_CODE = 42;
    private RecyclerViewEmptySupport recyclerViewImage;
    List<File> pdfArrayList = new ArrayList<>();
    List<File> items = null;
    private MainAdapter mAdapter;
    private android.view.ActionMode actionMode;
    private static final int RQS_OPEN_DOCUMENT_TREE_ALL = 43;
    private BottomSheetDialog mBottomSheetDialog;
    private SearchFragment currentActivity;
    private static final int RQS_OPEN_DOCUMENT_TREE = 24;
    private File selectedFile;
    Dialog ocrProgressdialog;
    MainAdapter.OnItemClickListener mOnItemClickListener;
    CharSequence search = "";
    private CircularProgressBar progressBar;
    private TextView progressBarPercentage;
    AppCompatEditText search_barimage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        search_barimage = view.findViewById(R.id.search_barimage);
        recyclerViewImage = view.findViewById(R.id.recyclerView2);
        pdfArrayList = new ArrayList<>();

        displaypdf();

        search_barimage.addTextChangedListener(new TextWatcher() {
               @Override
               public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

               }

               @Override
               public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                   mAdapter.getFilter().filter(charSequence);
                   search = charSequence;
//
                   mAdapter.notifyDataSetChanged();

//

               }

               @Override
               public void afterTextChanged(Editable editable) {
               }
           }
        );

        recyclerViewImage.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewImage.setHasFixedSize(true);

        CreateDataSource();
        currentActivity = this;
        InitBottomSheetProgress();
        return view;
    }


    @Override
    public void onBackPressed() {

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.sort_menu, menu);
        mainMenuItem = menu.findItem(R.id.fileSort);
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
                mainMenuItem.setTitle("Name");
                comparator = FileComparator.getNameComparator();
                FileComparator.isDescending = isChecked;
                sortFiles(comparator);
                return true;
            case R.id.modifiedSort:
                mainMenuItem.setTitle("Modified");
                comparator = FileComparator.getLastModifiedComparator();
                FileComparator.isDescending = isChecked;
                sortFiles(comparator);
                return true;
            case R.id.sizeSort:
                mainMenuItem.setTitle("Size");
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
        if (requestCode == Merge_Request_CODE && resultCode == RESULT_OK) {
            if (result != null) {
                CreateDataSource();
                mAdapter.notifyItemInserted(pdfArrayList.size() - 1);
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
                    File file = pdfArrayList.get(i);
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
        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures");
//        File file = new File(root ,  editText.getText().toString());
//        File root = getFilesDir();
        File myDir = new File(root + "/Pictures");
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

        recyclerViewImage.setAdapter(mAdapter);
    }

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void deleteItems() {
        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            File file = pdfArrayList.get(i);
            file.delete();
            mAdapter.removeData(selectedItemPositions.get(i));
        }
        mAdapter.notifyDataSetChanged();

    }


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


    @Override
    public void onItemClick(View view, File value, int position) {

    }

    @Override
    public void onItemLongClick(View view, File obj, int pos) {

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
            File file = pdfArrayList.get(i);
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

        ((View) view.findViewById(R.id.lyt_openFile)).setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                Intent intent = new Intent(getActivity(), ImageDetailActivity.class);

                String imageDetail = String.valueOf(currentFile);

                intent.putExtra("imagesDetail", imageDetail);
                startActivity(intent);

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

                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures");
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

        progressBar = (CircularProgressBar) ocrProgressdialog.findViewById(R.id.circularProgressBar);
        progressBarPercentage = (TextView) ocrProgressdialog.findViewById(R.id.progressPercentage);
        //  progressBarCount = (TextView) ocrProgressdialog.findViewById(R.id.progressCount);

        ocrProgressdialog.getWindow().setAttributes(lp);
    }

    public void showBottomSheet(int size) {
        ocrProgressdialog.show();
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

    public ArrayList<File> findPdf(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

//        ToastUtil.showInfoLog("list", "Files present : " + file.length());
        if (arrayList.isEmpty()) {
            // TODO : Add no files present icon and message here.
//            ToastUtil.showInfoLog("list", "Files list is empty : " + file.length());
        }

        try {
            Collections.addAll(arrayList, Objects.requireNonNull(file.listFiles()));
        } catch (Exception e) {
            e.printStackTrace();
//            ToastUtil.showToast("It looks like there is no files in folder");
        }

        return arrayList;
    }

    public void displaypdf() {
        recyclerViewImage.setHasFixedSize(true);
        recyclerViewImage.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        pdfArrayList = new ArrayList<>();
        File folder = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "MultiImageConverter");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        pdfArrayList.addAll(findPdf(folder));
        mAdapter = new MainAdapter(getActivity(), pdfArrayList, mOnItemClickListener);
        recyclerViewImage.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }


}
