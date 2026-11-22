package aros.services.rms.infraestructure.share.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Annotation for cash operations: ADMIN or a WORKER assigned to the CASH area.
 *
 * <p>Area assignment is read from the {@code areaTypes} claim of the access token. There is no
 * CASHIER role, so the area is the authorization unit.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
    "T(aros.services.rms.infraestructure.share.security.SecurityExpressions)"
        + ".hasArea(principal, 'CASH')"
        + " or principal.claims['role'] == 'ADMIN'")
public @interface JustCashierOrAdmin {}
