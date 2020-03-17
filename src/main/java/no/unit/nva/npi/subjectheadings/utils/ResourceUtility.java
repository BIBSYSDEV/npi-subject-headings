package no.unit.nva.npi.subjectheadings.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class ResourceUtility {

    /**
     * Get a classpath resource as a string.
     * @param resource The name of the resource to be fetched
     * @return A string of the contents of the resource
     * @throws IOException In case the resource cannot be fetched
     */
    public static String stringOf(String resource) throws IOException {
        String path = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource(resource))
                .getPath();
        return Files.readString(Paths.get(path));
    }
}
