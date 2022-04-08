package com.example.multipleimageconverter.activity;//package com.example.multipleimageconverter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.multipleimageconverter.R;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class CroppedActivity extends AppCompatActivity {
    private CropImageView cropperView;
    private Button rotateLeft;
    private Button done;
    private int processedImage = 0;
    private final Context context = this;
    CircleImageView nextButton, imageReverse;
    TextView currentStateCount;
    private ArrayList<Uri> selectedUriList = new ArrayList<>();
    private ArrayList<Uri> croppedUriList = new ArrayList<>();
    String cameraValueIntent;
    ArrayList<Rect> cropRects = new ArrayList<>();
    ArrayList<Uri> combineList;
    TextView textView7;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropped_test);

        done = findViewById(R.id.done);
        textView7=findViewById(R.id.textView7);
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
        currentStateCount = findViewById(R.id.currentStateCount);
        Log.i("procee", "process image we got is :" + processedImage);
        Button imageReset = findViewById(R.id.imageReset);
        Button imageDelete = findViewById(R.id.imageDelete);
        Button imageRotate = findViewById(R.id.imageRotate);
        imageRotate.setOnClickListener(view -> {
            cropperView.rotateImage(90);
            cropperView.setRotationX(0);
            cropperView.setRotationY(0);
        });
        imageReverse = findViewById(R.id.imageReverse);
        imageReverse.setVisibility(View.GONE);
        cropperView = findViewById(R.id.cropperView);
        nextButton = findViewById(R.id.imageview);
        Bundle extras = getIntent().getExtras();
        String message = extras.getString("message");
        cameraValueIntent = extras.getString("cameraValue");

        if (cameraValueIntent!=null){
            currentStateCount.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            imageReverse.setVisibility(View.GONE);
        }
        Log.i("camera", "string we got is :" + cameraValueIntent);

//        Log.i("size","cropped list we got is :" + croppedUriList.size());

        if (message != null) {
            cropperView.setImageUriAsync(Uri.parse(message));
        }

        croppedUriList=selectedUriList;
        if (extras.getParcelableArrayList("mylist") != null) {
            selectedUriList = extras.getParcelableArrayList("mylist");
            Log.i("size","selcted list we got is :" + selectedUriList.size());

//            croppedUriList = new ArrayList<>(selectedUriList.size());

            // first set image at 0 index to cropperView
            cropperView.setImageUriAsync(selectedUriList.get(processedImage));

        }
        if (croppedUriList.size()!=selectedUriList.size()){
//            done.setVisibility(View.GONE);
//            textView7.setVisibility(View.GONE);
        }

        if (selectedUriList.size() == 1) {
            nextButton.setVisibility(View.GONE);
            imageReverse.setVisibility(View.GONE);
            currentStateCount.setVisibility(View.GONE);
            done.setVisibility(View.VISIBLE);
            textView7.setVisibility(View.VISIBLE);
        }

        currentStateCount.setText(imageCount + "/" + selectedUriList.size());
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cameraValueIntent != null){
                    Bitmap croppedBitmap = cropperView.getCroppedImage();

                    // file name will be current time in millis which will always be unique
                    String fileName = System.currentTimeMillis() + ".png";
                    File savedFile = bitmapToFile(croppedBitmap, fileName);
                    // get uri from file and add it to cropped uri list
                  croppedUriList.add(Uri.fromFile(savedFile));
                    selectedUriList.set(processedImage, Uri.fromFile(savedFile));
//                    Toast.makeText(this, "CurrentPos : " + selectedUriList.get(processedImage), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, FilterActivityForTesting.class);
                    Bundle bundle = new Bundle();
                    // send croppedUriList to next activity
                    bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
                    intent.putExtras(bundle);
                    // send croppedUriList to next activity
                    startActivity(intent);

//                    extracted();
                }else if (selectedUriList.size()==1){
                    Bitmap croppedBitmap = cropperView.getCroppedImage();

                    // file name will be current time in millis which will always be unique
                    String fileName = System.currentTimeMillis() + ".png";
                    File savedFile = bitmapToFile(croppedBitmap, fileName);
                    // get uri from file and add it to cropped uri list
//                    croppedUriList.add(Uri.fromFile(savedFile));
                    selectedUriList.set(processedImage, Uri.fromFile(savedFile));
//                    Toast.makeText(this, "CurrentPos : " + selectedUriList.get(processedImage), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, FilterActivityForTesting.class);
                    Bundle bundle = new Bundle();
                    // send croppedUriList to next activity
                    bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
                    intent.putExtras(bundle);
                    // send croppedUriList to next activity
                    startActivity(intent);

//                    extracted();
                }

                else{
                    extracted();
                }
