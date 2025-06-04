package org.packagesettings;

import java.lang.reflect.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

// From https://github.com/approvals/ApprovalTests.Java/blob/master/approvaltests-util/src/main/java/org/packagesettings/PackageLevelSettings.java
public class PackageLevelSettings {
    public static String PACKAGE_SETTINGS = "PackageSettings";

    public static Map<String, Settings> get() {
        return getForStackTrace(Thread.currentThread().getStackTrace());
    }

    public static Map<String, Settings> getForStackTrace(StackTraceElement[] trace) {
        Map<String, Settings> settings = new HashMap<>();
        try {
            HashSet<String> done = new HashSet<String>();
            for (StackTraceElement element : trace) {
                String packageName = getNextLevel(element.getClassName());
                settings.putAll(getSettingsFor(packageName, done));
            }
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
        return settings;
    }

    private static Map<String, Settings> getSettingsFor(String packageName, HashSet<String> done) {
        if (packageName == null || done.contains(packageName)) {
            return Collections.emptyMap();
        }
        Map<String, Settings> settings = new HashMap<>(getSettingsFor(getNextLevel(packageName), done));
        try {
            Class<?> clazz = loadClass(packageName + "." + PACKAGE_SETTINGS);
            java.lang.reflect.Field[] declaredFields = clazz.getDeclaredFields();
            Object o = clazz.getDeclaredConstructor().newInstance();
            for (java.lang.reflect.Field field : declaredFields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    settings.put(field.getName(), getFieldValue(field, null));
                } else {
                    settings.put(field.getName(), getFieldValue(field, o));
                }
            }
        } catch (Throwable e) {
            //Ignore
        }
        done.add(packageName);
        return settings;
    }

    private static Settings getFieldValue(java.lang.reflect.Field field, Object from) {
        try {
            field.setAccessible(true);
            return new Settings(field.get(from), field.getDeclaringClass().getName());
        } catch (Throwable t) {
            //ignore
        }
        return null;
    }

    public static String getNextLevel(String className) {
        int last = className.lastIndexOf(".");
        return (last < 0) ? null : className.substring(0, last);
    }

    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getValueFor(String key) {
        Settings settings = get().get(key);
        return settings == null ? null : (T) settings.value();
    }

    public static <T> T getValueFor(org.packagesettings.Field<T> field) {
        return getValueFor(field, () -> null);
    }

    public static <T> T getValueFor(org.packagesettings.Field<T> field, Supplier<T> defaultSupplier) {
        return field.getValue(get(), defaultSupplier);
    }
}