package ErrorHandlers;

import Contracts.Responses.ErrorResponse;
import play.Environment;
import play.Logger;
import play.http.HttpErrorHandler;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CustomHttpErrorHandler implements HttpErrorHandler {
    private final Environment environment;

    @Inject
    public CustomHttpErrorHandler(Environment environment) {
        this.environment = environment;
    }

    @Override
    public CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
        var errorResponse = Json.toJson(new ErrorResponse("Client Error", message, statusCode));
        Logger.error("Client Error: " + message);
        return CompletableFuture.completedFuture(
                Results.status(statusCode, errorResponse)
                        .as(Http.MimeTypes.JSON)
        );
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
        int statusCode = Http.Status.INTERNAL_SERVER_ERROR;
        Logger.error("Server Error: " + exception.getMessage());
        String message = environment.isProd()
                ? "An unexpected error occurred. Please try again later."
                : exception.getMessage();
        var errorResponse = Json.toJson(new ErrorResponse("Server Error", message, statusCode));
        return CompletableFuture.completedFuture(
                Results.status(statusCode, errorResponse)
                        .as(Http.MimeTypes.JSON)
        );
    }
}
