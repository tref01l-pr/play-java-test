package controllers;

import Contracts.Requests.LoginUserRequest;
import Contracts.Requests.RegisterUserRequest;
import Contracts.Responses.AuthorizationResponse;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;

import entities.mongodb.MongoDbUser;
import jwt.JwtControllerHelper;
import jwt.VerifiedJwt;
import jwt.filter.Attrs;
import models.User;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import store.UserSessionsStore;
import store.UsersStore;
import utils.InputUtils;
import utils.SimpleSHA512;
import utils.Util;

import javax.inject.Inject;
import javax.validation.Validator;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;


public class AuthorizationController {
    private HttpExecutionContext ec;
    private UsersStore usersStore;
    private UserSessionsStore userSessionsStore;
    private final Validator validator;

    @Inject
    private JwtControllerHelper jwtControllerHelper;

    @Inject
    private Config config;

    @Inject
    public AuthorizationController(HttpExecutionContext ec, UsersStore userStore, UserSessionsStore userSessionsStore, Validator validator) {
        this.usersStore = userStore;
        this.userSessionsStore = userSessionsStore;
        this.ec = ec;
        this.validator = validator;
    }

    public CompletionStage<Result> login(Http.Request request) throws UnsupportedEncodingException {
        JsonNode json = request.body().asJson();
        return supplyAsync(() -> {
            if (json == null) {
                Logger.error("json body is null");
                return Results.forbidden();
            }

            try {
                LoginUserRequest loginUserRequest = Json.fromJson(json, LoginUserRequest.class);

                var violations = validator.validate(loginUserRequest);
                if (!violations.isEmpty()) {
                    throw new Exception(
                            violations.stream()
                                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                    .collect(Collectors.joining(", ")));
                }

                if (loginUserRequest.getUsername() == null || loginUserRequest.getPassword() == null) {
                    return Results.forbidden("Missing username or password");
                }


                var user = usersStore.getByUsername(loginUserRequest.getUsername());
                if (user == null) {
                    return Results.forbidden("Invalid username or password");
                }

                if (!user.getPasswordHash().equals(new SimpleSHA512().hash(loginUserRequest.getPassword())))
                {
                    return Results.forbidden("Invalid username or password");
                }

                String secret = config.getString("play.http.secret.key");
                var sessionResult = userSessionsStore.create(user.getId(), secret);

                AuthorizationResponse authorizationResponse = new AuthorizationResponse(user.getId(), user.getUsername(), user.getCreatedDate(),
                        sessionResult.getAccessToken());

                String refreshToken = sessionResult.getRefreshToken();
                Logger.info("User logged in: " + user.getId());
                return Results.ok(Json.toJson(authorizationResponse))
                        .withCookies(Http.Cookie.builder("refreshToken", refreshToken)
                                .withMaxAge(Duration.between(Instant.now(), sessionResult.getTokenExpiry().toInstant()))
                                .withHttpOnly(true)
                                .build());
            } catch (Exception e) {
                Logger.error(e.getMessage());
                return Results.forbidden(e.getMessage());
            }

        }, ec.current());
    }

    public CompletionStage<Result> registerUser(Http.Request request) {
        JsonNode json = request.body().asJson();
        return supplyAsync(() -> {
            if (json == null) {
                return Results.badRequest(Util.createResponse("Expecting Json data", false));
            }

            RegisterUserRequest registerUserRequest = Json.fromJson(json, RegisterUserRequest.class);

            try {
                var violations = validator.validate(registerUserRequest);
                if (!violations.isEmpty()) {
                    throw new Exception(
                            violations.stream()
                                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                    .collect(Collectors.joining(", ")));
                }

                if (registerUserRequest.getUsername() == null) {
                    return Results.badRequest("Missing username");
                }

                if (registerUserRequest.getPassword() == null) {
                    return Results.badRequest("Missing password");
                }

                if (!registerUserRequest.getPassword().equals(registerUserRequest.getPasswordConfirmation())) {
                    return Results.badRequest("Passwords do not match" + registerUserRequest.getPassword() + " " + registerUserRequest.getPasswordConfirmation());
                }

                User user = new User(registerUserRequest.getUsername(), registerUserRequest.getPassword());

                usersStore.create(user);
                Logger.info("User created: " + user.getId());
                return Results.created("User created");
            }
            catch (Exception e) {
                Logger.error(e.getMessage());
                return Results.forbidden(e.getMessage());
            }
        }, ec.current());
    }

