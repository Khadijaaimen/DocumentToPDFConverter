package com.example.multipleimageconverter.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewEmptySupport extends RecyclerView {

    private View emptyView;
    public List<File> items = new ArrayList<>();
    List<File> filteredUserDataList;
    private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            showEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            showEmptyView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            showEmptyView();
        }
    };

    public RecyclerViewEmptySupport(Context context) {
        super(context);
    }


    public RecyclerViewEmptySupport(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void showEmptyView() {

        Adapter<?> adapter = getAdapter();
        if (adapter != null && emptyView != null) {
            if (adapter.getItemCount() == 0) {
                emptyView.setVisibility(VISIBLE);
                RecyclerViewEmptySupport.this.setVisibility(GONE);
            } else {
                emptyView.setVisibility(GONE);
                RecyclerViewEmptySupport.this.setVisibility(VISIBLE);
            }
        }

    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
            observer.onChanged();
        }
    }

    public void setEmptyView(View v) {
        emptyView = v;
    }
//
//    public Filter getFilter(){
//
//        return new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence charSequence) {
//
//                String Key = charSequence.toString();
//                if(Key.isEmpty()){
//                    filteredUserDataList = items;
//                }
//                else {
//
//                    List<File> lstFiltered = new ArrayList<>();
//                    for(File row: items){
//                        if(row.getParentFile().toURI().isAbsolute()){
//                            lstFiltered.add(row);
//
//                        }
//
//
//                    }
//
//                    filteredUserDataList = lstFiltered;
//
//
//                }
//
//
//
//                FilterResults filterResults = new FilterResults();
//                filterResults.values = filteredUserDataList;
//                return filterResults;
//            }
//
//            @Override
//            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//
//                filteredUserDataList = (List<File>)filterResults.values;
//                notifyDataSetChanged();
//
//            }
//
//            private void notifyDataSetChanged() {
//            }
//
//            protected void showresult(String desp, FilterResults filterResults) {
//
//                filteredUserDataList = (List<File>)filterResults.values;
//                notifyDataSetChanged();
//
//            }
//        };
//
//    }
}


