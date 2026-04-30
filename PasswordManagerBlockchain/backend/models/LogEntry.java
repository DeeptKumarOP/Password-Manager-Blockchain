package backend.models;

public class LogEntry {
    public String username;
    public String action;
    public long timestamp;

    public LogEntry(String username, String action, long timestamp) {
        this.username = username;
        this.action = action;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "[" + new java.util.Date(timestamp) + "] User: " + username + " | Action: " + action;
    }
}
