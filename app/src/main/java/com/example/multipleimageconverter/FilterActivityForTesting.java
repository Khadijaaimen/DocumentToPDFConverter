package com.example.multipleimageconverter;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;
import static android.os.Environment.getExternalStorageDirectory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.multipleimageconverter.util.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.zomato.photofilters.BuildConfig;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class FilterActivityForTesting extends AppCompatActivity implements ViewEventAdapter.ThumbnailsAdapterListener {

    Dialog MyDialog;
    CircleImageView filterNext, imageReverseFilter;
    TextView filterListSize;
    int filterCount = -1;
    private static final int READ_REQUEST_CODE = 42;
    TextView progressBarPercentage;
    ImageView imageView;
    Dialog bottomSheetDialog;
    CircularProgressBar progressBar;
    Bitmap originalImage;
    int currentCountFilter = 0;
    Bitmap FilterImage;
    RecyclerView recyclerView;
    public static final URI IMAGE_NAME = URI.create("doc1.png");
    LinearLayout ll;
    public static ArrayList<ImageDocument> documents = new ArrayList<>();
    List<ThumbnailItem> items;
    ViewEventAdapter mAdapter;
    FilterActivityForTesting mainActivity;
    LinearLayoutManager mLinearLayoutManager;
    ArrayList<Uri> arrayListCropped;
    ArrayList<Bitmap> filterImage = new ArrayList<>();
    ArrayList<Uri> finalBitmapList = new ArrayList<>();

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity_for_testing);
        ll = findViewById(R.id.ll);
        recyclerView = findViewById(R.id.recycler_view);
        items = new ArrayList<>();
        mAdapter = new ViewEventAdapter(this, items, this);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(null);
        imageReverseFilter = findViewById(R.id.imageReverseFilter);
        filterListSize = findViewById(R.id.filterListSize);

        Bundle bundle = getIntent().getExtras();
        filterNext = findViewById(R.id.filternext);
        imageView = findViewById(R.id.image_preview);
        arrayListCropped = bundle.getParcelableArrayList("mycroppedlist");

        if (arrayListCropped.size() == 1) {
            filterNext.setVisibility(View.GONE);
            filterListSize.setVisibility(View.GONE);
            imageReverseFilter.setVisibility(View.GONE);

        }
        Glide.with(getApplicationContext())
                .load(arrayListCropped.get(currentCountFilter))
                .into(imageView);
        prepare(Utils.uriToBitmap(getApplicationContext(), arrayListCropped.get(currentCountFilter)));
        int maxcountiflter = arrayListCropped.size();

        if (currentCountFilter < maxcountiflter) {
            if (filterCount > currentCountFilter)
                filterCount = currentCountFilter;
            Log.i("currentcount", "current count Filter is :" + currentCountFilter);
            Log.i("currentcount", "maxcountiflter Filter is :" + maxcountiflter);
            Log.i("currentcount", "filterCount Filter is :" + filterCount);
            filterCount++;
            filterImage.add(FilterImage);
            imageReverseFilter.setVisibility(View.GONE);
            Glide.with(getApplicationContext())
                    .load(arrayListCropped.get(currentCountFilter))
                    .into(imageView);
            prepare(Utils.uriToBitmap(getApplicationContext(), arrayListCropped.get(currentCountFilter)));
            currentCountFilter++;
            if (arrayListCropped.size() == currentCountFilter) {
                filterNext.setVisibility(View.GONE);
                imageReverseFilter.setVisibility(View.GONE);
            }
            Log.i("listpos", "Current List size : " + arrayListCropped);
        }

        filterListSize.setText(String.valueOf(currentCountFilter) + "/" + String.valueOf(arrayListCropped.size()));
        filterNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterListSize.setText(currentCountFilter + 1 + "/" + String.valueOf(arrayListCropped.size()));
                if (FilterImage != null) {
//                    File finalFile = bitmapToFile(FilterImage, "" + currentcountfilter);
//                    Uri finalUri = Uri.fromFile(finalFile);
//                    finalBitmapList.add(finalUri);
                    // FIXME: 14/02/2022 here....
                }
                if (currentCountFilter < maxcountiflter) {
                    if (filterCount > currentCountFilter)
                        filterCount = currentCountFilter;
                    Log.i("currentcount", "current count Filter is :" + currentCountFilter);
                    Log.i("currentcount", "maxcountiflter Filter is :" + maxcountiflter);
                    Log.i("currentcount", "filterCount Filter is :" + filterCount);
                    Glide.with(getApplicationContext())
                            .load(arrayListCropped.get(currentCountFilter))
                            .into(imageView);
                    prepare(Utils.uriToBitmap(getApplicationContext(), arrayListCropped.get(currentCountFilter)));
                    filterCount++;
                    currentCountFilter++;
                    filterImage.add(FilterImage);
                    if (arrayListCropped.size() == currentCountFilter) {
                        filterNext.setVisibility(View.GONE);
                        imageReverseFilter.setVisibility(View.VISIBLE);
                    }
                    Log.i("listpos", "Current List size : " + arrayListCropped);
                }
            }
        });

        imageReverseFilter.setOnClickListener(new View.OnClickListener() {

            int maxcountiflter = finalBitmapList.size();

            @Override
            public void onClick(View view) {

                imageView.setImageURI(arrayListCropped.get(currentCountFilter - 1));
                filterListSize.setText(String.valueOf(currentCountFilter) + "/" + String.valueOf(arrayListCropped.size()));

                if (currentCountFilter > maxcountiflter) {
                    if (filterCount < currentCountFilter)
                        filterCount = currentCountFilter;
                    Log.i("currentcount", "current count Filter is :" + currentCountFilter);
                    Log.i("currentcount", "maxcountiflter Filter is :" + maxcountiflter);
                    Log.i("currentcount", "filterCount Filter is :" + filterCount);
                    filterCount--;
                    currentCountFilter--;
                }
                prepare(Utils.uriToBitmap(getApplicationContext(), arrayListCropped.get(currentCountFilter)));

                if (currentCountFilter - 1 == arrayListCropped.indexOf(0)) {
                    filterCount--;
                    imageReverseFilter.setVisibility(View.GONE);
                    filterNext.setVisibility(View.VISIBLE);
                }


            }
        });
        InitBottomSheetProgress();

        findViewById(R.id.finishbutton).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {

                myCustomAlertDialog();

            }
        });
    }

    public void saveImageToGallery() {
        Toast.makeText(this, "List size: " + finalBitmapList.size(), Toast.LENGTH_SHORT).show();

        Dexter.withActivity(this).withPermissions(READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            for (Uri uri : finalBitmapList) {

                                Bitmap myBitmap = Utils.uriToBitmap(FilterActivityForTesting.this, uri);

                                ImageDocument document = new ImageDocument(uri, FilterActivityForTesting.this);
                                addToDataStore(document);

                                String path = BitmapUtils.insertImage(getContentResolver(), myBitmap,
                                        getExternalStorageDirectory().getAbsolutePath() + "/MultiImageConverter" + FilterImage, null);

                                String root = getExternalStorageDirectory().getPath();
                                File myDir = new File(root + "/MultiImageConverter");

                                myDir.mkdirs();
                                Random generator = new Random();
                                int n = 10000;
                                n = generator.nextInt(n);
                                String fname = "Image-" + n + ".jpg";

                                File file = new File(myDir, fname);
                                Log.i(TAG, "" + file);

                                if (file.exists())
                                    file.delete();

                                try {
                                    FileOutputStream out = new FileOutputStream(file);
                                    out.flush();
                                    out.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (!TextUtils.isEmpty(path)) {

                                    Snackbar snackbar = Snackbar
                                            .make(ll, "Image saved to gallery!", Snackbar.LENGTH_LONG)
                                            .setAction("OPEN", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    openImage(Uri.parse(path));
                                                }
                                            });

                                    snackbar.show();
                                } else {
                                    Snackbar snackbar = Snackbar
                                            .make(ll, "Unable to save image!", Snackbar.LENGTH_LONG);

                                    snackbar.show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }


    public void prepare(Bitmap bitmap) {
        Bitmap thumbImage;
        if (bitmap != null)
            thumbImage = bitmap;
        else
            thumbImage = BitmapUtils.GetFromAssetFolder(FilterActivityForTesting.this, IMAGE_NAME);


        ThumbnailsManager.clearThumbs();
        items.clear();

        ThumbnailItem thumbnailItem = new ThumbnailItem();
        thumbnailItem.image = thumbImage;
        thumbnailItem.filterName = "Normal";
        ThumbnailsManager.addThumb(thumbnailItem);

        List<Filter> filters = FilterPack.getFilterPack(this);

        for (Filter filter : filters) {
            ThumbnailItem tI = new ThumbnailItem();
            tI.image = thumbImage;
            tI.filter = filter;
            tI.filterName = filter.getName();
            ThumbnailsManager.addThumb(tI);
        }

        List<ThumbnailItem> myList = ThumbnailsManager.processThumbs(FilterActivityForTesting.this);
        items.addAll(myList);
        mAdapter.notifyDataSetChanged();
    }


    private void addToDataStore(ImageDocument item) {
        documents.add(item);
        mAdapter.notifyItemInserted(documents.size());
    }

    public void openImage(Uri path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(path, "image/*");
        startActivity(intent);
    }

    // FIXME: 07/02/2022 Dobara aana haii yahan.....
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void myCustomAlertDialog() {
        MyDialog = new Dialog(FilterActivityForTesting.this);
        MyDialog.setContentView(R.layout.customdialog);
        MyDialog.setTitle("Save Pdf As");

        AppCompatButton _dialogsave = MyDialog.findViewById(R.id._dialogsave);
        AppCompatButton _dialogconverttopdf = MyDialog.findViewById(R.id._dialogconverttopdf);
        AppCompatImageView _dialogIV = MyDialog.findViewById(R.id._dialogIV);


        _dialogsave.setEnabled(true);
        _dialogconverttopdf.setEnabled(true);

        _dialogIV.setImageBitmap(FilterImage);

        _dialogsave.setOnClickListener(view -> saveImageToGallery());

        _dialogconverttopdf.setOnClickListener(view -> {

            for (Uri uri : arrayListCropped) {
                ImageDocument document = new ImageDocument(uri, FilterActivityForTesting.this);
                addToDataStore(document);
            }
            // FIXME: 07/02/2022 For each laga k
/*            steps ; arraylistcropped py foreach laga
                    index items ko document me pass kr
                    addToDataStore(document);*/


//            for (int i=0; i<=arraylistcropped.size(); i++){

//            }

            convertToPDF();

        });
        if (!MyDialog.isShowing())
            MyDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void convertToPDF() {
        mainActivity = this;
        if (filterImage.size() < 1) {
            Toast.makeText(this, "You need to add at least 1 image file", Toast.LENGTH_LONG).show();
        } else {
            final Dialog dialog = new Dialog(mainActivity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            final View alertView = getLayoutInflater().inflate(R.layout.file_alert_dialog, null);
            LinearLayout layout = alertView.findViewById(R.id.savePDFLayout);
            final AppCompatSpinner spn_timezone = alertView.findViewById(R.id.pageorientation);

            String[] timezones = new String[]{"Portrait", "Landscape"};
            ArrayAdapter<String> array = new ArrayAdapter<>(mainActivity, R.layout.simple_spinner_item, timezones);
            array.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            spn_timezone.setAdapter(array);
            spn_timezone.setSelection(0);

            final AppCompatSpinner pageSize = alertView.findViewById(R.id.pagesize);

            String[] sizes = new String[]{"Fit (Same page size as image)", "A4 (297x210 mm)", "US Letter (215x279.4 mm)"};
            ArrayAdapter<String> pagearrary = new ArrayAdapter<>(mainActivity, R.layout.simple_spinner_item, sizes);
            pagearrary.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            pageSize.setAdapter(pagearrary);
            pageSize.setSelection(0);

            final AppCompatSpinner pageMargin = alertView.findViewById(R.id.margin);
            String[] margins = new String[]{"No margin", "Small", "Big"};
            ArrayAdapter<String> marginArray = new ArrayAdapter<>(mainActivity, R.layout.simple_spinner_item, margins);
            marginArray.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            pageMargin.setAdapter(marginArray);
            pageMargin.setSelection(0);

            final AppCompatSpinner compression = alertView.findViewById(R.id.compression);
            String[] compressions = new String[]{"Low", "Medium", "High"};
            ArrayAdapter<String> compressionArray = new ArrayAdapter<>(mainActivity, R.layout.simple_spinner_item, compressions);
            compressionArray.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            compression.setAdapter(compressionArray);
            compression.setSelection(2);

            final EditText edittext = alertView.findViewById(R.id.editText2);
            dialog.setContentView(alertView);
            dialog.setCancelable(true);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.show();
            dialog.getWindow().setAttributes(lp);
            dialog.findViewById(R.id.bt_close).setOnClickListener(v -> dialog.dismiss());
            dialog.findViewById(R.id.bt_save).setOnClickListener(v -> {

                String fileName = edittext.getText().toString();

                if (!fileName.equals("")) {
                    ImageToPDFAsync converter = new ImageToPDFAsync(mainActivity, documents, fileName, null);

                    converter.setPageOrientation(spn_timezone.getSelectedItem().toString());
                    converter.setPageMargin(pageMargin.getSelectedItem().toString());
                    converter.setPageSize(pageSize.getSelectedItem().toString());
                    converter.setCompression(compression.getSelectedItem().toString());
                    converter.execute();
                    dialog.dismiss();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Snackbar.make(v, "File name should not be empty", Snackbar.LENGTH_LONG).show();
                }
        });

    }

}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (result != null) {
                if (result.getClipData() != null) {
                    int count = result.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = result.getClipData().getItemAt(i).getUri();
                        ImageDocument document = new ImageDocument(imageUri, this);
                        addToDataStore(document);
                    }
                } else if (result.getData() != null) {
                    Uri imageUri = result.getData();
                    ImageDocument document = new ImageDocument(imageUri, this);
                    addToDataStore(document);
                }
            }

            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, result.getData());

            originalImage.recycle();

            FilterImage.recycle();

            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            FilterImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

            Uri imageUri = result.getData();
            imageView.setImageURI(imageUri);
        }


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

        progressBar = bottomSheetDialog.findViewById(R.id.circularProgressBar);
        progressBarPercentage = bottomSheetDialog.findViewById(R.id.progressPercentage);

        bottomSheetDialog.getWindow().setAttributes(lp);
    }


    public void setProgress(int progress, int total) {
        this.progressBar.setProgress(progress);
        int percentage = (progress * 100) / total;
        this.progressBarPercentage.setText(percentage + "%");
        mAdapter.notifyDataSetChanged();
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
        this.finish();
    }

    public void showBottomSheet(int size) {
        bottomSheetDialog.show();
        this.progressBar.setProgressMax(size);
        this.progressBar.setProgress(0);
    }

    @Override
    public void onFilterSelected(@NonNull Filter filter) {
        try {
            Uri countGotUri;
            int list_size = arrayListCropped.size();
            if (filterCount <= list_size) {
                countGotUri = arrayListCropped.get(filterCount);
                //TODO: yahan changing kr rha hn


                /*String fileName = System.currentTimeMillis() + ".png";
                File savedFile = bitmapToFile( countGotUri, fileName);
                finalBitmapList.add(Uri.fromFile(savedFile));*/


                Log.i("filterClick", "inside " + countGotUri);
            } else {
                countGotUri = arrayListCropped.get(list_size);
                Log.i("filterClick", "outside " + countGotUri);
            }
            FilterImage = Utils.uriToBitmap(getApplicationContext(), countGotUri);
            FilterImage = FilterImage.copy(Bitmap.Config.ARGB_8888, true);

            Glide.with(getApplicationContext()).load(filter.processFilter(FilterImage)).into(imageView);
            Log.i("filterClick", "onFilterSelected: Name  : " + filter.getName());
            Log.i("filterClick", "onFilterSelected Bitmap : " + FilterImage);
            Log.i("filterClick", "onFilterSelected count  : " + filterCount);
            Log.i("filterClick", "onFilterSelected Uri List : " + arrayListCropped.size());

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("filterClick", "onFilterSelected: Error we got : " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(FilterActivityForTesting.this, MainActivity.class);
        startActivity(intent);
    }
}