/* (C) 2026 */

package aros.services.rms.infraestructure.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import aros.services.rms.infraestructure.user.config.AdminInitializer;
import jakarta.persistence.EntityManagerFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link aros.services.rms.infraestructure.order.persistence.Order} pages and the
 * Hibernate batch-fetch configuration that bounds their N+1 lazy loads.
 *
 * <p><b>Feature:</b> Lazy collections on order pages, the pending-payments board and order detail
 * mapping are fetched in batches instead of one query per row.
 *
 * <p><b>Expected behavior:</b> The session factory applies a positive {@code
 * default_batch_fetch_size}, so a page of N orders needs {@code ceil(N/50)} lazy loads per
 * association rather than N.
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Boots the application with the production configuration and an H2 datasource
 *   <li>Reads the resolved batch fetch size from the Hibernate session factory options
 * </ul>
 */
@SpringBootTest(
    properties = {
      "app.jwt.public-key=unused",
      "app.jwt.private-key=unused",
      "spring.datasource.url=jdbc:h2:mem:batchfetch;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.flyway.enabled=false",
      "spring.sql.init.mode=never"
    })
class PersistenceBatchFetchIntegrationTest {

  @MockitoBean private AdminInitializer adminInitializer;

  @Autowired private EntityManagerFactory entityManagerFactory;

  @DynamicPropertySource
  static void configureJwtKeys(DynamicPropertyRegistry registry) throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();
    String pubPem =
        "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getEncoder().encodeToString(kp.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----";
    String privPem =
        "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded())
            + "\n-----END PRIVATE KEY-----";
    registry.add("app.jwt.public-key", () -> pubPem);
    registry.add("app.jwt.private-key", () -> privPem);
  }

  @Test
  void should_bound_lazy_collection_queries_with_batch_fetch_size() {
    SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

    assertEquals(50, sessionFactory.getSessionFactoryOptions().getDefaultBatchFetchSize());
  }
}
