package im.mak.paddle.exceptions;

public class NodeError extends RuntimeException {

    public NodeError(Throwable e) {
        super(e);
    }

    public NodeError(String message) {
        super(message);
    }

}
