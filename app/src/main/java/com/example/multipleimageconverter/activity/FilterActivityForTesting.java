package com.example.multipleimageconverter.activity;

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
import android.widget.Button;
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
import com.example.multipleimageconverter.R;
import com.example.multipleimageconverter.adapter.ViewEventAdapter;
import com.example.multipleimageconverter.modelClass.ImageDocument;
import com.example.multipleimageconverter.util.BitmapUtils;
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
    private static final int PERMISSION_REQUEST_CODE = 202;
    //        implements ViewEventAdapter.ThumbnailsAdapterListener {
    Dialog MyDialog;
    Button backfilter;
    boolean a =true;
    Button rotateFilter;
    CircleImageView filternext, imageReverseFilter;
    TextView filterListSize;
    int angle = 0;
    int filterCount = -1;
    boolean aaa= true;
    int filtercount=1;
    private static final int READ_REQUEST_CODE = 42;
    private TextView progressBarPercentage;
    //    AppCompatButton save, mConvertToPDF;
    private TextView tv;
    ImageView imageView;
    private Dialog bottomSheetDialog;
    private CircularProgressBar progressBar;
    Bitmap originalImage;
    int currentcountfilter = 0;
    Bitmap FilterImage;
    RecyclerView recyclerView;
    public static final URI IMAGE_NAME = URI.create("doc1.png");
    LinearLayout ll;
    public static ArrayList<ImageDocument> documents = new ArrayList<>();
    List<ThumbnailItem> items;
    ViewEventAdapter mAdapter;
    FilterActivityForTesting mainActivity;
    String uri;
    String stringUri;
    Uri countGotUri;
    //    List<Uri> countGotUri;
    LinearLayoutManager mLinearLayoutManager;
    public static final int SELECT_GALLERY_IMAGE = 22;
    //    ArrayList<Uri> arraylistcropped = new ArrayList<>();
    ArrayList<Uri> arraylistcropped;
    ArrayList<Uri> arrayListSelected;
    //    Uri countGotUri;
    ArrayList<Bitmap> filterimage = new ArrayList<>();
    ArrayList<Uri> finalBitmapList = new ArrayList<>();
    static {
        System.loadLibrary("NativeImageProcessor");
    }
    private Uri fileUri;
    private FilterActivityForTesting filteractivity;
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
//        LoadImage();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(null);
        imageReverseFilter = findViewById(R.id.imageReverseFilter);
//        backfilter = findViewById(R.id.backfilter);
//        rotateFilter = findViewById(R.id.rotateFilter);
        filterListSize = findViewById(R.id.filterListSize);
//        rotateFilter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                angle = angle + 90;
//                imageView.setRotation(angle);
//            }
//        });
//        backfilter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(FilterActivityForTesting.this, CroppedActivity.class);
//                startActivity(intent);
//            }
//        });



        Bundle bundle = getIntent().getExtras();
//        String message = bundle.getString("mycroppedlist");
        filternext = findViewById(R.id.filternext);
        imageView = findViewById(R.id.image_preview);
        arraylistcropped = bundle.getParcelableArrayList("mycroppedlist");
//        arraylistcropped=bundle.getParcelableArrayList("mySelectedlist");
/*        Glide.with(getApplicationContext())
                .load(arraylistcropped.get(currentcountfilter))
                .into(imageView);*/

        if (arraylistcropped.size() == 1) {
            filternext.setVisibility(View.GONE);
            filterListSize.setVisibility(View.GONE);
            imageReverseFilter.setVisibility(View.GONE);

        }
        Glide.with(getApplicationContext())
                .load(arraylistcropped.get(currentcountfilter))
                .into(imageView);
        prepare(Utils.uriToBitmap(getApplicationContext(), arraylistcropped.get(currentcountfilter)));
        int maxcountiflter = arraylistcropped.size();

        if (currentcountfilter < maxcountiflter) {
            if (filterCount > currentcountfilter)
                filterCount = currentcountfilter;
            Log.i("currentcount", "current count Filter is :" + currentcountfilter);
            Log.i("currentcount", "maxcountiflter Filter is :" + maxcountiflter);
            Log.i("currentcount", "filterCount Filter is :" + filterCount);
            filterCount++;
            /*  if (currentcountfilter==arraylistcropped.size()) {*/
            filterimage.add(FilterImage);
            /*    }else{*/
//               filterimage.set(currentcountfilter, FilterImage);
//           }

            imageReverseFilter.setVisibility(View.GONE);
//                    Toast.makeText(getApplicationContext(), "current count filter  we got is :" + currentcountfilter, Toast.LENGTH_SHORT).show();
//                    Bitmap ui = arraylistcropped.get(currentcountfilter);
            Glide.with(getApplicationContext())
                    .load(arraylistcropped.get(currentcountfilter))
                    .into(imageView);
            prepare(Utils.uriToBitmap(getApplicationContext(), arraylistcropped.get(currentcountfilter)));
            //        imageView.setImageURI(arraylistcropped.get(currentcountfilter));
//                    Glide.with(getApplicationContext()).load(imageView).into(imageView);
            currentcountfilter++;
            if (arraylistcropped.size() == currentcountfilter) {
                filternext.setVisibility(View.GONE);
                imageReverseFilter.setVisibility(View.GONE);
            }
            Log.i("listpos", "Current List size : " + arraylistcropped);
        }

        filterListSize.setText(String.valueOf(currentcountfilter) + "/" + String.valueOf(arraylistcropped.size()));
        /*
         */
