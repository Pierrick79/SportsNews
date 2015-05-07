package com.example.android.sportsnews;

/**
 * Created by Pierrick on 08/04/2015.
 */
public class RssItem {
    private final String title;
    private final String link;
    private final String date;
    private final String description;
    private final String image;

    public RssItem(String title, String link, String date, String description, String image){
        this.title = title;
        this.link = link;
        this.date = date;
        this.description = description;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() { return link; }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }
}
