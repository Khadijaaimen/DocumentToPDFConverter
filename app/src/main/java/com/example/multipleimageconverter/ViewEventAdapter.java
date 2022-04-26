package com.example.multipleimageconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

public class ViewEventAdapter extends RecyclerView.Adapter<ViewEventAdapter.ViewHolder> {
    private List<ThumbnailItem> item;
    ViewEventAdapter.ThumbnailsAdapterListener listener1;
    Context c1;

    public ViewEventAdapter(Context context, List<ThumbnailItem> it, ViewEventAdapter.ThumbnailsAdapterListener listener) {
        c1 = context;
        listener1 = listener;
        item = it;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumnail_list_item_test, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {


        final ThumbnailItem item1 = item.get(position);
        viewHolder.title.setText(item1.filterName);
//        viewHolder.img.setImageResource(R.drawable.girl);
        viewHolder.img.setImageBitmap(item.get(position).image);

        viewHolder.img.setOnClickListener(view -> {
            listener1.onFilterSelected(item1.filter);
//            Toast.makeText(c1, "pakistan zindabad", Toast.LENGTH_SHORT).show();
            notifyDataSetChanged();
        });

    }

    @Override
    public int getItemCount() {
//        return 7;

        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img);
            title = (TextView) itemView.findViewById(R.id.filter_name);

//            itemView.setOnClickListener(view -> ThumbnailsAdapterListener.onFilterSelected(getAdapterPosition()));


        }
    }


    public interface ThumbnailsAdapterListener {
        void onFilterSelected(Filter  filter);

    }
}
