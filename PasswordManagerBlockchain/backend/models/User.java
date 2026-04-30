package backend.models;

public class User {
    private String username;
    private String salt;

    public User(String username, String salt) {
        this.username = username;
        this.salt = salt;
    }

    public String getUsername() { return username; }
    public String getSalt() { return salt; }
}
