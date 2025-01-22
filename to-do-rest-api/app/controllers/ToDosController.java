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
    private final HttpExecutionContext ec;
    private final ToDosService toDosService;
    private final FilesService filesService;
    private final Validator validator;


    @Inject
    public ToDosController(HttpExecutionContext ec, ToDosService toDosService, FilesService filesService, Validator validator) {
        this.ec = ec;
        this.toDosService = toDosService;
        this.filesService = filesService;
        this.validator = validator;
    }

    @Inject
    private MongoDb mongoDb;

    @Inject
    private JwtControllerHelper jwtControllerHelper;

    public CompletionStage<Result> listToDos(Http.Request request) {
        return supplyAsync(() -> Results.ok(Json.toJson(toDosService.getAll())), ec.current());
    }

    public Result listToDosByUser(Http.Request request, String username) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var response = toDosService.getToDosByUser(username, userId);
            return response.isPresent() ? Results.ok(Json.toJson(response.get())) : Results.notFound("User not found");
        });
    }

    public Result getToDoById(Http.Request request, String toDoId) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var response = toDosService.getToDoById(toDoId, userId);
            return response.isPresent() ? Results.ok(Json.toJson(response.get())) : Results.notFound("To-Do not found");
        });
    }

    public Result createToDo(Http.Request request) {
        Http.MultipartFormData<File> body = request.body().asMultipartFormData();
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            if (body == null) {
                return Results.badRequest("Request must be multipart/form-data");
            }

            String jsonPart = getJsonPart(body);
            if (jsonPart == null) {
                return Results.badRequest("Missing JSON part of the request");
            }

            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();

            long usedMemory = heapMemoryUsage.getUsed();
            long maxMemory = heapMemoryUsage.getMax();
            long committedMemory = heapMemoryUsage.getCommitted();

            long usedMemoryMB = usedMemory / (1024 * 1024);
            long maxMemoryMB = maxMemory / (1024 * 1024);
            long committedMemoryMB = committedMemory / (1024 * 1024);

            Logger.info("Memory usage: " + usedMemoryMB + " MB / " + maxMemoryMB + " MB / " + committedMemoryMB + " MB");

            try {
                var createToDoRequest = Json.fromJson(Json.parse(jsonPart), CreateToDoRequest.class);
                var violations = validator.validate(createToDoRequest);
                if (!violations.isEmpty()) {
                    throw new Exception(
                            violations.stream()
                                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                    .collect(Collectors.joining(", ")));
                }

                var response = toDosService.createToDo(createToDoRequest, userId, body.getFiles());
                Logger.info("To-Do created: " + response.getId());
                return Results.created(Json.toJson(response));
            } catch (IllegalArgumentException e) {
                Logger.error(e.getMessage());
                return Results.badRequest(e.getMessage());
            } catch (Exception e) {
                Logger.error(e.getMessage());
                return Results.internalServerError("Error creating To-Do");
            }
        });
    }

    public Result updateToDo(Http.Request request) {
        Http.MultipartFormData<File> body = request.body().asMultipartFormData();
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            if (body == null) {
                return Results.badRequest("Request must be multipart/form-data");
            }

            String jsonPart = getJsonPart(body);
            if (jsonPart == null) {
                return Results.badRequest("Missing JSON part of the request");
            }

            try {
                var updateToDoRequest = Json.fromJson(Json.parse(jsonPart), UpdateToDoRequest.class);
                var violations = validator.validate(updateToDoRequest);
                if (!violations.isEmpty()) {
                    throw new Exception(
                            violations.stream()
                                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                    .collect(Collectors.joining(", ")));
                }
                var response = toDosService.updateToDo(userId, updateToDoRequest, body.getFiles());
                Logger.info("To-Do updated: " + response.getId());
                return Results.ok(Json.toJson(response));
            }
            catch (IllegalArgumentException e) {
                Logger.error(e.getMessage());
                return Results.badRequest(e.getMessage());
            } catch (Exception e) {
                Logger.error(e.getMessage());
                return Results.internalServerError("Error updating To-Do");
            }
        });
    }

    public Result deleteToDoById(Http.Request request, String toDoId) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            try {
                toDosService.deleteToDoById(toDoId, userId);
                Logger.info("To-Do was deleted: " + toDoId);
                return Results.ok("Successfully removed To-Do");
            } catch (IllegalArgumentException e) {
                Logger.error(e.getMessage());
                return Results.notFound(e.getMessage());
            } catch (Exception e) {
                Logger.error(e.getMessage());
                return Results.internalServerError("Error removing To-Do");
            }
        });
    }

    public Result exportToDoById(Http.Request request, String toDoId) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            try {
                File zipFile = toDosService.exportToDoById(toDoId, userId);
                Logger.info("To-Do was exported: " + toDoId);
                return Results.ok(zipFile)
                        .as("application/zip")
                        .withHeader("Content-Disposition", "attachment; filename=" + zipFile.getName())
                        .withHeader("Access-Control-Expose-Headers", "Content-Disposition");
            } catch (IllegalArgumentException e) {
                Logger.error(e.getMessage());
                return Results.notFound(e.getMessage());
            } catch (Exception e) {
                Logger.error(e.getMessage());
                return Results.internalServerError("Error during export");
            }
        });
    }

    private String getJsonPart(Http.MultipartFormData<File> body) {
        String[] jsonParts = body.asFormUrlEncoded().get("data");
        return (jsonParts != null && jsonParts.length > 0) ? jsonParts[0] : null;
    }
}
