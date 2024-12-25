package utils;

import play.mvc.Http;

public class InputUtils {
    public static String getRefreshTokenFromCookies(Http.Request request) {
        Http.Cookie refreshTokenCookie = request.cookies().get("refreshToken").orElse(null);
        return refreshTokenCookie == null ? null : refreshTokenCookie.value();
    }

    public static String trimToNull(String string) {
        if (string == null) {
            return null;
        }
        string = string.trim();
        if (string.isEmpty()) {
            return null;
        }
        return string;
    }
}
