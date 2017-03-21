package com.example.bisho.interviewtask.classes;

/**
 * Created by bisho on 21-Mar-17.
 */
// this is a class to hold photo information

public class Photo {

    private String imageURL; // image url
    private String imageTitle; // image title

    public Photo() {
    }

    public Photo(String imageURL, String imageTitle) {
        this.imageURL = imageURL;
        this.imageTitle = imageTitle;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }
}
