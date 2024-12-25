package entities.mongodb;

import dev.morphia.annotations.*;
import models.User;
import org.bson.types.ObjectId;
import utils.SimpleSHA512;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(value = "users", useDiscriminator = false)
public class MongoDbUser {
    @Id
    private ObjectId _id;

    @Property("username")
    private String username;

    private String passwordHash;
    private Date createdDate;
    public MongoDbUser() {
        // dummy constructor for Morphia
    }

    public MongoDbUser(User user) {
        this.username = user.getUsername();
        this.passwordHash =  user.getPassword();
        this.createdDate = user.getCreatedDate();
    }

    public ObjectId getObjectId() {
        return _id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public ObjectId getId() {
        return _id;
    }
}
