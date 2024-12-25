package models;

import utils.SimpleSHA512;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class User {
    private String username;
    private String password;
    private Date createdDate;
    private int id;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = new SimpleSHA512().hash(password);
        this.createdDate = Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