//                if (selectedUriList.size()==1){
//                    Bitmap croppedBitmap = cropperView.getCroppedImage();
//                    // file name will be current time in millis which will always be unique
//                    String fileName = System.currentTimeMillis() + ".png";
//                    File savedFile = bitmapToFile(croppedBitmap, fileName);
//                    // get uri from file and add it to cropped uri list
//                    croppedUriList.add(Uri.fromFile(savedFile));
//                    selectedUriList.set(processedImage, Uri.fromFile(savedFile));
////                    Toast.makeText(this, "CurrentPos : " + selectedUriList.get(processedImage), Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(context, FilterActivityForTesting.class);
//                    Bundle bundle = new Bundle();
//                    // send croppedUriList to next activity
//                    bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
//                    intent.putExtras(bundle);
//                    // send croppedUriList to next activity
//                    startActivity(intent);
////                    extracted();
//                }
//                cropRects.set(processedImage, cropperView.getCropRect());
//

            }
        });
//        imageDelete.setOnClickListener(view -> selectedUriList.remove(processedImage));
        imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    selectedUriList.remove(processedImage);
                }catch (Exception e){

                }

            }
        });
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


            }

//            croppedUriList.set(processedImage,Uri.fromFile(savedFile));
          else {
//                Toast.makeText(getApplicationContext(), "else if  is called ", Toast.LENGTH_SHORT).show();
                croppedUriList.set(processedImage, Uri.fromFile(savedFile));

            }
            Log.i("size", "selected uri list" + selectedUriList.size());
            Log.i("size", "cropped of size is :" + croppedUriList.size());
//                croppedUriList.set(processedImage, selectedUriList.get(processedImage));
//          croppedUriList.set(processedImage, selectedUriList.get(processedImage));
            // TODO updated
            if (processedImage < cropRects.size())
                cropRects.set(processedImage, cropperView.getCropRect());
            else {
                cropRects.add(processedImage, cropperView.getCropRect());
//                cropperView.setCropRect(cropRects.get(processedImage));
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
//                cropRects.set(processedImage, cropperView.getCropRect());
                cropperView.setCropRect(cropRects.get(processedImage));
            } else {
                // show next image
                cropperView.setImageUriAsync(selectedUriList.get(processedImage));
                cropperView.setCropRect(cropRects.get(processedImage));

            }
            // If that was last image then open next activity else increment and show next image

            /*            if (processedImage>= selectedUriList.size()) {

             //*extracted();//*

             *//*Intent intent = new Intent(context, FilterActivityForTesting.class);
                Bundle bundle = new Bundle();

                // send croppedUriList to next activity
                bundle.putParcelableArrayList("mycroppedlist", croppedUriList);
                intent.putExtras(bundle);
                startActivity(intent);//
            }*/
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

                // TODO updated
                cropRects.set(processedImage, cropperView.getCropRect());
