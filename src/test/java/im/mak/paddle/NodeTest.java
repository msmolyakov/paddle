package im.mak.paddle;

import com.wavesplatform.wavesj.ScriptInfo;
import org.junit.jupiter.api.*;
import org.apache.maven.artifact.versioning.ComparableVersion;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static im.mak.paddle.token.Waves.WAVES;

class NodeTest {

    @Nested
    class WaitForHeight {

        @Test
        void canWaitForHeight() {
            int current = node().getHeight();

            int height = node().waitForHeight(current + 1);

            assertThat(height).isGreaterThanOrEqualTo(current + 1);
        }

        @Test
        void errorIfCantReachTheHeight() {
            int current = node().getHeight();

            assertThat(
                    assertThrows(RuntimeException.class, () ->
                            node().waitForHeight(current + 100, 1))
            ).hasMessageEndingWith(
                    "Could not wait for the height to rise from " + current + " to " + (current + 100) +
                            ": height " + current + " did not grow for 1 seconds");
        }

    }

    @Nested
    class WaitNBlocks {

        @Test
        void canWaitNBlocks() {
            int current = node().getHeight();

            int height = node().waitBlocks(1);

            assertThat(height).isGreaterThanOrEqualTo(current + 1);
        }

        @Test
        void errorIfCantWaitNBlocks() {
            int current = node().getHeight();

            assertThat(
                    assertThrows(RuntimeException.class, () ->
                            node().waitBlocks(100, 1))
            ).hasMessageEndingWith(
                    "Could not wait for the height to rise from " + current + " to " + (current + 100) +
                            ": height " + current + " did not grow for 1 seconds");
        }

    }

