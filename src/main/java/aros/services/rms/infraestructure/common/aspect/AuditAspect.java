package aros.services.rms.infraestructure.common.aspect;

import aros.services.rms.core.auth.application.dto.AuthFinalResult;
import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.port.output.CurrentUserPort;
import aros.services.rms.core.common.audit.domain.AuditLog;
import aros.services.rms.core.common.audit.port.output.AuditLogPort;
import aros.services.rms.core.user.domain.UserWithAreas;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private AuditLogPort auditLogPort;
  @Autowired private CurrentUserPort currentUserPort;

  private static final Set<String> READ_OPERATION_PREFIXES =
      Set.of("get", "find", "query", "list", "search", "load", "verify");

  private static final Set<String> EXCLUDED_PACKAGES = Set.of("aros.services.rms.core.email");

  @Pointcut("execution(* aros.services.rms.core..port.input.*UseCase.*(..))")
  public void useCaseMethods() {}

  @Around("useCaseMethods()")
  public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();

    if (isReadOperation(methodName)) {
      return joinPoint.proceed();
    }

    if (isExcludedPackage(joinPoint)) {
      return joinPoint.proceed();
    }

    String businessAction = extractBusinessAction(joinPoint);
    String targetEntity = extractTargetEntity(joinPoint);
    String details = sanitizeDetails(extractInputDetails(joinPoint));

    Object result = joinPoint.proceed();

    String newValue = sanitizeNewValue(result);
    String targetEntityId = extractEntityIdFromResult(result);

    saveAuditLog(businessAction, targetEntity, targetEntityId, details, null, newValue);

    return result;
  }

  private boolean isExcludedPackage(ProceedingJoinPoint joinPoint) {
    String className = joinPoint.getTarget().getClass().getName();
    return EXCLUDED_PACKAGES.stream().anyMatch(className::startsWith);
  }

  private boolean isReadOperation(String methodName) {
    String lower = methodName.toLowerCase();
    return READ_OPERATION_PREFIXES.stream().anyMatch(lower::startsWith);
  }

  private String extractBusinessAction(ProceedingJoinPoint joinPoint) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    if (className.endsWith("Service")) {
      return className.substring(0, className.length() - 7);
    }
    return className;
  }

  private String extractTargetEntity(ProceedingJoinPoint joinPoint) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    if (className.endsWith("Service")) {
      String baseName = className.substring(0, className.length() - 7);
      if (baseName.endsWith("Use")) {
        return baseName.substring(0, baseName.length() - 3);
      }
      return baseName.replaceAll("Service$", "");
    }
    return className;
  }

  private String extractInputDetails(ProceedingJoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    if (args.length == 0) {
      return null;
    }
    try {
      if (args.length == 1) {
        return objectMapper.writeValueAsString(args[0]);
      }
      return objectMapper.writeValueAsString(args);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  private String extractEntityIdFromResult(Object result) {
    if (result == null) {
      return null;
    }
    try {
      java.lang.reflect.Method idMethod = result.getClass().getMethod("getId");
      Object id = idMethod.invoke(result);
      return id != null ? id.toString() : null;
    } catch (Exception e) {
      return null;
    }
  }

  private String sanitizeDetails(String json) {
    if (json == null) {
      return null;
    }
    return json.replaceAll("\"password\"\\s*:\\s*\"[^\"]+\"", "\"password\": \"[FILTERED]\"")
        .replaceAll("\"code\"\\s*:\\s*\"[^\"]+\"", "\"code\": \"[FILTERED]\"");
  }

  private String sanitizeNewValue(Object result) {
    if (result == null) {
      return null;
    }

    if (result instanceof AuthResult.Success success) {
      return String.format("{\"username\": \"%s\", \"type\": \"SUCCESS\"}", success.username());
    }
    if (result instanceof AuthResult.RequiresTFA tfa) {
      return String.format("{\"username\": \"%s\", \"type\": \"TFA_REQUIRED\"}", tfa.username());
    }
    if (result instanceof AuthFinalResult finalResult) {
      return String.format("{\"username\": \"%s\", \"type\": \"SUCCESS\"}", finalResult.username());
    }
    if (result instanceof CreateUserUseCase.CreateUserResult createUserResult) {
      return String.format(
          "{\"userId\": \"%s\", \"email\": \"%s\"}",
          createUserResult.user().getId(), createUserResult.user().getEmail().value());
    }

    String json = serialize(result);
    return json.replaceAll("\"[^\"]*token[^\"]*\"\\s*:\\s*\"[^\"]*\"", "\"[FILTERED]\"")
        .replaceAll("\"password\"\\s*:\\s*\"[^\"]+\"", "\"password\": \"[FILTERED]\"");
  }

  private String serialize(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      return obj.toString();
    }
  }

  private void saveAuditLog(
      String businessAction,
      String targetEntity,
      String targetEntityId,
      String details,
      String oldValue,
      String newValue) {
    try {
      Optional<UserWithAreas> user = getCurrentUser();
      Long userId = user.map(u -> u.id().value()).orElse(null);
      String username = user.map(u -> u.email().value()).orElse("SYSTEM");
      String ipAddress = getClientIp();

      AuditLog auditLog =
          AuditLog.builder()
              .id(UUID.randomUUID().toString())
              .userId(userId)
              .username(username)
              .businessAction(businessAction)
              .targetEntity(targetEntity)
              .targetEntityId(targetEntityId)
              .details(details)
              .oldValue(oldValue)
              .newValue(newValue)
              .ipAddress(ipAddress)
              .timestamp(Instant.now())
              .build();

      auditLogPort.save(auditLog);
    } catch (Exception e) {
      // Silently fail to not impact the main flow
    }
  }

  private Optional<UserWithAreas> getCurrentUser() {
    try {
      return currentUserPort.fetchCurrentUserInfo();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private String getClientIp() {
    try {
      ServletRequestAttributes attrs =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attrs != null) {
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
          ip = request.getRemoteAddr();
        }
        return ip;
      }
    } catch (Exception e) {
      // Ignore
    }
    return null;
  }
}
