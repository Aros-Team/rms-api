package aros.services.rms.core.auth.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import aros.services.rms.core.auth.domain.jwk.JwkAlgorithm;
import aros.services.rms.core.auth.domain.jwk.JwkKey;
import aros.services.rms.core.auth.domain.jwk.JwkKeyId;
import aros.services.rms.core.auth.domain.jwk.JwkUse;
import aros.services.rms.core.auth.domain.jwk.JwksDocument;
import aros.services.rms.core.auth.domain.jwk.exception.NoActiveSigningKeyException;
import aros.services.rms.core.auth.domain.jwk.port.output.JwkSourcePort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link PublishJwksService}.
 *
 * <p><b>Feature:</b> JWKS publication and key distribution
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>set Up
 *   <li>should Return Document when Keys Present
 *   <li>should Throw No Active Signing Key when No Keys Configured
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks JwkSourcePort dependencies via Mockito
 *   <li>Verifies business logic with unit-level assertions
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class PublishJwksServiceTest {

  @Mock private JwkSourcePort jwkSourcePort;

  private PublishJwksService service;

  @BeforeEach
  void setUp() {
    service = new PublishJwksService(jwkSourcePort);
  }

  @Test
  void shouldReturnDocument_whenKeysPresent() {
    when(jwkSourcePort.loadAll())
        .thenReturn(new JwksDocument(List.of(createKey(new JwkKeyId("dummy")))));

    JwksDocument doc = service.publish();

    assertEquals(1, doc.keys().size());
  }

  @Test
  void shouldThrowNoActiveSigningKey_whenNoKeysConfigured() {
    JwksDocument emptyDoc = mock(JwksDocument.class);
    when(emptyDoc.keys()).thenReturn(List.of());
    when(jwkSourcePort.loadAll()).thenReturn(emptyDoc);

    assertThrows(NoActiveSigningKeyException.class, () -> service.publish());
  }

  private static JwkKey createKey(JwkKeyId kid) {
    return new JwkKey(kid, JwkAlgorithm.RS256, JwkUse.SIGNATURE, null, true);
  }
}
