package com.example.multipleimageconverter;

import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.mikhaellopez.circularprogressbar.BuildConfig;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ImageToPDF extends AppCompatActivity {

    public static int imgeCounter = 0;
    int Pic_Code = 0;
    private RecyclerViewEmptySupport recyclerView;
    private AdapterGridBasic mAdapter;
    private static final int READ_REQUEST_CODE = 42;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_Multiple = 2;
    private static final int REQUEST_COLLAGE = 265;
    public static ArrayList<ImageDocument> documents = null;
    ImageToPDF mainActivity;
    int currentSelected = -1;
    private CircularProgressBar progressBar;
    private TextView progressBarPercentage;
    TextInputLayout textInputLayout;
    EditText passwordText;
    public ItemTouchHelper itemTouchHelper;
    ArrayList<String> croppedUriList = new ArrayList<>();
    private ActionMode actionMode;
    LinearLayout mParentFloatButton;
    private String mCurrentCameraFile;
    private Dialog bottomSheetDialog;
    LinearLayout layout;

    private void CheckStoragePermission() {
        if (ContextCompat.checkSelfPermission(ImageToPDF.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ImageToPDF.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog alertDialog = new AlertDialog.Builder(ImageToPDF.this).create();
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
                ActivityCompat.requestPermissions(ImageToPDF.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_pdf);
        ActionBar ab = getSupportActionBar();
        layout = findViewById(R.id.imagetoPdfGone);

        CheckStoragePermission();

        if (ContextCompat.checkSelfPermission(ImageToPDF.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            CheckStoragePermission();
        }

        ab.setDisplayHomeAsUpEnabled(true);
        initComponent();

        Intent intent = getIntent();
        String message = intent.getStringExtra("ActivityAction");
        if (message.equals("FileSearch")) {
            performFileSearch();
        } else if (message.equals("CameraActivity")) {
            StartCameraActivity();
        } else if (message.equals("ImagesSearch")) {
            startImageActivity();
        } else if (message.equals("CaptureMultiple")) {
            StartCameraActivityForIdCard();
        }
        mainActivity = this;
        InitBottomSheetProgress();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void startImageToPDF() {
        if (documents.size() < 1) {
            Toast.makeText(this, "You need to add at least 1 image file", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), PdfCreator.class);
            startActivityForResult(intent, REQUEST_COLLAGE);
        }
    }

    private void convertToPDF() {
        if (documents.size() < 1) {
            Toast.makeText(this, "You need to add at least 1 image file", Toast.LENGTH_LONG).show();
        } else {
            final Dialog dialog = new Dialog(mainActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            final View alertView = getLayoutInflater().inflate(R.layout.file_alert_dialog, null);
            textInputLayout = (TextInputLayout) alertView.findViewById(R.id.editTextPassword);
            passwordText = (EditText) alertView.findViewById(R.id.password);
//            securePDF = (AppCompatCheckBox) alertView.findViewById(R.id.securePDF);
//            securePDF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//
//                    if (b) {
//                        textInputLayout.setVisibility(View.VISIBLE);
//
//                    } else {
//                        textInputLayout.setVisibility(View.GONE);
//                    }
//
//                }
//            });
            final AppCompatSpinner spn_timezone = (AppCompatSpinner) alertView.findViewById(R.id.pageorientation);

            String[] timezones = new String[]{"Portrait", "Landscape"};
            ArrayAdapter<String> array = new ArrayAdapter<>(mainActivity, R.layout.simple_spinner_item, timezones);
            array.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            spn_timezone.setAdapter(array);
            spn_timezone.setSelection(0);

            final AppCompatSpinner pageSize = (AppCompatSpinner) alertView.findViewById(R.id.pagesize);

            String[] sizes = new String[]{"Fit (Same page size as image)", "A4 (297x210 mm)", "US Letter (215x279.4 mm)"};
            ArrayAdapter<String> pagearrary = new ArrayAdapter<>(mainActivity, R.layout.simple_spinner_item, sizes);
            pagearrary.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            pageSize.setAdapter(pagearrary);
            pageSize.setSelection(0);

            final AppCompatSpinner pageMargin = (AppCompatSpinner) alertView.findViewById(R.id.margin);
            String[] margins = new String[]{"No margin", "Small", "Big"};
            ArrayAdapter<String> marginArray = new ArrayAdapter<>(mainActivity, R.layout.simple_spinner_item, margins);
            marginArray.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            pageMargin.setAdapter(marginArray);
            pageMargin.setSelection(0);

            final AppCompatSpinner compression = (AppCompatSpinner) alertView.findViewById(R.id.compression);
            String[] compressions = new String[]{"Low", "Medium", "High"};
            ArrayAdapter<String> compressionArray = new ArrayAdapter<>(mainActivity, R.layout.simple_spinner_item, compressions);
            compressionArray.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            compression.setAdapter(compressionArray);
            compression.setSelection(2);

            final EditText edittext = (EditText) alertView.findViewById(R.id.editText2);
            dialog.setContentView(alertView);
            dialog.setCancelable(true);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.show();
            dialog.getWindow().setAttributes(lp);
            ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            ((Button) dialog.findViewById(R.id.bt_save)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fileName = edittext.getText().toString();

                    if (!fileName.equals("")) {
                        ImageToPDFAsync converter = new ImageToPDFAsync(mainActivity, documents, fileName, null);
//                            if (securePDF.isChecked()) {
//                                String password = passwordText.getText().toString();
//                                converter.setPassword(password);
//                            }
                        converter.setPageOrientation(spn_timezone.getSelectedItem().toString());
                        converter.setPageMargin(pageMargin.getSelectedItem().toString());
                        converter.setPageSize(pageSize.getSelectedItem().toString());
                        converter.setCompression(compression.getSelectedItem().toString());
                        converter.execute();
                        dialog.dismiss();
                    } else {
                        Snackbar.make(v, "File name should not be empty", Snackbar.LENGTH_LONG).show();
                    }
                }
            });

        }
    }

    private void initComponent() {
        documents = new ArrayList<ImageDocument>();
        recyclerView = (RecyclerViewEmptySupport) findViewById(R.id.recyclerView);
        recyclerView.setEmptyView(findViewById(R.id.toDoEmptyView));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setItemAnimator(null);
        recyclerView.addItemDecoration(new SpacingItemDecoration(3, dpToPx(this, 2), true));
        recyclerView.setHasFixedSize(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    if (mParentFloatButton.getVisibility() == View.VISIBLE)
                        mParentFloatButton.setVisibility(View.GONE);
                } else if (dy < 0) {
                    if (mParentFloatButton.getVisibility() != View.VISIBLE)
                        mParentFloatButton.setVisibility(View.VISIBLE);
                }
            }
        });
        //set data and list adapter
        mAdapter = new AdapterGridBasic(this, documents);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setDragListener(new AdapterGridBasic.OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                if (actionMode == null)
                    itemTouchHelper.startDrag(viewHolder);
            }
        });
        ItemTouchHelper.Callback callback = new ItemTouchHelperClass(mAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void StartCameraActivityForIdCard() {

        if (Pic_Code < 3) {
            // add new requset of picture like this
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
                File root = (File) getCacheDir();
                mCurrentCameraFile = root + "/MultiImageConverter";
                File myDir = new File(mCurrentCameraFile);

                myDir = new File(mCurrentCameraFile);
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                mCurrentCameraFile = root + "/MultiImageConverter/IMG" + System.currentTimeMillis() + ".jpeg";
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(mCurrentCameraFile));

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                imgeCounter++;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_Multiple);
                Pic_Code++;
            }

        }
    }

    public void StartCameraActivity() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            File root = (File) getCacheDir();
            mCurrentCameraFile = root + "/MultiImageConverter";
            File myDir = new File(mCurrentCameraFile);
            myDir = new File(mCurrentCameraFile);
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            mCurrentCameraFile = root + "/MultiImageConverter/IMG" + System.currentTimeMillis() + ".jpeg";
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(mCurrentCameraFile));

            Log.i("cam", "uri we got is :" + uri);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (result != null) {
                ArrayList<Uri> uriList = new ArrayList<>();

                if (result.getClipData() != null) {

                    int count = result.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = result.getClipData().getItemAt(i).getUri();
                        uriList.add(imageUri);
                        ImageDocument document = new ImageDocument(imageUri, this);
                        addToDataStore(document);

                    }
                    Intent intent = new Intent(this, CroppedActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("mylist", uriList);
                    intent.putExtras(bundle);
                    this.startActivity(intent);

                    Log.i("tazaUri", "Taza uri in itp list : " + uriList.size());
                    Log.i("tazaUri", "Taza uri in itp list at index 0 : " + uriList.get(0));
                } else if (result.getData() != null) {
                    Uri imageUri = result.getData();
                    uriList.add(imageUri);
                    ImageDocument document = new ImageDocument(imageUri, this);
                    addToDataStore(document);
                }
                Intent intent = new Intent(this, CroppedActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("mylist", uriList);
                intent.putExtras(bundle);
                this.startActivity(intent);

            }
        }
        if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult cropped = CropImage.getActivityResult(result);
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(ImageToPDF.this, FilterActivityForTesting.class);
                intent.setClass(this, FilterActivityForTesting.class);
                intent.putExtra("KEY", cropped.getUri().toString());
                startActivity(intent);
                Uri resultUri = cropped.getUri();

                if (currentSelected != -1) {
                    ImageDocument document = documents.get(currentSelected);
                    document.setImageDocument(resultUri);
                    mAdapter.notifyItemChanged(currentSelected);

                } else {
                    ImageDocument document = new ImageDocument(resultUri, getApplicationContext());
                    addToDataStore(document);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = cropped.getError();
            }
