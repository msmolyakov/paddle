package im.mak.paddle.exceptions;

public class ApiError extends Error {

    public final int code;

    public ApiError(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getErrorCode() {
        return this.code;
    }
}
