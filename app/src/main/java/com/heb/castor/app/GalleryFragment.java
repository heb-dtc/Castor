package com.heb.castor.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private GalleryImageAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container);

        recyclerView = (RecyclerView) root.findViewById(R.id.image_recycler_view);

        layoutManager = new GridLayoutManager(getActivity(), 1);
        adapter = new GalleryImageAdapter();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
