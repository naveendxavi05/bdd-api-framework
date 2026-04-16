# BDD Architecture Decision Records

> Five key design decisions made during the build of `bdd-api-framework` and the rationale behind each.

---

## BDD-001 — @BeforeAll vs Per-Scenario Auth Token

**Decision:** Fetch the auth token once in `@BeforeAll` and store it as a `static` field on `Hooks`.

**Alternatives considered:**
- Fetch token in every `@Before` hook — simple but wasteful, adds ~200ms per scenario and hammers `/auth` unnecessarily.
- Fetch token in a `@BeforeAll` TestNG listener — possible but outside Cucumber's lifecycle, harder to maintain.

**Rationale:**
Restful Booker tokens do not expire within a test run. Fetching once reduces network overhead, keeps the suite fast, and mirrors real-world patterns where a CI pipeline authenticates once per run. The `static` field is intentional — `@BeforeAll` in Cucumber 7 runs in a static context so instance fields are not available.

---

## BDD-002 — Hook Scoping with Tag Expressions

**Decision:** Scope `@Before` and `@After` hooks using tag expressions `not @auth` and `not @create`.

**Alternatives considered:**
- Single unscoped `@Before`/`@After` for all scenarios — causes auth scenarios to fail (they don't need a pre-existing booking) and `@create` scenarios to double-create bookings.
- Conditional logic inside hooks (`if scenario.tags contains`) — works but mixes concerns and is harder to read.

**Rationale:**
Cucumber 7 tag expressions on hooks are the idiomatic solution. `@auth` scenarios test the auth endpoint itself — injecting a booking would pollute the test intent. `@create` scenarios use Scenario Outline to create bookings explicitly — a `@Before` creation would be redundant and leave orphaned bookings.

---

## BDD-003 — PicoContainer vs ThreadLocal for Shared State

**Decision:** Use PicoContainer for dependency injection of `ScenarioContext` across step definition classes.

**Alternatives considered:**
- `ThreadLocal<ScenarioContext>` static fields — works for parallel execution but requires manual cleanup in `@After` and is error-prone.
- Single monolithic step definition class — no DI needed but violates single responsibility and becomes unmaintainable at scale.
- Spring DI (`cucumber-spring`) — powerful but heavyweight for an API-only framework with no Spring Boot target.

**Rationale:**
PicoContainer is Cucumber's lightest DI solution. It creates one instance of `ScenarioContext` per scenario and injects it into every step class that declares it as a constructor parameter. No static state, no cleanup required, naturally parallel-safe. Exactly the right tool for sharing state between step classes without coupling them.

---

## BDD-004 — JavaFaker Sentinel Pattern

**Decision:** Wrap every `Faker` call with a `fallback()` method that substitutes a hardcoded value if Faker returns null or blank.

**Alternatives considered:**
- Trust Faker to always return valid data — Faker occasionally returns empty strings for certain locales or data types, causing silent test failures.
- Use fixed hardcoded test data throughout — removes randomness, reduces confidence that the API handles varied inputs correctly.

**Rationale:**
Faker is non-deterministic by design. The sentinel pattern (`fallback(faker.name().firstName(), "John")`) gives us the best of both worlds — random data when Faker works correctly, guaranteed non-empty fallback when it doesn't. This pattern prevents flaky tests caused by Faker internals without sacrificing data variety.

---

## BDD-005 — DELETE Returns 201, Not 204

**Decision:** Assert `DELETE /booking/{id}` returns `201` in both `@After` hook and the explicit delete scenario.

**Alternatives considered:**
- Assert 204 (standard REST) — fails on every run against Restful Booker.
- Assert 200 — also incorrect for this endpoint.
- Skip status assertion on DELETE — hides real failures.

**Rationale:**
Restful Booker intentionally returns `201 Created` for successful DELETE operations — this is a known quirk of the application, not a bug in the framework. Asserting the correct actual behaviour (`201`) rather than the REST standard (`204`) keeps tests green and documents the API's non-standard behaviour explicitly. This decision is called out in step definitions and hooks with inline comments so future maintainers understand the intent.