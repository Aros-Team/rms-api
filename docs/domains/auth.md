# Auth - Flujo de Negocio

> Sistema de autenticación y autorización.

## 1. Resumen

El sistema de auth gestiona:
- **Login** con email/password
- **2FA** (Two-Factor Authentication)
- **Tokens** JWT (access + refresh)
- **Password Reset** por email
- **Account Setup** para nuevos usuarios

---

## 2. Flujo de Login

```
┌─────────────────────────────────────────────────────────────┐
│                     LOGIN NORMAL                             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. Usuario ingresa email + password                        │
│     POST /api/auth/login                                    │
│                                                              │
│  2. Si 2FA NO habilitado:                                   │
│     → Retorna ACCESS_TOKEN + REFRESH_TOKEN                  │
│     → tipo: "SUCCESS"                                       │
│                                                              │
│  3. Si 2FA SÍ habilitado:                                   │
│     → Retorna TFA_TOKEN (temporal)                          │
│     → tipo: "REQUIRE_2FA"                                   │
│     → Usuario debe verificar código                         │
│                                                              │
│  4. Verificar 2FA                                            │
│     POST /api/auth/verify                                   │
│     { code: "123456" }                                      │
│     → Retorna ACCESS_TOKEN + REFRESH_TOKEN                  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Tokens

### Access Token
- Duración: 1 hora (configurable)
- Contiene: `sub` (email), `role`, `exp`
- Se envía en: `Authorization: Bearer <token>`

### Refresh Token
- Duración: 7 días
- Se usa para obtener nuevo access token
- Se envía en: `Authorization: Bearer <refresh_token>`

### TFA Token
- Duración: 5 minutos
- Temporal para verificar código 2FA
- Se usa una sola vez

---

## 4. Flujo de Tokens

```
┌─────────────────────────────────────────────────────────────┐
│                    CICLO DE VIDA                             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Login → ACCESS_TOKEN (1h) + REFRESH_TOKEN (7d)            │
│    │                                                         │
│    ▼                                                         │
│  Usar API con ACCESS_TOKEN                                 │
│    │                                                         │
│    ▼ (después de 1h)                                        │
│  ACCESS_TOKEN expira                                        │
│    │                                                         │
│    ▼                                                         │
│  Refrescar con REFRESH_TOKEN                               │
│    POST /api/auth/refresh                                   │
│    → Nuevo ACCESS_TOKEN + REFRESH_TOKEN                     │
│    │                                                         │
│    ▼                                                         │
│  REFRESH_TOKEN expira (después de 7d)                      │
│    │                                                         │
│    ▼                                                         │
│  Login nuevamente                                           │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Password Reset

### 5.1 Solicitar Reset
```bash
POST /api/auth/forgot-password
{
  "email": "juan@restaurante.com"
}

# Envía email con token de reset
```

### 5.2 Resetear Contraseña
```bash
POST /api/auth/reset-password
{
  "token": "abc123...",
  "newPassword": "nueva123"
}
```

### 5.3 Reenviar Email
```bash
POST /api/auth/resend-password
{
  "email": "juan@restaurante.com"
}

# Genera nuevo token, invalida el anterior
```

---

## 6. Account Setup (Nuevo Usuario)

### 6.1 Flujo

```
Admin crea trabajador
    │
    ▼
Sistema envía email con token de setup
    │
    ▼
Trabajador hace clic en link
    │
    ▼
GET /api/auth/setup-account/validate?token=abc123
    │
    ▼
Trabajador configura contraseña
POST /api/auth/setup-password
{
  "token": "abc123",
  "newPassword": "mi123pass",
  "name": "Juan Pérez",
  "document": "1234567890"
}
```

### 6.2 Validar Token
```bash
GET /api/auth/setup-account/validate?token=abc123

# Respuesta:
{
  "email": "juan@restaurante.com",
  "role": "WORKER",
  "isValid": true
}
```

### 6.3 Configurar Contraseña
```bash
POST /api/auth/setup-password
{
  "token": "abc123",
  "newPassword": "mi123pass",
  "name": "Juan Pérez",
  "document": "1234567890"
}
```

---

## 7. JWKS (JSON Web Key Set)

### Endpoint Público
```
GET /.well-known/jwks.json
```

### Uso
- Clientes validan tokens sin llamar al backend
- Soporte para rotación de llaves
- Cache: `Cache-Control: public, max-age=PT1H`

---

## 8. Seguridad por Endpoint

| Endpoint | Auth Requerido | Token |
|----------|----------------|-------|
| `POST /login` | No | - |
| `POST /verify` | TFA Token | Solo TFA |
| `POST /refresh` | Refresh Token | Solo Refresh |
| `GET /auth` | Access Token | Cualquiera |
| `POST /forgot-password` | No | - |
| `POST /reset-password` | No | - |
| `POST /setup-password` | No | - |

---

## 9. Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/verify` | Verificar 2FA |
| POST | `/api/auth/refresh` | Refrescar token |
| GET | `/api/auth` | Usuario actual |
| POST | `/api/auth/forgot-password` | Solicitar reset |
| POST | `/api/auth/resend-password` | Reenviar reset |
| POST | `/api/auth/reset-password` | Resetear contraseña |
| POST | `/api/auth/setup-password` | Configurar contraseña (nuevo) |
| GET | `/api/auth/setup-account/validate` | Validar token setup |
| GET | `/.well-known/jwks.json` | JWKS keys |

---

## 10. Notas para Frontend

### Login Screen
```
┌─────────────────────────────────────┐
│         Restaurante RMS             │
│                                     │
│  Email: [_________________]         │
│  Pass:  [_________________]         │
│                                     │
│  [Iniciar Sesión]                  │
│                                     │
│  ¿Olvidaste tu contraseña?         │
└─────────────────────────────────────┘
```

### 2FA Screen
```
┌─────────────────────────────────────┐
│      Verificación 2FA              │
│                                     │
│  Ingresa el código de 6 dígitos    │
│                                     │
│  [__] [__] [__] [__] [__] [__]    │
│                                     │
│  [Verificar]                       │
│                                     │
│  ¿No recibiste el código?          │
│  [Reenviar]                        │
└─────────────────────────────────────┘
```

### Guardar Tokens
```typescript
// localStorage
localStorage.setItem('accessToken', token);
localStorage.setItem('refreshToken', refreshToken);

// Axios interceptor
axios.interceptors.request.use(config => {
  config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});
```
