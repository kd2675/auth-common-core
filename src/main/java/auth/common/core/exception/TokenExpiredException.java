package auth.common.core.exception;

public class TokenExpiredException extends AuthException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
