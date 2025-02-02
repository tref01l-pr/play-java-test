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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class CustomHttpErrorHandler implements HttpErrorHandler {
    private static final Logger.ALogger logger = Logger.of(CustomHttpErrorHandler.class);
    private final Environment environment;
    private final Map<Class<? extends Throwable>, ErrorHandler> errorHandlers;

    @Inject
    public CustomHttpErrorHandler(Environment environment) {
        this.environment = environment;
        this.errorHandlers = initializeErrorHandlers();
    }

    @Override
    public CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
        logger.error("Client Error: {} - {}", statusCode, message);
        return createErrorResponse("Client Error", message, statusCode);
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
        Throwable actualException = unwrapException(exception);
        logger.error("Server Error: ", actualException);

        ErrorHandler errorHandler = errorHandlers.getOrDefault(
                actualException.getClass(),
                createDefaultErrorHandler()
        );

        ErrorDetails errorDetails = errorHandler.handle(actualException);
        logger.error("Status Code: {}", errorDetails.statusCode());

        return createErrorResponse(
                errorDetails.title(),
                getAppropriateMessage(errorDetails.message(), actualException),
                errorDetails.statusCode()
        );
    }

    private Map<Class<? extends Throwable>, ErrorHandler> initializeErrorHandlers() {
        Map<Class<? extends Throwable>, ErrorHandler> handlers = new HashMap<>();

        handlers.put(ResourceNotFoundException.class,
                ex -> new ErrorDetails("Resource Not Found", ex.getMessage(), ((ResourceNotFoundException) ex).getStatusCode()));

        handlers.put(InvalidRequestException.class,
                ex -> new ErrorDetails("Invalid Request", ex.getMessage(), ((InvalidRequestException) ex).getStatusCode()));

        handlers.put(FileProcessingException.class,
                ex -> new ErrorDetails("File Processing Error", ex.getMessage(), ((FileProcessingException) ex).getStatusCode()));

        handlers.put(DatabaseException.class,
                ex -> new ErrorDetails("Database Error", ex.getMessage(), ((DatabaseException) ex).getStatusCode()));

        handlers.put(ServiceUnavailableException.class,
                ex -> new ErrorDetails("Service Unavailable", ex.getMessage(), ((ServiceUnavailableException) ex).getStatusCode()));

        handlers.put(ValidationException.class,
                ex -> new ErrorDetails("File Validation Error", ex.getMessage(), ((ValidationException) ex).getStatusCode()));

        handlers.put(TokenGenerationException.class,
                ex -> new ErrorDetails("Authentication Error", ex.getMessage(), ((TokenGenerationException) ex).getStatusCode()));

        handlers.put(MongoTimeoutException.class,
                ex -> new ErrorDetails("Database Timeout", "Database is temporarily unavailable", Http.Status.SERVICE_UNAVAILABLE));

        handlers.put(MongoException.class,
                ex -> new ErrorDetails("Database Error", "Database error occurred", Http.Status.BAD_GATEWAY));

        return handlers;
    }

    private ErrorHandler createDefaultErrorHandler() {
        return ex -> new ErrorDetails(
                "Server Error",
                "An unexpected error occurred. Please try again later.",
                Http.Status.INTERNAL_SERVER_ERROR
        );
    }

    private String getAppropriateMessage(String defaultMessage, Throwable exception) {
        if (environment.isProd()) {
            if (exception instanceof BaseCustomException) {
                return ((BaseCustomException) exception).getProdMessage();
            }
            return defaultMessage;
        }
        return exception.getMessage();
    }

    private CompletionStage<Result> createErrorResponse(String title, String message, int statusCode) {
        var errorResponse = Json.toJson(new ErrorResponse(title, message, statusCode));
        return CompletableFuture.completedFuture(
                Results.status(statusCode, errorResponse)
                        .as(Http.MimeTypes.JSON)
        );
    }

    private Throwable unwrapException(Throwable e) {
        if (e instanceof CompletionException || e instanceof ExecutionException) {
            return e.getCause() != null ? unwrapException(e.getCause()) : e;
        }
        return e;
    }
}

record ErrorDetails(String title, String message, int statusCode) {}

@FunctionalInterface
interface ErrorHandler {
    ErrorDetails handle(Throwable exception);
}