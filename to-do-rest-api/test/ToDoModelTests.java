import models.ToDo;
import org.bson.types.ObjectId;
import org.junit.Test;
import play.test.WithApplication;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ToDoModelTests extends WithApplication {
    @Test
    public void testCreateToDoSuccess() {
        ObjectId userId = new ObjectId();
        String title = "Test Title";
        String description = "Test Description";
        var tags = Arrays.asList(" urgent ", "Work", "personal  ");

        ToDo toDo = ToDo.create(userId, title, description, tags);

        assertNotNull(toDo);
        assertEquals(userId, toDo.getUserId());
        assertEquals(title, toDo.getTitle());
        assertEquals(description, toDo.getDescription());
        assertEquals(Arrays.asList("urgent", "work", "personal"), toDo.getTags());
        assertNotNull(toDo.getCreatedAt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateToDoWithNullUserId() {
        ToDo.create(null, "Test Title", "Test Description", Arrays.asList("tag1", "tag2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateToDoWithEmptyTitle() {
        ObjectId userId = new ObjectId();
        ToDo.create(userId, "  ", "Test Description", Arrays.asList("tag1", "tag2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateToDoWithLongTitle() {
        ObjectId userId = new ObjectId();
        String longTitle = "T".repeat(ToDo.MAX_TITLE_LENGTH + 1);
        ToDo.create(userId, longTitle, "Test Description", Arrays.asList("tag1", "tag2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateToDoWithEmptyDescription() {
        ObjectId userId = new ObjectId();
        ToDo.create(userId, "Test Title", "   ", Arrays.asList("tag1", "tag2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateToDoWithLongDescription() {
        ObjectId userId = new ObjectId();
        String longDescription = "D".repeat(ToDo.MAX_DESCRIPTION_LENGTH + 1);
        ToDo.create(userId, "Test Title", longDescription, Arrays.asList("tag1", "tag2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateToDoWithDuplicateTags() {
        ObjectId userId = new ObjectId();
        ToDo.create(userId, "Test Title", "Test Description", Arrays.asList("tag1", "Tag1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateToDoWithLongTag() {
        ObjectId userId = new ObjectId();
        String longTag = "T".repeat(ToDo.MAX_TAG_LENGTH + 1);
        ToDo.create(userId, "Test Title", "Test Description", Arrays.asList(longTag));
    }

    @Test
    public void testTagNormalization() {
        ObjectId userId = new ObjectId();
        var tags = Arrays.asList("  TAG1  ", "Tag2 ", " TAG3");

        ToDo toDo = ToDo.create(userId, "Test Title", "Test Description", tags);

        assertEquals(Arrays.asList("tag1", "tag2", "tag3"), toDo.getTags());
    }

    @Test
    public void testCreateToDoWithEmptyTags() {
        ObjectId userId = new ObjectId();
        ToDo toDo = ToDo.create(userId, "Test Title", "Test Description", Collections.emptyList());
        assertEquals(Collections.emptyList(), toDo.getTags());
    }

    @Test
    public void testCreateToDoWithNullTag() {
        ObjectId userId = new ObjectId();
        ToDo toDo = ToDo.create(userId, "Test Title", "Test Description", Arrays.asList("tag1", null));
        assertEquals(Arrays.asList("tag1"), toDo.getTags());
    }
}
