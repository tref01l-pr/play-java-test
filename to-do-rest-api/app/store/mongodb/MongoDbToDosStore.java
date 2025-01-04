package store.mongodb;

import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import entities.mongodb.MongoDbToDo;
import models.ToDo;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import services.MongoDb;
import store.ToDosStore;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
        newToDo.setFiles(model.getFiles());

        mongoDb.getDS().save(newToDo);
        return newToDo;
    }

    @Override
    public MongoDbToDo update(ToDo model) {
        Filter filter = Filters.eq("_id", model.getId());
        MongoDbToDo existingToDo = query().filter(Filters.eq("_id", model.getId())).first();
        if (existingToDo == null) {
            throw new IllegalArgumentException("ToDo with ID " + model.getId() + " not found.");
        }

        existingToDo.setTitle(model.getTitle());
        existingToDo.setDescription(model.getDescription());
        existingToDo.setTags(model.getTags());
        existingToDo.setFiles(model.getFiles());

        List<Bson> updates = new ArrayList<>();
        if (model.getTags() == null || model.getTags().isEmpty()) {
            updates.add(Updates.unset("tags"));
        }
        if (model.getFiles() == null || model.getFiles().isEmpty()) {
            updates.add(Updates.unset("files"));
        }

        if (!updates.isEmpty()) {
            mongoDb.get().getCollection("todos").updateOne(
                    new org.bson.Document("_id", model.getId()),
                    Updates.combine(updates)
            );
        }

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
