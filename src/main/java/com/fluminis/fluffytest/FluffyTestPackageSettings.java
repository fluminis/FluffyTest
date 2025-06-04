package com.fluminis.fluffytest;

import org.packagesettings.Field;

import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FluffyTestPackageSettings {
    public static final Field<ObjectMapper> OBJECT_MAPPER = new Field<>("objectMapper", ObjectMapper.class);

    public static final Field<Path> RESSOURCE_FOLDER = new Field<>("ressourceFolder", Path.class);
}