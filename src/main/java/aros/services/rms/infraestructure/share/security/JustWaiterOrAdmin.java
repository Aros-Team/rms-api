package aros.services.rms.infraestructure.share.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Annotation for waiters: ADMIN or a WORKER assigned to the SERVE activity.
 *
 * <p>Area assignment is read from the {@code areaTypes} claim of the access token.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
    "T(aros.services.rms.infraestructure.share.security.SecurityExpressions)"
        + ".hasArea(principal,"
        + " T(aros.services.rms.core.area.domain.AreaActivity).SERVE)"
        + " or principal.claims['role'] == 'ADMIN'")
public @interface JustWaiterOrAdmin {}
