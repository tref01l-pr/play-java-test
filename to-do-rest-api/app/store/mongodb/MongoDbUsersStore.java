package store.mongodb;

import CustomExceptions.DatabaseException;
import CustomExceptions.ServiceUnavailableException;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import entities.mongodb.MongoDbUser;
import models.User;
import org.bson.types.ObjectId;
import play.Logger;
import services.MongoDb;
import store.UsersStore;

import javax.inject.Inject;
import java.util.*;

public class MongoDbUsersStore implements UsersStore {
    @Inject
    private MongoDb mongoDb;

    private Query<MongoDbUser> query() {
        return mongoDb.getDS().find(MongoDbUser.class);
    }

    private Query<MongoDbUser> query(MongoDbUser user) {
        MongoDbUser mongoDbUser = (MongoDbUser)user;
        return query().filter(Filters.eq("_id", mongoDbUser.getObjectId()));
    }

    @Override
    public MongoDbUser getByUsername(String username) {
        try {
            return query()
                    .filter(Filters.eq("username", username))
                    .first();
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while getting all todos", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while getting all todos", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public MongoDbUser getById(ObjectId id) {
        return query().filter(Filters.eq("_id", id)).first();
    }

    @Override
    public MongoDbUser create(User user) {
        MongoDbUser userEntity = new MongoDbUser(user);
        mongoDb.getDS().save(userEntity);
        return userEntity;
    }

    @Override
    public List<? extends MongoDbUser> getAll() {
        return query().iterator().toList();
    }

    /*@Override
    public boolean update(User user) {
        MongoDbUser mongoDbUser = (MongoDbUser)user;

        String fromUserEmail = openIdUser.isEmailVerified() ? openIdUser.getEmail() : mongoDbUser.getEmail();

        List<UpdateOperator> updateOperators = new LinkedList<>();
        if (!Objects.equals(mongoDbUser.getEmail(), fromUserEmail)) {
            mongoDbUser.setEmail(fromUserEmail);
            updateOperators.add(UpdateOperators.set("email", mongoDbUser.getEmail()));
        }
        if (!Objects.equals(mongoDbUser.getFirstName(), openIdUser.getFirstName())) {
            mongoDbUser.setFirstName(openIdUser.getFirstName());
            updateOperators.add(UpdateOperators.set("firstName", mongoDbUser.getFirstName()));
        }
        if (!Objects.equals(mongoDbUser.getLastName(), openIdUser.getLastName())) {
            mongoDbUser.setLastName(openIdUser.getLastName());
            updateOperators.add(UpdateOperators.set("lastName", mongoDbUser.getLastName()));
        }
        Set<String> oldGroupPaths = new HashSet<>(mongoDbUser.getGroupPaths());
        Set<String> newGroupPaths = new HashSet<>(openIdUser.getGroupPaths());
        if (!Objects.equals(oldGroupPaths, newGroupPaths)) {
            mongoDbUser.setGroupPaths(openIdUser.getGroupPaths());
            updateOperators.add(UpdateOperators.set("groupPaths", mongoDbUser.getGroupPaths()));
        }
        if (updateOperators.isEmpty()) {
            return false;
        }
        query(mongoDbUser).update(updateOperators).execute();
        return true;
    }
*/



}
