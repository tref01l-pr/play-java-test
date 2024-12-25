package Contracts.Requests;

import org.bson.types.ObjectId;

import java.util.List;

public class CreateToDoRequest {
    private ObjectId userId;
    private String title;
    private String description;
    private List<String> tags;

    public CreateToDoRequest(ObjectId userId, String title, String description, List<String> tags) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.tags = tags;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
