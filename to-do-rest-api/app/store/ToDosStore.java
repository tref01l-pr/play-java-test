package store;

import entities.mongodb.MongoDbToDo;
import models.ToDo;
import org.bson.types.ObjectId;

import java.util.List;

public interface ToDosStore {
    List<? extends MongoDbToDo> getAll();
    List<MongoDbToDo> getByUserId(ObjectId userId);
    MongoDbToDo getById(ObjectId id);
    MongoDbToDo create(ToDo model);
    MongoDbToDo update(ToDo model);
    void removeById(ObjectId id) throws Exception;
}
