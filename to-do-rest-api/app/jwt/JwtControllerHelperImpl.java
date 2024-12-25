package jwt;

import org.bson.types.ObjectId;
import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Function;

import static play.mvc.Results.forbidden;
import static play.mvc.Results.unauthorized;

public class JwtControllerHelperImpl implements JwtControllerHelper {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String ERR_AUTHORIZATION_HEADER = "ERR_AUTHORIZATION_HEADER";
    private JwtValidator jwtValidator;

    @Inject
    public JwtControllerHelperImpl(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Result verify(Http.Request request, Function<F.Either<JwtValidator.Error, VerifiedJwt>, Result> f) {
        Optional<String> authHeader =  request.getHeaders().get(HEADER_AUTHORIZATION);

        if (!authHeader.filter(ah -> ah.contains(BEARER)).isPresent()) {
            Logger.error("f=JwtControllerHelperImpl, event=verify, error=authHeaderNotPresent");
            return unauthorized(ERR_AUTHORIZATION_HEADER);
        }

        String token = authHeader.map(ah -> ah.replace(BEARER, "")).orElse("");

        return f.apply(jwtValidator.verify(token));
    }

    @Override
    public Result withAuthenticatedUser(Http.Request request, Function<ObjectId, Result> action) {
        return verify(request, res -> {
            if (res.left.isPresent()) {
                return forbidden(res.left.get().toString());
            }

            VerifiedJwt verifiedJwt = res.right.get();
            ObjectId userId = verifiedJwt.getUserId();

            if (userId == null) {
                return forbidden("User ID not found in token");
            }

            return action.apply(userId);
        });
    }
}
