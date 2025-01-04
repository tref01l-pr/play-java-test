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
    private List<FileMetadataResponse> filesMetadata;

    public ToDoResponse(ObjectId id, String title, String description, Date createdAt, List<String> tags, List<FileMetadata> filesMetadata) {
        this.id = id.toString();
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.tags = tags;
        List<FileMetadataResponse> filesMetadataToResponse = new ArrayList<>();
        for (FileMetadata file : filesMetadata) {
            filesMetadataToResponse.add(new FileMetadataResponse(file.getFileName(), file.getFileType(), file.getHash(), file.getImageIds().size()));
        }
        this.filesMetadata = filesMetadataToResponse;
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

    public List<FileMetadataResponse> getFilesMetadata() { return filesMetadata; }
}
