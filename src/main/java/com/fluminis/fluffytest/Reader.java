package com.fluminis.fluffytest;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface Reader {

    /**
     * Specify which ObjectMapper to use for the unmarshalling
     */
    Reader withObjectMapper(ObjectMapper mapper);

    /**
     * Transforms the string content of the file using a given function.
     * The file is loaded into memory if not already done.
     * <p>
     * Multiple transformations can be chained.
     * The original file remains unchanged.
     * <p>
     * Example:
     * <pre>{@code
     * {
     *     "foo": "PLACEHOLDER"
     * }
     * }</pre>
     * <pre>{@code
     * Foo content = read("in/myfile.json")
     *     .mutate(replaceAll("PLACEHOLDER", "hello"))
     *     .asObject(Foo.class);
     * // content.foo() == "hello"
     * }</pre>
     * <p>
     * See: {@link Mutators#replaceAll(String, String)}
     */
    Reader mutate(Function<String, String> mutator);

    /**
     * Apply some mutators.
     * See: {@link Reader#mutate(Function)}
     */
    default Reader mutate(Function<String, String>... mutators) {
        for (var mutator : mutators) {
            mutate(mutator);
        }
        return this;
    }

    /**
     * Modifies the JSON content by transforming its tree structure.
     * <p>
     * The content will be loaded as a JsonNode tree using either:<br>
     * - The ObjectMapper provided via withObjectMapper()<br>
     * - Or the default ObjectMapper if none was set
     * <p>
     * Key features:<br>
     * - Can chain multiple transformations<br>
     * - Works with content already loaded from previous operations<br>
     * - Calls to {@link #mutate(Function)} could have been made before the first call to this method but not after.
     * - Does not modify the original file
     * <p>
     * Example usage:
     * <pre>{@code
     * // Input JSON:
     * {
     *     "foo": "PLACEHOLDER"
     * }
     *
     * // Code:
     * Foo content = read("in/myfile.json")
     *     .mutate(setValue("foo", "hello"))
     *     .asObject(Foo.class);
     * // Result: content.foo() == "hello"
     * }</pre>
     *
     * @see Mutators#setValue(String, Object)
     */
    Reader mutate(BiConsumer<JsonNode, ObjectMapper> mutator);

    /**
     * Apply some mutators.
     * See: {@link Reader#mutate(BiConsumer)}
     */
    default Reader mutate(BiConsumer<JsonNode, ObjectMapper>... mutators) {
        for (var mutator : mutators) {
            mutate(mutator);
        }
        return this;
    }

    /**
     * Returns the current String representation of the file.
     * If the file has not been read yet. It will be loaded in memory at this time.
     */
    String asString();

    /**
     * Unmarshall the current String or JsonNode tree as a given clazz object using ObjectMapper.
     */
    <T> T asObject(Class<T> clazz);

    /**
     * Unmarshall the current String or JsonNode tree as a given clazz object using ObjectMapper.
     */
    <T> T asObject(TypeReference<T> typeReference);

    /**
     * Returns the current JsonNode tree.
     */
    <T extends JsonNode> T asJsonNode();
}
