package org.packagesettings;

// From https://github.com/approvals/ApprovalTests.Java/blob/master/approvaltests-util/src/main/java/org/packagesettings/Settings.java
public record Settings(Object value, String location) {

    @Override
    public String toString() {
        return value + " [from " + location + "]";
    }
}