package com.example.multipleimageconverter.misc;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.module.AppGlideModule;

public class ThumbnailAppModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // register your Builder in Module
        // String.class is input and Bitmap.class is the output of ThumbnailBuilder
        registry.prepend(String.class, Bitmap.class, new ThumbnailBuilderFactory(context));
    }
}
