package Contracts.Responses;

import models.FileMetadata;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class ToDoResponse {
    private String id;
    private String title;
    private String description;
    private Date createdAt;
    private List<String> tags;
    private List<FileMetadata> files;

    public ToDoResponse(ObjectId id, String title, String description, Date createdAt, List<String> tags, List<FileMetadata> files) {
        this.id = id.toString();
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.tags = tags;
        this.files = files;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public List<FileMetadata> getFiles() { return files; }
}
