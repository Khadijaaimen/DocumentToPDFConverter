package com.example.multipleimageconverter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class CroppedActivity extends AppCompatActivity {
    private CropImageView cropperView;
    private Button rotateLeft;
    private Button done;
    private int processedImage = 0;
    private final Context context = this;
    ImageView nextButton, imageReverse;
    TextView currentStateCount;
    private ArrayList<Uri> selectedUriList = new ArrayList<>();
    private ArrayList<Uri> croppedUriList = new ArrayList<>();
    String cameraValueIntent;
    ArrayList<Rect> cropRects = new ArrayList<>();
    TextView textView7;

    InterstitialAd mInterstitialAd;
    Boolean isChecked = false, isButtonClicked;
    boolean a = true;
    boolean listRotation = false;
    AtomicInteger imageCount = new AtomicInteger(1);

    @Nullable
    public File bitmapToFile(Bitmap bitmap, String fileNameToSave) {
        Log.i("TAG", "File name :" + fileNameToSave);
        try {
            File cacheDir = getBaseContext().getCacheDir();

            File folder = new File(cacheDir, "pic");
            if (!folder.exists() && folder.mkdirs())
                Log.i("TAG", "Folder created");
            else Log.i("TAG", "Folder exists");

            File file = new File(folder, fileNameToSave);
            if (file.createNewFile()) {

                // Convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos); // YOU can also save it in JPEG
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropped_test);

        done = findViewById(R.id.done);
        textView7 = findViewById(R.id.textView7);
        rotateLeft = findViewById(R.id.rotateleft);
        rotateLeft.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                rotateLeft.setBackgroundResource(R.drawable.rotate_left_colored);
                cropperView.rotateImage(-90);
                cropperView.setRotationX(0);
                cropperView.setRotationY(0);
            }
        });
        nextButton = findViewById(R.id.imageview);

        currentStateCount = findViewById(R.id.currentStateCount);
        Log.i("procee", "process image we got is :" + processedImage);
        Button imageReset = findViewById(R.id.imageReset);
//        Button imageDelete = findViewById(R.id.imageDelete);
        Button imageRotate = findViewById(R.id.imageRotate);
        imageRotate.setOnClickListener(view -> {
            cropperView.rotateImage(90);
            cropperView.setRotationX(0);
            cropperView.setRotationY(0);
        });
        imageReverse = findViewById(R.id.imageReverse);
        imageReverse.setVisibility(View.GONE);
        cropperView = findViewById(R.id.cropperView);
        Bundle extras = getIntent().getExtras();
        String message = extras.getString("message");
        cameraValueIntent = extras.getString("cameraValue");

        if (cameraValueIntent != null) {
            currentStateCount.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            imageReverse.setVisibility(View.GONE);
        }
        Log.i("camera", "string we got is :" + cameraValueIntent);