    public CompletionStage<Result> listUsers(Http.Request request) {
        return supplyAsync(() -> {
            var result = usersStore.getAll();
            return Results.ok(Json.toJson(result));
        }, ec.current());
    }

    public Result requiresJwt(Http.Request request) {
        return jwtControllerHelper.verify(request, res -> {
            if (res.left.isPresent()) {
                return Results.forbidden(res.left.get().toString());
            }

            VerifiedJwt verifiedJwt = res.right.get();
            Logger.debug("{}", verifiedJwt);

            ObjectNode result = Json.newObject();
            result.put("access", "granted");
            result.put("secret_data", "birds fly");
            return Results.ok(result);
        });
    }

    public Result requiresJwtViaFilter(Http.Request request) {
        Optional<VerifiedJwt> oVerifiedJwt = request.attrs().getOptional(Attrs.VERIFIED_JWT);
        return oVerifiedJwt.map(jwt -> {
            Logger.debug(jwt.toString());
            return Results.ok("access granted via filter");
        }).orElse(Results.forbidden("eh, no verified jwt found"));
    }

    public Result generateSignedToken(String userId) throws UnsupportedEncodingException {
        ObjectId objectId;

        if (userId == null || userId.isEmpty()) {
            objectId = new ObjectId();
        } else {
            try {
                objectId = new ObjectId(userId);
            } catch (IllegalArgumentException e) {
                objectId = new ObjectId();
            }
        }

        JsonNode responseJson = Json.newObject().put("accessToken", getSignedToken(objectId));
        return Results.ok(responseJson);
    }

    private String getSignedToken(ObjectId userId) throws UnsupportedEncodingException {
        String secret = config.getString("play.http.secret.key");

        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer("ThePlayApp")
                .withClaim("user_id", userId.toString())
                .withExpiresAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(10).toInstant()))
                .sign(algorithm);
    }

    public Result refreshAccessToken(Http.Request request) {
        String refreshToken = InputUtils.getRefreshTokenFromCookies(request);
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Results.forbidden("No refresh token found");
        }
        try {
            var session = userSessionsStore.getByRefreshToken(refreshToken);
            var user = usersStore.getById(session.getUserId());
            if (user == null) {
                throw new Exception("Invalid refresh token");
            }

            if (session == null) {
                throw new Exception("Invalid refresh token");
            }

            var updatedSession = userSessionsStore.update(session, config.getString("play.http.secret.key"));
            AuthorizationResponse authorizationResponse = new AuthorizationResponse(user.getId(), user.getUsername(),
                    user.getCreatedDate(), updatedSession.getAccessToken());
            Logger.info("Access token refreshed for user: " + user.getId());
            return Results.ok(Json.toJson(authorizationResponse));
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return Results.forbidden(e.getMessage())
                    .discardingCookie("refreshToken");
        }
    }

    public Result logout(Http.Request request) {
            return jwtControllerHelper.verify(request, res -> {
                if (res.left.isPresent()) {
                    return Results.forbidden(res.left.get().toString());
                }

                VerifiedJwt verifiedJwt = res.right.get();
                var userId = verifiedJwt.getUserId();
                if (userId == null) {
                    return Results.forbidden("No user found");
                }

                String refreshToken = InputUtils.getRefreshTokenFromCookies(request);
                if (refreshToken == null || refreshToken.isEmpty()) {
                    return Results.forbidden("No refresh token found");
                }

                try {
                    userSessionsStore.logout(userId, refreshToken);

                    return Results.ok("Logged out")
                            .discardingCookie("refreshToken");
                } catch (Exception e) {
                    return Results.forbidden(e.getMessage());
                }
            });
    }
}