/*
        imageView.setImageURI(arraylistcropped.get(0));
*/
        filternext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                    imageReverseFilter.setVisibility(View.VISIBLE);
                filterListSize.setText(currentcountfilter + 1 + "/" + String.valueOf(arraylistcropped.size()));
                if (FilterImage != null) {
//                    File finalFile = bitmapToFile(FilterImage, "" + currentcountfilter);
//                    Uri finalUri = Uri.fromFile(finalFile);
//                    finalBitmapList.add(finalUri);
                    // FIXME: 14/02/2022 here....
                }
                if (currentcountfilter < maxcountiflter) {
                    if (filterCount > currentcountfilter)
                        filterCount = currentcountfilter;
                    Log.i("currentcount", "current count Filter is :" + currentcountfilter);
                    Log.i("currentcount", "maxcountiflter Filter is :" + maxcountiflter);
                    Log.i("currentcount", "filterCount Filter is :" + filterCount);
//                        filterCount++;
//                        filterimage.add(FilterImage);

//                   Toast.makeText(getApplicationContext(), "current count filter  we got is :" + currentcountfilter, Toast.LENGTH_SHORT).show();
//                    Bitmap ui = arraylistcropped.get(currentcountfilter);
                    Glide.with(getApplicationContext())
                            .load(arraylistcropped.get(currentcountfilter))
                            .into(imageView);
                    prepare(Utils.uriToBitmap(getApplicationContext(), arraylistcropped.get(currentcountfilter)));
                    //        imageView.setImageURI(arraylistcropped.get(currentcountfilter));
//                    Glide.with(getApplicationContext()).load(imageView).into(imageView);
                    filterCount++;
                    currentcountfilter++;
                    filterimage.add(FilterImage);
                    if (arraylistcropped.size() == currentcountfilter) {
                        filternext.setVisibility(View.GONE);
//                            filterCount--;
//                       currentcountfilter--;

                        imageReverseFilter.setVisibility(View.VISIBLE);
                    }
                    Log.i("listpos", "Current List size : " + arraylistcropped);
                }
               /* else if (arraylistcropped.size()==currentcountfilter){
                    filternext.setVisibility(View.GONE);
                }*/
            }
        });

        imageReverseFilter.setOnClickListener(new View.OnClickListener() {

            int maxcountiflter = finalBitmapList.size();
            @Override
            public void onClick(View view) {

                imageView.setImageURI(arraylistcropped.get(currentcountfilter-1));
                filterListSize.setText(String.valueOf(currentcountfilter) + "/" + String.valueOf(arraylistcropped.size()));
//                prepare(Utils.uriToBitmap(getApplicationContext(), arraylistcropped.get(currentcountfilter)));

                if (currentcountfilter > maxcountiflter) {
                    if (filterCount < currentcountfilter)
                        filterCount = currentcountfilter;
                    Log.i("currentcount", "current count Filter is :" + currentcountfilter);
                    Log.i("currentcount", "maxcountiflter Filter is :" + maxcountiflter);
                    Log.i("currentcount", "filterCount Filter is :" + filterCount);
                    filterCount--;
                    currentcountfilter--;
//                    filterimage.set(currentcountfilter,FilterImage);
                }
                prepare(Utils.uriToBitmap(getApplicationContext(), arraylistcropped.get(currentcountfilter)));

                if (currentcountfilter -1 == arraylistcropped.indexOf(0)) {
                    filterCount--;
                    imageReverseFilter.setVisibility(View.GONE);
                    filternext.setVisibility(View.VISIBLE);

//                    Toast.makeText(getApplicationContext(), "Press Save All ", Toast.LENGTH_SHORT).show();
                }


            }
        });
        InitBottomSheetProgress();
