# ADR: Database Schema Management Strategy

## Status

Accepted

## Context

We are building a Spring Boot application that uses JPA/Hibernate for persistence.
Hibernate provides an option (`spring.jpa.hibernate.ddl-auto=update`) to automatically adjust the database schema to
match entity definitions.
Alternatively, tools like Flyway or Liquibase offer explicit, versioned migration scripts for controlled schema
evolution.

## Decision

We will **start with Hibernate `ddl-auto=update`** during the early development phase to accelerate iteration and reduce
setup overhead.
Once the schema stabilizes and the project requires reproducibility across environments, we will **introduce Flyway**
for versioned, controlled migrations.
At that point, Hibernate’s automatic schema update will be disabled (`ddl-auto=none`).

## Consequences

- **Short-term benefits:**
  - Faster development and prototyping.
  - No need to manage migration scripts initially.
- **Long-term benefits:**
  - Migration history and reproducibility with Flyway.
  - Safer schema evolution across environments.
- **Risks:**
  - Potential inconsistencies if relying too long on Hibernate `update`.
  - Manual effort required to establish a baseline migration when transitioning to Flyway.

## Alternatives Considered

- **Always use Hibernate `update`:** Simple, but lacks reproducibility and control in production.
- **Start with Flyway immediately:** Provides safety and history from day one, but adds setup overhead during rapid
  prototyping.

## Notes

This hybrid approach balances **speed in early development** with **safety and reproducibility in later stages**.
