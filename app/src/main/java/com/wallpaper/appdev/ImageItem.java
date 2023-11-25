package com.wallpaper.appdev;

public class ImageItem {
    private String imageName;
    private String imageUrl;

    public ImageItem(String imageName, String imageUrl) {
        this.imageName = imageName;
        this.imageUrl = imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}