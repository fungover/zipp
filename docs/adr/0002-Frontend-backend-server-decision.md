# ADR: Frontend/Backend Server Decision

## Status
Accepted

## Context
As to not cause CORS-related problems and extra
configuration in local development and testing.
To keep momentum and avoid spending time on gateway setup before it's
actually needed, we decided to temporarily serve the frontend pages
directly from the same server as the backend.


## Decision
For now frontend and backend will be served from same server.

## Consequences
#### Pros
- No CORS issues
- Simple setup
- Fast implementation

#### Cons
- Poor separation of concern
- Less scalable
- Harder to deploy frontend and backend independently
- Potential longer build times

## Alternatives Considered

### Separate frontend
#### Pros
- Cleaner architecture
- Clear separation of concerns
- Better scaling options (each service can scale independently)
- Gateway enables routing, rate limiting, auth policies, caching

#### Cons
- CORS configuration required
- More complex local development setup
- Gateway configuration is overkill for current project size

## Notes
We will revisit this decision later.

## References
- Issue #53 “Create ADR for frontend/backend server decision”

