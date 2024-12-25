package Contracts.Responses;

import org.bson.types.ObjectId;

import java.time.OffsetDateTime;
import java.util.Date;

public class AuthorizationResponse {
    private String id;
    private String username;
    private Date userCreatedAt;
    private String accessToken;

    public AuthorizationResponse(ObjectId id, String username, Date userCreatedAt, String accessToken) {
        this.id = id.toString();
        this.username = username;
        this.userCreatedAt = userCreatedAt;
        this.accessToken = accessToken;
    }

    public String getUsername() {
        return username;
    }

    public Date getUserCreatedAt() {
        return userCreatedAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getId() {
        return id;
    }
}
