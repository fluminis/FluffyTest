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

## 📄 License

This project is licensed under the MIT License.