//        findViewById(R.id.gallery).
//
//                setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Dexter.withActivity(FilterActivityForTesting.this).withPermissions(READ_EXTERNAL_STORAGE,
//                                WRITE_EXTERNAL_STORAGE)
//                                .withListener(new MultiplePermissionsListener() {
//                                    @Override
//                                    public void onPermissionsChecked(MultiplePermissionsReport report) {
//                                        if (report.areAllPermissionsGranted()) {
//                                            Intent intent = new Intent(Intent.ACTION_PICK);
//                                            intent.setType("image/*");
//                                            startActivityForResult(intent, SELECT_GALLERY_IMAGE);
//                                        } else {
//                                            Toast.makeText(getApplicationContext(), "Permissions are not granted!",
//                                                    Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
//                                                                                   PermissionToken token) {
//                                        token.continuePermissionRequest();
//                                    }
//                                }).check();
//
//                    }
//                });


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


//                            for (Bitmap myBitmap : filterimage) {
                               /*myBitmap = Utils.uriToBitmap(FilterActivityForTesting.this, finalBitmapList);

                            }*/


                            // kl yahan sy hi agy krna hy
                            //

                            for (Uri uri : finalBitmapList) {

                                Bitmap myBitmap = Utils.uriToBitmap(FilterActivityForTesting.this, uri);

                                ImageDocument document = new ImageDocument(uri, FilterActivityForTesting.this);
                                addToDataStore(document);
                                // do something with object

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
//                               bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
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
//                            else{
//                                Toast.makeText(getApplicationContext(), "Permissions are not granted!",
//                                        Toast.LENGTH_SHORT).show();
//                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }


    //
    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denined.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }


    public void prepare(Bitmap bitmap) {
        Bitmap thumbImage;
        if (bitmap != null)
            thumbImage = bitmap;
        else
            thumbImage = BitmapUtils.GetFromAssetFolder(FilterActivityForTesting.this, IMAGE_NAME);


        ThumbnailsManager.clearThumbs();
        items.clear();


//         add normal bitmap first
       /* try {
            thumbImage = getBitmap_Uri(countGotUri);
        } catch (IOException e) {
            Log.e("Error", "prepare: Error we got is (onPrepare) -> thumb uri  : " + e.getLocalizedMessage());
        }*/

    /*    thumbImage = (Bitmap) BitmapUtils.GetFromAssetFolder(FilterActivityForTesting.this,
                filteractivity.IMAGE_NAME);

*/
//        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(uri), 40, 40);
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
//        list<> myList  =  ThumbnailsManager.processThumbs(filteractivity.this);
//                addAll(ThumbnailsManager.processThumbs(this));
        mAdapter.notifyDataSetChanged();
    }

    private Bitmap getBitmap_Uri(Uri fileUri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(getApplicationContext().getContentResolver(), fileUri));
        } else {
            return MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), fileUri);
        }
    }


    private void addToDataStore(ImageDocument item) {
        documents.add(item);
        mAdapter.notifyItemInserted(documents.size());
    }

//    @Override
//    public void onFilterSelected(Filter filter) {
//        FilterImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
//        imageView.setImageBitmap(filter.processFilter(FilterImage));
//    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK && requestCode == SELECT_GALLERY_IMAGE) {
////            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData());
////
////            originalImage.recycle();
////
////            FilterImage.recycle();
////
////            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
////            FilterImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
////
////            Uri imageUri = data.getData();
////            imageView.setImageURI(imageUri);
//        }
//
//    }

    //    Gallery Picker
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
            //
