package im.mak.paddle.exceptions;

public class ApiError extends Error {

    public final int code;
    public final String message;

    public ApiError(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
