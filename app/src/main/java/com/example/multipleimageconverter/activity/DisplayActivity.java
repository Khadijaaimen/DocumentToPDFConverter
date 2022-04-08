package com.example.multipleimageconverter.activity;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.multipleimageconverter.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;

import java.util.List;

public class DisplayActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {
    public static final String SAMPLE_FILE = "file_path";

    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;
    ImageView imgView;
    String filePathPdf;
    String filePathJpg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Bundle Extras = getIntent().getExtras();
        if (Extras != null) {
            filePathPdf = Extras.getString("file_path");
            filePathJpg = Extras.getString("file_path");
        }


        Log.i(TAG, "onCreate: Extra we got is : " + filePathJpg);

        pdfView= (PDFView)findViewById(R.id.pdfView);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        displayfromUri(Uri.parse(filePathPdf));
//        displayfromUri(filePathJpg);
    }

    private void displayfromUri(Uri filePathJpg) {

        pdfView.fromUri(filePathJpg)
                .defaultPage(pageNumber).enableSwipe(true)
                .swipeHorizontal(false)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();

    /*    pdfView.fromAsset(SAMPLE_FILE).defaultPage(pageNumber).enableSwipe(true)
                .swipeHorizontal(false)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();*/
    }


    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }


    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

}


//        imgView = findViewById(R.id.imgView);

//        Bundle Extras = getIntent().getExtras();
//        if (Extras != null) {
//            filePathPdf = Extras.getString("file_path");
//            filePathJpg = Extras.getString("file_path");
//        }
//
//        if (filePathPdf != null) {
//            File path = new File(filePathPdf);
//            String u = "file:///" + path.getAbsolutePath();
//            pdfView.fromUri(Uri.parse(u)).load();
//        }
//
//        if (filePathJpg != null) {
////            File path2 = new File(filePathJpg);
//           pdfView.fromUri(Uri.parse(filePathJpg)).defaultPage(pageNumber).
//                    enableSwipe(true).swipeHorizontal(false).
//                    onPageChange((OnPageChangeListener) this).enableAnnotationRendering(true).
//                    onLoad((OnLoadCompleteListener) this).scrollHandle(new DefaultScrollHandle(this.getApplicationContext()))
//                    .load();
//
////            Glide.with(DisplayActivity.this)
////                    .load(filePathJpg) // Uri of the picture
////                    .into();
//        }
//
//        if (filePathJpg == null && filePathPdf == null) {
//            Toast.makeText(getApplicationContext(), "File can not be loaded", Toast.LENGTH_SHORT).show();
//        }
//    }
//}