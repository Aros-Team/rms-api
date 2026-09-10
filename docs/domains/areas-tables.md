# Areas & Tables - Flujo de Negocio

> Gestión de áreas del restaurante y mesas.

## 1. Resumen

- **Areas**: Secciones del restaurante (Cocina, Bar, Sala, Terraza)
- **Tables**: Mesas disponibles para clientes

---

## 2. Areas

### Concepto
Cada área representa una zona funcional:
- **Cocina Principal** (BOH) - Preparación de alimentos
- **Bar** (FOH) - Bebidas
- **Sala Principal** (FOH) - Mesas de clientes
- **Terraza** (FOH) - Mesas exteriores

### Estructura
```
Area
├── id
├── name                    ← "Cocina Principal"
├── defaultExpectedHours    ← 192 (horas/mes esperadas)
└── workers[]               ← Trabajadores asignados
```

### Uso en Costos
El área define el **costo hora** para sus trabajadores:
```
Costo Hora Área = Salario Real / expectedHoursPerMonth

Ejemplo:
- Cocina: 192 horas/mes
- Salario Juan: $1,551,563
- Costo Hora: $8,080/hora
```

---

## 3. Tables (Mesas)

### Estados

```
    ┌───────────┐
    │ AVAILABLE │ ← Libre para clientes
    └─────┬─────┘
          │
          ▼
    ┌───────────┐
    │ OCCUPIED  │ ← Clientes sentados
    └─────┬─────┘
          │
          ▼ (orden entregada)
    ┌───────────┐
    │ AVAILABLE │ ← Se libera automáticamente
    └───────────┘
```

### Estructura
```
Table
├── id
├── tableNumber              ← "Mesa 5"
├── name                     ← "Terraza Norte"
├── capacity                 ← 4 personas
├── status                   ← AVAILABLE | OCCUPIED
└── areaId → Area            ← "Sala Principal"
```

---

## 4. Flujo de Venta

```
1. Cliente llega
   └── Mesero busca mesa AVAILABLE

2. Tomar orden
   POST /api/v1/orders
   { "tableId": 5, ... }
   └── Mesa cambia a OCCUPIED automáticamente

3. Entregar orden
   PUT /api/v1/orders/1/deliver
   └── Si es última orden de la mesa → AVAILABLE
```

---

## 5. Endpoints

### Areas
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/areas` | Crear área |
| PUT | `/api/v1/areas/{id}` | Actualizar área |
| GET | `/api/v1/areas` | Listar áreas (?search=) |
| GET | `/api/v1/areas/{id}` | Detalle de área |
| DELETE | `/api/v1/areas/{id}` | Eliminar área |

### Tables
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/tables` | Crear mesa |
| PUT | `/api/v1/tables/{id}` | Actualizar mesa |
| GET | `/api/v1/tables` | Listar mesas (?search=) |
| GET | `/api/v1/tables/{id}` | Detalle de mesa |
| DELETE | `/api/v1/tables/{id}` | Eliminar mesa |

---

## 6. Notas para Frontend

### Mapa del Restaurante
```
┌─────────────────────────────────────────┐
│              SALA PRINCIPAL             │
│  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐   │
│  │M 1  │  │M 2  │  │M 3  │  │M 4  │   │
│  │ ○   │  │ ●   │  │ ○   │  │ ○   │   │
│  └─────┘  └─────┘  └─────┘  └─────┘   │
│                                         │
│  ┌─────┐  ┌─────┐  ┌─────┐             │
│  │M 5  │  │M 6  │  │M 7  │             │
│  │ ○   │  │ ●   │  │ ○   │             │
│  └─────┘  └─────┘  └─────┘             │
└─────────────────────────────────────────┘

○ = AVAILABLE (verde)
● = OCCUPIED (rojo)
```

### Seleccionar Mesa para Orden
- Dropdown con mesas disponibles
- Filtro por área
- Indicador de capacidad
