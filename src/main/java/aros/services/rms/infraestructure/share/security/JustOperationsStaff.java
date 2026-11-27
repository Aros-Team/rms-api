package aros.services.rms.infraestructure.share.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Annotation for operational staff whose area feeds the shared order lifecycle: ADMIN or a WORKER
 * assigned to any operational area (PREPARATION, SERVE or CASH).
 *
 * <p>Used for order reads and delivery because waiters, kitchen and cashier all participate in
 * those transitions today.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
    "T(aros.services.rms.infraestructure.share.security.SecurityExpressions)"
        + ".hasAnyActivity(principal)"
        + " or principal.claims['role'] == 'ADMIN'")
public @interface JustOperationsStaff {}
