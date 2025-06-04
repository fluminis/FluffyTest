package org.packagesettings;

import java.util.Map;
import java.util.function.Supplier;

// From https://github.com/approvals/ApprovalTests.Java/blob/master/approvaltests-util/src/main/java/org/packagesettings/Field.java
public record Field<T>(String name, Class<T> clazz) {

    public boolean isPresent(Map<String, Settings> settings) {
        Settings value = settings.get(name);
        return value != null && this.clazz.isInstance(value.value());
    }

    public T getValue(Map<String, Settings> settings, Supplier<T> defaultValue) {
        return isPresent(settings) ? (T) settings.get(name).value() : defaultValue.get();
    }
}