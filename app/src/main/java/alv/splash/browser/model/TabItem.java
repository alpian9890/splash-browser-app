package alv.splash.browser.model;

import android.graphics.Bitmap;

import java.util.UUID;

public class TabItem {
    private String id;
    private String title;
    private String url;
    private Bitmap favicon;
    private boolean isActive;

    public TabItem(String url) {
        this.id = UUID.randomUUID().toString();
        this.title = "New Tab";
        this.url = url;
        this.isActive = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Bitmap getFavicon() { return favicon; }
    public void setFavicon(Bitmap favicon) { this.favicon = favicon; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
