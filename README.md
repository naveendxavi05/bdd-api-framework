# BDD API Framework

> Production-grade BDD API test framework targeting [Restful Booker](https://restful-booker.herokuapp.com)

[![CI - Smoke Tests](https://github.com/naveendxavi05/bdd-api-framework/actions/workflows/smoke.yml/badge.svg)](https://github.com/naveendxavi05/bdd-api-framework/actions/workflows/smoke.yml)
[![CI - Regression Tests](https://github.com/naveendxavi05/bdd-api-framework/actions/workflows/regression.yml/badge.svg)](https://github.com/naveendxavi05/bdd-api-framework/actions/workflows/regression.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=naveendxavi05_bdd-api-framework&metric=alert_status)](https://sonarcloud.io/project/overview?id=naveendxavi05_bdd-api-framework)
[![Allure Report](https://img.shields.io/badge/Allure-Report-green?logo=data:image/png;base64,iVBORw0KGgo=)](https://naveendxavi05.github.io/bdd-api-framework)
[![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://openjdk.org)

---

## Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Cucumber | 7.18.0 | BDD framework |
| RestAssured | 5.4.0 | API client |
| TestNG | 7.9.0 | Test runner |
| PicoContainer | 7.18.0 | DI for step sharing |
| JavaFaker | 1.0.2 | Test data generation |
| Owner | 1.0.12 | Config management |
| Allure | 2.27.0 | Reporting |
| AspectJ | 1.9.22 | Allure agent |
| SonarCloud | — | Static analysis |
| GitHub Actions | — | CI/CD pipeline |

---

## Project Structure

bdd-api-framework/
├── .github/workflows/
│   ├── smoke.yml           # Job 1 — Smoke tests on push/PR
│   ├── regression.yml      # Job 2 — Full regression after smoke
│   ├── sonarcloud.yml      # Job 3 — Static analysis after regression
│   └── allure-pages.yml    # Job 4 — Allure report to GitHub Pages
├── src/test/
│   ├── java/com/bdd/
│   │   ├── config/         # FrameworkConfig (Owner) + ConfigManager
│   │   ├── hooks/          # @BeforeAll health check + auth, @Before/@After lifecycle
│   │   ├── models/         # Booking POJO (Lombok + Jackson)
│   │   ├── runner/         # TestRunner extends AbstractTestNGCucumberTests
│   │   ├── steps/          # AuthSteps + BookingSteps
│   │   └── utils/          # ScenarioContext + BaseRequestSpec + BookingPayloadBuilder
│   └── resources/
│       ├── features/       # auth.feature + booking.feature
│       └── config/         # config.properties.template
├── docs/
│   └── BDD-DECISIONS.md    # 5 architecture decision records
├── testng.xml
└── pom.xml


---

## Architecture Highlights

### PicoContainer DI
One `ScenarioContext` instance injected per scenario into `Hooks`, `AuthSteps`, and `BookingSteps`. No static state, no ThreadLocal — clean parallel-safe design.

### Hook Scoping
- `@BeforeAll` — runs once per suite: health check + auth token fetch
- `@Before("not @auth and not @create")` — creates a fresh booking, stores `bookingId` in context
- `@After("not @auth")` — deletes the booking, asserts 201 (Restful Booker quirk)

### SLA Assertion
Every API call asserts response time ≤ `sla.response.time.ms` (default 500ms) configured via Owner + overridable in CI via `-Dresponse.time.ms`.

### Tag Strategy
| Tag | Purpose |
|---|---|
| `@smoke` | Fast subset — runs on every push |
| `@booking` | Full booking CRUD scenarios |
| `@auth` | Auth scenarios — skips booking lifecycle hooks |
| `@create` | Scenario Outline — skips `@Before` booking creation |

---

## Running Locally

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker

### Start Restful Booker
```bash
docker run -d --name restful-booker -p 3001:3001 ankurpshah/restful-booker:latest
```

### Run all tests
```bash
mvn clean test
```

### Run smoke tests only
```bash
mvn test -Dcucumber.filter.tags="@smoke"
```

### Generate Allure report
```bash
allure serve target/allure-results
```

---

## CI Pipeline

Push to main
│
▼
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  1 — Smoke  │────▶│  2 — Regression  │────▶│  3 — SonarCloud │
└─────────────┘     └──────────────────┘     └─────────────────┘
│
▼
┌─────────────────────────┐
│  4 — Allure → gh-pages  │
└─────────────────────────┘


---

## Live Reports

- 📊 [Allure Report](https://naveendxavi05.github.io/bdd-api-framework)
- 🔍 [SonarCloud Dashboard](https://sonarcloud.io/project/overview?id=naveendxavi05_bdd-api-framework)

---

## Author

**Naveen D Xavi** — QA Automation Engineer
- GitHub: [@naveendxavi05](https://github.com/naveendxavi05)
- LinkedIn: [naveen-d-xavi](https://linkedin.com/in/naveen-d-xavi)
- Email: naveendxavi@gmail.com