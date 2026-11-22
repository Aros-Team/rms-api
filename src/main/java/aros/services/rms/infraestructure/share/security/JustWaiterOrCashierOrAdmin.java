package aros.services.rms.infraestructure.share.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Annotation for table readers: ADMIN or a WORKER assigned to SERVICE, BAR or CASH.
 *
 * <p>Kitchen staff do not consume table data; waiters and cashiers do.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
    "T(aros.services.rms.infraestructure.share.security.SecurityExpressions)"
        + ".hasAnyArea(principal, 'SERVICE', 'BAR', 'CASH')"
        + " or principal.claims['role'] == 'ADMIN'")
public @interface JustWaiterOrCashierOrAdmin {}
