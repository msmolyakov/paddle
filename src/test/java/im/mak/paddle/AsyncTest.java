package im.mak.paddle;

import im.mak.paddle.util.Async;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncTest {

    @Test
    void asyncRan() {
        List<String> results = new ArrayList<>();

        Async.async(
                () -> {
                    try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                    results.add("Second!");
                },
                () -> {
                    try { Thread.sleep(5); } catch (InterruptedException ignored) {}
                    results.add("First!");
                }
        );

        assertThat(results).containsExactly("First!", "Second!");
    }

}
