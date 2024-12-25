package entities.mongodb;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Objects;

@Entity(value = "sessions", useDiscriminator = false)
public class MongoDbUserSession {
    @Id
    private ObjectId _id;
    private ObjectId user_id;
    private String accessToken;
    private String refreshToken;
    private Date TokenExpiry;

    public MongoDbUserSession() {
        // dummy constructor for Morphia
    }

    public MongoDbUserSession(ObjectId user_id, String accessToken, String refreshToken, Date TokenExpiry) {
        this.user_id = user_id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.TokenExpiry = TokenExpiry;
    }

    public ObjectId get_id() {
        return _id;
    }

    public ObjectId getUserId() {
        return user_id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Date getTokenExpiry() {
        return TokenExpiry;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setTokenExpiry(Date TokenExpiry) {
        this.TokenExpiry = TokenExpiry;
    }

    public void setUserId(ObjectId userId) {
        this.user_id = userId;
    }
}
