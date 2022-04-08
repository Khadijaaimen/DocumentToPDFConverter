package com.example.multipleimageconverter.misc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.multipleimageconverter.R;
import com.paginate.abslistview.LoadingListItemCreator;

public class CustomLoadingListItemCreator implements LoadingListItemCreator {
    @Override
    public View newView(int position, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_file, parent, false);
//        view.setTag(new VH(view));
        return view;
    }

    @Override
    public void bindView(int position, View view) {
        // Bind custom loading row if needed
    }
}
