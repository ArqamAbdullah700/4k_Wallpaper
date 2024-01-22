package com.wallpaper.appdev;

public class ImageItem {
    private String imageOriginalUrl;
    private String imageThumbUrl;

    public ImageItem(String imageName, String imageUrl) {
        this.imageOriginalUrl = imageName;
        this.imageThumbUrl = imageUrl;
    }

    public String getImageOriginalUrl() {
        return imageOriginalUrl;
    }

    public String getImageThumbUrl() {
        return imageThumbUrl;
    }
}