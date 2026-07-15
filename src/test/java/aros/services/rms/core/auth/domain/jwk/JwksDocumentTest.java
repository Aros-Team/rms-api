package aros.services.rms.core.auth.domain.jwk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class JwksDocumentTest {

  private static final JwkKeyId KEY_ID_1 = new JwkKeyId("key-1");
  private static final JwkKeyId KEY_ID_2 = new JwkKeyId("key-2");

  private static JwkKey createKey(JwkKeyId kid) {
    return new JwkKey(kid, JwkAlgorithm.RS256, JwkUse.SIGNATURE, null, true);
  }

  @Test
  void shouldRejectNullKeys() {
    assertThrows(NullPointerException.class, () -> new JwksDocument(null));
  }

  @Test
  void shouldAllowEmptyList() {
    JwksDocument doc = new JwksDocument(List.of());
    assertTrue(doc.keys().isEmpty());
  }

  @Test
  void shouldFindKeyByIdWhenPresent() {
    List<JwkKey> keys = List.of(createKey(KEY_ID_1), createKey(KEY_ID_2));
    JwksDocument doc = new JwksDocument(keys);

    var found = doc.findById(KEY_ID_1);

    assertTrue(found.isPresent());
    assertEquals(KEY_ID_1, found.get().kid());
  }

  @Test
  void shouldReturnEmptyWhenKeyNotFound() {
    List<JwkKey> keys = List.of(createKey(KEY_ID_1));
    JwksDocument doc = new JwksDocument(keys);

    var found = doc.findById(KEY_ID_2);

    assertTrue(found.isEmpty());
  }

  @Test
  void shouldReturnImmutableCopy() {
    List<JwkKey> original = List.of(createKey(KEY_ID_1));
    JwksDocument doc = new JwksDocument(original);

    assertThrows(UnsupportedOperationException.class, () -> doc.keys().add(createKey(KEY_ID_2)));
  }
}
