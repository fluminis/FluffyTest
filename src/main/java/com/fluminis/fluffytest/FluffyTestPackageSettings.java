package com.fluminis.fluffytest;

import org.packagesettings.Field;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FluffyTestPackageSettings {
    public static final Field<ObjectMapper> OBJECT_MAPPER = new Field<>("objectMapper", ObjectMapper.class);

    public static final Field<String> RESSOURCE_FOLDER = new Field<>("ressourceFolder", String.class);
}