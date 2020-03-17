package no.unit.nva.npi.subjectheadings;


import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class EnvironmentTest {

    public static final String TEST_ENV = "TEST";
    public static final String TEST_VAL = "test";

    @Test
    public void testEnv() throws Exception {
        withEnvironmentVariable(TEST_ENV, TEST_VAL)
                .execute(() -> {
                    Environment environment = new Environment();
                    Optional<String> test = environment.get(TEST_ENV);
                    assertEquals(TEST_VAL, test.orElse(null));
                });
    }

    @Test
    public void testNoEnv() {
        Environment environment = new Environment();
        Optional<String> test = environment.get(TEST_ENV);
        assertFalse(test.isPresent());
    }

}
