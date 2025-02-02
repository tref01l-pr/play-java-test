package store.mongodb;

import CustomExceptions.DatabaseException;
import CustomExceptions.ResourceNotFoundException;
import CustomExceptions.ServiceUnavailableException;
import CustomExceptions.TokenGenerationException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import entities.mongodb.MongoDbUserSession;
import jwt.JwtResult;
import org.bson.types.ObjectId;
import play.Logger;
import services.MongoDb;
import store.UserSessionsStore;
import utils.Config;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public class MongoDbUserSessionsStore implements UserSessionsStore {
    @Inject
    private MongoDb mongoDb;

    private Query<MongoDbUserSession> query() {
        try {
            return mongoDb.getDS().find(MongoDbUserSession.class);
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while creating query", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while creating query", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    private Query<MongoDbUserSession> query(MongoDbUserSession session) {
        try {
            return query().filter(Filters.eq("_id", session.get_id()));
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while creating query for session: " + session.get_id(), e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while creating query for session: " + session.get_id(), e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public MongoDbUserSession getById(ObjectId id) {
        try {
            MongoDbUserSession session = query().filter(Filters.eq("_id", id)).first();
            if (session == null) {
                throw new ResourceNotFoundException("Session with ID " + id + " not found");
            }
            return session;
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while getting session by ID: " + id, e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while getting session by ID: " + id, e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public List<? extends MongoDbUserSession> getAll() {
        try {
            return query().iterator().toList();
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while getting all sessions", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while getting all sessions", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public MongoDbUserSession getByRefreshToken(String refreshToken) {
        try {
            MongoDbUserSession session = query()
                    .filter(Filters.eq("refreshToken", refreshToken))
                    .first();
            if (session == null) {
                throw new ResourceNotFoundException("Session with refresh token not found");
            }
            return session;
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while getting session by refresh token", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while getting session by refresh token", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    private JwtResult getSignedToken(ObjectId userId, String secret, Long timeToExpire) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Date expireDate = Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusSeconds(timeToExpire).toInstant());
            String token = JWT.create()
                    .withIssuer("ThePlayApp")
                    .withClaim("user_id", userId.toString())
                    .withExpiresAt(expireDate)
                    .sign(algorithm);
            return new JwtResult(token, expireDate);
        } catch (Exception e) {
            Logger.error("Unexpected error generating token for user: " + userId, e);
            throw new TokenGenerationException("Unexpected error generating token", e);
        }
    }

    @Override
    public MongoDbUserSession create(ObjectId userId, String secret) {
        try {
            JwtResult accessToken = getSignedToken(userId, secret, Config.getLong(Config.Option.USER_ACCESS_TOKEN_EXPIRES));
            JwtResult refreshToken = getSignedToken(userId, secret, Config.getLong(Config.Option.USER_SESSION_EXPIRES));
            MongoDbUserSession session = new MongoDbUserSession(userId, accessToken.Token, refreshToken.Token, refreshToken.Expires);

            mongoDb.getDS().save(session);
            return session;
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while creating session for user: " + userId, e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while creating session for user: " + userId, e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public MongoDbUserSession update(MongoDbUserSession session, String secret) {
        try {
            JwtResult newAccessToken = getSignedToken(session.getUserId(), secret, Config.getLong(Config.Option.USER_ACCESS_TOKEN_EXPIRES));
            session.setAccessToken(newAccessToken.Token);
            mongoDb.getDS().save(session);
            return session;
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while updating session: " + session.get_id(), e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while updating session: " + session.get_id(), e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public void logout(ObjectId userId, String refreshToken) {
        try {
            MongoDbUserSession session = getByRefreshToken(refreshToken);

            if (!session.getUserId().equals(userId)) {
                throw new ResourceNotFoundException("Session not found for user");
            }

            query().filter(Filters.eq("_id", session.get_id())).delete();
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while logging out user: " + userId, e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while logging out user: " + userId, e);
            throw new DatabaseException("Error accessing database", e);
        }
    }
}