    @Nested
    class CompileScript {
        private final String INITIAL_SCRIPT = fromFile("wallet.ride");
        private final String INITIAL_BIG_SCRIPT = fromFile("bigScript.ride");
        private final String ERROR_SCRIPT = fromFile("wallet.ride").replace("i.payment", "v.payment");
        private final String BASE_64_SCRIPT = "AAIDAAAAAAAAAAkIARIAEgMKAQEAAAAAAAAAAgAAAAFpAQAAAAdkZXBvc2l0AAAAAAQAAAADcG10CQEAAAAHZXh0cmFjdAAAAAEIBQAAAAFpAAAAB3BheW1lbnQDCQEAAAAJaXNEZWZpbmVkAAAAAQgFAAAAA3BtdAAAAAdhc3NldElkCQAAAgAAAAECAAAAIWNhbiBob2RsIHdhdmVzIG9ubHkgYXQgdGhlIG1vbWVudAQAAAAKY3VycmVudEtleQkAAlgAAAABCAgFAAAAAWkAAAAGY2FsbGVyAAAABWJ5dGVzBAAAAA1jdXJyZW50QW1vdW50BAAAAAckbWF0Y2gwCQAEGgAAAAIFAAAABHRoaXMFAAAACmN1cnJlbnRLZXkDCQAAAQAAAAIFAAAAByRtYXRjaDACAAAAA0ludAQAAAABYQUAAAAHJG1hdGNoMAUAAAABYQAAAAAAAAAAAAQAAAAJbmV3QW1vdW50CQAAZAAAAAIFAAAADWN1cnJlbnRBbW91bnQIBQAAAANwbXQAAAAGYW1vdW50CQEAAAAIV3JpdGVTZXQAAAABCQAETAAAAAIJAQAAAAlEYXRhRW50cnkAAAACBQAAAApjdXJyZW50S2V5BQAAAAluZXdBbW91bnQFAAAAA25pbAAAAAFpAQAAAAh3aXRoZHJhdwAAAAEAAAAGYW1vdW50BAAAAApjdXJyZW50S2V5CQACWAAAAAEICAUAAAABaQAAAAZjYWxsZXIAAAAFYnl0ZXMEAAAADWN1cnJlbnRBbW91bnQEAAAAByRtYXRjaDAJAAQaAAAAAgUAAAAEdGhpcwUAAAAKY3VycmVudEtleQMJAAABAAAAAgUAAAAHJG1hdGNoMAIAAAADSW50BAAAAAFhBQAAAAckbWF0Y2gwBQAAAAFhAAAAAAAAAAAABAAAAAluZXdBbW91bnQJAABlAAAAAgUAAAANY3VycmVudEFtb3VudAUAAAAGYW1vdW50AwkAAGYAAAACAAAAAAAAAAAABQAAAAZhbW91bnQJAAACAAAAAQIAAAAeQ2FuJ3Qgd2l0aGRyYXcgbmVnYXRpdmUgYW1vdW50AwkAAGYAAAACAAAAAAAAAAAABQAAAAluZXdBbW91bnQJAAACAAAAAQIAAAASTm90IGVub3VnaCBiYWxhbmNlCQEAAAAMU2NyaXB0UmVzdWx0AAAAAgkBAAAACFdyaXRlU2V0AAAAAQkABEwAAAACCQEAAAAJRGF0YUVudHJ5AAAAAgUAAAAKY3VycmVudEtleQUAAAAJbmV3QW1vdW50BQAAAANuaWwJAQAAAAtUcmFuc2ZlclNldAAAAAEJAARMAAAAAgkBAAAADlNjcmlwdFRyYW5zZmVyAAAAAwgFAAAAAWkAAAAGY2FsbGVyBQAAAAZhbW91bnQFAAAABHVuaXQFAAAAA25pbAAAAAEAAAACdHgBAAAABnZlcmlmeQAAAAAGSVTa3w==";
        private final String BASE_64_COMPACTED_SCRIPT = "AAIDAAAAAAAAAIkIARIAEgMKAQEaBgoBYRIBaRoICgFiEgNwbXQaDwoBYxIKY3VycmVudEtleRoSCgFkEg1jdXJyZW50QW1vdW50GgwKAWUSByRtYXRjaDAaBgoBZhIBYRoOCgFnEgluZXdBbW91bnQaCwoBaBIGYW1vdW50GgcKAWkSAnR4GgsKAWoSBnZlcmlmeQAAAAAAAAACAAAAAWEBAAAAB2RlcG9zaXQAAAAABAAAAAFiCQEAAAAHZXh0cmFjdAAAAAEIBQAAAAFhAAAAB3BheW1lbnQDCQEAAAAJaXNEZWZpbmVkAAAAAQgFAAAAAWIAAAAHYXNzZXRJZAkAAAIAAAABAgAAACFjYW4gaG9kbCB3YXZlcyBvbmx5IGF0IHRoZSBtb21lbnQEAAAAAWMJAAJYAAAAAQgIBQAAAAFhAAAABmNhbGxlcgAAAAVieXRlcwQAAAABZAQAAAABZQkABBoAAAACBQAAAAR0aGlzBQAAAAFjAwkAAAEAAAACBQAAAAFlAgAAAANJbnQEAAAAAWYFAAAAAWUFAAAAAWYAAAAAAAAAAAAEAAAAAWcJAABkAAAAAgUAAAABZAgFAAAAAWIAAAAGYW1vdW50CQEAAAAIV3JpdGVTZXQAAAABCQAETAAAAAIJAQAAAAlEYXRhRW50cnkAAAACBQAAAAFjBQAAAAFnBQAAAANuaWwAAAABYQEAAAAId2l0aGRyYXcAAAABAAAAAWgEAAAAAWMJAAJYAAAAAQgIBQAAAAFhAAAABmNhbGxlcgAAAAVieXRlcwQAAAABZAQAAAABZQkABBoAAAACBQAAAAR0aGlzBQAAAAFjAwkAAAEAAAACBQAAAAFlAgAAAANJbnQEAAAAAWYFAAAAAWUFAAAAAWYAAAAAAAAAAAAEAAAAAWcJAABlAAAAAgUAAAABZAUAAAABaAMJAABmAAAAAgAAAAAAAAAAAAUAAAABaAkAAAIAAAABAgAAAB5DYW4ndCB3aXRoZHJhdyBuZWdhdGl2ZSBhbW91bnQDCQAAZgAAAAIAAAAAAAAAAAAFAAAAAWcJAAACAAAAAQIAAAASTm90IGVub3VnaCBiYWxhbmNlCQEAAAAMU2NyaXB0UmVzdWx0AAAAAgkBAAAACFdyaXRlU2V0AAAAAQkABEwAAAACCQEAAAAJRGF0YUVudHJ5AAAAAgUAAAABYwUAAAABZwUAAAADbmlsCQEAAAALVHJhbnNmZXJTZXQAAAABCQAETAAAAAIJAQAAAA5TY3JpcHRUcmFuc2ZlcgAAAAMIBQAAAAFhAAAABmNhbGxlcgUAAAABaAUAAAAEdW5pdAUAAAADbmlsAAAAAQAAAAFpAQAAAAFqAAAAAAZ4X1/l";
        private final String BASE_64_COMPACTED_SCRIPT_NODEv4 = "AAIDAAAAAAAAAFcIARIAEgMKAQEiAWkiA3BtdCIKY3VycmVudEtleSINY3VycmVudEFtb3VudCIHJG1hdGNoMCIBYSIJbmV3QW1vdW50IgZhbW91bnQiAnR4IgZ2ZXJpZnkAAAAAAAAAAgAAAAFhAQAAAAdkZXBvc2l0AAAAAAQAAAABYgkBAAAAB2V4dHJhY3QAAAABCAUAAAABYQAAAAdwYXltZW50AwkBAAAACWlzRGVmaW5lZAAAAAEIBQAAAAFiAAAAB2Fzc2V0SWQJAAACAAAAAQIAAAAhY2FuIGhvZGwgd2F2ZXMgb25seSBhdCB0aGUgbW9tZW50BAAAAAFjCQACWAAAAAEICAUAAAABYQAAAAZjYWxsZXIAAAAFYnl0ZXMEAAAAAWQEAAAAAWUJAAQaAAAAAgUAAAAEdGhpcwUAAAABYwMJAAABAAAAAgUAAAABZQIAAAADSW50BAAAAAFmBQAAAAFlBQAAAAFmAAAAAAAAAAAABAAAAAFnCQAAZAAAAAIFAAAAAWQIBQAAAAFiAAAABmFtb3VudAkBAAAACFdyaXRlU2V0AAAAAQkABEwAAAACCQEAAAAJRGF0YUVudHJ5AAAAAgUAAAABYwUAAAABZwUAAAADbmlsAAAAAWEBAAAACHdpdGhkcmF3AAAAAQAAAAFoBAAAAAFjCQACWAAAAAEICAUAAAABYQAAAAZjYWxsZXIAAAAFYnl0ZXMEAAAAAWQEAAAAAWUJAAQaAAAAAgUAAAAEdGhpcwUAAAABYwMJAAABAAAAAgUAAAABZQIAAAADSW50BAAAAAFmBQAAAAFlBQAAAAFmAAAAAAAAAAAABAAAAAFnCQAAZQAAAAIFAAAAAWQFAAAAAWgDCQAAZgAAAAIAAAAAAAAAAAAFAAAAAWgJAAACAAAAAQIAAAAeQ2FuJ3Qgd2l0aGRyYXcgbmVnYXRpdmUgYW1vdW50AwkAAGYAAAACAAAAAAAAAAAABQAAAAFnCQAAAgAAAAECAAAAEk5vdCBlbm91Z2ggYmFsYW5jZQkBAAAADFNjcmlwdFJlc3VsdAAAAAIJAQAAAAhXcml0ZVNldAAAAAEJAARMAAAAAgkBAAAACURhdGFFbnRyeQAAAAIFAAAAAWMFAAAAAWcFAAAAA25pbAkBAAAAC1RyYW5zZmVyU2V0AAAAAQkABEwAAAACCQEAAAAOU2NyaXB0VHJhbnNmZXIAAAADCAUAAAABYQAAAAZjYWxsZXIFAAAAAWgFAAAABHVuaXQFAAAAA25pbAAAAAEAAAABaQEAAAABagAAAAAGy1msfg==";

