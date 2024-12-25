package store;

import entities.mongodb.MongoDbUserSession;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserSessionsStore {
    List<? extends MongoDbUserSession> getAll();
    MongoDbUserSession getById(ObjectId id);
    MongoDbUserSession getByRefreshToken(String refreshToken);
    MongoDbUserSession create(ObjectId userId, String secret);
    MongoDbUserSession update(MongoDbUserSession session, String secret);
    void logout(ObjectId userId, String refreshToken) throws Exception;
}
