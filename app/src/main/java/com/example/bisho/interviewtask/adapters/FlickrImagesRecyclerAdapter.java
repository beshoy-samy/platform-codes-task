package com.example.bisho.interviewtask.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bisho.interviewtask.R;
import com.example.bisho.interviewtask.classes.Photo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by bisho on 23-Mar-17.
 */

public class FlickrImagesRecyclerAdapter extends RecyclerView.Adapter<FlickrImagesRecyclerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Photo> flickrImages;

    public FlickrImagesRecyclerAdapter(Context context, ArrayList<Photo> flickrImages) {
        this.context = context;
        this.flickrImages = flickrImages;
    }

    public void updateFlickrImages(ArrayList<Photo> flickrImages){
        this.flickrImages = flickrImages;
    }

    @Override
    public FlickrImagesRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.flickr_image_item, parent, false);
        FlickrImagesRecyclerAdapter.ViewHolder viewHolder = new FlickrImagesRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FlickrImagesRecyclerAdapter.ViewHolder holder, int position) {

        Photo flickrPhoto = flickrImages.get(position);
        holder.imageTitle.setText(flickrPhoto.getImageTitle());
        Picasso.with(context).load(flickrPhoto.getImageURL()).into(holder.flickrImage);

    }

    @Override
    public int getItemCount() {
        return flickrImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView imageTitle;
        ImageView flickrImage;

        public ViewHolder(View itemView) {
            super(itemView);

            imageTitle = (TextView) itemView.findViewById(R.id.image_title);
            flickrImage= (ImageView) itemView.findViewById(R.id.flickr_image);

        }
    }
}
