package entities.mongodb;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

@Entity(value = "todos", useDiscriminator = false)
public class MongoDbToDo {
    @Id
    private ObjectId _id;

    @Indexed
    private ObjectId userId;

    @Property("title")
    private String title;

    @Property("description")
    private String description;

    @Property("createdAt")
    private Date createdAt;

    @Property("tags")
    @Indexed
    private List<String> tags;

    public MongoDbToDo() {
        // dummy constructor for Morphia
    }

    public MongoDbToDo(ObjectId userId, String title, String description) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.createdAt = new Date();
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
