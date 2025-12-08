# ADR: Formatting of LOG objects

## Status
Accepted

## Context
Consistent logger declaration formatting improves code readability and maintainability across the codebase.
Without a standard format, we may use various styles, potentially making the code harder to navigate.

## Decision
All Java classes must declare their logger to use the following format:

```java
private static final Logger LOG = LoggerFactory.getLogger(CurrentClassName.class);
```
```java
LOG.info("Creating user - id={}", userId);
LOG.warn("Failed to update profile - id={}, reason={}", userId, error.getMessage());
LOG.error("Unexpected exception occurred", exception);
```

## Consequences
#### Pros
- Consistency across the codebase
- Improve discoverability (easier to search for)
- Align with common Java conventions
- Supports static analysis and enforcement

#### Cons
- Requires updating existing classes
- Checkstyle or Taikai needs configuration

## Taikai tests
### Add architectural test to verify logger declarations:
```java
@Test
void loggersShouldFollowNamingConvention() {
    fields()
        .that().haveRawType(Logger.class)
        .should().haveName("LOG")
        .andShould().bePrivate()
        .andShould().beStatic()
        .andShould().beFinal()
        .check(classes);
}
```

## References
Issue #55 "Create ADR for LOG"
