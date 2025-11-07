package alv.splash.browser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryItem {
    private long id;
    private String url;
    private String title;
    private long timestamp;

    public HistoryItem(String url, String title, long timestamp) {
        this.url = url;
        this.title = title;
        this.timestamp = timestamp;
    }

    public HistoryItem(long id, String url, String title, long timestamp) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}