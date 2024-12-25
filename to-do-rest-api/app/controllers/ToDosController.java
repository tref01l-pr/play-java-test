package controllers;

import Contracts.Requests.CreateToDoRequest;
import Contracts.Requests.UpdateToDoRequest;
import Contracts.Responses.ToDoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import entities.mongodb.MongoDbToDo;
import jwt.JwtControllerHelper;
import jwt.VerifiedJwt;
import models.ToDo;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import store.ToDosStore;
import play.mvc.Result;
import play.mvc.Results;
import store.UsersStore;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;


public class ToDosController {
    private final HttpExecutionContext ec;
    private final ToDosStore toDosStore;
    private final UsersStore usersStore;

    @Inject
    public ToDosController(HttpExecutionContext ec, ToDosStore toDosStore, UsersStore userStore) {
        this.ec = ec;
        this.toDosStore = toDosStore;
        this.usersStore = userStore;
    }

    @Inject
    private JwtControllerHelper jwtControllerHelper;

    public CompletionStage<Result> listToDos(Http.Request request) {
        return supplyAsync(() -> {
            List<? extends MongoDbToDo> toDos = toDosStore.getAll();
            List<ToDoResponse> response = toDos.stream()
                    .map(toDo -> new ToDoResponse(
                            toDo.getId(),
                            toDo.getTitle(),
                            toDo.getDescription(),
                            toDo.getCreatedAt(),
                            toDo.getTags()
                    ))
                    .collect(Collectors.toList());
            return Results.ok(Json.toJson(response));
        }, ec.current());
    }

    public Result listToDosByUser(Http.Request request, String username) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var userExist = usersStore.getByUsername(username);
            if (userExist == null) {
                return Results.notFound("User not found");
            }

            if (!userExist.getId().equals(userId)) {
                return Results.forbidden("User ID in token does not match user ID in request");
            }

            List<MongoDbToDo> toDos = toDosStore.getByUserId(userId);
            List<ToDoResponse> response = toDos.stream()
                    .map(toDo -> new ToDoResponse(
                            toDo.getId(),
                            toDo.getTitle(),
                            toDo.getDescription(),
                            toDo.getCreatedAt(),
                            toDo.getTags()
                    ))
                    .collect(Collectors.toList());

            return Results.ok(Json.toJson(response));
        });
    }

    public Result getToDoById(Http.Request request, String toDoId) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var toDoObjectId = new org.bson.types.ObjectId(toDoId);
            MongoDbToDo toDo = toDosStore.getById(toDoObjectId);

            if (toDo == null) {
                return Results.notFound("To-Do not found");
            }

            if (!toDo.getUserId().equals(userId)) {
                return Results.forbidden("To-Do does not belong to user");
            }

            ToDoResponse response = new ToDoResponse(toDo.getId(), toDo.getTitle(), toDo.getDescription(), toDo.getCreatedAt(), toDo.getTags());
            return Results.ok(Json.toJson(response));
        });
    }

    public Result createToDo(Http.Request request) {
        JsonNode json = request.body().asJson();
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            if (json == null) {
                return Results.badRequest("Invalid JSON data");
            }

            CreateToDoRequest createToDoRequest = Json.fromJson(json, CreateToDoRequest.class);

            if (createToDoRequest.getTitle() == null || createToDoRequest.getDescription() == null) {
                return Results.badRequest("Missing title or description");
            }

            if (!createToDoRequest.getUserId().equals(userId)) {
                return Results.forbidden("User ID in token does not match user ID in request");
            }

            try {
                ToDo toDo = ToDo.create(createToDoRequest.getUserId(), createToDoRequest.getTitle(), createToDoRequest.getDescription(), createToDoRequest.getTags());
                MongoDbToDo createdToDo = toDosStore.create(toDo);
                ToDoResponse response = new ToDoResponse(createdToDo.getId(), createdToDo.getTitle(), createdToDo.getDescription(), createdToDo.getCreatedAt(), createdToDo.getTags());
                return Results.created(Json.toJson(response));
            }
            catch (Exception e) {
                return Results.internalServerError(e.getMessage());
            }
        });
    }

    public Result updateToDo(Http.Request request) {
        JsonNode json = request.body().asJson();
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            if (json == null) {
                return Results.badRequest("Invalid JSON data");
            }

            UpdateToDoRequest updateToDoRequest = Json.fromJson(json, UpdateToDoRequest.class);

            MongoDbToDo toDoExist = toDosStore.getById(updateToDoRequest.getId());

            if (toDoExist == null) {
                return Results.notFound("To-Do not found");
            }

            if (!toDoExist.getUserId().equals(userId)) {
                return Results.forbidden("To-Do does not belong to user");
            }

            try {
                ToDo toDo = ToDo.create(userId, updateToDoRequest.getTitle(), updateToDoRequest.getDescription(), updateToDoRequest.getTags());
                toDo.setId(updateToDoRequest.getId());
                MongoDbToDo updatedToDo = toDosStore.update(toDo);
                ToDoResponse response = new ToDoResponse(updatedToDo.getId(), updatedToDo.getTitle(), updatedToDo.getDescription(), updatedToDo.getCreatedAt(), updatedToDo.getTags());
                return Results.ok(Json.toJson(response));
            }
            catch (Exception e) {
                return Results.internalServerError(e.getMessage());
            }


        });
    }

    public Result deleteToDoById(Http.Request request, String toDoId) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var toDoObjectId = new org.bson.types.ObjectId(toDoId);
            MongoDbToDo toDo = toDosStore.getById(toDoObjectId);
            if (toDo == null) {
                return Results.notFound("To-Do not found");
            }

            if (!toDo.getUserId().equals(userId)) {
                return Results.forbidden("To-Do does not belong to user");
            }

            try {
                toDosStore.removeById(toDoObjectId);
                return Results.noContent();
            } catch (Exception e) {
                return Results.internalServerError("Error removing To-Do");
            }
        });
    }
}
