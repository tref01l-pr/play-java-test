package models;

import CustomExceptions.ValidationException;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.stream.Collectors;

public class ToDo {
    private ObjectId _id;
    private ObjectId userId;
    private String title;
    private String description;
    private Date createdAt;
    private List<String> tags;
    private List<FileMetadata> files;

    public static final int MAX_TITLE_LENGTH = 255;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_TAG_LENGTH = 50;

    private ToDo(ObjectId _id, ObjectId userId, String title, String description, List<String> tags, List<FileMetadata> files) {
        this._id = _id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.createdAt = new Date();
        this.tags = tags;
        this.files = files;
    }

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId id) {
        this._id = id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public List<FileMetadata> getFiles() { return files; }


    public List<String> getTags() {
        return tags;
    }

    private static String validateCreate(ObjectId userId, String title, String description, List<String> tags) {
        if (userId == null) {
            return "User ID can't be null";
        }

        if (title == null || title.trim().isEmpty()) {
            return "Title can't be null or empty";
        } else if (title.length() > MAX_TITLE_LENGTH) {
            return "Title can't be longer than " + MAX_TITLE_LENGTH + " characters";
        }

        if (description == null || description.trim().isEmpty()) {
            return "Description can't be null or empty";
        } else if (description.length() > MAX_DESCRIPTION_LENGTH) {
            return "Description can't be longer than " + MAX_DESCRIPTION_LENGTH + " characters";
        }

        if (tags == null || tags.isEmpty()) {
            return null;
        }

        Set<String> uniqueTags = new HashSet<>(tags);
        if (uniqueTags.size() != tags.size()) {
            return "Tags must be unique";
        }

        for (String tag : tags) {
            if (tag == null || tag.trim().isEmpty()) {
                return "Tags can't contain null or empty values";
            }
            if (tag.length() > MAX_TAG_LENGTH) {
                return "Each tag can't be longer than " + MAX_TAG_LENGTH + " characters";
            }
        }

        return null;
    }

    private static List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }

        return tags.stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(tag -> tag.trim().toLowerCase())
                .collect(Collectors.toList());
    }

    public static ToDo create(ObjectId userId, String title, String description, List<String> tags, List<FileMetadata> files) {
        tags = normalizeTags(tags);
        String validationError = validateCreate(userId, title, description, tags);

        if (validationError != null) {
            throw new ValidationException(validationError);
        }

        return new ToDo(null, userId, title, description, tags, files);
    }
}