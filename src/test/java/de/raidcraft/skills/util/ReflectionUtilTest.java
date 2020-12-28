package de.raidcraft.skills.util;

import io.artframework.Target;
import io.artframework.util.ReflectionUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionUtilTest {

    @Nested
    @DisplayName("getEntryForTarget(...)")
    @SuppressWarnings({"unchecked", "rawtypes"})
    class entryForTarget {

        private HashMap<Class<?>, Function> map;

        @BeforeEach
        void beforeEach() {
            map = new HashMap<>();
        }

        @Test
        @DisplayName("should return empty optional if not found")
        void shouldReturnNullIfNotFound() {

            Assertions.assertThat(io.artframework.util.ReflectionUtil.getEntryForTarget("foobar", new HashMap<>()))
                    .isEmpty();
        }

        @Test
        @DisplayName("should return direct class match first")
        void shouldReturnDirectMatchFirst() {

            map.put(MySuperTarget.class, o -> new MySuperTargetWrapper<>((MySuperTarget) o));
            map.put(MyTarget.class, o -> new MyTargetWrapper((MyTarget) o));

            MyTarget target = new MyTarget();
            Optional<Function> entryForTarget = io.artframework.util.ReflectionUtil.getEntryForTarget(target, map);
            assertThat(entryForTarget)
                    .isNotEmpty();
            assertThat(entryForTarget.get().apply(target))
                    .isInstanceOf(MyTargetWrapper.class);
        }

        @Test
        @DisplayName("should return super class match if direct match is not found")
        void shouldReturnSuperClassMatchIfDirectMatchIsNotFound() {

            map.put(MySuperTarget.class, o -> new MySuperTargetWrapper<>((MySuperTarget) o));

            MyTarget target = new MyTarget();
            Optional<Function> entryForTarget = io.artframework.util.ReflectionUtil.getEntryForTarget(target, map);
            assertThat(entryForTarget)
                    .isNotEmpty();
            assertThat(entryForTarget.get().apply(target))
                    .isInstanceOf(MySuperTargetWrapper.class);
        }

        @Test
        @DisplayName("should pick the nearest possible target wrapper")
        void shouldPickTheNearestPossibleWrapper() {

            map.put(MySuperTarget.class, o -> new MySuperTargetWrapper<>((MySuperTarget) o));
            map.put(MyTarget.class, o -> new MyTargetWrapper((MyTarget) o));

            MyLowTarget target = new MyLowTarget();
            Optional<Function> entryForTarget = ReflectionUtil.getEntryForTarget(target, map);

            assertThat(entryForTarget)
                    .isNotEmpty();
            assertThat(entryForTarget.get().apply(target))
                    .isInstanceOf(MyTargetWrapper.class);
        }

        class MySuperTarget {
        }

        class MyTarget extends MySuperTarget {
        }

        class MyLowTarget extends MyTarget {
        }

        class MySuperTargetWrapper<TTarget extends MySuperTarget> implements Target<TTarget> {

            private final TTarget target;

            MySuperTargetWrapper(TTarget target) {
                this.target = target;
            }

            @Override
            public String uniqueId() {
                return null;
            }

            @Override
            public TTarget source() {
                return target;
            }
        }

        class MyTargetWrapper extends MySuperTargetWrapper<MyTarget> {

            MyTargetWrapper(MyTarget target) {
                super(target);
            }
        }
    }

}