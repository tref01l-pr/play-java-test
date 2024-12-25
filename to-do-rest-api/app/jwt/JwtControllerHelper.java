package jwt;

import org.bson.types.ObjectId;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.function.Function;

public interface JwtControllerHelper {
    Result verify(Http.Request request, Function<F.Either<JwtValidator.Error, VerifiedJwt>, Result> f);
    Result withAuthenticatedUser(Http.Request request, Function<ObjectId, Result> action);
}

