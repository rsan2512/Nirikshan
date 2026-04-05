package model;

public class User {
    private int    userId;
    private String name;
    private String email;
    private String password;
    private String role;

    // Constructor
    public User(int userId, String name, String email, String password, String role) {
        this.userId   = userId;
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    // Getters
    public int    getUserId()  { return userId; }
    public String getName()    { return name; }
    public String getEmail()   { return email; }
    public String getPassword(){ return password; }
    public String getRole()    { return role; }

    @Override
    public String toString() {
        return name + " [" + role + "]";
    }
}