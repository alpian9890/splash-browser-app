package alv.splash.browser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookmarkItem {
    private long id;
    private String url;
    private String title;
    private long timestamp;

    public BookmarkItem(long id, String url, String title, long timestamp) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.timestamp = timestamp;
    }

    // Getters
    public long getId() { return id; }
    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public long getTimestamp() { return timestamp; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}