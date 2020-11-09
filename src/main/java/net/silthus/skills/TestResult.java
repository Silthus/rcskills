package net.silthus.skills;

import lombok.Value;
import lombok.experimental.Accessors;
import org.apache.commons.lang.ArrayUtils;

@Value
@Accessors(fluent = true)
public class TestResult {

    public static TestResult ofSuccess() {

        return new TestResult(true, new String[0]);
    }

    public static TestResult ofError(String errorMessage) {

        return new TestResult(false, errorMessage);
    }

    public static TestResult of(boolean result) {

        return of(result, null);
    }

    public static TestResult of(boolean result, String errorMessage) {

        return new TestResult(result, errorMessage);
    }

    boolean success;
    String[] errorMessages;

    TestResult(boolean success, String errorMessages) {
        this.success = success;
        this.errorMessages = new String[] {errorMessages};
    }

    private TestResult(boolean success, String[] errorMessages) {
        this.success = success;
        this.errorMessages = errorMessages;
    }


    public TestResult merge(TestResult result) {

        return new TestResult(success && result.success, (String[]) ArrayUtils.addAll(errorMessages, result.errorMessages));
    }
}
