# Special Selections - Flujo de Negocio

> Sistema de selecciones especiales (combos/configurables).

## 1. Resumen

Las Special Selections son productos configurables donde el cliente elige:
- **Grupos** de selección (proteína, guarnición, etc.)
- **Adiciones** (extras con cargo)
- **Preguntas** (clarificaciones/sinstrucciones)

Ejemplo: "Almuerzo Ejecutivo" donde el cliente elige:
1. Proteína (SINGLE_CHOICE): Pollo, Carne, Pescado
2. Guarnición (MULTI_CHOICE): Arroz, Ensalada, Papas
3. Bebida (SINGLE_CHOICE): Agua, Gaseosa, Jugo

---

## 2. Estructura

```
SpecialSelectionConfiguration
├── productId → Product
├── name                        ← "Almuerzo Ejecutivo"
├── description
├── basePrice                   ← $18,000
├── active                      ← true/false
├── preparationAreaId           ← 2 (Cocina)
├── selectionType               ← CONFIGURABLE
├── baseRecipeEnabled           ← false
├── schedulingRequired          ← true (solo horario lunch)
│
├── groups[]                    ← Grupos de selección
│   └── SpecialSelectionGroup
│       ├── name                ← "Proteína"
│       ├── selectionType       ← SINGLE_CHOICE
│       ├── required            ← true
│       ├── maxSelections       ← 1
│       └── options[]           ← Opciones del grupo
│           └── optionId        ← Pollo, Carne, Pescado
│
├── additions[]                 ← Extras con cargo
│   └── SpecialSelectionAddition
│       ├── optionId            ← Tocineta
│       └── extraPrice          ← $3,000
│
├── questions[]                 ← Preguntas al cliente
│   └── SpecialSelectionQuestion
│       ├── question            ← "¿Punto de la carne?"
│       ├── type                ← TEXT | CHOICE | BOOLEAN
│       └── options[]           ← (para CHOICE)
│
└── schedule[]                  ← Horario de disponibilidad
    └── SpecialSelectionScheduleEntry
        ├── dayOfWeek           ← MONDAY
        ├── startTime           ← 11:00
        └── endTime             ← 15:00
```

---

## 3. Flujo de Venta

### 3.1 Tomar Orden con Special Selection

```bash
POST /api/v1/orders
{
  "tableId": 5,
  "details": [
    {
      "productId": 50,  # Almuerzo Ejecutivo
      "quantity": 1,
      "selectedProductIds": [10],  # Pollo (proteína)
      "additionIds": [15, 20],     # Tocineta, Queso extra
      "clarifications": [
        { "questionId": 1, "answer": "Medio" },
        { "questionId": 2, "answer": "Sin cebolla" }
      ]
    }
  ]
}
```

### 3.2 Validación

```
1. Verificar disponibilidad horaria
   └── schedule: L-V 11:00-15:00
   └── Si no disponible → SpecialSelectionNotAvailableException

2. Validar selecciones
   ├── Grupo "Proteína": max 1 selección → OK (Pollo)
   ├── Grupo "Guarnición": max 2 selecciones → OK
   └── Si excede límite → ValidationException

3. Calcular precio
   ├── basePrice: $18,000
   ├── additions: $3,000 + $2,000 = $5,000
   └── total: $23,000
```

---

## 4. Precios

### Precio Base
- Precio fijo del combo

### Precio Final
```
Precio Final = basePrice + Σ addition.extraPrice

Ejemplo:
- Base: $18,000
- Tocineta: +$3,000
- Queso: +$2,000
- Total: $23,000
```

### Sugerencia de Precio
```bash
POST /api/v1/admin/special-selections/50/suggest-price
```
El sistema sugiere un precio basado en:
- Costo de ingredientes
- Margen deseado
- Precios de competencia (futuro)

---

## 5. Horario de Disponibilidad

### Configuración
```json
{
  "schedule": [
    { "dayOfWeek": "MONDAY", "startTime": "11:00", "endTime": "15:00" },
    { "dayOfWeek": "TUESDAY", "startTime": "11:00", "endTime": "15:00" },
    { "dayOfWeek": "WEDNESDAY", "startTime": "11:00", "endTime": "15:00" },
    { "dayOfWeek": "THURSDAY", "startTime": "11:00", "endTime": "15:00" },
    { "dayOfWeek": "FRIDAY", "startTime": "11:00", "endTime": "15:00" }
  ]
}
```

### Validación
- Si `schedulingRequired = true`, validar horario actual
- Si fuera de horario → `SpecialSelectionNotAvailableException`

---

## 6. Versionado

Cada cambio crea una nueva versión:
```
Historial:
├── v1: Configuración inicial
├── v2: Cambio de precio
├── v3: Agregó nueva opción
└── v4: Cambió horario
```

### Revertir
```bash
POST /api/v1/admin/special-selections/50/revert/2
# Revierte a versión 2
```

---

## 7. Endpoints

### Admin
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/admin/special-selections` | Crear configuración |
| PUT | `/api/v1/admin/special-selections/{productId}` | Actualizar |
| PUT | `/api/v1/admin/special-selections/{productId}/price` | Actualizar precio |
| PATCH | `/api/v1/admin/special-selections/{productId}/active` | Activar/desactivar |
| PUT | `/api/v1/admin/special-selections/{productId}/schedule` | Actualizar horario |
| DELETE | `/api/v1/admin/special-selections/{productId}` | Eliminar |
| GET | `/api/v1/admin/special-selections/{productId}` | Detalle |
| GET | `/api/v1/admin/special-selections` | Listar todas |
| GET | `/api/v1/admin/special-selections/{productId}/history` | Historial |
| POST | `/api/v1/admin/special-selections/{productId}/revert/{version}` | Revertir |
| POST | `/api/v1/admin/special-selections/{productId}/suggest-price` | Sugerir precio |

### Público
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/special-selections/available` | Disponibles ahora |
| GET | `/api/v1/special-selections/{productId}` | Configuración para venta |

---

## 8. Notas para Frontend

### Menú - Producto Special Selection
```
Almuerzo Ejecutivo - $18,000
[Configurar]

→ Grupo "Proteína" (Requerido, 1 selección)
  ○ Pollo
  ○ Carne
  ○ Pescado

→ Grupo "Guarnición" (Requerido, 2 selecciones)
  ☐ Arroz
  ☐ Ensalada
  ☐ Papas

→ Adiciones (Opcional)
  ☐ Tocineta +$3,000
  ☐ Queso +$2,000

→ Preguntas
  "¿Punto de la carne?" → [Medio ▼]
  "Instrucciones especiales" → [________]

Total: $23,000
[Agregar al carrito]
```

### Admin - Configuración
- Editor visual de grupos
- Drag & drop de opciones
- Preview en tiempo real
- Historial de cambios
