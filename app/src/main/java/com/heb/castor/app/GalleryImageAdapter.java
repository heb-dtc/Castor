package com.heb.castor.app;

import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GalleryImageAdapter extends RecyclerView.Adapter<GalleryImageAdapter.ViewHolder> implements Callback {

    List<Uri> imageUri;

    public GalleryImageAdapter() {
        imageUri = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_item, parent, false);

        ViewHolder vh = new ViewHolder(root);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Uri contentUri = imageUri.get(position);

        Picasso.with(holder.imageView.getContext())
                .load(contentUri)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_launcher)
                .into(holder.imageView, this);
    }

    @Override
    public int getItemCount() {
        return imageUri.size();
    }

    public void addContent(List<Uri> content) {
        imageUri.addAll(content);
    }

    @Override
    public void onSuccess() {
        Log.e("GA", "load success");
    }

    @Override
    public void onError() {
        Log.e("GA", "load failure");
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            this.cardView = (CardView) view;
            this.imageView = (ImageView) cardView.findViewById(R.id.gallery_image);
        }
    }
}
