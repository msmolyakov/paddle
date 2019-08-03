package im.mak.paddle;

public class Async {

    public static void async(Runnable... actions) {
        for (Runnable action : actions) {
            action.run();
        }
    }

}
