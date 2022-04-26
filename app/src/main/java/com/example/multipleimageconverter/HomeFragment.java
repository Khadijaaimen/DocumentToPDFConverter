package com.example.multipleimageconverter;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.example.multipleimageconverter.R.drawable.ic_baseline_keyboard_arrow_down_24;
import static com.example.multipleimageconverter.R.drawable.ic_baseline_keyboard_arrow_up_24;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.mikhaellopez.circularprogressbar.BuildConfig;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.zhihu.matisse.Matisse;

import org.jetbrains.annotations.NotNull;

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

public class HomeFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener, MainRecycleViewAdapter.OnItemClickListener {
    private static final int Merge_Request_CODE = 42;
    RecyclerView recyclerView;
    List<File> pdfArrayList;
    List<File> items = null;
    RelativeLayout scanner_ly;
    RelativeLayout qr_ly;
    private MainAdapter mAdapter;
    final int CAMERA_PERM = 1;
    boolean CameraPermission = false;

    private android.view.ActionMode actionMode;
    private static final int RQS_OPEN_DOCUMENT_TREE_ALL = 43;
    private BottomSheetDialog mBottomSheetDialog;
    private static final int RQS_OPEN_DOCUMENT_TREE = 24;
    private static final int REQUEST_CODE_CHOOSE = 97;
    private File selectedFile;
    RelativeLayout images;
    Dialog ocrProgressDialog;
    MainAdapter.OnItemClickListener mOnItemClickListener;
    CharSequence search = "";
    private CircularProgressBar progressBar;
    private TextView progressBarPercentage;
    SharedPreferences mSharedPreferences;
    EditText searchView;
    RelativeLayout idCardLayout;
    List<Uri> files;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {

        if (requestCode == CAMERA_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CameraPermission = true;
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                    new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                            .setTitle("Permission")
                            .setMessage("Please provide camera permission for using all the features of this app")
                            .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERM);
                                }
                            }).setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();

                } else {

                    new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                            .setTitle("Permission")
                            .setMessage("You have denied some permissions. Allow all permission at [Settings] > [Permissions]")
                            .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", getActivity().getPackageName(), null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    getActivity().finish();
                                }
                            }).setNegativeButton("Exit app", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            getActivity().finish();

                        }
                    }).create().show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void askPermission() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERM);
            } else {
                CameraPermission = true;
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
                                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

//        String data = getArguments().getString("message");
//        uriImage = Uri.parse(data);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            CheckStoragePermission();
        }

        File file = new File(Environment.getExternalStoragePublicDirectory("").getAbsolutePath() + "/MultiImageConverter");
        if (!file.exists()) {
            file.mkdirs();
        }
        scanner_ly = view.findViewById(R.id.scanner_ly);
        scanner_ly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermission();
                if (CameraPermission) {
                    StartMergeActivity("CameraActivity");
                }
            }
        });
        idCardLayout = view.findViewById(R.id.idcard_ly);
        idCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartMergeActivityForMultiple("CaptureMultiple");
            }
        });

        qr_ly = view.findViewById(R.id.qr_ly);
        qr_ly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), QRcode.class);
                startActivity(intent);
            }
        });
        recyclerView = view.findViewById(R.id.rv);



        items = new ArrayList<File>();
        pdfArrayList = new ArrayList<>();
        files = new ArrayList<Uri>();
        searchView = view.findViewById(R.id.search_bar);
        images = view.findViewById(R.id.images);

        images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartMergeActivityForAllImages("ImagesSearch");
            }
        });
        displaypdf();


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

        RelativeLayout maddCameraFAB = view.findViewById(R.id.mainaddCameraFAB);
        RelativeLayout maddFilesFAB = view.findViewById(R.id.mainaddFilesFAB);
        mSharedPreferences = getActivity().getSharedPreferences("configuration", MODE_PRIVATE);

        maddFilesFAB.setOnClickListener(v -> StartMergeActivity("FileSearch"));

        maddCameraFAB.setOnClickListener(v -> StartMergeActivity("CameraActivity"));

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        CreateDataSource();
        InitBottomSheetProgress();
        return view;
    }


    public void StartMergeActivity(String message) {
        askPermission();
        Intent intent = new Intent(getActivity(), ImageToPDF.class);
        intent.putExtra("ActivityAction", message);
        startActivityForResult(intent, Merge_Request_CODE);

    }

    public void StartMergeActivityForAllImages(String message) {
        Intent intent = new Intent(getActivity(), ImageToPDF.class);
        intent.putExtra("ActivityAction", message);
        startActivityForResult(intent, Merge_Request_CODE);
    }

    public void StartMergeActivityForMultiple(String message) {
        Intent intent = new Intent(getActivity(), ImageToPDF.class);
        intent.putExtra("ActivityAction", message);
        startActivityForResult(intent, Merge_Request_CODE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.sort_menu, menu);
//        mainMenuItem = menu.findItem(R.id.fileSort);
        super.onCreateOptionsMenu(menu, inflater);
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
                    item.setIcon(ic_baseline_keyboard_arrow_up_24);
                } else {
                    item.setIcon(ic_baseline_keyboard_arrow_down_24);
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

                Uri uri = result.getData();
                Log.i("tazaUri", "Taza uri we got is : " + uri);

                CreateDataSource();
                mAdapter.notifyItemInserted(pdfArrayList.size() - 1);
            }
        }

        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {


            files = Matisse.obtainResult(result);
            Log.d("Matisse", "mSelected: " + files);

            Log.i("uriparse", "uri parse we got is :" + files.get(0));

            Intent intent = new Intent(getActivity(), CroppedActivity.class);
            intent.putExtra("imageUri", files.toString());
            startActivity(intent);

            Log.i("listSize", "onActivityResult: files size " + files.size());
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

        File root = getActivity().getFilesDir();
        File myDir = new File(root + "/MultiImageConverter");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        File[] files = myDir.listFiles();

        Arrays.sort(files, (file1, file2) -> {
            long result = file2.lastModified() - file1.lastModified();
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        });

        for (int i = 0; i < files.length; i++) {
            pdfArrayList.add(files[i]);
        }

        //set data and list adapter
        mAdapter = new MainAdapter(requireContext(), pdfArrayList, mOnItemClickListener);
        mAdapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(View view, File value, int position) {
                if (mAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(position);
                } else {
                    showBottomSheetDialog(value);
                }
            }

//            @RequiresApi(api = Build.VERSION_CODES.M)
//            @Override
//            public void onItemLongClick(View view, File obj, int pos) {
//                enableActionMode(pos);
//                Toast.makeText(getActivity(), "item clicked :" + pos, Toast.LENGTH_SHORT).show();
//            }


        });


        recyclerView.setAdapter(mAdapter);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                actionMode = getActivity().startActionMode(new android.view.ActionMode.Callback2() {
                    @Override
                    public boolean onCreateActionMode(android.view.ActionMode actionMode, Menu menu) {
                        actionMode.getMenuInflater().inflate(R.menu.menu_mainactionmode, menu);
                        return true;
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

    }

    @Override
    public void onBackPressed() {
        getActivity().onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }


    public void setPackageManager(PackageManager packageManager) {
        this.setPackageManager(packageManager);/* = packageManager;*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
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

    private void showBottomSheetDialog(final File currentFile) {
        final View view = getLayoutInflater().inflate(R.layout.sheet_list, null);

        if(currentFile.getName().contains(".jpg")){
            view.findViewById(R.id.lyt_rename).setVisibility(View.GONE);
        }

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

        ((View) view.findViewById(R.id.lyt_openFile)).setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                String name = currentFile.getName();
                if (name.contains(".pdf")) {
                    Intent intent = new Intent(getActivity(), DisplayActivity.class);
                    Uri uriToSend = Uri.fromFile(currentFile.getAbsoluteFile());
                    intent.putExtra("file_name", currentFile.getName());
                    intent.putExtra("file_path", "" + uriToSend);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(currentFile.getPath()), "image/*");
                    startActivity(intent);
                }
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

    //:TODO Rename 717 yahan ana hy :
    public void showCustomRenameDialog(final File currentFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.rename_layout, null);
        builder.setView(view);
        final EditText editText = (EditText) view.findViewById(R.id.renameEditText2);
        String name = currentFile.getName();
        String[] names;
        if (name.contains(".pdf")) {
            names = name.split(".pdf");
            String splitName = names[0];
            editText.setText(splitName);
            builder.setTitle("Rename");
            builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    File dir = Environment.getExternalStorageDirectory();
                    if (dir.exists()) {
                        String root = currentFile.getName();

                        File from = new File(dir + "/MultiImageConverter", root);
                        File to = new File(dir + "/MultiImageConverter", editText.getText().toString() + ".pdf");
                        if (from.exists())
                            from.renameTo(to);
                        from.delete();
                        pdfArrayList.clear();
                        displaypdf();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
//        } else {
//            names = name.split(".jpg");
//            String splitName = names[0];
//            editText.setText(splitName);
//            builder.setTitle("Rename");
//            builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    File dir = Environment.getExternalStorageDirectory();
//                    if (dir.exists()) {
//                        String root = currentFile.getName();
//
//                        File from = new File(dir + "/MultiImageConverter", root);
//                        File to = new File(dir + "/MultiImageConverter", editText.getText().toString() + ".jpg");
//                        if (from.exists())
//                            from.renameTo(to);
//                        pdfArrayList.clear();
//                        displaypdf();
//                    }
//                }
//            });
//            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//
//                }
//            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showCustomDeleteDialog(final File currentFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure want to delete this file?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                currentFile.delete();
                pdfArrayList.clear();
                mAdapter.notifyDataSetChanged();
                displaypdf();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        mAdapter.notifyDataSetChanged();
    }

    private void showDialogAbout() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
//        dialog.setContentView(R.layout.dialog_about);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void InitBottomSheetProgress() {

        ocrProgressDialog = new Dialog(getActivity());
        ocrProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ocrProgressDialog.setContentView(R.layout.progressdialog);
        ocrProgressDialog.setCancelable(false);
        ocrProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
        int percentage = (progress * 100) / total;
        this.progressBarPercentage.setText(percentage + "%");
    }

    public void runPostExecution(File file) {
        ocrProgressDialog.dismiss();
        progressBarPercentage.setText("0%");
        this.progressBar.setProgress(0);
        showOCRSuccessDialog(file);
    }

    public void showOCRSuccessDialog(final File outputFile) {
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
        for (File singleFile : files) {

            if (singleFile.isDirectory() && !singleFile.isHidden()) {

                arrayList.addAll(findPdf(singleFile));
            } else {
                if (singleFile.getName().endsWith(".pdf")) {
                    arrayList.add(singleFile);
                }
                if (singleFile.getName().endsWith(".jpg")) {
                    arrayList.add(singleFile);
                }
            }
        }

        return arrayList;
    }

    public void displaypdf() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        File file = new File(Environment.getExternalStoragePublicDirectory("").getAbsolutePath() + "/MultiImageConverter");
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            pdfArrayList.addAll(findPdf(file));
            mAdapter = new MainAdapter(getActivity(), pdfArrayList, mOnItemClickListener);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

        } catch (Exception ex) {

        }
    }


}