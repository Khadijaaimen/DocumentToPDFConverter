package com.example.multipleimageconverter;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;
import static android.os.Environment.getExternalStorageDirectory;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mikhaellopez.circularprogressbar.BuildConfig;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class FilterActivityForTesting extends AppCompatActivity implements ViewEventAdapter.ThumbnailsAdapterListener {

    Dialog MyDialog;
    ImageView filterNext, imageReverseFilter;
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
    Bitmap myLogo;
    InterstitialAd mInterstitialAd;
    Boolean isChecked = false, isButtonClicked;
    ArrayList<Bitmap> filterImage = new ArrayList<>();
    ArrayList<Uri> finalBitmapList = new ArrayList<>();

    ArrayList<Filter> selectedFilters = new ArrayList<>();
    ArrayList<Bitmap> selectedFiltersBitmap = new ArrayList<>();
    Integer selectedImageIndex = 0;

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private void CheckStoragePermission() {
        if (ContextCompat.checkSelfPermission(FilterActivityForTesting.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(FilterActivityForTesting.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog alertDialog = new AlertDialog.Builder(FilterActivityForTesting.this).create();
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
                ActivityCompat.requestPermissions(FilterActivityForTesting.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity_for_testing);

        CheckStoragePermission();

        if (ContextCompat.checkSelfPermission(FilterActivityForTesting.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            CheckStoragePermission();
        }

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

        isButtonClicked = AppPreferences.isButtonCLicked(getApplication());

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        if (!isButtonClicked) {
            setAds();
        }

        myLogo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.photo);

        Glide.with(getApplicationContext())
                .load(arrayListCropped.get(currentCountFilter))
                .into(imageView);
        prepare(myLogo);
        int maxcountiflter = arrayListCropped.size();

        if (currentCountFilter < maxcountiflter) {
            if (filterCount > currentCountFilter)
                filterCount = currentCountFilter;
            filterCount++;
            filterImage.add(FilterImage);
            imageReverseFilter.setVisibility(View.GONE);
            Glide.with(getApplicationContext())
                    .load(arrayListCropped.get(currentCountFilter))
                    .into(imageView);
            prepare(myLogo);
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

                Glide.with(getApplicationContext())
                        .load(selectedFilters.get(++selectedImageIndex).processFilter(selectedFiltersBitmap.get(selectedImageIndex)))
                        .into(imageView);

                filterListSize.setText(selectedImageIndex + 1 + "/" + String.valueOf(arrayListCropped.size()));

                if (selectedImageIndex + 1 >= arrayListCropped.size()) {
                    filterNext.setVisibility(View.GONE);
                }

                imageReverseFilter.setVisibility(View.VISIBLE);

            }
        });

        imageReverseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Glide.with(getApplicationContext())
                        .load(selectedFilters.get(--selectedImageIndex).processFilter(selectedFiltersBitmap.get(selectedImageIndex)))
                        .into(imageView);

                filterListSize.setText(selectedImageIndex + 1 + "/" + String.valueOf(arrayListCropped.size()));

                if (selectedImageIndex <= 0) {
                    imageReverseFilter.setVisibility(View.GONE);
                }

                filterNext.setVisibility(View.VISIBLE);
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

    public void setAds() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, getString(R.string.adUnitID), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("TAG", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("TAG", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    public void saveImageToGallery() {
        if (!isButtonClicked) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(FilterActivityForTesting.this);

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        mainActivity = FilterActivityForTesting.this;
                        getImageUri(mainActivity, selectedFiltersBitmap);
                        Dexter.withActivity(mainActivity).withPermissions(READ_EXTERNAL_STORAGE,
                                WRITE_EXTERNAL_STORAGE)
                                .withListener(new MultiplePermissionsListener() {
                                    @Override
                                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                                        File file = null;
                                        if (report.areAllPermissionsGranted()) {
                                            Bitmap myBitmap = null;

                                            for (Uri uri : finalBitmapList) {
                                                myBitmap = Utils.uriToBitmap(FilterActivityForTesting.this, uri);
                                                String root = getApplication().getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/MultiImageConverter";
                                                File myDir = new File(root);
                                                myDir.mkdirs();
                                                Random generator = new Random();
                                                int n = 10000;
                                                n = generator.nextInt(n);
                                                String fname = "Image-" + n + ".jpg";
                                                file = new File(myDir, fname);
                                                System.out.println(file.getAbsolutePath());
                                                if (file.exists()) file.delete();
                                                Log.i("LOAD", root + fname);
                                                try {
                                                    FileOutputStream out = new FileOutputStream(file);
                                                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                                    out.flush();
                                                    out.close();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
                                            }
                                            if (file.exists()) {
                                                Toast.makeText(getApplicationContext(), "Image saved to gallery!", Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(FilterActivityForTesting.this, MainActivity.class);
                                                startActivity(intent);
                                                mAdapter.notifyItemInserted(0);
                                                mAdapter.notifyDataSetChanged();
                                                finalBitmapList.clear();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Unable to save image!", Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    }

                                    @Override
                                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                                   PermissionToken token) {
                                        token.continuePermissionRequest();
                                    }
                                }).check();
                        mInterstitialAd = null;
                        setAds();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        isChecked = true;
                        AppPreferences.setButtonCLicked(getApplication(), true);
                        mainActivity = FilterActivityForTesting.this;
                        getImageUri(mainActivity, selectedFiltersBitmap);
                        Dexter.withActivity(mainActivity).withPermissions(READ_EXTERNAL_STORAGE,
                                WRITE_EXTERNAL_STORAGE)
                                .withListener(new MultiplePermissionsListener() {
                                    @Override
                                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                                        File file = null;
                                        if (report.areAllPermissionsGranted()) {
                                            Bitmap myBitmap = null;

                                            for (Uri uri : finalBitmapList) {
                                                myBitmap = Utils.uriToBitmap(FilterActivityForTesting.this, uri);
                                                String root = getApplication().getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/MultiImageConverter";
                                                File myDir = new File(root);
                                                myDir.mkdirs();
                                                Random generator = new Random();
                                                int n = 10000;
                                                n = generator.nextInt(n);
                                                String fname = "Image-" + n + ".jpg";
                                                file = new File(myDir, fname);
                                                System.out.println(file.getAbsolutePath());
                                                if (file.exists()) file.delete();
                                                Log.i("LOAD", root + fname);
                                                try {
                                                    FileOutputStream out = new FileOutputStream(file);
                                                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                                    out.flush();
                                                    out.close();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
                                            }
                                            if (file.exists()) {
                                                Toast.makeText(getApplicationContext(), "Image saved to gallery!", Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(FilterActivityForTesting.this, MainActivity.class);
                                                startActivity(intent);
                                                mAdapter.notifyItemInserted(0);
                                                mAdapter.notifyDataSetChanged();
                                                finalBitmapList.clear();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Unable to save image!", Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    }

                                    @Override
                                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                                   PermissionToken token) {
                                        token.continuePermissionRequest();
                                    }
                                }).check();
                        mInterstitialAd = null;
                    }
                });
            }
        } else {
            mainActivity = FilterActivityForTesting.this;
            getImageUri(mainActivity, selectedFiltersBitmap);
            Dexter.withActivity(mainActivity).withPermissions(READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            File file = null;
                            if (report.areAllPermissionsGranted()) {
                                Bitmap myBitmap = null;

                                for (Uri uri : finalBitmapList) {
                                    myBitmap = Utils.uriToBitmap(FilterActivityForTesting.this, uri);
                                    String root = getApplication().getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/MultiImageConverter";
                                    File myDir = new File(root);
                                    myDir.mkdirs();
                                    Random generator = new Random();
                                    int n = 10000;
                                    n = generator.nextInt(n);
                                    String fname = "Image-" + n + ".jpg";
                                    file = new File(myDir, fname);
                                    System.out.println(file.getAbsolutePath());
                                    if (file.exists()) file.delete();
                                    Log.i("LOAD", root + fname);
                                    try {
                                        FileOutputStream out = new FileOutputStream(file);
                                        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                        out.flush();
                                        out.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
                                }
                                if (file.exists()) {
                                    Toast.makeText(getApplicationContext(), "Image saved to gallery!", Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(FilterActivityForTesting.this, MainActivity.class);
                                    startActivity(intent);
                                    mAdapter.notifyItemInserted(0);
                                    mAdapter.notifyDataSetChanged();
                                    finalBitmapList.clear();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Unable to save image!", Toast.LENGTH_LONG).show();
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

    }

    public ArrayList<Uri> getImageUri(Context inContext, ArrayList<Bitmap> inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String path;
        for (int i = 0; i < inImage.size(); i++) {
            inImage.get(i).compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage.get(i), "Image", null);
            finalBitmapList.add(Uri.parse(path));
        }
        return finalBitmapList;
    }


    public void prepare(Bitmap bitmap) {
        Bitmap thumbImage;
        if (bitmap != null)
            thumbImage = myLogo;
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

        selectedFilters.clear();
        selectedFiltersBitmap.clear();
        for (int i = 0; i < arrayListCropped.size(); i++) {
            selectedFilters.add(items.get(0).filter);
            Bitmap selectedFilterBitmap = Utils.uriToBitmap(getApplicationContext(), arrayListCropped.get(i));
            selectedFilterBitmap = selectedFilterBitmap.copy(Bitmap.Config.ARGB_8888, true);
            selectedFiltersBitmap.add(selectedFilterBitmap);
        }

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

            convertToPDF();

        });
        if (!MyDialog.isShowing())
            MyDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void convertToPDF() {
        if (!isButtonClicked) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(FilterActivityForTesting.this);

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        mainActivity = FilterActivityForTesting.this;
                        getImageUri(mainActivity, selectedFiltersBitmap);
                        for (Uri uri : finalBitmapList) {
                            ImageDocument document = new ImageDocument(uri, FilterActivityForTesting.this);
                            documents.add(document);
                            mAdapter.notifyItemInserted(documents.size());
                        }
                        if (filterImage.size() < 1) {
                            Toast.makeText(mainActivity, "You need to add at least 1 image file", Toast.LENGTH_LONG).show();
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
                            pageSize.setSelection(1);

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
                            compression.setSelection(0);

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

                                String fileName = edittext.getText().toString().trim();

                                if (!fileName.equals("")) {
                                    ImageToPDFAsync converter = new ImageToPDFAsync(mainActivity, documents, fileName, null);

                                    converter.setPageOrientation(spn_timezone.getSelectedItem().toString());
                                    converter.setPageMargin(pageMargin.getSelectedItem().toString());
                                    converter.setPageSize(pageSize.getSelectedItem().toString());
                                    converter.setCompression(compression.getSelectedItem().toString());
                                    converter.execute();
                                    dialog.dismiss();

                                    Intent intent = new Intent(FilterActivityForTesting.this, MainActivity.class);
                                    intent.putExtra("first_page", finalBitmapList.get(0));
                                    finalBitmapList.clear();
                                    startActivity(intent);
                                    mAdapter.notifyItemInserted(0);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    Snackbar.make(v, "File name should not be empty", Snackbar.LENGTH_LONG).show();
                                }
                            });

                        }
                        mInterstitialAd = null;
                        setAds();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        isChecked = true;
                        AppPreferences.setButtonCLicked(getApplication(), true);
                        mainActivity = FilterActivityForTesting.this;
                        getImageUri(mainActivity, selectedFiltersBitmap);
                        for (Uri uri : finalBitmapList) {
                            ImageDocument document = new ImageDocument(uri, FilterActivityForTesting.this);
                            documents.add(document);
                            mAdapter.notifyItemInserted(documents.size());
                        }
                        if (filterImage.size() < 1) {
                            Toast.makeText(mainActivity, "You need to add at least 1 image file", Toast.LENGTH_LONG).show();
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
                            pageSize.setSelection(1);

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
                            compression.setSelection(0);

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

                                String fileName = edittext.getText().toString().trim();

                                if (!fileName.equals("")) {
                                    ImageToPDFAsync converter = new ImageToPDFAsync(mainActivity, documents, fileName, null);

                                    converter.setPageOrientation(spn_timezone.getSelectedItem().toString());
                                    converter.setPageMargin(pageMargin.getSelectedItem().toString());
                                    converter.setPageSize(pageSize.getSelectedItem().toString());
                                    converter.setCompression(compression.getSelectedItem().toString());
                                    converter.execute();
                                    dialog.dismiss();

                                    Intent intent = new Intent(FilterActivityForTesting.this, MainActivity.class);
                                    intent.putExtra("first_page", finalBitmapList.get(0));
                                    finalBitmapList.clear();
                                    startActivity(intent);
                                    mAdapter.notifyItemInserted(0);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    Snackbar.make(v, "File name should not be empty", Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                        mInterstitialAd = null;
                    }
                });
            }
        } else {
            mainActivity = this;
            getImageUri(mainActivity, selectedFiltersBitmap);
            for (Uri uri : finalBitmapList) {
                ImageDocument document = new ImageDocument(uri, FilterActivityForTesting.this);
                documents.add(document);
                mAdapter.notifyItemInserted(documents.size());
            }
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
                pageSize.setSelection(1);

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
                compression.setSelection(0);

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

                    String fileName = edittext.getText().toString().trim();

                    if (!fileName.equals("")) {
                        ImageToPDFAsync converter = new ImageToPDFAsync(mainActivity, documents, fileName, null);

                        converter.setPageOrientation(spn_timezone.getSelectedItem().toString());
                        converter.setPageMargin(pageMargin.getSelectedItem().toString());
                        converter.setPageSize(pageSize.getSelectedItem().toString());
                        converter.setCompression(compression.getSelectedItem().toString());
                        converter.execute();
                        dialog.dismiss();

                        Intent intent = new Intent(FilterActivityForTesting.this, MainActivity.class);
                        intent.putExtra("first_page", finalBitmapList.get(0));
                        finalBitmapList.clear();
                        startActivity(intent);
                        mAdapter.notifyItemInserted(0);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Snackbar.make(v, "File name should not be empty", Snackbar.LENGTH_LONG).show();
                    }
                });

            }
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
    public void onBackPressed() {
        Intent intent = new Intent(FilterActivityForTesting.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFilterSelected(Filter filter) {
        try {
            FilterImage = Utils.uriToBitmap(getApplicationContext(), arrayListCropped.get(selectedImageIndex));
            FilterImage = FilterImage.copy(Bitmap.Config.ARGB_8888, true);

            selectedFilters.set(selectedImageIndex, filter);
            selectedFiltersBitmap.set(selectedImageIndex, FilterImage);

            Glide.with(getApplicationContext()).load(filter.processFilter(FilterImage)).into(imageView);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("filterClick", "onFilterSelected: Error we got : " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppPreferences.setButtonCLicked(getApplication(), false);
    }
}