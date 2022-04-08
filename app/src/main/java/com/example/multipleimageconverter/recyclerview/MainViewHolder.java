package com.example.multipleimageconverter.recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multipleimageconverter.R;

public class MainViewHolder extends RecyclerView.ViewHolder {
    public TextView txtname;
    public CardView cardView;
    public ImageView pdf_imageView;
    public LinearLayout lyt_parentdocument;
    public TextView brief;


    public MainViewHolder(@NonNull View itemView) {
        super(itemView);

        txtname = itemView.findViewById(R.id.pdf_txtNAme);
        cardView = itemView.findViewById(R.id.pdf_cardView);
        pdf_imageView = (itemView).findViewById(R.id.pdf_imageView);
        brief = itemView.findViewById(R.id.dateItemTimeTextView);
        lyt_parentdocument = (itemView).findViewById(R.id.listItemLinearLayoutdocument);

    }
}

