package com.fluminis.fluffytest;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.fluminis.fluffytest.TestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

class TestUtilsTest {

    @Nested
    class ReadAsString {
        @Test
        void shouldReadFileAsString() {
            String actual = read("in/small.json").asString();
            assertThat(actual).isEqualToIgnoringWhitespace("""
            {
              "foo": "Hello",
              "bar": "Guys"
            }""");
        }

        @Test
        void shouldReadFileAsStringAndMutateContent() {
            String actual = read("in/small.json")
                .mutate(str -> str.replaceAll("Hello", "Goodbye"))
                .asString();
            assertThat(actual).isEqualToIgnoringWhitespace("""
            {
              "foo": "Goodbye",
              "bar": "Guys"
            }""");
        }

        @Test
        void shouldReadFileAsStringAndMutateContentsMultipleTimes() {
            String actual = read("in/small.json")
                .mutate(str -> str.replaceAll("Hello", "Goodbye"))
                .mutate(str -> str.replaceAll("Guys", "Women"))
                .asString();
            assertThat(actual).isEqualToIgnoringWhitespace("""
            {
              "foo": "Goodbye",
              "bar": "Women"
            }""");
        }
    }

    @Nested
    class ReadAsObject {

        @Test
        void shouldReadFileAsObject() {
            Small actual = read("in/small.json").asObject(Small.class);
            assertThat(actual).isEqualTo(new Small("Hello", "Guys"));
        }

        @Test
        void shouldReadFileAsObjectAndMutateStringContent() {
            Small actual = read("in/small.json")
                .mutate(str -> str.replaceAll("Hello", "Goodbye"))
                .asObject(Small.class);
            assertThat(actual).isEqualTo(new Small("Goodbye", "Guys"));
        }

        @Test
        void shouldReadFileAsObjectAndMutateJsonNodeContent() {
            Small actual = read("in/small.json")
                .mutate(Mutators.setNull("foo"))
                .asObject(Small.class);
            assertThat(actual).isEqualTo(new Small(null, "Guys"));
        }

        @Test
        void shouldReadFileAsObjectAndMutateJsonNodeContentComplex() {
            Big actual = read("in/big.json")
                .mutate(Mutators.setNull("buzz.foo"))
                .asObject(Big.class);
            assertThat(actual).isEqualTo(new Big(new Small(null, "Guys")));
        }

        @Test
        void shouldReadFileAsListOfObjects() {
            List<Small> actual = read("in/list.json")
                .asObject(new TypeReference<>() {
                });
            assertThat(actual).containsExactly(new Small("Hello", "Guys"), new Small("Hello", "Women"));
        }

        @Test
        void shouldReadFileAsListOfObjectsAndMutateJsonNodeContent() {
            List<Small> actual = read("in/list.json")
                .mutate((root, objectMapper) -> {
                    ((ObjectNode) (root.get(0))).set("foo", new TextNode("Goodbye"));
                })
                .asObject(new TypeReference<>() {
                });
            assertThat(actual).containsExactly(new Small("Goodbye", "Guys"), new Small("Hello", "Women"));
        }

        @Test
        void shouldReadFileAsListOfObjectsAndMutateJsonNodeContentWithMutator() {
            List<Small> actual = read("in/list.json")
                .mutate(Mutators.setValue("0.foo", "Goodbye"))
                .mutate(Mutators.setValue("1.bar", "Child"))
                .asObject(new TypeReference<>() {
                });
            assertThat(actual).containsExactly(new Small("Goodbye", "Guys"), new Small("Hello", "Child"));
        }
    }

    @Nested
    class FromObject {

        @Test
        void shouldMutateRecords() {
            Big big = new Big(new Small("Hello", "Guys"));

            Big actual = TestUtils.from(big)
                .mutate(Mutators.setNull("buzz.foo"))
                .mutate(Mutators.setValue("buzz.bar", "Women"))
                .asObject(Big.class);

            assertThat(actual).isEqualTo(new Big(new Small(null, "Women")));
        }
    }

    record Big(Small buzz) {}

    record Small(String foo, String bar) {}
}