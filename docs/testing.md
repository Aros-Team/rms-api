# Testing Policy

> How to write tests that actually catch bugs.
> A test's value = its ability to **fail when the code is wrong**.

---

## 0. Core principle: the mutation mindset

Before writing a test, ask: *"If this function were broken — deleted, returned the
wrong value, or crashed on input X — would this test fail?"*

If the test would still pass, it has no value. Rewrite it or delete it.

---

## 1. Test-first (Red-Green-Refactor)

Tests are written **before** the implementation. The test is the contract; the
code is what makes it pass.

1. **Write the test** for the new function/route — it must fail on the current
   code (or fail to compile because the symbol doesn't exist yet).
2. **Run it → confirm RED** (`./gradlew test --tests "*TestName"`). A test
   that passes before any implementation exists is a signal it asserts nothing
   meaningful.
3. **Implement** the minimal code to make it pass.
4. **Run → confirm GREEN**.
5. **Refactor** (clean up; tests stay green).

Enforcement:
- A task is `in_progress` only after its tests exist.
- The reviewer asks: *"would this test have failed before the implementation?"*
  If not, the test wasn't written first (or is too weak) — reject.

---

## 2. Structure

- **AAA**: Arrange (build inputs/mocks) → Act (call the unit once) → Assert (verify
  the observable outcome).
- **One scenario per test.** No test that asserts three unrelated things.
- **Naming**: `should_<behavior>_<condition>` — the name states the contract.
- **File header**: Every test class must have a Javadoc comment explaining:
  - What feature is being tested
  - What the feature should do
  - How the test exercises the feature

---

## 3. Test file header

Every test class MUST have a Javadoc header right before the class declaration (not at the top of the file):

```java
package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
// ... imports ...

/**
 * Tests for {@link RegisterPayrollEventService}.
 *
 * <p><b>Feature:</b> Register a new payroll event (overtime, bonus, deduction, etc.)
 * for a user on a specific date.
 *
 * <p><b>Expected behavior:</b>
 * <ul>
 *   <li>Valid event → saved to repository and returned with generated ID
 *   <li>Repository error → exception propagated to caller
 * </ul>
 *
 * <p><b>How this test works:</b>
 * <ul>
 *   <li>Mocks the PayrollEventRepositoryPort to isolate the service
 *   <li>Verifies the repository's save() method is called with the correct event
 *   <li>Asserts the returned event has the expected fields from the saved result
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RegisterPayrollEventServiceTest {
```

---

## 4. Assertion quality

Assert the **observable contract**, not a side effect or a weak signal.

### Strong assertions

- Deterministic results → assert the **exact** value/structure.
- REST endpoints → assert status code **and** the meaningful body fields.
- Domain objects → assert specific field values, not just `assertNotNull`.
- Every public entry point → negative path (missing, malformed, not-found) **and**
  edge cases (empty, null, special characters, boundaries).

### Forbidden anti-patterns

| Pattern | Why it's weak |
|---|---|
| `assertNotNull(result)` alone | Almost always true, verifies nothing meaningful |
| `assert result.status == 200` alone | Passes even if the body is wrong |
| `verify(repository).save(any())` alone | Doesn't verify WHAT was saved |
| `assertDoesNotThrow(...)` | Verifies nothing about the outcome |
| Asserting "no exception"/"didn't crash" | Verifies nothing about the result |
| Reading private state via reflection | Tests implementation, not behavior |

### Example: weak → strong

Weak:

```java
@Test
void should_save_event() {
    PayrollEvent event = PayrollEvent.create(...);
    when(repository.save(any())).thenReturn(saved);

    PayrollEvent result = service.execute(event);

    verify(repository).save(any());  // Doesn't verify WHAT was saved
    assertNotNull(result);           // Always true
}
```

Strong:

```java
@Test
void should_save_event_with_correct_fields() {
    PayrollEvent event = PayrollEvent.create(
        1L, LocalDate.of(2026, 8, 1), PayrollEventType.OVERTIME,
        new BigDecimal("2.5"), new BigDecimal("23437.50"), "notes", "admin");

    when(repository.save(any())).thenReturn(savedEvent);

    PayrollEvent result = service.execute(event);

    // Verify WHAT was saved
    ArgumentCaptor<PayrollEvent> captor = ArgumentCaptor.forClass(PayrollEvent.class);
    verify(repository).save(captor.capture());
    PayrollEvent saved = captor.getValue();
    assertEquals(1L, saved.userId());
    assertEquals(PayrollEventType.OVERTIME, saved.eventType());
    assertEquals(0, new BigDecimal("2.5").compareTo(saved.quantity()));

    // Verify result
    assertEquals(1L, result.id());
    assertEquals(1L, result.userId());
}
```

---

## 5. Mocking: only at boundaries

- Mock **external** boundaries: databases (repository ports), HTTP clients, file
  system, time (Clock), email services.
- **Never** mock the unit's own logic. A test that mocks everything the function
  does passes no matter what the implementation does.
- Assert mocks were called with the **exact** contract arguments
  (`verify(port).save(argThat(e -> e.userId().equals(1L)))`), not just "called".
- Keep real inputs as realistic as production payloads.

---

## 6. Minimum coverage per new function/route

For every new public function or route, ship all four:

1. **Happy path** — exact expected result.
2. **Error paths** — each error branch (400/404/500, typed domain exceptions).
3. **Edge cases** — empty input, null, boundaries, zero values.
4. **Security-relevant inputs** where applicable — unauthorized access, role validation.

A new function without all four is not `done`; the reviewer must reject it.

---

## 7. REST Controller tests

For `@WebMvcTest` controllers:

- Use `@MockitoBean` for all dependencies.
- Mock JWT authentication with `SecurityMockMvcRequestPostProcessors.jwt()`.
- Assert status code **and** response body fields (not just status).
- Test both success and failure paths.
- Verify the use case was called with correct arguments.

### Example

```java
@Test
@WithMockUser(roles = "ADMIN")
void should_return_201_when_registering_event() throws Exception {
    PayrollEvent event = PayrollEvent.create(...);
    when(registerEvent.execute(any())).thenReturn(event);

    mockMvc.perform(post("/api/v1/payroll/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.eventType").value("OVERTIME"));

    verify(registerEvent).execute(argThat(e -> e.userId().equals(1L)));
}
```

---

## 8. Domain model tests

For domain records/entities:

- Test factory methods (e.g., `PayrollEvent.create()`).
- Test computed values (e.g., `amount = quantity × unitRate`).
- Test validation logic (e.g., status transitions).
- Test edge cases (zero quantities, null fields, boundary values).

---

## 9. Reviewer gate

The reviewer checks every PR/activity against this policy:

- [ ] **File header**: Test class has Javadoc explaining feature, behavior, and test approach
- [ ] **Mutation mindset**: each test would fail if its unit were broken
- [ ] **Exact-value assertions** on deterministic results; no weak `assertNotNull`/`assertDoesNotThrow`
- [ ] **Negative + edge paths** tested for every new entry point
- [ ] **Mocks only at boundaries**; call arguments asserted precisely
- [ ] **No private-state access** via reflection
- [ ] **Deterministic**: no randomness, no wall-clock asserts without frozen time
- [ ] **AAA structure**: Arrange → Act → Assert clearly separated
- [ ] **One scenario per test**: no test asserts three unrelated things
