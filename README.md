# FluffyTest

<p align="center">
  <img src="https://raw.githubusercontent.com/fluminis/FluffyTest/main/assets/fluffytest_logo.png" alt="FluffyTest Logo" width="200"/>
</p>

**FluffyTest** is a lightweight, fluffy Java utility library designed to make writing tests easier, especially when
working with file parsing and JSON (Jackson) mapping.

---

## ✨ Features

- Simplified file reading in tests
- Easy Jackson ObjectMapper integration
- Handy test utilities out of the box

---

## 🚀 Getting Started

To use FluffyTest in your project:

```xml

<dependency>
    <groupId>com.fluminis</groupId>
    <artifactId>fluffytest</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

## Read and manipulate a file from tests

Given

```json
{
  "firstname": "Harry",
  "lastname": "Potter",
  "address": null,
  "father": {
    "firstname": "James",
    "lastname": "Potter",
    "address": null
  },
  "mother": {
    "firstname": "Lily",
    "lastname": "Evans",
    "address": null
  },
  "updateTime": "NOW"
}
```

```java
record Person(String firstname, String lastname, Person father, Person mother, Address address, LocalDate updateTime) {
}
```

```java
import java.time.LocalDate;

import com.fluminis.fluffytest.Mutators;
import com.fluminis.fluffytest.TestUtils;

@Test
void shouldDoSomethingWithPerson() {
    Person input = TestUtils.read("in/person1.json")
            .mutate(Mutators.replaceAll("NOW", LocalDate.of(2025, 6, 4)))
            .mutate(
                    Mutators.setValue("firstname", "Fluffy"),
                    Mutators.setValue("father.address",
                            new Address("10 Downing Street", "London SW1A 2AA, United Kingdom")),
                    Mutators.setNull("lastname"),
                    Mutators.copy("father.address", "mother.address")
            )
            .asObject(Person.class);
    // do something with input...
}

@Test
void shouldMutateExistingObject() {
    Person input = TestUtils.from(PersonFixtures.aPerson())
            .mutate(
                    Mutators.setValue("firstname", "Fluffy"),
                    Mutators.setValue("father.address",
                            new Address("10 Downing Street", "London SW1A 2AA, United Kingdom")),
                    Mutators.setNull("lastname"),
                    Mutators.copy("father.address", "mother.address")
            )
            .asObject(Person.class);
    // do something with input...
}
```

## Configuration

👍 Thanks to [approvalTests](https://github.com/approvals/ApprovalTests.Java/) for the PackageSettings stuff.

### What is configurable?

Configuration of FluffyTest mainly occurs via PackageSettings.
Currently you can configure:

* ressourceFolder: default ressource folder to load file from.
* objectMapper: default objectMapper to use

### Package Level Settings

FluffyTest will read the following options:

```java
public class PackageSettings {
    public static String ressourceFolder = "org/fluminis/some";
    public static ObjectMapper objectMapper = ...
}
```

The default ressource folder is `scr/test/ressources`.

The default objectMapper is created in `TestUtils.createObjectMapper()`

### PackageLevelSettings

Package Level Settings allows for programmatic setting of configuration at the package level. It follows the principle
of least surprise.

Your Package Leveling configuration must be in class called PackageSettings. The fields can be private, public and or
static. They will be picked up regardless. All methods will be ignored.

For example if you had a class:

```java
package org.packagesettings;

public class PackageSettings {
    public String name = "Llewellyn";
    private int rating = 10;
    public static String lastName = "Falco";
}
```

If you where to call at the org.packagesettings level.

```java
Map<String, Settings> settings = PackageLevelSettings.get();
```

Then you would get the following settings

```txt
lastName : Falco [from org.packagesettings.PackageSettings] 
name : Llewellyn [from org.packagesettings.PackageSettings] 
rating : 10 [from org.packagesettings.PackageSettings]
```

However, if you also had

```java
package org.packagesettings.subpackage;

public class PackageSettings {
    public String name = "Test Name";
    private boolean rating = true;
    public String ratingScale = "logarithmic";
}
```

and you ran the same code but from the org.packagesettings.subpackage  
then you would get a blended view of the two classes where anything in the sub-package would override the parents.

```txt
lastName : Falco [from org.packagesettings.PackageSettings] 
name : Test Name [from org.packagesettings.subpackage.PackageSettings] 
rating : true [from org.packagesettings.subpackage.PackageSettings] 
ratingScale : logarithmic [from org.packagesettings.subpackage.PackageSettings]
```

## 📄 License

This project is licensed under the MIT License.

