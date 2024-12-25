package store.mongodb;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import entities.mongodb.MongoDbUserSession;
import jwt.JwtResult;
import org.bson.types.ObjectId;
import services.MongoDb;
import store.UserSessionsStore;
import utils.Config;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public class MongoDbUserSessionsStore implements UserSessionsStore {
    @Inject
    private MongoDb mongoDb;

    private Query<MongoDbUserSession> query() {
        return mongoDb.getDS().find(MongoDbUserSession.class);
    }

    private Query<MongoDbUserSession> query(MongoDbUserSession session) {
        return query().filter(Filters.eq("_id", session.get_id()));
    }

    @Override
    public MongoDbUserSession getById(ObjectId id) {
        return query().filter(Filters.eq("_id", id)).first();
    }

    @Override
    public List<? extends MongoDbUserSession> getAll() {
        return query().iterator().toList();
    }

    @Override
    public MongoDbUserSession getByRefreshToken(String refreshToken) {
        return query()
                .filter(Filters.eq("refreshToken", refreshToken))
                .first();
    }

    private JwtResult getSignedToken(ObjectId userId, String secret, Long timeToExpire) throws UnsupportedEncodingException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Date expireDate = Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusSeconds(timeToExpire).toInstant());
        var jwtCreate =  JWT.create()
                .withIssuer("ThePlayApp")
                .withClaim("user_id", userId.toString())
                .withExpiresAt(expireDate)
                .sign(algorithm);
        return new JwtResult(jwtCreate, expireDate);
    }

    @Override
    public MongoDbUserSession create(ObjectId userId, String secret) {
        try{
            JwtResult accessToken = getSignedToken(userId, secret, Config.getLong(Config.Option.USER_ACCESS_TOKEN_EXPIRES));
            JwtResult refreshToken = getSignedToken(userId, secret, Config.getLong(Config.Option.USER_SESSION_EXPIRES));
            MongoDbUserSession session = new MongoDbUserSession(userId, accessToken.Token, refreshToken.Token, refreshToken.Expires);

            mongoDb.getDS().save(session);
            return session;
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MongoDbUserSession update(MongoDbUserSession session, String secret) {
        try {
            JwtResult newAccessToken = getSignedToken(session.getUserId(), secret, Config.getLong(Config.Option.USER_ACCESS_TOKEN_EXPIRES));
            session.setAccessToken(newAccessToken.Token);
            mongoDb.getDS().save(session);
            return session;
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logout(ObjectId userId, String refreshToken) throws Exception {
        MongoDbUserSession session = query()
                .filter(Filters.eq("refreshToken", refreshToken))
                .first();

        if (session == null) {
            throw new Exception("Session not found.");
        }

        if (!session.getUserId().equals(userId)) {
            throw new Exception("Session not found.");
        }

        query().filter(Filters.eq("_id", session.get_id())).delete();

        //TODO: remove session from userComputer on frontend
    }
}
