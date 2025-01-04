package Contracts.Responses;

import models.FileMetadata;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ToDoResponse {
    private String id;
    private String title;
    private String description;
    private Date createdAt;
    private List<String> tags;
    private List<FileMetadataResponse> files;

    public ToDoResponse(ObjectId id, String title, String description, Date createdAt, List<String> tags, List<FileMetadata> files) {
        this.id = id.toString();
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.tags = tags;
        List<FileMetadataResponse> filesToResponse = new ArrayList<>();
        for (FileMetadata file : files) {
            filesToResponse.add(new FileMetadataResponse(file.getFileName(), file.getFileType(), file.getHash(), file.getImageIds().size()));
        }
        this.files = filesToResponse;
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

    public List<FileMetadataResponse> getFiles() { return files; }
}
