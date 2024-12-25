package store.mongodb;

import com.mongodb.client.result.DeleteResult;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import entities.mongodb.MongoDbToDo;
import models.ToDo;
import org.bson.types.ObjectId;
import services.MongoDb;
import store.ToDosStore;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MongoDbToDosStore implements ToDosStore {
    @Inject
    private MongoDb mongoDb;

    private Query<MongoDbToDo> query() {
        return mongoDb.getDS().find(MongoDbToDo.class);
    }

    private Query<MongoDbToDo> query(MongoDbToDo toDo) {
        return query().filter(Filters.eq("_id", toDo.getId()));
    }

    @Override
    public List<MongoDbToDo> getAll() {
        return query().stream().collect(Collectors.toList());
    }

    @Override
    public List<MongoDbToDo> getByUserId(ObjectId userId) {
        return query().filter(Filters.eq("userId", userId)).stream().collect(Collectors.toList());
    }

    @Override
    public MongoDbToDo getById(ObjectId id) {
        return query().filter(Filters.eq("_id", id)).first();
    }

    @Override
    public MongoDbToDo create(ToDo model) {
        MongoDbToDo newToDo = new MongoDbToDo();

        newToDo.setUserId(model.getUserId());
        newToDo.setTitle(model.getTitle());
        newToDo.setDescription(model.getDescription());
        newToDo.setCreatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        newToDo.setTags(model.getTags());

        mongoDb.getDS().save(newToDo);
        return newToDo;
    }

    @Override
    public MongoDbToDo update(ToDo model) {
        MongoDbToDo existingToDo = query().filter(Filters.eq("_id", model.getId())).first();
        if (existingToDo == null) {
            throw new IllegalArgumentException("ToDo with ID " + model.getId() + " not found.");
        }

        existingToDo.setTitle(model.getTitle());
        existingToDo.setDescription(model.getDescription());
        existingToDo.setTags(model.getTags());

        mongoDb.getDS().merge(existingToDo);
        return existingToDo;
    }

    @Override
    public void removeById(ObjectId id) {
        MongoDbToDo toDelete = query().filter(Filters.eq("_id", id)).first();
        DeleteResult result = mongoDb.getDS().delete(toDelete);
        if (result.getDeletedCount() == 0) {
            throw new IllegalArgumentException("ToDo with ID " + id + " not found.");
        }
    }
}
