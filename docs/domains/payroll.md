# Payroll - Flujo de Negocio

> Sistema de nómina y cálculo de costos laborales para el restaurante.

## 1. Resumen

El sistema de payroll gestiona:
- **Nómina mensual** de cada trabajador (salario base + bonificaciones - deducciones)
- **Eventos de nómina** (horas extra, bonos, ausencias, deducciones)
- **Pagos realizados** (settlements) contra la nómina
- **Costo laboral real** para cálculo de costos de producción

### Relación con Costos de Producción

El payroll alimenta directamente el cálculo del **costo de producción** de cada plato:

```
Costo Laboral Plato = (Tiempo Preparación / 60) × Costo Hora Área
```

Donde el **Costo Hora** puede ser:
- `STANDARD`: `salary / expected_hours_per_month` (teórico)
- `REAL`: Datos del payroll real del mes

---

## 2. Conceptos Clave

### 2.1 Nómina (Payroll)

Registro mensual del ingreso de un trabajador.

| Campo | Descripción |
|-------|-------------|
| `userId` | Trabajador |
| `period` | Año-Mes (ej: 2026-08) |
| `baseSalary` | Salario base mensual |
| `bonuses` | Total bonificaciones (auto-calculado de eventos) |
| `deductions` | Total deducciones (auto-calculado de eventos) |
| `hoursWorked` | Horas trabajadas en el período |
| `netAmount` | Salario neto = base + bonuses - deductions |
| `paidAmount` | Total pagado (acumulado de settlements) |
| `status` | PENDING → PAID |

### 2.2 Eventos de Nómina (PayrollEvent)

Incidencias que modifican el ingreso del trabajador.

| EventType | Categoría | Multiplicador | Cálculo |
|-----------|-----------|---------------|---------|
| `OVERTIME` | Horas | 1.5x | quantity × hourlyRate × 1.5 |
| `NIGHT_SURCHARGE` | Horas | 1.75x | quantity × hourlyRate × 1.75 |
| `ABSENCE` | Deducción | -1x | quantity × hourlyRate × -1 |
| `BONUS_ATTENDANCE` | Bono | 0.5x | quantity × hourlyRate × 0.5 |
| `BONUS_PERFORMANCE` | Bono | Manual | amount directo |
| `DEDUCTION` | Deducción | Manual | amount directo |

### 2.3 Settlements (Pagos)

Registro de pagos efectuados al trabajador contra su nómina.

| Tipo | Periodicidad |
|------|-------------|
| `DAILY` | Pago diario |
| `WEEKLY` | Pago semanal |
| `BIWEEKLY` | Pago quincenal |
| `MONTHLY` | Pago mensual |

---

## 3. Estados de la Nómina

```
    ┌─────────┐
    │ PENDING │ ← Estado inicial al registrar
    └────┬────┘
         │
         ├──────────────────┐
         ▼                  ▼
    ┌─────────┐        ┌─────────┐
    │  PAID   │        │ ACCRUED │
    └─────────┘        └────┬────┘
                             │
                             ▼
                        ┌─────────┐
                        │  PAID   │
                        └─────────┘
```

| Transición | Permitida | Descripción |
|------------|-----------|-------------|
| PENDING → PAID | ✅ | Pago completo |
| PENDING → ACCRUED` | ✅` | Devengado (pendiente de pago) |
| ACCRUED → PAID | ✅` | Pago después de devengar |
| PAID → * | ❌ | Inmutable una vez pagado |

---

## 4. Flujo de Trabajo

### 4.1 Flujo Mensual Típico

```
Día 1-5 del mes:
┌─────────────────────────────────────────────────────────────────┐
│ 1. Registrar nómina del mes anterior                            │
│    POST /api/v1/payroll                                         │
│    { userId, year, month, baseSalary, hoursWorked }            │
├─────────────────────────────────────────────────────────────────┤
│ 2. Registrar eventos del mes (durante el mes)                   │
│    POST /api/v1/payroll/events                                  │
│    { userId, eventDate, eventType, quantity, unitRate }        │
├─────────────────────────────────────────────────────────────────┤
│ 3. Registrar pagos parciales (quincenal/semanal)                │
│    POST /api/v1/payroll/settle                                  │
│    { payrollId, userId, settlementType, amount }               │
├─────────────────────────────────────────────────────────────────┤
│ 4. Al finalizar el mes:                                         │
│    - Los eventos se agregan automáticamente a bonuses/deductions│
│    - Verificar: paidAmount vs netAmount                         │
│    - Marcar como PAID si está completo                          │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Ejemplo Completo - Trabajador Juan

