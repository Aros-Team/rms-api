/* (C) 2026 */

package aros.services.rms.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.area.domain.AreaActivity;
import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.area.port.output.WorkerAreaActivityPort;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.infraestructure.auth.jwt.JwtService;
import aros.services.rms.infraestructure.auth.jwt.TokenAdapter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link TokenAdapter}.
 *
 * <p><b>Feature:</b> Area type resolution for the access token.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>The adapter resolves assigned area ids to semantic area type names.
 *   <li>A user without areas gets an empty areaTypes list, never {@code null}.
 *   <li>The resolved names are forwarded to the JWT service that emits the claim.
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the area repository and the JWT service.
 *   <li>Calls {@code generateAccessToken} and captures the exact arguments passed to the JWT
 *       service, including the resolved area types.
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class TokenAdapterTest {

  @Mock private JwtService jwtService;

  @Mock private WorkerAreaActivityPort workerAreaActivityPort;

  @InjectMocks private TokenAdapter adapter;

  @Test
  void should_resolve_area_types_when_generating_access_token() {
    when(workerAreaActivityPort.findByUserId(10L)).thenReturn(java.util.Map.of());
    when(workerAreaActivityPort.findAreaActivities(List.of(2L)))
        .thenReturn(java.util.Map.of(2L, java.util.Set.of(AreaActivity.SERVE)));
    when(jwtService.generateAccessToken(anyString(), any(), anyList(), anyList(), anyBoolean()))
        .thenReturn("access-token");

    User user =
        new User(
            UserId.of(10L),
            "1000",
            "Waiter",
            new UserEmail("waiter@test.com"),
            "encoded",
            "Address",
            "555-0100",
            UserRole.WORKER,
            UserStatus.ACTIVE,
            List.of(AreaId.of(2L)));

    adapter.generateAccessToken(user, false);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> areaTypes = ArgumentCaptor.forClass(List.class);
    verify(jwtService)
        .generateAccessToken(
            eq("waiter@test.com"),
            eq(UserRole.WORKER),
            eq(List.of(AreaId.of(2L))),
            areaTypes.capture(),
            eq(false));
    assertEquals(List.of(AreaActivity.SERVE.name()), areaTypes.getValue());
  }

  @Test
  void should_send_empty_area_types_when_user_has_no_areas() {
    User user =
        new User(
            UserId.of(11L),
            "1001",
            "No Area",
            new UserEmail("noarea@test.com"),
            "encoded",
            "Address",
            "555-0101",
            UserRole.WORKER,
            UserStatus.ACTIVE,
            List.of());

    when(jwtService.generateAccessToken(anyString(), any(), anyList(), anyList(), anyBoolean()))
        .thenReturn("access-token");

    adapter.generateAccessToken(user, false);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> areaTypes = ArgumentCaptor.forClass(List.class);
    verify(jwtService)
        .generateAccessToken(
            eq("noarea@test.com"),
            eq(UserRole.WORKER),
            eq(List.of()),
            areaTypes.capture(),
            eq(false));
    assertEquals(List.of(), areaTypes.getValue());
  }
}
