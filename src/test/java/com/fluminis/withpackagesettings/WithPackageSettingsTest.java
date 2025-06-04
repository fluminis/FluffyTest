package com.fluminis.withpackagesettings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fluminis.fluffytest.TestUtils;

public class WithPackageSettingsTest {

    @Test
    void shouldFindRessourceInSubFolderWhenPackageSettings() {
        assertThat(TestUtils.read("one.json").asString()).isEqualToIgnoringWhitespace("{\"foo\":\"hello\"}");
    }

    @Test
    void shouldNotFindRessourceInDefaultFolder() {
        assertThatThrownBy(() -> TestUtils.read("two.json").asString())
                .hasMessage("Could not read src\\test\\resources\\org\\fluminis\\some\\two.json");
    }
}
