package com.example.multipleimageconverter;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageToPDFAsync extends AsyncTask<Void, Integer, Boolean> {

    private OnPostExecuteListener mListener = null;
    @SuppressLint("StaticFieldLeak")
    FilterActivityForTesting filteractivity;
    private String mFileName = null;
    private String password;
    private String pageSize;
    private String pageMargin;
    private String pageOrientation;
    private String compression;
    List<ImageDocument> items;

    public ImageToPDFAsync(ImageToPDF mainActivity, ArrayList<ImageDocument> documents, String fileName, Object onPostExecuteListener) {
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public void setPageMargin(String pageMargin) {
        this.pageMargin = pageMargin;
    }

    public void setPageOrientation(String pageOrientation) {
        this.pageOrientation = pageOrientation;
    }

    public interface OnPostExecuteListener {
        void onPostExecute(ImageToPDFAsync imageToPDFAsync, Boolean bool);
    }

    public ImageToPDFAsync(FilterActivityForTesting activity, ArrayList<ImageDocument> items, String fileName, OnPostExecuteListener onPostExecuteListener) {
        this.items = items;
        this.mListener = onPostExecuteListener;
      this.filteractivity=activity;
        this.mFileName = fileName;
    }
    public void ImageToPDFAsyncfilter(com.example.multipleimageconverter.FilterActivityForTesting activity, List<ThumbnailItem> documents, String fileName, OnPostExecuteListener onPostExecuteListener) {
        this.mListener = onPostExecuteListener;
        this.filteractivity = activity;
        this.mFileName = fileName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        filteractivity.showBottomSheet(items.size());
    }


    @Override
    protected Boolean doInBackground(Void... voids) {
        ContextWrapper cw = new ContextWrapper(filteractivity.getApplication());
        File myDir = new File(cw.getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/MultiImageConverter");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        File file = new File(myDir.getAbsolutePath(),mFileName+".pdf");
        if (file.exists())
            file.delete();
        try {
            Document destination = new Document();
            setPageSize(destination);
            setPageMargin(destination);
            PdfWriter writer = PdfWriter.getInstance((com.itextpdf.text.Document) destination, new FileOutputStream(file));
            if (password != null && password.length() > 0) {
                byte[] bytes = password.getBytes("UTF-8");
                writer.setEncryption(bytes, null, PdfWriter.ALLOW_PRINTING, PdfWriter.STANDARD_ENCRYPTION_128);
            }
            ((com.itextpdf.text.Document) destination).open();
            for (int i = 0; i < items.size(); i++) {
                ImageDocument document = items.get(i);
                Bitmap bitmap = null;
                InputStream stream = filteractivity.getContentResolver().openInputStream(document.getImageDocument());
                Image image = null;
                if (isJpeg(stream)) {
                    if (this.compression == "Medium") {
                        bitmap = Glide.with(filteractivity).asBitmap().load(document.getImageDocument()).override(1000, 1000)
                                .submit().get();
                    } else if (this.compression == "High") {
                        bitmap = Glide.with(filteractivity).asBitmap().load(document.getImageDocument()).override(500, 500)
                                .submit().get();
                    }
                    if (bitmap != null) {
                        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageStream);
                        image = Image.getInstance(imageStream.toByteArray());
                        image.setAlignment(Image.MIDDLE);
                        imageStream.close();
                    } else {
                        stream = filteractivity.getContentResolver().openInputStream(document.getImageDocument());
                        image = Image.getInstance(toByteArray(stream));
                        image.setAlignment(Image.MIDDLE);
                    }

                } else {
                    stream = filteractivity.getContentResolver().openInputStream(document.getImageDocument());
                    image = Image.getInstance(toByteArray(stream));
                    image.setAlignment(Image.MIDDLE);
                }
                if (image != null) {
                    if (this.pageSize == "Fit (Same page size as image)") {
                        ((com.itextpdf.text.Document) destination).setPageSize(new Rectangle(0, 0, image.getPlainWidth(), image.getPlainHeight()));
                        ((com.itextpdf.text.Document) destination).setMargins(0, 0, 0, 0);
                        ((com.itextpdf.text.Document) destination).newPage();
                    } else {
                        image.scaleToFit(new Rectangle(((com.itextpdf.text.Document) destination).topMargin(), ((com.itextpdf.text.Document) destination).leftMargin(), ((com.itextpdf.text.Document) destination).getPageSize().getWidth() - ((com.itextpdf.text.Document) destination).topMargin(), ((com.itextpdf.text.Document) destination).getPageSize().getHeight() - ((com.itextpdf.text.Document) destination).topMargin()));
                    }
                    ((com.itextpdf.text.Document) destination).add(image);
                }
                publishProgress(i + 1);
            }
            ((com.itextpdf.text.Document) destination).close();

        } catch (Exception ex) {

        }
        return true;
    }



    public void onPostExecute(Boolean bool) {
        filteractivity.runPostExecution(bool);
    }

    public void onCancelled(Boolean bool) {
        if (this.mListener != null) {
            this.mListener.onPostExecute(this, Boolean.valueOf(false));
        }
    }

    protected void onProgressUpdate(Integer... values) {
        filteractivity.setProgress(values[0], items.size());
    }

    public static boolean isJpeg(InputStream in) throws IOException {
        int c1 = in.read();
        int c2 = in.read();
        if (c1 == 0xFF && c2 == 0xD8) {
            return true;
        }
        in.close();
        return false;
    }

    public static byte[] toByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }

    private void setPageSize(Document document) {
        switch (this.pageSize) {
            case "Fit (Same page size as image)":
                break;
            case "A4 (297x210 mm)":
                if (pageOrientation == "Landscape") {
                    document.setPageSize(PageSize.A4.rotate());
                } else {
                    document.setPageSize(PageSize.A4);
                }
                break;
            case "US Letter (215x279.4 mm)":
                if (pageOrientation == "Landscape") {
                    document.setPageSize(PageSize.LETTER.rotate());
                } else {
                    document.setPageSize(PageSize.LETTER);
                }
                break;
        }
    }

    private void setPageMargin(Document document) {
        switch (this.pageMargin) {
            case "No margin":
                document.setMargins(0, 0, 0, 0);
                break;
            case "Small":
                document.setMargins(20, 20, 20, 20);
                break;
            case "Big":
                document.setMargins(40, 40, 40, 40);
                break;
        }
    }
}