        @Test
        void canCompileScript() {
            ScriptInfo scriptInfo = node().compileScript(INITIAL_SCRIPT);
            assertThat(scriptInfo.script().toString()).isEqualTo(BASE_64_SCRIPT);
        }

        @Test
        void canCompileScriptCompacted() {
            ScriptInfo scriptInfoCompacted = node().compileScript(INITIAL_SCRIPT, true);
            ComparableVersion version1_4 = new ComparableVersion("Waves v1.4");
            ComparableVersion nodeVersion = new ComparableVersion(node().getVersion());
            if(nodeVersion.compareTo(version1_4) >= 0) {
                assertThat(scriptInfoCompacted.script().toString()).isEqualTo(BASE_64_COMPACTED_SCRIPT_NODEv4);
            } else {
                assertThat(scriptInfoCompacted.script().toString()).isEqualTo(BASE_64_COMPACTED_SCRIPT);
            }
        }
        @Test
        void canSetScriptNodeV4DynamicFee() {
            ComparableVersion version1_4 = new ComparableVersion("Waves v1.4");
            ComparableVersion nodeVersion = new ComparableVersion(node().getVersion());
            if(nodeVersion.compareTo(version1_4) >= 0) {
                Account account = new Account(WAVES.amount(1));
                account.setScript(INITIAL_BIG_SCRIPT, true);
                assertThat(account.getScriptInfo().script().toString())
                        .isEqualTo(node().compileScript(INITIAL_BIG_SCRIPT, true).script().toString());
            }
        }

        @Test
        void cantCompileScriptWithError() {
            assertThat(
                    assertThrows(RuntimeException.class, () ->
                            node().compileScript(ERROR_SCRIPT, true))
            ).hasMessageContaining(
                    " A definition of 'v' is not found");
        }

    }

}
