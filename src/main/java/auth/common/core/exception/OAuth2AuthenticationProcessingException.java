package auth.common.core.exception;

public class OAuth2AuthenticationProcessingException extends RuntimeException {
    private final String errorCode;
    private final String providerHint;

    public OAuth2AuthenticationProcessingException(String msg, Throwable t) {
        this(msg, t, null, null);
    }

    public OAuth2AuthenticationProcessingException(String msg) {
        this(msg, null, null, null);
    }

    public OAuth2AuthenticationProcessingException(String msg, String errorCode, String providerHint) {
        this(msg, null, errorCode, providerHint);
    }

    public OAuth2AuthenticationProcessingException(
            String msg,
            Throwable t,
            String errorCode,
            String providerHint
    ) {
        super(msg, t);
        this.errorCode = errorCode;
        this.providerHint = providerHint;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getProviderHint() {
        return providerHint;
    }
}
