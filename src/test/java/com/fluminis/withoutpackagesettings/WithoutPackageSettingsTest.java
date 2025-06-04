package com.fluminis.withoutpackagesettings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fluminis.fluffytest.TestUtils;

public class WithoutPackageSettingsTest {

    @Test
    void shouldNotFindRessourceInSubFolderWhenNoPackageSettings() {
        assertThatThrownBy(() -> TestUtils.read("one.json").asString())
                .hasMessageContaining("Could not read ");
    }

    @Test
    void shouldFindRessourceInDefaultFolder() {
        assertThat(TestUtils.read("two.json").asString()).isEqualToIgnoringWhitespace("{\"bar\":\"goodbye\"}");
    }
}
