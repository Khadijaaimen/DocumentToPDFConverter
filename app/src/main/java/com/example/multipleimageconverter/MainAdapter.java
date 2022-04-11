package com.example.multipleimageconverter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.paginate.Paginate;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainAdapter extends RecyclerView.Adapter<MainViewHolder> {
    Context context;
    List<File> pdfArrayList = new ArrayList<>();
    final int VIEW_TYPE_ITEM = 0;
    final int VIEW_TYPE_LOADING = 1;

    SparseBooleanArray selected_items;
    int current_selected_idx = -1;
    MainAdapter.OnItemClickListener mOnItemClickListener;
    List<File> filteredUserDataList;

    public MainAdapter(Context context, List<File> pdfArrayList, OnItemClickListener mOnItemClickListener) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.mOnItemClickListener = mOnItemClickListener;
        this.filteredUserDataList = pdfArrayList;
        selected_items = new SparseBooleanArray();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, File value, int position);

        void onItemLongClick(View view, File obj, int pos);
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

            populateItemRows((MainViewHolder) holder, position);
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

    private void populateItemRows(MainViewHolder viewHolder, int position) {

        File file = filteredUserDataList.get(position);

        viewHolder.txtname.setText(filteredUserDataList.get(position).getName());
        viewHolder.txtname.setSelected(true);

        Date lastModDate = new Date(file.lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        String strDate = formatter.format(lastModDate);
        viewHolder.brief.setText(strDate);

        Glide.with(context)
                .load(pdfArrayList.get(position))
                .placeholder(R.drawable.document)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(viewHolder.pdf_imageView);

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
                mOnItemClickListener.onItemLongClick(v, file, position);
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

    }
}




