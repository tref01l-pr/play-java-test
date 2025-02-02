package Factories;

import models.FileMetadata;
import models.ToDo;
import org.bson.types.ObjectId;

import java.util.List;

public class ToDoFactory {
    public static ToDo createToDo(ObjectId userId, String title, String description, List<String> tags, List<FileMetadata> files) {
        return ToDo.create(userId, title, description, tags, files);
    }

    public static ToDo createUpdatedToDo(ObjectId id, ObjectId userId, String title, String description, List<String> tags, List<FileMetadata> files) {
        ToDo todo = ToDo.create(userId, title, description, tags, files);
        todo.setId(id);
        return todo;
    }
}