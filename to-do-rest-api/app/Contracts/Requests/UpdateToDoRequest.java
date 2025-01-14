package Contracts.Requests;

import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class UpdateToDoRequest {
    @NotNull(message = "ID must not be null")
    private ObjectId id;

    @NotBlank(message = "Title must not be blank")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private List<@NotBlank(message = "Tags must not contain blank values")String> tags;

    @Valid
    private List<@NotNull(message = "File metadata must not be null") FileMetadataRequest> filesMetadata;

    public UpdateToDoRequest(ObjectId id, String title, String description, List<String> tags, List<FileMetadataRequest> filesMetadata) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.filesMetadata = filesMetadata;
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

    public List<FileMetadataRequest> getFilesMetadata() { return filesMetadata; }
}
