package ErrorHandlers;

import Contracts.Responses.ErrorResponse;
import CustomExceptions.*;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import play.Environment;
import play.Logger;
import play.http.HttpErrorHandler;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class CustomHttpErrorHandler implements HttpErrorHandler {
    private final Environment environment;

    @Inject
    public CustomHttpErrorHandler(Environment environment) {
        this.environment = environment;
    }

    @Override
    public CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
        Logger.error("Client Error: " + message);
        var errorResponse = Json.toJson(new ErrorResponse("Client Error", message, statusCode));
        return CompletableFuture.completedFuture(
                Results.status(statusCode, errorResponse)
                        .as(Http.MimeTypes.JSON)
        );
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
        Throwable actualException = unwrapException(exception);
        Logger.error("Server Error: ", actualException);
        int statusCode;
        String title;
        String message;

        if (actualException instanceof ResourceNotFoundException) {
            var e = (ResourceNotFoundException) exception;
            statusCode = e.getStatusCode();
            title = "Resource Not Found";
            message = environment.isProd() ? e.getProdMessage() : exception.getMessage();
        } else if (actualException instanceof InvalidRequestException) {
            var e = (InvalidRequestException) exception;
            statusCode = e.getStatusCode();
            title = "Invalid Request";
            message = environment.isProd() ? e.getProdMessage() : exception.getMessage();
        } else if (actualException instanceof FileProcessingException) {
            var e = (FileProcessingException) exception;
            statusCode = e.getStatusCode();
            title = "File Processing Error";
            message = environment.isProd() ? e.getProdMessage() : exception.getMessage();
        } else if (actualException instanceof DatabaseException) {
            var e = (DatabaseException) exception;
            statusCode = e.getStatusCode();
            title = "Database Error";
            message = environment.isProd() ? e.getProdMessage() : exception.getMessage();
        } else if (actualException instanceof ServiceUnavailableException) {
            var e = (ServiceUnavailableException) exception;
            statusCode = e.getStatusCode();
            title = "Service Unavailable";
            message = environment.isProd() ? e.getProdMessage() : exception.getMessage();
        }  else if (actualException instanceof ValidationException) {
            var e = (ValidationException) exception;
            statusCode = e.getStatusCode();
            title = "File Validation Error";
            message = environment.isProd() ? e.getProdMessage() : exception.getMessage();
        } else if (actualException instanceof MongoTimeoutException) {
            statusCode = Http.Status.SERVICE_UNAVAILABLE;
            title = "Database Timeout";
            message = environment.isProd() ? "Database is temporarily unavailable" : exception.getMessage();
        } else if (actualException instanceof MongoException) {
            statusCode = Http.Status.BAD_GATEWAY;
            title = "Database Error";
            message = environment.isProd() ? "Database error occurred" : exception.getMessage();
        } else {
            statusCode = Http.Status.INTERNAL_SERVER_ERROR;
            title = "Server Error";
            message = environment.isProd()
                    ? "An unexpected error occurred. Please try again later."
                    : exception.getMessage();
        }

        Logger.error("statusCode Error: " + statusCode);
        var errorResponse = Json.toJson(new ErrorResponse(title, message, statusCode));
        return CompletableFuture.completedFuture(
                Results.status(statusCode, errorResponse)
                        .as(Http.MimeTypes.JSON)
        );
    }

    private Throwable unwrapException(Throwable e) {
        if (e instanceof CompletionException) {
            return unwrapException(e.getCause());
        }
        if (e instanceof ExecutionException) {
            return unwrapException(e.getCause());
        }
        return e;
    }
}