package store;

import com.mongodb.client.ClientSession;
import dev.morphia.transactions.MorphiaSession;
import entities.mongodb.MongoDbToDo;
import models.ToDo;
import org.bson.types.ObjectId;

import java.util.List;

public interface ToDosStore {
    List<? extends MongoDbToDo> getAll();
    List<MongoDbToDo> getByUserId(ObjectId userId);
    MongoDbToDo getById(ObjectId id);
    MongoDbToDo create(ToDo model, MorphiaSession session);
    MongoDbToDo update(ToDo model, MorphiaSession session);
    void removeById(ObjectId id, MorphiaSession session);
}
