package store;


import entities.mongodb.MongoDbUser;
import models.User;
import org.bson.types.ObjectId;

import java.util.*;

public interface UsersStore {
    MongoDbUser getByUsername(String username);
    MongoDbUser getById(ObjectId id);
    List<? extends MongoDbUser> getAll();

    MongoDbUser create(User user);
}