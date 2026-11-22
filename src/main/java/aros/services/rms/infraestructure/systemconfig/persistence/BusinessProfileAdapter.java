/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import aros.services.rms.core.systemconfig.domain.BusinessProfile;
import aros.services.rms.core.systemconfig.domain.port.output.BusinessProfilePort;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adapter for the business profile singleton. */
@Component
@RequiredArgsConstructor
public class BusinessProfileAdapter implements BusinessProfilePort {

  private static final long SINGLETON_ID = 1L;

  private final BusinessProfileJpaRepository repository;
  private final BusinessProfileMapper mapper;

  /** {@inheritDoc} */
  @Override
  public Optional<BusinessProfile> find() {
    return repository.findById(SINGLETON_ID).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public BusinessProfile save(BusinessProfile profile) {
    BusinessProfileEntity entity =
        repository.findById(SINGLETON_ID).orElseGet(BusinessProfileEntity::new);
    entity.setId(SINGLETON_ID);
    entity.setRestaurantName(profile.getRestaurantName());
    entity.setLegalName(profile.getLegalName());
    entity.setTaxId(profile.getTaxId());
    entity.setAddress(profile.getAddress());
    entity.setPhone(profile.getPhone());
    entity.setEmail(profile.getEmail());
    entity.setTimezone(profile.getTimezone());
    entity.setLocale(profile.getLocale());
    entity.setUpdatedAt(Instant.now());
    return mapper.toDomain(repository.save(entity));
  }
}
