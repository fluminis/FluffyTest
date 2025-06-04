package com.fluminis.fluffytest;

import org.packagesettings.PackageLevelSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileReader implements Reader {

    private final Path path;
    private String content;
    private JsonNode root;
    private ObjectMapper objectMapper;

    FileReader(Path path) {
        this.path = path;
        this.objectMapper = PackageLevelSettings.getValueFor(FluffyTestPackageSettings.OBJECT_MAPPER, TestUtils::createObjectMapper);
    }

    public FileReader withObjectMapper(ObjectMapper mapper) {
        this.objectMapper = mapper;
        return this;
    }

    public FileReader mutate(Function<String, String> mutator) {
        this.content = mutator.apply(asString());
        return this;
    }

    public FileReader mutate(BiConsumer<JsonNode, ObjectMapper> mutator) {
        mutator.accept(asJsonNode(), objectMapper);
        return this;
    }

    public String asString() {
        if (content == null) {
            try {
                content = Files.readString(path, UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Could not read %s", path), e);
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
            try {
                root = objectMapper.readTree(asString());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return (T) root;
    }

}
