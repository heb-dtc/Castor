package com.heb.castor.app;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class GalleryImageAdapter extends RecyclerView.Adapter<GalleryImageAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_item, parent, false);

        ViewHolder vh = new ViewHolder(root);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //do stuff
    }

    @Override
    public int getItemCount() {
        return 20;
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
