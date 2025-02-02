package controllers;

import Contracts.Requests.CreateToDoRequest;
import Contracts.Requests.UpdateToDoRequest;
import com.google.inject.Inject;
import jwt.JwtControllerHelper;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import services.FilesService;
import services.MongoDb;
import services.ToDosService;
import play.mvc.Result;
import play.mvc.Results;

import javax.validation.Validator;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;


public class ToDosController {
    private final ToDosService toDosService;
    private final JwtControllerHelper jwtControllerHelper;

    @Inject
    public ToDosController(ToDosService toDosService, JwtControllerHelper jwtControllerHelper) {
        this.toDosService = toDosService;
        this.jwtControllerHelper = jwtControllerHelper;
    }

    public Result listToDos(Http.Request request) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var todos = toDosService.getAllToDos();
            return Results.ok(Json.toJson(todos));
        });
    }

    public Result listToDosByUser(Http.Request request, String username) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var todos = toDosService.getToDosByUser(username, userId);
            return Results.ok(Json.toJson(todos));
        });
    }

    public Result getToDoById(Http.Request request, String toDoId) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var todo = toDosService.getToDoById(toDoId, userId);
            return Results.ok(Json.toJson(todo));
        });
    }

    public Result createToDo(Http.Request request) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var todo = toDosService.createToDo(request, userId);
            return Results.created(Json.toJson(todo));
        });
    }

    public Result updateToDo(Http.Request request) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var todo = toDosService.updateToDo(request, userId);
            return Results.ok(Json.toJson(todo));
        });
    }

    public Result deleteToDoById(Http.Request request, String toDoId) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            toDosService.deleteToDoById(toDoId, userId);
            return Results.ok("Successfully removed To-Do");
        });
    }

    public Result exportToDoById(Http.Request request, String toDoId) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            File zipFile = toDosService.exportToDoById(toDoId, userId);
            return Results.ok(zipFile)
                    .as("application/zip")
                    .withHeader("Content-Disposition", "attachment; filename=" + zipFile.getName())
                    .withHeader("Access-Control-Expose-Headers", "Content-Disposition");
        });
    }
}