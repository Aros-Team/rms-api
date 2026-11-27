package aros.services.rms.infraestructure.share.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Annotation for kitchen staff: ADMIN or a WORKER assigned to the PREPARATION area.
 *
 * <p>Area assignment is read from the {@code areaTypes} claim of the access token.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
    "T(aros.services.rms.infraestructure.share.security.SecurityExpressions)"
        + ".hasArea(principal,"
        + " T(aros.services.rms.core.area.domain.AreaActivity).PREPARATION)"
        + " or principal.claims['role'] == 'ADMIN'")
public @interface JustKitchenOrAdmin {}
