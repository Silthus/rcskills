package net.silthus.skills.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigUtilTest {

    @Nested
    @DisplayName("getFileIdentifier(...)")
    class getFileIdentifier {

        @Test
        @DisplayName("should return correct path")
        void shouldReturnCorrectPath(@TempDir File parent) {

            String result = ConfigUtil.getFileIdentifier(parent.toPath(), new File(new File(parent, "foo"), "foobar.yml"));
            assertThat(result).isEqualTo("foo.foobar");
        }
    }
}