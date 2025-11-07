package alv.splash.browser;

public class SpeedDialItem {
    private String name;
    private String url;

    public SpeedDialItem(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}