/* (C) 2026 */

package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.user.domain.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Converter for UserRole JPA attribute. */
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

  /** Converts UserRole to database column. */
  @Override
  public String convertToDatabaseColumn(UserRole attribute) {
    return attribute == null ? null : attribute.name();
  }

  /** Converts database column to UserRole. */
  @Override
  public UserRole convertToEntityAttribute(String dbData) {
    return dbData == null ? null : UserRole.valueOf(dbData);
  }
}
