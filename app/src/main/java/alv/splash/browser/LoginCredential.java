package alv.splash.browser;

public class LoginCredential {
    private long id;
    private String host;
    private String username;
    private String password;
    private long timestamp;

    public LoginCredential(long id, String host, String username, String password, long timestamp) {
        this.id = id;
        this.host = host;
        this.username = username;
        this.password = password;
        this.timestamp = timestamp;
    }

    // Getters
    public long getId() { return id; }
    public String getHost() { return host; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public long getTimestamp() { return timestamp; }
}