#### Semana 1 (Lunes 4 Ago)
```bash
# 1. Registrar nómina mensual
POST /api/v1/payroll
{
  "userId": 5,
  "year": 2026,
  "month": 8,
  "periodStart": "2026-08-01",
  "periodEnd": "2026-08-31",
  "baseSalary": 1500000,
  "hoursWorked": 192
}
# Respuesta: { id: 1, netAmount: 1500000, status: "PENDING" }
```

#### Durante el mes - Registrar eventos
```bash
# 2. Juan trabaja 4 horas extra un martes
POST /api/v1/payroll/events
{
  "userId": 5,
  "eventDate": "2026-08-05",
  "eventType": "OVERTIME",
  "quantity": 4,
  "unitRate": 9375
}
# Calcula: 4 × 9375 × 1.5 = 56,250 COP extra

# 3. Juan tiene 2 horas de recargo nocturno
POST /api/v1/payroll/events
{
  "userId": 5,
  "eventDate": "2026-08-07",
  "eventType": "NIGHT_SURCHARGE",
  "quantity": 2,
  "unitRate": 9375
}
# Calcula: 2 × 9375 × 1.75 = 32,813 COP extra

# 4. Juan falta 1 día (8 horas)
POST /api/v1/payroll/events
{
  "userId": 5,
  "eventDate": "2026-08-12",
  "eventType": "ABSENCE",
  "quantity": 8,
  "unitRate": 9375
}
# Calcula: 8 × 9375 × -1 = -75,000 COP descuento

# 5. Bono por asistencia perfecta
POST /api/v1/payroll/events
{
  "userId": 5,
  "eventDate": "2026-08-31",
  "eventType": "BONUS_ATTENDANCE",
  "quantity": 1,
  "unitRate": 75000
}
# Calcula: 1 × 75000 × 0.5 = 37,500 COP bono
```

#### Pago quincenal
```bash
# 6. Pago quincenal (primera quincena)
POST /api/v1/payroll/settle
{
  "payrollId": 1,
  "userId": 5,
  "settlementType": "BIWEEKLY",
  "periodStart": "2026-08-01",
  "periodEnd": "2026-08-15",
  "amount": 800000
}
# paidAmount: 800,000 / netAmount: ~1,551,563

# 7. Pago final del mes
POST /api/v1/payroll/settle
{
  "payrollId": 1,
  "userId": 5,
  "settlementType": "MONTHLY",
  "periodStart": "2026-08-16",
  "periodEnd": "2026-08-31",
  "amount": 751563
}
# paidAmount: 1,551,563 / netAmount: 1,551,563 → Completo
```

#### Verificar estado
```bash
GET /api/v1/payroll/worker/5/2026/8
# Respuesta:
{
  "id": 1,
  "userId": 5,
  "period": "2026-08",
  "baseSalary": 1500000,
  "bonuses": 126563,
  "deductions": 75000,
  "netAmount": 1551563,
  "paidAmount": 1551563,
  "pendingAmount": 0,
  "status": "PENDING"  // Cambiar a PAID cuando esté completo
}
```

---

## 5. Cálculo de Costo Laboral por Área

El payroll se usa para calcular el **costo real por hora** en cada área.

### Fórmula

```
Costo Hora Real = Salario Real / Horas Esperadas

Donde:
- Salario Real = baseSalary + bonuses - deductions (del payroll)
- Horas Esperadas = expected_hours_per_month (configuración del área)
```

### Ejemplo

```bash
# Juan trabaja en Cocina (área BOH)
# Salario real agosto: $1,551,563
# Horas esperadas: 192 horas/mes

Costo Hora Cocina = 1,551,563 / 192 = $8,080 COP/hora

# Un plato tarda 15 minutos en preparar
Costo Laboral Plato = (15/60) × 8,080 = $2,020 COP
```

### Modos de Cálculo

| Modo | Fuente | Cuándo usar |
|------|--------|-------------|
| `STANDARD` | salary / 160 | Sin payroll registrado, estimación |
| `REAL` | payroll real / expected_hours | Mes cerrado, datos reales |

