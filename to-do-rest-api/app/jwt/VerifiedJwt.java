package jwt;

import org.bson.types.ObjectId;

import java.util.Date;

public interface VerifiedJwt {
    String getHeader();
    String getPayload();
    String getIssuer();
    Date getExpiresAt();
    ObjectId getUserId();
}
