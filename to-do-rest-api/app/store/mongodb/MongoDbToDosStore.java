package store.mongodb;

import CustomExceptions.DatabaseException;
import CustomExceptions.ResourceNotFoundException;
import CustomExceptions.ServiceUnavailableException;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import entities.mongodb.MongoDbToDo;
import models.ToDo;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.Logger;
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
        try {
            return mongoDb.getDS().find(MongoDbToDo.class);
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while creating query", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while creating query", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    private MongoDbToDo findById(ObjectId id) {
        try {
            MongoDbToDo todo = query().filter(Filters.eq("_id", id)).first();
            if (todo == null) {
                throw new ResourceNotFoundException("ToDo with ID " + id + " not found.");
            }
            return todo;
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while finding todo by ID: " + id, e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while finding todo by ID: " + id, e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public List<MongoDbToDo> getAll() {
        try {
            return query().stream().collect(Collectors.toList());
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while getting all todos", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while getting all todos", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public List<MongoDbToDo> getByUserId(ObjectId userId) {
        try {
            return query().filter(Filters.eq("userId", userId)).stream().collect(Collectors.toList());
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while getting todos for user: " + userId, e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while getting todos for user: " + userId, e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public MongoDbToDo getById(ObjectId id) {
        try {
            return findById(id);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    @Override
    public MongoDbToDo create(ToDo model) {
        try {
            MongoDbToDo newToDo = new MongoDbToDo();

            newToDo.setUserId(model.getUserId());
            newToDo.setTitle(model.getTitle());
            newToDo.setDescription(model.getDescription());
            newToDo.setCreatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
            newToDo.setTags(model.getTags());
            newToDo.setFiles(model.getFiles());

            mongoDb.getDS().save(newToDo);
            return newToDo;
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while creating todo", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while creating todo", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public MongoDbToDo update(ToDo model) {
        try {
            MongoDbToDo existingToDo = findById(model.getId());

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
                mongoDb.get().getCollection("todos").updateOne(new org.bson.Document("_id", model.getId()), Updates.combine(updates));
            }

            mongoDb.getDS().merge(existingToDo);
            return existingToDo;
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while updating todo: " + model.getId(), e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while updating todo: " + model.getId(), e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public void removeById(ObjectId id) {
        try {
            MongoDbToDo toDelete = findById(id);
            DeleteResult result = mongoDb.getDS().delete(toDelete);
            if (result.getDeletedCount() == 0) {
                throw new ResourceNotFoundException("ToDo with ID " + id + " not found.");
            }
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while deleting todo: " + id, e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while deleting todo: " + id, e);
            throw new DatabaseException("Error accessing database", e);
        }
    }
}