//
        }
        if (requestCode == REQUEST_IMAGE_Multiple && resultCode == Activity.RESULT_OK) {
            File file = new File(mCurrentCameraFile);
            if (file.exists()) {
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(mCurrentCameraFile));
                croppedUriList.add(String.valueOf(uri));
//                String croppedUri = String.valueOf(uri);
                String camera = "camera";
                if (imgeCounter < 2) {
                    Log.d("TEST_test", "if called: counter=" + imgeCounter);
                    Log.d("TEST_test", "if called: list size=" + croppedUriList.size());
                    StartCameraActivityForIdCard();
                } else if (imgeCounter == 2) {
                    Log.d("TEST_test", "else if called: counter=" + imgeCounter);
                    Log.d("TEST_test", "else if called: list size=" + croppedUriList.size());
                    Intent intent = new Intent(this, CroppedActivity.class);

                    String firstImage = croppedUriList.get(0);
                    String secondImage = croppedUriList.get(1);

                    ArrayList<Uri> uriList = new ArrayList<>();
                    uriList.add(0, Uri.parse(firstImage));
                    uriList.add(1, Uri.parse(secondImage));

                    intent.putParcelableArrayListExtra("mylist", uriList);
                    startActivity(intent);

                    Log.i("extra", "cropped uri list we got is :" + croppedUriList.size());


                }
            }

        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            File file = new File(mCurrentCameraFile);
            if (file.exists()) {
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(mCurrentCameraFile));
                String croppedUri = String.valueOf(uri);
                String camera = "camera";

                Intent intent = new Intent(this, CroppedActivity.class);
                intent.putExtra("message", croppedUri);
                intent.putExtra("cameraValue", camera);
                startActivity(intent);
            }

        }

        if(resultCode == 0){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (requestCode == REQUEST_COLLAGE && resultCode == Activity.RESULT_OK) {
            makeResult();
        }
    }

    private void addToDataStore(ImageDocument item) {
        documents.add(item);
        mAdapter.notifyItemInserted(documents.size() - 1);
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    public void startImageActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/jpeg");
        String[] mimetypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }


    public void performFileSearch() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/jpeg");
        String[] mimetypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void InitBottomSheetProgress() {

        bottomSheetDialog = new Dialog(this);
        bottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomSheetDialog.setContentView(R.layout.progressdialog);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(bottomSheetDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        progressBar = (CircularProgressBar) bottomSheetDialog.findViewById(R.id.circularProgressBar);
        progressBarPercentage = (TextView) bottomSheetDialog.findViewById(R.id.progressPercentage);

        bottomSheetDialog.getWindow().setAttributes(lp);
    }

    public void showBottomSheet(int size) {
        bottomSheetDialog.show();
        this.progressBar.setProgressMax(size);
        this.progressBar.setProgress(0);
    }

    public void setProgress(int progress, int total) {
        this.progressBar.setProgress(progress);
        int percentage = (progress * 100) / total;
        this.progressBarPercentage.setText(percentage + "%");
    }

    public void runPostExecution(Boolean isMergeSuccess) {
        bottomSheetDialog.dismiss();
        progressBarPercentage.setText("0%");
        this.progressBar.setProgress(0);
        makeResult();
    }

    public void makeResult() {
        Intent i = new Intent();
        this.setResult(RESULT_OK, i);
    }

    private void deleteItems() {
        List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            mAdapter.removeData(selectedItemPositions.get(i));
            mAdapter.notifyDataSetChanged();

        }

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
            mode.getMenuInflater().inflate(R.menu.menu_delete, menu);
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
                deleteItems();
                mode.finish();
                return true;
            }
            if (id == R.id.select_all) {
                selectAll();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            actionMode = null;
        }

    }

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private MenuItem mainMenuItem;
    private boolean isChecked = false;

    //>>>>>>>>>>>>MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_menu, menu);
//        mainMenuItem = menu.findItem(R.id.fileSort);
        return true;
    }

    Comparator<ImageDocument> comparator = null;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.nameSort:
//                mainMenuItem.setTitle("Name");
                comparator = FileComparator.getNameFileComparator();
                FileComparator.isDescending = isChecked;
                sortFiles(comparator);
                return true;
            case R.id.modifiedSort:
//                mainMenuItem.setTitle("Modified");
                comparator = FileComparator.getLastModifiedFileComparator();
                FileComparator.isDescending = isChecked;
                sortFiles(comparator);
                return true;
            case R.id.sizeSort:
//                mainMenuItem.setTitle("Size");
                comparator = FileComparator.getSizeFileComparator();
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
                    comparator = FileComparator.getLastModifiedFileComparator();
                    FileComparator.isDescending = isChecked;
                    sortFiles(comparator);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //>>>>>>>>>.

    private void sortFiles(Comparator<ImageDocument> comparator) {
        Collections.sort(mAdapter.items, comparator);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}



