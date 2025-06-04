package com.fluminis.fluffytest;

import org.packagesettings.PackageLevelSettings;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectReader implements Reader {

    private final Object object;
    private String content;
    private JsonNode root;
    private ObjectMapper objectMapper;

    ObjectReader(Object object) {
        this.object = object;
        this.objectMapper = PackageLevelSettings.getValueFor(FluffyTestPackageSettings.OBJECT_MAPPER, TestUtils::createObjectMapper);
    }

    public ObjectReader withObjectMapper(ObjectMapper mapper) {
        this.objectMapper = mapper;
        return this;
    }

    public ObjectReader mutate(Function<String, String> mutator) {
        this.content = mutator.apply(asString());
        return this;
    }

    public ObjectReader mutate(BiConsumer<JsonNode, ObjectMapper> mutator) {
        mutator.accept(asJsonNode(), objectMapper);
        return this;
    }

    public String asString() {
        if (content == null) {
            try {
                content = objectMapper.writeValueAsString(object);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Could serialize object: %s", object), e);
            }
        }
        return content;
    }

    public <T> T asObject(Class<T> clazz) {
        return objectMapper.convertValue(asJsonNode(), clazz);
    }

    public <T> T asObject(TypeReference<T> typeReference) {
        return objectMapper.convertValue(asJsonNode(), typeReference);
    }

    public <T extends JsonNode> T asJsonNode() {
        if (root == null) {
            if (content == null) {
                root = objectMapper.valueToTree(object);
            } else {
                try {
                    root = objectMapper.readTree(asString());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return (T) root;
    }

}
