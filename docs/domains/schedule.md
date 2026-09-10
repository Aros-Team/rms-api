# Schedule - Flujo de Negocio

> Sistema de horarios y control de asistencia.

## 1. Resumen

El sistema de horarios gestiona:
- **Horarios** de trabajo del restaurante
- **Turnos** por día y hora
- **Asignaciones** de trabajadores a horarios
- **Time Logs** (registros de entrada/salida)

---

## 2. Conceptos Clave

### 2.1 Schedule (Horario)
Plantilla semanal de trabajo:
```
Horario "Restaurante Regular"
├── Lunes: 08:00-17:00, 14:00-23:00
├── Martes: 08:00-17:00, 14:00-23:00
├── ...
└── Domingo: 10:00-20:00
```

### 2.2 ScheduleShift (Turno)
Bloque de horas en un día:
```
Turno: Lunes 08:00-17:00 (9 horas)
```

### 2.3 WorkerScheduleAssignment
Asignación de un trabajador a un horario:
```
Juan → Horario "Restaurante Regular" (Lunes a Viernes)
```

### 2.4 TimeLog
Registro de entrada/salida real:
```
Juan: 01 Ago 2026, 08:05 - 17:10 (9.08 horas)
```

---

## 3. Estructura

```
Schedule
├── id
├── name                    ← "Horario Regular"
├── description             ← "Lunes a Domingo"
└── shifts[]                ← Turnos por día
    └── ScheduleShift
        ├── dayOfWeek       ← MONDAY, TUESDAY...
        ├── startTime       ← 08:00
        └── endTime         ← 17:00

WorkerScheduleAssignment
├── id
├── workerId → User
├── scheduleId → Schedule
├── startDate               ← 2026-08-01
└── endDate (nullable)      ← null = indefinido

TimeLog
├── id
├── workerId → User
├── date                    ← 2026-08-01
├── clockIn                 ← 08:05
├── clockOut                ← 17:10
└── hoursWorked             ← 9.08
```

---

## 4. Flujo de Trabajo

### 4.1 Crear Horario

```bash
POST /api/v1/admin/schedule
{
  "name": "Horario Regular Cocina",
  "description": "Lunes a Domingo, turnos rotativos",
  "shifts": [
    { "dayOfWeek": "MONDAY", "startTime": "06:00", "endTime": "14:00" },
    { "dayOfWeek": "MONDAY", "startTime": "14:00", "endTime": "22:00" },
    { "dayOfWeek": "TUESDAY", "startTime": "06:00", "endTime": "14:00" },
    { "dayOfWeek": "TUESDAY", "startTime": "14:00", "endTime": "22:00" }
  ]
}
```

### 4.2 Asignar Trabajador

```bash
POST /api/v1/workers/5/schedule-assignments
{
  "scheduleId": 1,
  "startDate": "2026-08-01"
}
```

### 4.3 Ver Mi Horario

```bash
GET /api/v1/workers/me/schedule
```

### 4.4 Ver Time Logs

```bash
GET /api/v1/admin/time-logs
```

---

## 5. Reglas de Negocio

### 5.1 Turnos
- No se pueden solapar turnos en el mismo día
- Un horario debe tener al menos 1 turno
- Nombre del horario es obligatorio

### 5.2 Asignaciones
- Un trabajador puede tener solo 1 horario activo por vez
- Si `endDate` es null, la asignación es indefinida
- Al asignar, se genera el horario para el trabajador

### 5.3 Time Logs
- `clockIn` y `clockOut` son obligatorios
- `hoursWorked` se calcula automáticamente
- No se pueden crear registros en el futuro

---

## 6. Cálculo de Horas para Nómina

```
Horas Trabajadas (mes) = Σ time_logs.hoursWorked (del mes)

Ejemplo:
- 01 Ago: 9.08 horas
- 02 Ago: 8.45 horas
- ...
- 31 Ago: 7.50 horas
─────────────────────
Total: 192 horas (agosto)
```

---

## 7. Endpoints

### Horarios (Admin)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/admin/schedule` | Crear horario |
| GET | `/api/v1/admin/schedule` | Listar horarios |
| GET | `/api/v1/admin/schedule/{id}` | Detalle de horario |
| GET | `/api/v1/admin/time-logs` | Listar time logs |

### Asignaciones (Admin)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/workers/{workerId}/schedule-assignments` | Asignar horario |
| GET | `/api/v1/workers/{workerId}/schedule-assignments` | Ver asignaciones |
| DELETE | `/api/v1/workers/{workerId}/schedule-assignments/{id}` | Eliminar asignación |

### Mi Horario (Auth)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/workers/me/schedule` | Ver mi horario |

---

## 8. Notas para Frontend

### Vista de Calendario
- Mostrar turnos por día
- Colores por tipo de turno
- Indicador de trabajadores asignados

### Admin - Asignación Drag & Drop
- Arrastrar trabajadores a turnos
- Validar conflictos en tiempo real
- Guardar asignación múltiple

### Trabajador - Mi Horario
- Vista semanal/mensual
- Próximos turnos
- Horas trabajadas del mes
