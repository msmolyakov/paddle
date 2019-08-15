package im.mak.paddle;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class Async {

    public static void async(Runnable... actions) {
        CompletableFuture[] futures = new CompletableFuture[actions.length];

        IntStream.range(0, actions.length)
                .forEach(i ->
                        futures[i] = CompletableFuture.runAsync(actions[i])
                );

        CompletableFuture.allOf(futures).join();
    }

}
