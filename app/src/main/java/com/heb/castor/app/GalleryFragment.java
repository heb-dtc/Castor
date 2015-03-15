package com.heb.castor.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView recyclerView;
    private GalleryImageAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private static final String[] TN_IMAGES_PROJECTION = { MediaStore.Images.Thumbnails._ID };
    private static final String[] MEDIA_PROJECTION = { MediaStore.Images.Media._ID };

    private static final Uri TN_BASE_URI = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
    private static final Uri MEDIA_BASE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    private static final String TN_IMAGE_ID_COLUMN = MediaStore.Images.Thumbnails._ID;
    private static final String MEDIA_IMAGE_ID_COLUMN = MediaStore.Images.Media._ID;

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MEDIA_BASE_URI, MEDIA_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            List<Uri> imageUriList = new ArrayList<>();

            try {
                int  imageIdColumnIndex = data.getColumnIndexOrThrow(MEDIA_IMAGE_ID_COLUMN);
                while (data.moveToNext()) {
                    int imageId = data.getInt(imageIdColumnIndex);

                    Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(getActivity().getContentResolver(), imageId,
                            MediaStore.Images.Thumbnails.MINI_KIND, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();

                        //TODO: save "real" path into object for cast
                        //String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(TN_IMAGE_ID_COLUMN));
                        cursor.close();

                        //TODO: force thumbnail generation if not here!!
                        /*MediaStore.Images.Thumbnails.getThumbnail(getActivity().getContentResolver(),
                                imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);*/

                        Uri tnUri = TN_BASE_URI.buildUpon().appendPath(id).build();
                        imageUriList.add(tnUri);
                    }
                }
                updateAdapter(imageUriList);
            } catch (Exception e) {

            } finally {
                data.close();
            }
        }
    }

    private void updateAdapter(List<Uri> content) {
        adapter.addContent(content);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