---

## 6. Reglas de Negocio

### 6.1 Nómina
- Un trabajador solo puede tener **una nómina por mes**
- El `netAmount` no puede ser negativo
- Una vez en estado `PAID`, no se puede modificar
- `paidAmount` se acumula con cada settlement

### 6.2 Eventos
- Se agregan automáticamente a la nómina del mes correspondiente
- Los eventos de horas (OVERTIME, NIGHT_SURCHARGE) usan el `hourly_rate` del trabajador
- Los eventos manuales (BONUS_PERFORMANCE, DEDUCTION) usan `unitRate` directo
- Al eliminar un evento, se recalcula la nómina

### 6.3 Settlements
- El total de settlements no puede superar el `netAmount`
- Se validan por período (no solapamiento)
- Cada settlement registra quién lo hizo y cuándo

---

## 7. Endpoints Relacionados

### Payroll
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/payroll` | Registrar nómina mensual |
| GET | `/api/v1/payroll` | Listar todas las nóminas |
| GET | `/api/v1/payroll/{year}/{month}` | Nóminas por período |
| GET | `/api/v1/payroll/worker/{userId}` | Nóminas de un trabajador |
| GET | `/api/v1/payroll/worker/{userId}/{year}/{month}` | Nómina específica |
| PATCH | `/api/v1/payroll/{id}` | Actualizar (cambiar estado) |
| DELETE | `/api/v1/payroll/{id}` | Eliminar nómina |

### Events
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/payroll/events` | Registrar evento |
| GET | `/api/v1/payroll/events/{userId}/{year}/{month}` | Eventos por período |
| DELETE | `/api/v1/payroll/events/{id}` | Eliminar evento |
| POST | `/api/v1/payroll/events/calculate` | Calcular tarifa sugerida |

### Settlements
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/payroll/settle` | Registrar pago |
| GET | `/api/v1/payroll/settlements/{userId}/{year}/{month}` | Pagos por período |

---

## 8. Configuración Relacionada

### System Configuration

| Key | Valor | Descripción |
|-----|-------|-------------|
| `labor_cost_mode` | `STANDARD` o `REAL` | Modo de cálculo de costo laboral |
| `currency` | `COP` | Moneda del sistema |

### User Fields

| Campo | Descripción |
|-------|-------------|
| `expected_hours_per_month` | Horas esperadas (default: 192) |
| `hourly_rate` | Generado: `salary / expected_hours` |

---

## 9. Diagrama de Entidades

```
┌─────────────────┐       ┌─────────────────┐
│    Payroll       │       │  PayrollEvent    │
├─────────────────┤       ├─────────────────┤
│ id              │◄──┐   │ id              │
│ userId          │   │   │ userId          │
│ period          │   │   │ eventDate       │
│ baseSalary      │   │   │ eventType       │
│ bonuses         │   ├───│ quantity        │
│ deductions      │   │   │ unitRate        │
│ netAmount       │   │   │ amount          │
│ paidAmount      │   │   └─────────────────┘
│ hoursWorked     │   │
│ status          │   │   ┌─────────────────┐
└─────────────────┘   │   │PayrollSettlement│
                      │   ├─────────────────┤
                      │   │ id              │
                      ├───│ payrollId       │
                      │   │ userId          │
                      │   │ settlementType  │
                      │   │ amount          │
                      │   └─────────────────┘
                      │
                      │   ┌─────────────────┐
                      │   │    User         │
                      │   ├─────────────────┤
                      └───│ hourly_rate     │
                          │ expected_hours  │
                          └─────────────────┘
```

---

## 10. Notas para Frontend

### Estados para UI
- **PENDING**: Botón "Marcar como Pagado" habilitado
- **PAID**: Badge verde, sin acciones disponibles
- **ACCRUED**: Badge amarillo, pendiente de pago

### Formato de Moneda
```typescript
// Usar MoneyDto del backend
{
  "amount": 1500000,
  "currency": "COP"
}

// Formatear en UI
new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP'
}).format(money.amount)
```

### Eventos - Tips de UI
- Si el tipo tiene `hasAutomaticMultiplier: true`, solo mostrar `quantity`
- Si es manual (BONUS_PERFORMANCE, DEDUCTION), mostrar campo `unitRate`
- Mostrar cálculo en tiempo real: `quantity × unitRate × multiplier`