//        Log.i("size","cropped list we got is :" + croppedUriList.size());

        if (message != null) {
            cropperView.setImageUriAsync(Uri.parse(message));
        }

        croppedUriList = selectedUriList;
        if (extras.getParcelableArrayList("mylist") != null) {
            selectedUriList = extras.getParcelableArrayList("mylist");
            Log.i("size", "selcted list we got is :" + selectedUriList.size());
            cropperView.setImageUriAsync(selectedUriList.get(processedImage));
        }

        if (selectedUriList.size() == 1) {
            nextButton.setVisibility(View.GONE);
            imageReverse.setVisibility(View.GONE);
            currentStateCount.setVisibility(View.GONE);
            done.setVisibility(View.VISIBLE);
            textView7.setVisibility(View.VISIBLE);
        }

        isButtonClicked = AppPrefrences.isButtonCLicked(getApplication());

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        if (!isButtonClicked) {
            setAds();
        }

        currentStateCount.setText(imageCount + "/" + selectedUriList.size());
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isButtonClicked) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(CroppedActivity.this);

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                if (cameraValueIntent != null) {
                                    Bitmap croppedBitmap = cropperView.getCroppedImage();

                                    // file name will be current time in millis which will always be unique
                                    String fileName = System.currentTimeMillis() + ".png";
                                    File savedFile = bitmapToFile(croppedBitmap, fileName);

                                    // get uri from file and add it to cropped uri list
                                    croppedUriList.add(Uri.fromFile(savedFile));
                                    selectedUriList.set(processedImage, Uri.fromFile(savedFile));

                                    Intent intent = new Intent(context, FilterActivityForTesting.class);
                                    Bundle bundle = new Bundle();
                                    // send croppedUriList to next activity
                                    bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
                                    intent.putExtras(bundle);

                                    // send croppedUriList to next activity
                                    startActivity(intent);
                                } else if (selectedUriList.size() == 1) {
                                    Bitmap croppedBitmap = cropperView.getCroppedImage();

                                    // file name will be current time in millis which will always be unique
                                    String fileName = System.currentTimeMillis() + ".png";
                                    File savedFile = bitmapToFile(croppedBitmap, fileName);
                                    // get uri from file and add it to cropped uri list
                                    selectedUriList.set(processedImage, Uri.fromFile(savedFile));
                                    Intent intent = new Intent(context, FilterActivityForTesting.class);
                                    Bundle bundle = new Bundle();
                                    // send croppedUriList to next activity
                                    bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
                                    intent.putExtras(bundle);
                                    // send croppedUriList to next activity
                                    startActivity(intent);
                                } else {
                                    extracted();
                                }
                                mInterstitialAd = null;
                                setAds();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                isChecked = true;
                                AppPrefrences.setButtonCLicked(getApplication(), true);
                                if (cameraValueIntent != null) {
                                    Bitmap croppedBitmap = cropperView.getCroppedImage();

                                    // file name will be current time in millis which will always be unique
                                    String fileName = System.currentTimeMillis() + ".png";
                                    File savedFile = bitmapToFile(croppedBitmap, fileName);

                                    // get uri from file and add it to cropped uri list
                                    croppedUriList.add(Uri.fromFile(savedFile));
                                    selectedUriList.set(processedImage, Uri.fromFile(savedFile));

                                    Intent intent = new Intent(context, FilterActivityForTesting.class);
                                    Bundle bundle = new Bundle();
                                    // send croppedUriList to next activity
                                    bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
                                    intent.putExtras(bundle);

                                    // send croppedUriList to next activity
                                    startActivity(intent);
                                } else if (selectedUriList.size() == 1) {
                                    Bitmap croppedBitmap = cropperView.getCroppedImage();

                                    // file name will be current time in millis which will always be unique
                                    String fileName = System.currentTimeMillis() + ".png";
                                    File savedFile = bitmapToFile(croppedBitmap, fileName);
                                    // get uri from file and add it to cropped uri list
                                    selectedUriList.set(processedImage, Uri.fromFile(savedFile));
                                    Intent intent = new Intent(context, FilterActivityForTesting.class);
                                    Bundle bundle = new Bundle();
                                    // send croppedUriList to next activity
                                    bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
                                    intent.putExtras(bundle);
                                    // send croppedUriList to next activity
                                    startActivity(intent);

                                } else {
                                    extracted();
                                }
                                mInterstitialAd = null;
                            }
                        });
                    }
                } else {
                    if (cameraValueIntent != null) {
                        Bitmap croppedBitmap = cropperView.getCroppedImage();

                        // file name will be current time in millis which will always be unique
                        String fileName = System.currentTimeMillis() + ".png";
                        File savedFile = bitmapToFile(croppedBitmap, fileName);

                        // get uri from file and add it to cropped uri list
                        croppedUriList.add(Uri.fromFile(savedFile));
                        selectedUriList.set(processedImage, Uri.fromFile(savedFile));

                        Intent intent = new Intent(context, FilterActivityForTesting.class);
                        Bundle bundle = new Bundle();
                        // send croppedUriList to next activity
                        bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
                        intent.putExtras(bundle);

                        // send croppedUriList to next activity
                        startActivity(intent);
                    } else if (selectedUriList.size() == 1) {
                        Bitmap croppedBitmap = cropperView.getCroppedImage();

                        // file name will be current time in millis which will always be unique
                        String fileName = System.currentTimeMillis() + ".png";
                        File savedFile = bitmapToFile(croppedBitmap, fileName);
                        // get uri from file and add it to cropped uri list
                        selectedUriList.set(processedImage, Uri.fromFile(savedFile));
                        Intent intent = new Intent(context, FilterActivityForTesting.class);
                        Bundle bundle = new Bundle();
                        // send croppedUriList to next activity
                        bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
                        intent.putExtras(bundle);
                        // send croppedUriList to next activity
                        startActivity(intent);

                    } else {
                        extracted();
                    }
                }


            }
        });

