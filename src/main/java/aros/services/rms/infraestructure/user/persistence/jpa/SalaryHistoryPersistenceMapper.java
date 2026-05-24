/* (C) 2026 */

package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.user.domain.Salary;
import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

/** MapStruct mapper for SalaryHistory persistence. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SalaryHistoryPersistenceMapper {

  /**
   * Converts a SalaryHistoryEntity to a domain SalaryHistoryEntry.
   *
   * @param entity the JPA entity
   * @return the domain entity
   */
  @Mapping(source = "id", target = "id.value")
  @Mapping(source = "userId", target = "userId.value")
  @Mapping(source = "oldSalary", target = "oldSalary", qualifiedByName = "bigDecimalToSalary")
  @Mapping(source = "newSalary", target = "newSalary", qualifiedByName = "bigDecimalToSalary")
  SalaryHistoryEntry toDomain(SalaryHistoryEntity entity);

  /**
   * Converts a domain SalaryHistoryEntry to a SalaryHistoryEntity.
   *
   * @param domain the domain entity
   * @return the JPA entity
   */
  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "userId.value", target = "userId")
  @Mapping(source = "oldSalary", target = "oldSalary", qualifiedByName = "salaryToBigDecimal")
  @Mapping(source = "newSalary", target = "newSalary", qualifiedByName = "salaryToBigDecimal")
  @Mapping(target = "createdAt", ignore = true)
  SalaryHistoryEntity toEntity(SalaryHistoryEntry domain);

  /**
   * Converts a BigDecimal to a Salary value object.
   *
   * @param value the BigDecimal value
   * @return the Salary value object
   */
  @Named("bigDecimalToSalary")
  default Salary bigDecimalToSalary(BigDecimal value) {
    if (value == null) {
      return null;
    }
    return Salary.of(value);
  }

  /**
   * Converts a Salary value object to a BigDecimal.
   *
   * @param salary the Salary value object
   * @return the BigDecimal value
   */
  @Named("salaryToBigDecimal")
  default BigDecimal salaryToBigDecimal(Salary salary) {
    if (salary == null) {
      return null;
    }
    return salary.value();
  }
}
