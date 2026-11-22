/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import java.util.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/** Mapper for Payroll persistence operations. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class PayrollMapper {

  /** Converts a PayrollEntity to a Payroll domain object using the given currency. */
  @Mapping(target = "id", source = "entity.id")
  @Mapping(target = "userId", source = "entity.userId")
  @Mapping(
      target = "period",
      expression = "java(YearMonth.of(entity.getPeriodYear(), entity.getPeriodMonth()))")
  @Mapping(target = "periodStart", source = "entity.periodStart")
  @Mapping(target = "periodEnd", source = "entity.periodEnd")
  @Mapping(target = "baseSalary", expression = "java(new Money(entity.getBaseSalary(), currency))")
  @Mapping(target = "bonuses", expression = "java(new Money(entity.getBonuses(), currency))")
  @Mapping(
      target = "manualBonuses",
      expression = "java(new Money(entity.getManualBonuses(), currency))")
  @Mapping(target = "deductions", expression = "java(new Money(entity.getDeductions(), currency))")
  @Mapping(
      target = "manualDeductions",
      expression = "java(new Money(entity.getManualDeductions(), currency))")
  @Mapping(target = "netAmount", expression = "java(new Money(entity.getNetAmount(), currency))")
  @Mapping(target = "paidAmount", expression = "java(new Money(entity.getPaidAmount(), currency))")
  @Mapping(target = "hoursWorked", source = "entity.hoursWorked")
  @Mapping(target = "status", expression = "java(parseStatus(entity.getStatus()))")
  @Mapping(target = "notes", source = "entity.notes")
  @Mapping(target = "registeredBy", source = "entity.registeredBy")
  @Mapping(target = "createdAt", source = "entity.createdAt")
  @Mapping(target = "updatedAt", source = "entity.updatedAt")
  public abstract Payroll toDomain(PayrollEntity entity, Currency currency);

  /** Converts a Payroll domain object to a PayrollEntity. */
  @Mapping(target = "id", source = "domain.id")
  @Mapping(target = "userId", source = "domain.userId")
  @Mapping(target = "periodYear", expression = "java(domain.period().getYear())")
  @Mapping(target = "periodMonth", expression = "java(domain.period().getMonthValue())")
  @Mapping(target = "periodStart", source = "domain.periodStart")
  @Mapping(target = "periodEnd", source = "domain.periodEnd")
  @Mapping(target = "baseSalary", expression = "java(domain.baseSalary().amount())")
  @Mapping(target = "bonuses", expression = "java(domain.bonuses().amount())")
  @Mapping(target = "manualBonuses", expression = "java(domain.manualBonuses().amount())")
  @Mapping(target = "deductions", expression = "java(domain.deductions().amount())")
  @Mapping(target = "manualDeductions", expression = "java(domain.manualDeductions().amount())")
  @Mapping(target = "netAmount", expression = "java(domain.netAmount().amount())")
  @Mapping(target = "paidAmount", expression = "java(domain.paidAmount().amount())")
  @Mapping(target = "hoursWorked", source = "domain.hoursWorked")
  @Mapping(target = "status", expression = "java(domain.status().name())")
  @Mapping(target = "notes", source = "domain.notes")
  @Mapping(target = "registeredBy", source = "domain.registeredBy")
  @Mapping(target = "createdAt", source = "domain.createdAt")
  @Mapping(target = "updatedAt", source = "domain.updatedAt")
  public abstract PayrollEntity toEntity(Payroll domain);

  /**
   * Parses a persisted status, tolerating the legacy seed value {@code APPROVED}.
   *
   * <p>The deterministic seed wrote {@code APPROVED} for the previous month, but the domain models
   * that state as {@link PayrollStatus#ACCRUED}. Mapping it here keeps reads and aggregation
   * working without changing the public status vocabulary.
   *
   * @param status the persisted status
   * @return the domain status
   */
  protected PayrollStatus parseStatus(String status) {
    if ("APPROVED".equals(status)) {
      return PayrollStatus.ACCRUED;
    }
    return PayrollStatus.valueOf(status);
  }
}
