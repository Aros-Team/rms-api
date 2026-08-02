/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import aros.services.rms.core.payroll.domain.Payroll;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/** Mapper for Payroll persistence operations. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class PayrollMapper {

  /** Converts a PayrollEntity to a Payroll domain object. */
  @Mapping(target = "id", source = "entity.id")
  @Mapping(target = "userId", source = "entity.userId")
  @Mapping(
      target = "period",
      expression = "java(YearMonth.of(entity.getPeriodYear(), entity.getPeriodMonth()))")
  @Mapping(target = "periodStart", source = "entity.periodStart")
  @Mapping(target = "periodEnd", source = "entity.periodEnd")
  @Mapping(
      target = "baseSalary",
      expression =
          "java(new Money(entity.getBaseSalary(), java.util.Currency.getInstance(\"COP\")))")
  @Mapping(
      target = "bonuses",
      expression = "java(new Money(entity.getBonuses(), java.util.Currency.getInstance(\"COP\")))")
  @Mapping(
      target = "deductions",
      expression =
          "java(new Money(entity.getDeductions(), java.util.Currency.getInstance(\"COP\")))")
  @Mapping(
      target = "netAmount",
      expression =
          "java(new Money(entity.getNetAmount(), java.util.Currency.getInstance(\"COP\")))")
  @Mapping(target = "hoursWorked", source = "entity.hoursWorked")
  @Mapping(target = "status", expression = "java(PayrollStatus.valueOf(entity.getStatus()))")
  @Mapping(target = "notes", source = "entity.notes")
  @Mapping(target = "registeredBy", source = "entity.registeredBy")
  @Mapping(target = "createdAt", source = "entity.createdAt")
  @Mapping(target = "updatedAt", source = "entity.updatedAt")
  public abstract Payroll toDomain(PayrollEntity entity);

  /** Converts a Payroll domain object to a PayrollEntity. */
  @Mapping(target = "id", source = "domain.id")
  @Mapping(target = "userId", source = "domain.userId")
  @Mapping(target = "periodYear", expression = "java(domain.period().getYear())")
  @Mapping(target = "periodMonth", expression = "java(domain.period().getMonthValue())")
  @Mapping(target = "periodStart", source = "domain.periodStart")
  @Mapping(target = "periodEnd", source = "domain.periodEnd")
  @Mapping(target = "baseSalary", expression = "java(domain.baseSalary().amount())")
  @Mapping(target = "bonuses", expression = "java(domain.bonuses().amount())")
  @Mapping(target = "deductions", expression = "java(domain.deductions().amount())")
  @Mapping(target = "netAmount", expression = "java(domain.netAmount().amount())")
  @Mapping(target = "hoursWorked", source = "domain.hoursWorked")
  @Mapping(target = "status", expression = "java(domain.status().name())")
  @Mapping(target = "notes", source = "domain.notes")
  @Mapping(target = "registeredBy", source = "domain.registeredBy")
  @Mapping(target = "createdAt", source = "domain.createdAt")
  @Mapping(target = "updatedAt", source = "domain.updatedAt")
  public abstract PayrollEntity toEntity(Payroll domain);
}
