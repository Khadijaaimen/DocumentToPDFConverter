package com.example.multipleimageconverter;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;

import java.util.List;

public class DisplayActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {

    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;
    String filePathPdf;
    String filePathJpg;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Bundle Extras = getIntent().getExtras();
        if (Extras != null) {
            filePathPdf = Extras.getString("file_path");
            filePathJpg = Extras.getString("file_path");
            pdfFileName = Extras.getString("file_name");

        }
        Log.i(TAG, "onCreate: Extra we got is : " + filePathJpg);

        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        displayfromUri(Uri.parse(filePathPdf));
        textView = findViewById(R.id.tv_header);
        textView.setText(pdfFileName);
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
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }


    @Override
    public void loadComplete(int nbPages) {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}