//        imageDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    selectedUriList.remove(processedImage);
//                } catch (Exception e) {
//
//                }
//
//            }
//        });
        nextButton.setOnClickListener(view -> {
            imageCount.getAndIncrement();
            Log.i("clicked", "processimage we got is first : " + imageCount);
            Log.i("clicked", "list we got is first : " + selectedUriList.size());
            currentStateCount.setText(imageCount + "/" + selectedUriList.size());
            imageReverse.setVisibility(View.VISIBLE);
            Log.i("clicked", "processimage we got is after : " + imageCount);
            Log.i("clicked", "list we got is after : " + selectedUriList.size());
            Bitmap croppedBitmap = cropperView.getCroppedImage();
            int height = croppedBitmap.getHeight();
            int width = croppedBitmap.getWidth();
            Log.i("cropped", "cropped bitmap of height we got is:" + height);
            Log.i("cropped", "cropped bitmap of weidth we got is:" + width);

            Log.i("cameraclick", "click we got is else:" + croppedBitmap);
            // convert it to file
            // file name will be current time in millis which will always be unique
            String fileName = System.currentTimeMillis() + ".png";
            File savedFile = bitmapToFile(croppedBitmap, fileName);
            // get uri from file and add it to cropped uri list
            if (croppedUriList.size() != selectedUriList.size() - 1) {
                croppedUriList.add(Uri.fromFile(savedFile));


            } else {
                croppedUriList.set(processedImage, Uri.fromFile(savedFile));

            }
            Log.i("size", "selected uri list" + selectedUriList.size());
            Log.i("size", "cropped of size is :" + croppedUriList.size());

            if (processedImage < cropRects.size())
                cropRects.set(processedImage, cropperView.getCropRect());
            else {
                cropRects.add(processedImage, cropperView.getCropRect());
            }
            processedImage++;
            Rect rect;
            if (processedImage < cropRects.size())
                rect = cropRects.get(processedImage);
            else {
                cropperView.resetCropRect();
                rect = cropperView.getCropRect();
                cropRects.add(rect);
            }
            cropperView.setCropRect(rect);

            Log.i("clicked", "processimage we got is out : " + imageCount);
            Log.i("clicked", "list we got is out : " + selectedUriList.size());
            if (imageCount.get() >= (selectedUriList.size())) {
                Log.i("clicked", "processimage we got is : " + processedImage);
                Log.i("clicked", "list we got is : " + selectedUriList.size());

                nextButton.setVisibility(View.GONE);
                done.setVisibility(View.VISIBLE);
                textView7.setVisibility(View.VISIBLE);
                cropperView.setImageUriAsync(selectedUriList.get(processedImage));
                cropperView.setCropRect(cropRects.get(processedImage));
            } else {
                cropperView.setImageUriAsync(selectedUriList.get(processedImage));
                cropperView.setCropRect(cropRects.get(processedImage));

            }
        });
        imageReset.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View view) {
                if (a) {
                    cropperView.setImageUriAsync(selectedUriList.get(processedImage));
                } else {
                    cropperView.setCropRect(cropRects.get(processedImage));
                }
            }
        });

        imageReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listRotation = true;
                cropRects.set(processedImage, cropperView.getCropRect());
                nextButton.setVisibility(View.VISIBLE);
                cropperView.setImageUriAsync(selectedUriList.get(processedImage - 1));
                cropperView.setCropRect(cropRects.get(processedImage - 1));
                currentStateCount.setText(processedImage + "/" + selectedUriList.size());
                processedImage--;
                imageCount.getAndDecrement();
                if (processedImage - 1 == selectedUriList.indexOf(0)) {
                    imageReverse.setVisibility(View.GONE);
                }
            }
        });
    }

    private void extracted() {
        if (processedImage == selectedUriList.size() - 1) {
            cropRects.set(processedImage, cropperView.getCropRect());

            Bitmap croppedBitmap = cropperView.getCroppedImage();
            String fileName = System.currentTimeMillis() + ".png";
            File savedFile = bitmapToFile(croppedBitmap, fileName);
            croppedUriList.add(processedImage, Uri.fromFile(savedFile));


            if (processedImage <= selectedUriList.size()) {
                done.setVisibility(View.VISIBLE);
                textView7.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.GONE);


                Intent intent = new Intent(context, FilterActivityForTesting.class);
                Bundle bundle = new Bundle();
                // send croppedUriList to next activity
                bundle.putParcelableArrayList("mycroppedlist", croppedUriList);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
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

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(CroppedActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public class startAsyncTask {
    }

    private class ExampleAsyncTask extends AsyncTask<Bitmap, File, ArrayList<Uri>> {
        @Override
        protected ArrayList<Uri> doInBackground(Bitmap... bitmaps) {
            return selectedUriList;
        }
    }
}