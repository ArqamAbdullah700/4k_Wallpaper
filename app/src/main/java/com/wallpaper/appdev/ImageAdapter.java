package com.wallpaper.appdev;

import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<ImageItem> imageList;
    private Context context;

    public ImageAdapter(Context context, List<ImageItem> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageItem imageItem = imageList.get(position);
        holder.imageNameTextView.setText(imageItem.getImageName());

        // Load the image into the ImageView using Glide
        Log.d("ImageURL", imageItem.getImageUrl());

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the position of the clicked item
                int position = holder.getAdapterPosition();

                // Ensure the position is valid
                if (position != RecyclerView.NO_POSITION) {
                    // Retrieve the clicked ImageItem
                    ImageItem clickedImage = imageList.get(position);

                    // You can now handle the click event for the specific image (clickedImage)
                    // For example, you might want to open a detailed view or perform some action.
                    // You can pass the clickedImage to another activity or fragment.

                    // Example: Open a detail activity and pass the image URL
                    Intent intent = new Intent(context, ImageViewActivity.class);
                    intent.putExtra("imageUrl", clickedImage.getImageUrl());
                    context.startActivity(intent);
                }
            }
        });


        Picasso.get()
                .load(imageItem.getImageUrl())
                .rotate(getImageRotation(imageItem.getImageUrl()))
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.progressBar.setVisibility(View.GONE); // Hide the progress bar on success
                    }

                    @Override
                    public void onError(Exception e) {
                        holder.progressBar.setVisibility(View.GONE); // Hide the progress bar on error
                    }
                });

    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }
    public void addImages(List<ImageItem> newImageUrls) {
        // Add newly fetched images
        imageList.addAll(newImageUrls);

    }
    private int getImageRotation(String imageUrl) {
        try {
            // Get the orientation of the image from its metadata
            ExifInterface exifInterface = new ExifInterface(imageUrl);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            // Calculate the rotation angle based on the orientation
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView imageNameTextView;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            imageNameTextView = itemView.findViewById(R.id.imageNameTextView);
            progressBar = itemView.findViewById(R.id.progressBar);

        }
    }
}