//                Toast.makeText(getApplicationContext(), "list rotation we got is :" + listRotation, Toast.LENGTH_SHORT).show();
                nextButton.setVisibility(View.VISIBLE);
               /* done.setVisibility(View.GONE);
                textView7.setVisibility(View.GONE);*/
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
        if (processedImage==selectedUriList.size()-1) {
            cropRects.set(processedImage, cropperView.getCropRect());

            Bitmap croppedBitmap = cropperView.getCroppedImage();
            int height = croppedBitmap.getHeight();
            int width = croppedBitmap.getWidth();
            String fileName = System.currentTimeMillis() + ".png";
            File savedFile = bitmapToFile(croppedBitmap, fileName);
//        cropperView.setCropRect(cropRects.get(processedImage));
            croppedUriList.add(processedImage, Uri.fromFile(savedFile));


            if (processedImage <= selectedUriList.size()) {
                done.setVisibility(View.VISIBLE);
                textView7.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.GONE);


                Intent intent = new Intent(context, FilterActivityForTesting.class);
                Bundle bundle = new Bundle();
                // send croppedUriList to next activity
                bundle.putParcelableArrayList("mycroppedlist", croppedUriList);
//            bundle.putParcelableArrayList("mySelectedlist", selectedUriList);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
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

// TODO: old One
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.Rect;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.AppCompatTextView;
//
//import com.theartofdev.edmodo.cropper.CropImageView;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.util.ArrayList;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class CroppedActivity extends AppCompatActivity {
//    private CropImageView cropperView;
//    private Button imageDelete, imageRotate, imageReset, rotateleft, done;
//    private int processedImage = 0;
//    private final Context context = this;
//    CircleImageView nextButton, imageReverse;
//    AppCompatTextView currentStateCount;
//    private ArrayList<Uri> selectedUriList;
//    private ArrayList<Uri> croppedUriList = new ArrayList<>();
//    ArrayList<Rect> cropRect = new ArrayList<>();
//    ArrayList<Uri> combineList;
//    boolean a = true;
//    boolean listRotation = false;
//
//    @Nullable
//    public static File bitmapToFile(Bitmap bitmap, String fileNameToSave) {
//        Log.i("TAG", "File name :" + fileNameToSave);
//        try {
//            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//                    "MultiImageConverter");
//            if (!folder.exists() && folder.mkdirs())
//                Log.i("TAG", "Folder created");
//            else Log.i("TAG", "Folder exists");
//
//            File file = new File(folder, fileNameToSave);
//            if (file.createNewFile()) {
//                // Convert bitmap to byte array
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos); // YOU can also save it in JPEG
//                byte[] bytes = bos.toByteArray();
//
//                // write the bytes in file
//                FileOutputStream fos = new FileOutputStream(file);
//                fos.write(bytes);
//                fos.flush();
//                fos.close();
//                return file;
//            }
//            Log.e("TAG", "Failed to create file");
//            return null; // it will return null
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null; // it will return null
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_cropped_test);
//        done = findViewById(R.id.done);
//        rotateleft = findViewById(R.id.rotateleft);
//        rotateleft.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("ResourceAsColor")
//            @Override
//            public void onClick(View view) {
//                rotateleft.setBackgroundResource(R.drawable.rotate_left_colored);
//                cropperView.rotateImage(-90);
//                cropperView.setRotationX(0);
//                cropperView.setRotationY(0);
//            }
//        });
//        currentStateCount = findViewById(R.id.currentStateCount);
//        Log.i("procee", "process image we got is :" + processedImage);
//        imageReset = findViewById(R.id.imageReset);
//        imageDelete = findViewById(R.id.imageDelete);
//        imageRotate = findViewById(R.id.imageRotate);
//        imageRotate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                cropperView.rotateImage(90);
//                cropperView.setRotationX(0);
//                cropperView.setRotationY(0);
//            }
//        });
//        imageReverse = findViewById(R.id.imageReverse);
//        imageReverse.setVisibility(View.GONE);
//        cropperView = findViewById(R.id.cropperView);
//        nextButton = findViewById(R.id.imageview);
////
//        Bundle extras = getIntent().getExtras();
//        String message = extras.getString("message");
//        String cameraValueIntent = extras.getString("cameraValue");
//        Log.i("camera", "string we got is :" + cameraValueIntent);
//        if (message != null) {
//            cropperView.setImageUriAsync(Uri.parse(message));
//        }
//        imageDelete.setOnClickListener(view -> selectedUriList.remove(selectedUriList.get(processedImage)));
//      /*  if (myUri!=null) {
//            cropperView.setImageUriAsync(myUri);
//        }
//*/
//        if (extras.getParcelableArrayList("mylist") != null) {
//            selectedUriList = extras.getParcelableArrayList("mylist");
//            croppedUriList = new ArrayList<>(selectedUriList.size());
//            // first set image at 0 index to cropperView
//            cropperView.setImageUriAsync(selectedUriList.get(processedImage));
//        }
//        if (selectedUriList.size() == 1) {
//            nextButton.setVisibility(View.GONE);
//            imageReverse.setVisibility(View.GONE);
//            currentStateCount.setVisibility(View.GONE);
//        }
//        AtomicInteger imageCount = new AtomicInteger(1);
//        currentStateCount.setText(imageCount + "/" + selectedUriList.size());
//
//
//        nextButton.setOnClickListener(view -> {
//
//
//            if (listRotation)
//            cropRect.set(processedImage, cropperView.getCropRect());
//            else cropRect.add(processedImage, cropperView.getCropRect());
//
//            Log.i("clickedRect", "list we got is after cropRect : " + cropRect.size());
//            Log.i("clickedRect", "Processed Image : " + processedImage);
//
//            imageCount.getAndIncrement();
//            Log.i("clicked", "processimage we got is first : " + imageCount);
//            Log.i("clicked", "list we got is first : " + selectedUriList.size());
//            currentStateCount.setText(imageCount + "/" + selectedUriList.size());
//            imageReverse.setVisibility(View.VISIBLE);
//            Log.i("clicked", "processimage we got is after : " + imageCount);
//            Log.i("clicked", "list we got is after : " + selectedUriList.size());
//            Bitmap croppedBitmap = cropperView.getCroppedImage();
//            int height = croppedBitmap.getHeight();
//            int width = croppedBitmap.getWidth();
//            Log.i("cropped", "cropped bitmap of height we got is:" + height);
//            Log.i("cropped", "cropped bitmap of weidth we got is:" + width);
//            if (cameraValueIntent != null) {
//                Log.i("cameraclick", "click we got is if:" + croppedBitmap);
//                // convert it to file
//                // file name will be current time in millis which will always be unique
//                String fileName = System.currentTimeMillis() + ".png";
//                File savedFile = bitmapToFile(croppedBitmap, fileName);
//                // get uri from file and add it to cropped uri list
//                croppedUriList.add(Uri.fromFile(savedFile));
//                selectedUriList.set(processedImage, Uri.fromFile(savedFile));
//                Toast.makeText(this, "CurrentPos : " + selectedUriList.get(processedImage), Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(context, FilterActivityForTesting.class);
//                Bundle bundle = new Bundle();
//                // send croppedUriList to next activity
//                bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
//                intent.putExtras(bundle);
//                // send croppedUriList to next activity
//                startActivity(intent);
//            } else {
//                if (listRotation)
//                    cropperView.setCropRect(cropRect.get(processedImage));
//
//                croppedBitmap = cropperView.getCroppedImage();
//
//                Log.i("cameraclick", "click we got is else:" + croppedBitmap);
//                // convert it to file
//                // file name will be current time in millis which will always be unique
//                String fileName = System.currentTimeMillis() + ".png";
//                File savedFile = bitmapToFile(croppedBitmap, fileName);
//                // get uri from file and add it to cropped uri list
//                croppedUriList.add(Uri.fromFile(savedFile));
////                croppedUriList.set(processedImage, selectedUriList.get(processedImage));
//                selectedUriList.set(processedImage, selectedUriList.get(processedImage));
//                processedImage++;
//                // first get cropped bitmap
////                        cropperView.setCropRect(cropRect.get(processedImage++));
////                        processedImage++;
//
//            /*    HashMap<Uri, Uri> myMap = new HashMap<Uri, Uri>();
//                for(int i = 0; i < selectedUriList.size(); i++){
//                    myMap.put(selectedUriList.get(i), croppedUriList.get(i));
//                }
//                ArrayList<Uri> combineList = new ArrayList<Uri>();
//                for(int i = 0; i < selectedUriList.size(); i++){
//                    // if list2 contains string represented with numbers
//                    Uri uri = Uri.parse(selectedUriList.get(i) + "," + croppedUriList.get(i));
//                    // if list2 contains integers
////            String str = list1.get(i) + "," + String.valueOf(list2.get(i));
//                    combineList.add(uri);
//                }*/
//            }
//            Log.i("clicked", "processimage we got is out : " + imageCount);
//            Log.i("clicked", "list we got is out : " + selectedUriList.size());
//            if (imageCount.get() >= (selectedUriList.size())) {
//                Log.i("clicked", "processimage we got is : " + processedImage);
//                Log.i("clicked", "list we got is : " + selectedUriList.size());
//                nextButton.setVisibility(View.GONE);
//                cropperView.setImageUriAsync(selectedUriList.get(processedImage));
//            } else {
//                // show next image
//                cropperView.setImageUriAsync(selectedUriList.get(processedImage));
//            }
//            // If that was last image then open next activity else increment and show next image
//
//            /*            if (processedImage>= selectedUriList.size()) {
//             *//*extracted();*//*
//             *//*Intent intent = new Intent(context, FilterActivityForTesting.class);
//                Bundle bundle = new Bundle();
//                // send croppedUriList to next activity
//                bundle.putParcelableArrayList("mycroppedlist", croppedUriList);
//                intent.putExtras(bundle);
//                startActivity(intent);*//*
//            }*/
//        });
////        combineList.addAll(croppedUriList);
//        done.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                extracted();
//            }
//        });
//        imageReset.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.R)
//            @Override
//            public void onClick(View view) {
//                if (a) {
//                    cropperView.setImageUriAsync(selectedUriList.get(processedImage));
//                } else {
//                    cropperView.setCropRect(cropRect.get(processedImage));
//                }
////
//            }
//        });
//        imageReverse.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                listRotation = true;
//                Toast.makeText(getApplicationContext(), "list rotation we got is :" + listRotation, Toast.LENGTH_SHORT).show();
//                nextButton.setVisibility(View.VISIBLE);
//                cropperView.setImageUriAsync(selectedUriList.get(processedImage - 1));
///*                cropperView.setGuidelines(guidelines);
//                cropperView.setAspectRatio(aspectRatio.first,aspectRatio.second);*/
////                cropperView.setCropRect(cropRect);
//                cropperView.setCropRect(cropRect.get(processedImage - 1));
//                currentStateCount.setText(processedImage + "/" + selectedUriList.size());
//                processedImage--;
//                imageCount.getAndDecrement();
//                if (processedImage - 1 == selectedUriList.indexOf(0)) {
//                    imageReverse.setVisibility(View.GONE);
//                } /*else if (processedImage-1 != selectedUriList.indexOf(0)){
//                    imageReverse.setVisibility(View.VISIBLE);
//                }*/
//            }
//        });
//    }
//
//    private void extracted() {
//        if (processedImage < selectedUriList.size()) {
//
//            nextButton.setVisibility(View.GONE);
//            Intent intent = new Intent(context, FilterActivityForTesting.class);
//            Bundle bundle = new Bundle();
//            // send croppedUriList to next activity
//            bundle.putParcelableArrayList("mycroppedlist", croppedUriList);
////            bundle.putParcelableArrayList("mySelectedlist", selectedUriList);
//            intent.putExtras(bundle);
//            startActivity(intent);
//        }
////        } else if (processedImage >= selectedUriList.size()) {
////            Intent intent = new Intent(context, FilterActivityForTesting.class);
////            Bundle bundle = new Bundle();
////            // send croppedUriList to next activity
//////                    bundle.putParcelableArrayList("mycroppedlist", croppedUriList);
////            bundle.putParcelableArrayList("mycroppedlist", selectedUriList);
////            intent.putExtras(bundle);
////            startActivity(intent);
////            // show next image
//////            cropperView.setImageUriAsync(selectedUriList.get(processedImage));
////        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent(CroppedActivity.this, MainActivity.class);
//        startActivity(intent);
//    }
//
//    public class startAsyncTask {
//    }
//
//    private class ExampleAsyncTask extends AsyncTask<Bitmap, File, ArrayList<Uri>> {
//        @Override
//        protected ArrayList<Uri> doInBackground(Bitmap... bitmaps) {
//            return croppedUriList;
//        }
//    }
//
//
//}




