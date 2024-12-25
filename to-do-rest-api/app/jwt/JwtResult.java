package jwt;

import java.time.OffsetDateTime;
import java.util.Date;

public class JwtResult {
    public String Token;
    public Date Expires;
    public JwtResult(String Token, Date expiry) {
        this.Token = Token;
        this.Expires = expiry;
    }
}
