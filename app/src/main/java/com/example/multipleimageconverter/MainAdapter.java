package com.example.multipleimageconverter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.multipleimageconverter.util.Utils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.internal.Util;

public class MainAdapter extends RecyclerView.Adapter<MainViewHolder> {
    private Context context;
    List<File> pdfArrayList = new ArrayList<>();

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private SparseBooleanArray selected_items;
    private int current_selected_idx = -1;
    private MainAdapter.OnItemClickListener mOnItemClickListener;
    List<File> filteredUserDataList;
    int pageCount = 0;

    public MainAdapter(Context context, List<File> pdfArrayList, OnItemClickListener mOnItemClickListener) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.mOnItemClickListener = mOnItemClickListener;
        this.filteredUserDataList = pdfArrayList;
        selected_items = new SparseBooleanArray();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, File value, int position);

//        void onItemLongClick(View view, File obj, int pos);
    }

    public void setOnItemClickListener(MainAdapter.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }


    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
            return new MainViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof MainViewHolder) {

            try {
                populateItemRows((MainViewHolder) holder, position);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (holder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) holder, position);
        }

    }


    @Override
    public int getItemCount() {
        return filteredUserDataList.size();
    }

    /**
     * The following method decides the type of ViewHolder to display in the RecyclerView
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return filteredUserDataList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }


    public String GetSize(long size) {
        String[] dictionary = {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int index = 0;
        double m = size;
        DecimalFormat dec = new DecimalFormat("0.00");
        for (index = 0; index < dictionary.length; index++) {
            if (m < 1024) {
                break;
            }
            m = m / 1024;
        }
        return dec.format(m).concat(" " + dictionary[index]);

    }

    public void toggleSelection(int pos) {
        current_selected_idx = pos;
        if (selected_items.get(pos, false)) {
            selected_items.delete(pos);
        } else {
            selected_items.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public int getSelectedItemCount() {
        return selected_items.size();
    }

    public void selectAll() {
        for (int i = 0; i < pdfArrayList.size(); i++) {
            selected_items.put(i, true);
            notifyItemChanged(i);
        }
    }

    public void clearSelections() {
        selected_items.clear();
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> pdfFiles = new ArrayList<>(selected_items.size());
        for (int i = 0; i < selected_items.size(); i++) {
            pdfFiles.add(selected_items.keyAt(i));
        }
        return pdfFiles;
    }

    public void removeData(int position) {
        pdfArrayList.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        current_selected_idx = -1;
    }


    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String Key = charSequence.toString();

                if (Key.isEmpty()) {
                    filteredUserDataList = pdfArrayList;
                } else {
                    List<File> lstFiltered = new ArrayList<>();
                    for (File row : pdfArrayList) {
                        if ((row.getName().toLowerCase().contains(Key.toLowerCase()))) {
                            lstFiltered.add(row);
                            Log.i("rowData", "performFiltering: row data  " + row.getName().toLowerCase(Locale.ROOT));
                            Log.i("rowData", "performFiltering: key data  " + Key.toLowerCase());
                            Log.i("rowData", "performFiltering: List data  " + lstFiltered.size());

                        }
                    }
                    filteredUserDataList = lstFiltered;
                }


                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredUserDataList;


                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredUserDataList = ((List<File>) filterResults.values);
                notifyDataSetChanged();
            }

        };


    }

    private class LoadingViewHolder extends MainViewHolder {
        ProgressBar progressBar;

        public LoadingViewHolder(View view) {


            super(view);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private Bitmap pdfToBitmap(File pdfFile) throws IOException {
        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer renderer = new PdfRenderer(fileDescriptor);
        pageCount = renderer.getPageCount();
        if(pageCount>0) {
            PdfRenderer.Page page = renderer.openPage(0);
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();

            return bitmap;
        }
         return null;
    }

    private void populateItemRows(MainViewHolder viewHolder, int position) throws IOException {

        File file = filteredUserDataList.get(position);

        viewHolder.txtname.setText(filteredUserDataList.get(position).getName());
        viewHolder.txtname.setSelected(true);

        Date lastModDate = new Date(file.lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        String strDate = formatter.format(lastModDate);
        viewHolder.brief.setText(strDate);

        if (pdfArrayList.get(position).getName().contains(".pdf")) {
            Bitmap uri = pdfToBitmap(pdfArrayList.get(position));
            assert uri != null;
            Drawable d = new BitmapDrawable(context.getResources(), uri);
            viewHolder.pageCount.setText(String.valueOf(pageCount));
            viewHolder.pdf_imageView.setImageDrawable(d);
//            Picasso.get().load(String.valueOf(d))
//                    .placeholder(R.drawable.pdffinal)
//                    .resize(40, 50)
//                    .centerCrop()
//                    .into(viewHolder.pdf_imageView);
        } else {
            Picasso.get().load(pdfArrayList.get(position))
                    .placeholder(R.drawable.pdffinal)
                    .into(viewHolder.pdf_imageView);
            viewHolder.pageCount.setVisibility(View.GONE);
            viewHolder.docImage.setVisibility(View.GONE);
        }

        viewHolder.lyt_parentdocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mOnItemClickListener == null) return;
                mOnItemClickListener.onItemClick(view, file, position);


            }
        });

        viewHolder.lyt_parentdocument.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (mOnItemClickListener == null) return false;
//                mOnItemClickListener.onItemLongClick(v, file, position);
                return true;
            }
        });


        toggleCheckedIcon(viewHolder, position);
    }

    private void toggleCheckedIcon(MainViewHolder holder, int position) {

        MainViewHolder viewHolder = holder;
        if (selected_items.get(position, false)) {
            holder.lyt_parentdocument.setBackgroundColor(Color.parseColor("#000000"));
            if (current_selected_idx == position) resetCurrentIndex();
        } else {
            holder.lyt_parentdocument.setBackgroundColor(Color.parseColor("#ffffff"));
            if (current_selected_idx == position) resetCurrentIndex();
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed


    }
}




