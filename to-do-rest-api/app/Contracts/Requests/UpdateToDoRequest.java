package Contracts.Requests;

import models.FileMetadata;
import org.bson.types.ObjectId;

import java.util.List;

public class UpdateToDoRequest {
    private ObjectId id;
    private String title;
    private String description;
    private List<String> tags;
    private List<FileMetadataRequest> files;

    public UpdateToDoRequest(ObjectId id, String title, String description, List<String> tags, List<FileMetadataRequest> files) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.files = files;
    }

    public ObjectId getId() {
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

    public List<FileMetadataRequest> getFiles() { return files; }
}
