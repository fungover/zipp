# ADR: Database Schema Management Strategy

## Status

Accepted

## Context
We need a single, consistent way to collect traces, metrics, and logs across services.
We want to have easier troubleshooting.

## Decision
Adopt OpenTelemetry as the standard observability framework across the platform.
Prometheus will serve as the primary metrics backend.
Jaeger will serve as the primary distributed tracing backend.
Loki will serve as the primary logging backend.
Grafana will serve as the primary dashboard and visualization platform.

## Consequences
- **Benefits**
Easy way to collect server metrics, traces, and logs.
Statistics over system performance.
Easier troubleshooting and debugging.
Maintain response time.

- **Drawbacks**
Increased complexity in setup and maintenance.
Additional resource consumption for telemetry data collection and storage.

## Alternatives Considered
Opensearch as a possible alternative instead of Loki for log management.




