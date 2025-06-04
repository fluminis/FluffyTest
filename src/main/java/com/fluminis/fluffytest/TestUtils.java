package com.fluminis.fluffytest;

import org.openapitools.jackson.nullable.JsonNullableModule;
import org.packagesettings.PackageLevelSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class TestUtils {

    private TestUtils() {
    }

    /**
     * @return A new ObjectMapper configured with relatively standard features and JavaTimeModule.
     */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new JsonNullableModule());
        return objectMapper;
    }

    /**
     * Read the content of a file located in the src/test/resources folder and returned a Reader to manipulate it.
     * <pre>{@code
     * String content = read("in/myfile.json").asString(); // will return the content of src/test/resources/in/myfile.json
     * }</pre>
     * <pre>{@code
     * Foo content = read("in/myfile.json").asObject(Foo.class); // will unmarshall the content of src/test/resources/in/myfile.json as Foo object
     * }</pre>
     */
    public static Reader read(String path) {
        try {
            String resourceFolder = PackageLevelSettings
                    .getValueFor(FluffyTestPackageSettings.RESSOURCE_FOLDER, () -> "")
                    .replaceAll("[/\\\\]", Matcher.quoteReplacement(File.separator));

            String fullPath = resourceFolder.isEmpty()
                    ? path
                    : resourceFolder + File.separator + path;

            URL resourceUrl = TestUtils.class.getClassLoader().getResource(fullPath);
            if (resourceUrl == null) {
                throw new RuntimeException("Could not read " + path);
            }

            return new FileReader(Paths.get(resourceUrl.toURI()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the content of a file located at the given Path and returned a Reader to manipulate it.
     * <pre>{@code
     * String content = read(Path.of(...)).asString(); // will return the content of the file
     * }</pre>
     * <pre>{@code
     * Foo content = read(Path.of(...)).asObject(Foo.class); // will unmarshall the content of the file as Foo object
     * }</pre>
     */
    public static Reader read(Path path) {
        return new FileReader(path);
    }

    /**
     * Useful helper method to mutate an arbitrary Object (like a record, for example). Returns a Reader to manipulate it.
     * <pre>{@code
     * MyClass derived = from(myObject).mutate(...).asObject(MyClass.class);
     * }</pre>
     */
    public static Reader from(Object object) {
        return new ObjectReader(object);
    }

    /**
     * Useful helper method to read a file and unmarshall it to the given class.
     * <pre>{@code
     * Foo content = readObject("in/myfile.json", Foo.class);
     * }</pre>
     * witch is exactly the same as
     * <pre>{@code
     * Foo content = read("in/myfile.json").asObject(Foo.class);
     * }</pre>
     */
    public static <T> T readObject(String file, Class<T> clazz) {
        return read(file).asObject(clazz);
    }

    /**
     * Useful helper method to read a file and unmarshall it to the given class.
     * <pre>{@code
     * List<Foo> content = readObject("in/myfile.json", new TypeReference<List<Foo>>() {
     *         }));
     * }</pre>
     * witch is exactly the same as
     * <pre>{@code
     * List<Foo> content = read("in/myfile.json").asObject(new TypeReference<List<Foo>>() {
     *         });
     * }</pre>
     */
    public static <T> T readObject(String file, TypeReference<T> clazz) {
        return read(file).asObject(clazz);
    }

    public static Instant utcTime(int year, int month, int dayOfMonth, int hour, int minute) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute).toInstant(ZoneOffset.UTC);
    }
}
