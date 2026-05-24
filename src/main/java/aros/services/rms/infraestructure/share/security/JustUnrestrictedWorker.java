package aros.services.rms.infraestructure.share.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/** Security annotation for admin or unrestricted worker access. */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
    "principal.claims['role'] == 'ADMIN' or "
        + "(principal.claims['role'] == 'WORKER' and principal.claims['restricted'] == false)")
public @interface JustUnrestrictedWorker {}
