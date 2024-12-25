package jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.bson.types.ObjectId;
import play.libs.Json;

import java.util.Date;

public class VerifiedJwtImpl implements VerifiedJwt {
    private String header;
    private String payload;
    private String issuer;
    private ObjectId userId;
    private Date expiresAt;

    public VerifiedJwtImpl(DecodedJWT decodedJWT) {
        this.header = decodedJWT.getHeader();
        this.payload = decodedJWT.getPayload();
        this.issuer = decodedJWT.getIssuer();
        this.expiresAt = decodedJWT.getExpiresAt();
        String userIdString = decodedJWT.getClaim("user_id").asString();
        this.userId = new ObjectId(userIdString);
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public Date getExpiresAt() {
        return expiresAt;
    }
    @Override
    public String toString() {
        return Json.toJson(this).toString();
    }
    @Override
    public ObjectId getUserId() {
        return userId;
    }
}
