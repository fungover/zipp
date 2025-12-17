# Test Coverage Documentation

## Overview
This project uses automated test quality tools to ensure code reliability.

## JaCoCo Code Coverage ✅

### Current Status
- **Coverage:** 36%
- **Threshold:** 30% (enforced)
- **Status:** WORKING

### Running Coverage
```bash
# Maven panel: Lifecycle → verify
# Or command line:
mvn clean verify
```

### Viewing Reports
Open: `backend/target/site/jacoco/index.html`

### Configuration
- Plugin: `jacoco-maven-plugin` v0.8.13
- Minimum threshold: 30% line coverage
- Build fails if coverage drops below threshold
- Reports generated in HTML + XML format

### Package Coverage
- **controller:** 97% ✅
- **interceptor:** 88% ✅
- **config:** 83% ✅
- **dto:** 100% ✅
- **security:** 49%
- **entity:** 28%
- **service:** 13%
- **repository:** 5%

## PIT Mutation Testing ⚠️

### Current Status
- **Status:** Configured but NOT operational
- **Blocker:** Java 25 compatibility

### Configuration
- Plugin: `pitest-maven` v1.17.4
- Mutation threshold: 50%
- Coverage threshold: 30%
- Target packages: `org.fungover.zipp.**`

### Known Issue
```
Unsupported class file major version 69
```

**Root Cause:** PIT does not support Java 25 yet (released September 2024).

**Tracking:**
- PIT GitHub: https://github.com/hcoles/pitest/issues
- Expected support: Q1-Q2 2025

### Workaround Options
1. **Wait for PIT update** (recommended)
  - Monitor PIT releases
  - Update when Java 25 support added
2. **Downgrade to Java 21 LTS** (requires team decision)
  - Would enable PIT immediately
  - Affects entire project
3. **Use JaCoCo only** (current approach)
  - Sufficient for code coverage
  - Missing mutation testing benefits

### Running PIT (when supported)
```bash
# Maven panel: Plugins → pitest → pitest:mutationCoverage
```

Reports will be in: `backend/target/pit-reports/index.html`

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Run Tests with Coverage
  run: mvn clean verify

- name: Upload Coverage Report
  uses: actions/upload-artifact@v3
  with:
    name: jacoco-report
    path: backend/target/site/jacoco/
```

## For Developers

### Before Committing
1. Run: `mvn clean verify`
2. Check coverage report
3. Ensure threshold maintained (≥30%)

### Improving Coverage
- Focus on: service, repository, entity layers
- Write unit tests for business logic
- Use Testcontainers for integration tests

## Future Enhancements
- [ ] Enable PIT when Java 25 support added
- [ ] Increase coverage threshold to 50%
- [ ] Add Sonar integration
- [ ] Generate coverage badges

## Resources
- [JaCoCo Docs](https://www.jacoco.org/jacoco/trunk/doc/)
- [PIT Docs](https://pitest.org/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