//            ImageDocument document = new ImageDocument(arraylistcropped.get(0), Filteractivity.this);

            for (Uri uri : arraylistcropped) {
                ImageDocument document = new ImageDocument(uri, FilterActivityForTesting.this);
                addToDataStore(document);
                // do something with object
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
        if (filterimage.size() < 1) {
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

                if (ContextCompat.checkSelfPermission(mainActivity, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    CheckStoragePermission();
                } else {
                    String fileName = edittext.getText().toString();

                    if (!fileName.equals("")) {
                        ImageToPDFAsync converter = new ImageToPDFAsync(mainActivity, documents, fileName, null);

                        converter.setPageOrientation(spn_timezone.getSelectedItem().toString());
                        converter.setPageMargin(pageMargin.getSelectedItem().toString());
                        converter.setPageSize(pageSize.getSelectedItem().toString());
                        converter.setCompression(compression.getSelectedItem().toString());
//                        String pdffile=Environment.getExternalStorageState()+ "/mypdffile.pdf";
                        converter.execute();
                        dialog.dismiss();
                        Intent intent = new Intent(this, MainActivity.class);
                        /*
                        Bundle bundle = new Bundle();
                        // send croppedUriList to next activity
                        bundle.putParcelableArrayList("myfilterlist", finalBitmapList);
//            bundle.putParcelableArrayList("mySelectedlist", selectedUriList);
                        intent.putExtras(bundle);*/
                        startActivity(intent);


                    } else {
                        Snackbar.make(v, "File name should not be empty", Snackbar.LENGTH_LONG).show();
                    }
                }
            });

        }
    }
    //
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


    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
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
        //  progressBarCount = (TextView) ocrProgressdialog.findViewById(R.id.progressCount);

        bottomSheetDialog.getWindow().setAttributes(lp);
    }


    public void setProgress(int progress, int total) {
        this.progressBar.setProgress(progress);
        // this.progressBarCount.setText(progress + "/" + total);
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


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//    private MenuItem mainMenuItem;
//    private final boolean isChecked = false;
//
//    //>>>>>>>>>>>>MENU
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.sort_menu, menu);
//        mainMenuItem = menu.findItem(R.id.fileSort);
//        return true;
//    }
//
//    Comparator<ThumbnailItem> comparator = null;
//
//
//    //>>>>>>>>>.
//
//    private void sortFiles(Comparator<ThumbnailItem> comparator) {
//        Collections.sort(items, comparator);
//        mAdapter.notifyDataSetChanged();
//    }


    private void CheckStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    WRITE_EXTERNAL_STORAGE)) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Storage Permission");
                alertDialog.setMessage("Storage permission is required in order to " +
                        "provide PDF merge feature, please enable permission in app settings");
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
                ActivityCompat.requestPermissions(this,
                        new String[]{WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }
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
            int list_size = arraylistcropped.size();
            if (filterCount <= list_size) {
                countGotUri = arraylistcropped.get(filterCount);
                //TODO: yahan changing kr rha hn


                /*String fileName = System.currentTimeMillis() + ".png";
                File savedFile = bitmapToFile( countGotUri, fileName);
                finalBitmapList.add(Uri.fromFile(savedFile));*/


                Log.i("filterClick", "inside " + countGotUri);
            } else {
                countGotUri = arraylistcropped.get(list_size);
                Log.i("filterClick", "outside " + countGotUri);
            }
            FilterImage = Utils.uriToBitmap(getApplicationContext(), countGotUri);
            FilterImage = FilterImage.copy(Bitmap.Config.ARGB_8888, true);

//            finalBitmapList.add(getImageUri(this,FilterImage));
            Glide.with(getApplicationContext()).load(filter.processFilter(FilterImage)).into(imageView);
            Log.i("filterClick", "onFilterSelected: Name  : " + filter.getName());
            Log.i("filterClick", "onFilterSelected Bitmap : " + FilterImage);
            Log.i("filterClick", "onFilterSelected count  : " + filterCount);
            Log.i("filterClick", "onFilterSelected Uri List : " + arraylistcropped.size());

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("filterClick", "onFilterSelected: Error we got : " + e.getLocalizedMessage());
        }
    }


    /*    try {
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "MultiImageConverter");
            if (!folder.exists() && folder.mkdirs())
                Log.i("TAG", "Folder created");
            else Log.i("TAG", "Folder exists");

            File file = new File(folder, fileNameToSave);
            if (file.createNewFile()) {
                // Convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos); // YOU can also save it in JPEG
                byte[] bytes = bos.toByteArray();

                // write the bytes in file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.flush();
                fos.close();
                return file;
            }
            Log.e("TAG", "Failed to create file");
            return null; // it will return null
        } catch (Exception e) {
            e.printStackTrace();
            return null; // it will return null
        }
    }
*/

    @Nullable
    public static File bitmapToFile(Bitmap bitmap, String fileNameToSave) {
        Log.i("TAG", "File name :" + fileNameToSave);
        try {
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "MultiImageConverter");
            /*    if (!folder.exists() && folder.mkdirs())*/

            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    Log.d(TAG, "Successfully created the parent dir:" + folder.getName());
                } else {
                    Log.d(TAG, "Failed to create the parent dir:" + folder.getName());
                }
            }

//
//
//
//                Log.i("TAG", "Folder created");
//            else Log.i("TAG", "Folder exists");

            File file = new File(folder, fileNameToSave);
            if (file.createNewFile()) {
                // Convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos); // YOU can also save it in JPEG
                byte[] bytes = bos.toByteArray();

                // write the bytes in file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.flush();
                fos.close();
                return file;
            }
            Log.e("TAG", "Failed to create file");
            return null; // it will return null
        } catch (Exception e) {
            e.printStackTrace();
            return null; // it will return null
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(FilterActivityForTesting.this, MainActivity.class);
        startActivity(intent);
    }
}