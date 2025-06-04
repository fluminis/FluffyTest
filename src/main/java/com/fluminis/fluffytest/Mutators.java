package com.fluminis.fluffytest;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface Mutators {

    Pattern NUMBER = Pattern.compile("\\d+");

    /**
     * Replace all <code>placeholder</code>s by <code>newValue</code>.
     * <pre>{@code
     * // before
     * {
     *     "foo": "PLACEHOLDER",
     *     "bar": "PLACEHOLDER"
     * }
     *
     * //after
     * {
     *     "foo": "newValue",
     *     "bar": "newValue"
     * }
     * }</pre>
     */
    static Function<String, String> replaceAll(String placeholder, String newValue) {
        return str -> str.replaceAll(placeholder, newValue);
    }

    /**
     * Sets null a specific field or a complex JsonNode in the JSON structure using dot notation for nested fields.
     * <p>
     * Exemple 1:
     * <pre>{@code
     * {
     *     "foo": {
     *          "bar": "value"
     *     }
     * }
     * Foo content = read(..).mutate(setNull("foo.bar")).asObject(Foo.class);
     * assertThat(content.foo().bar()).isNull(); // true
     * }</pre>
     * <p>
     * Example 2:
     * <pre>{@code
     * {
     *     "foo": {
     *          "bar" : {
     *               "buzz": "value"
     *          }
     *     }
     * }
     * Foo content = read(..).mutate(setNull("foo.bar")).asObject(Foo.class);
     * assertThat(content.foo().bar()).isNull(); // true
     * }</pre>
     */
    static BiConsumer<JsonNode, ObjectMapper> setNull(String fieldName) {
        return (root, __) -> Mutators.setNull(root, fieldName, "");
    }

    /**
     * Sets a value for a specific field in the JSON structure using dot notation for nested fields.
     * <p>
     * The value can be:<ul>
     * <li>A simple type (String, Number, etc.)
     * <li>A complex object (will be converted to JSON using ObjectMapper)
     * </ul>
     * Example path: "foo.bar.buzz" requires that:<ul>
     * <li>'foo' exists in root
     * <li>'bar' exists in foo
     * <li>'buzz' will be created or updated
     * <p>
     * Examples:
     * <pre>{@code
     * // Set simple string value
     * setValue("foo.bar", "hello")
     *
     * // Set complex object
     * setValue("foo.bar", new Bar("some", "thing"))
     * }</pre>
     *
     * @throws IllegalArgumentException if parent fields don't exist
     */
    static BiConsumer<JsonNode, ObjectMapper> setValue(String fieldName, Object value) {
        return (root, objectMapper) -> Mutators.setValue(root, objectMapper, fieldName, value, "");
    }

    /**
     * Apply some mutators on a common field in the JSON structure using dot notation for nested fields.
     * <p>
     * Example fieldName: "foo.bar.buzz" requires that:<ul>
     * <li>'foo' exists in root
     * <li>'bar' exists in foo
     * <li>'buzz' exists in bar
     * <p>
     * Example:
     * <pre>{@code
     * // Set two attributes on the same node
     * setValue("foo.bar.buzz",
     *    setValue("attr1", "hello"),
     *    setValue("attr2", "goodbye")
     * )
     * }</pre>
     *
     * @throws IllegalArgumentException if parent fields don't exist
     */
    @SafeVarargs
    static BiConsumer<JsonNode, ObjectMapper> setValue(String fieldName, BiConsumer<JsonNode, ObjectMapper>... mutators) {
        return (root, objectMapper) -> {
            JsonNode commonNode = getDeepNode(root, fieldName, "");
            for (BiConsumer<JsonNode, ObjectMapper> mutator : mutators) {
                mutator.accept(commonNode, objectMapper);
            }
        };
    }

    /**
     * Copy a part of the JSON structure to another location
     * <p>
     * <pre>{@code
     * // to copy a element of a list:
     * copy("foo.bar.0", "foo.bar.1")
     *
     * // to copy a element to another
     * copy("foo.bar", "foo.baz")
     * }</pre>
     */
    static BiConsumer<JsonNode, ObjectMapper> copy(String sourceFieldName, String targetFieldName) {
        return (root, objectMapper) -> {
            JsonNode sourceNode = getDeepNode(root, sourceFieldName, "");
            String[] tokens = targetFieldName.split("\\.(?=[^.]*$)", 2);
            JsonNode targetParentNode = getDeepNode(root, tokens[0], "");
            setValue(targetParentNode, objectMapper, tokens[1], sourceNode.deepCopy(), targetFieldName);
        };
    }

    private static void setValue(JsonNode root, ObjectMapper objectMapper, String fieldName, Object value, String context) {
        if (fieldName.contains(".")) {
            String[] tokens = fieldName.split("\\.", 2);
            setValue(getNode(root, tokens[0], context), objectMapper, tokens[1], value, context + tokens[0] + ".");
            return;
        }
        if (root instanceof ArrayNode arrayNode) {
            if (!isNumeric(fieldName)) {
                throw new IllegalArgumentException(String.format("As %s is an array, '%s' should be a number", context, fieldName)
                                                   + "\n  " + context + fieldName
                                                   + "\n  " + " ".repeat(context.length()) + "^".repeat(fieldName.length()));
            }
            int index = Integer.parseInt(fieldName);
            if (index >= arrayNode.size()) {
                arrayNode.insert(index, objectMapper.valueToTree(value));
            } else {
                arrayNode.set(index, objectMapper.valueToTree(value));
            }
            return;
        }
        ((ObjectNode) root).set(fieldName, objectMapper.valueToTree(value));
    }

    private static JsonNode getDeepNode(JsonNode root, String fieldName, String context) {
        if (fieldName.contains(".")) {
            String[] tokens = fieldName.split("\\.", 2);
            return getDeepNode(getNode(root, tokens[0], context), tokens[1], context + tokens[0] + ".");
        }
        return getNode(root, fieldName, context);
    }

    private static JsonNode getNode(JsonNode root, String fieldName, String context) {
        JsonNode node = isNumeric(fieldName) ? root.get(Integer.parseInt(fieldName)) : root.get(fieldName);
        if (node == null) {
            throw new IllegalArgumentException(String.format("Could not find node %s in %s", fieldName, context)
                                               + "\n  " + context + fieldName
                                               + "\n  " + " ".repeat(context.length()) + "^".repeat(fieldName.length()));
        }
        return node;
    }

    private static boolean isNumeric(String strNum) {
        return NUMBER.matcher(strNum).matches();
    }

    private static void setNull(JsonNode root, String fieldName, String context) {
        if (fieldName.contains(".")) {
            String[] tokens = fieldName.split("\\.", 2);
            setNull(getNode(root, tokens[0], context), tokens[1], context + tokens[0] + ".");
        } else {
            ((ObjectNode) root).set(fieldName, NullNode.getInstance());
        }
    }
}
