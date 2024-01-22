package com.wallpaper.appdev;

public class ImageHolder {
    private String thumbUrl;
    private String originalUrl;

    public ImageHolder(String thumbUrl, String originalUrl) {
        this.thumbUrl = thumbUrl;
        this.originalUrl = originalUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
