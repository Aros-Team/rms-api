package aros.services.rms.infraestructure.share.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/** Annotation to require worker role. */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("principal.claims['role'] == 'WORKER'")
public @interface JustWorkerAdminUser {}
