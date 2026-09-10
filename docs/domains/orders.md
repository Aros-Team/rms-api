# Orders - Flujo de Negocio

> Sistema de órdenes/ventas del restaurante.

## 1. Resumen

El sistema de órdenes gestiona el ciclo de vida completo de una venta:
- **Captura** de la orden con productos y opciones
- **Validación** de inventario y selecciones especiales
- **Flujo** de preparación hasta entrega
- **Cálculo** de precios con cargos extra

---

## 2. Estados de la Orden

```
                    ┌───────────┐
                    │   QUEUE   │ ← Estado inicial
                    └─────┬─────┘
                          │
                          ▼
                    ┌───────────┐
                    │PREPARING  │ ← Cocinero procesa
                    └─────┬─────┘
                          │
                          ▼
                    ┌───────────┐
                    │   READY   │ ← Lista para entregar
                    └─────┬─────┘
                          │
                          ▼
                    ┌───────────┐
                    │ DELIVERED │ ← Entregada al cliente
                    └───────────┘

                    ┌───────────┐
         Cualquier →│CANCELLED  │ ← No se procesa
         estado     └───────────┘
```

| Estado | Descripción | Acciones permitidas |
|--------|-------------|---------------------|
| `QUEUE` | Orden en cola | `prepare`, `cancel` |
| `PREPARING` | En preparación | `ready`, `cancel` |
| `READY` | Lista para entregar | `deliver` |
| `DELIVERED` | Entregada | Ninguna (terminal) |
| `CANCELLED` | Cancelada | Ninguna (terminal) |

---

## 3. Estructura de una Orden

```
Order
├── id
├── date ( LocalDateTime)
├── status (OrderStatus)
├── table (Table)              ← Mesa asociada
├── partySize (Integer)        ← Número de comensales
├── openTime (LocalDateTime)   ← Hora de apertura
├── closeTime (LocalDateTime)  ← Hora de cierre
│
└── details[] (OrderDetail)
    ├── product (Product)
    ├── unitPrice (Money)       ← Precio final = base + extras
    ├── extraCharge (Money)     ← Cargos por ADD_ON options
    ├── optionExtraPrices{}     ← Mapa optionId → cargo
    ├── instructions (String)   ← Instrucciones especiales
    ├── selectedOptions[]       ← Opciones seleccionadas
    ├── selectedProductIds[]    ← Para Special Selections
    ├── additionIds[]           ← Para Special Selections
    └── clarifications[]        ← Respuestas a preguntas
```

---

## 4. Flujo de Trabajo

### 4.1 Tomar Orden (POST /api/v1/orders)

```bash
POST /api/v1/orders
{
  "tableId": 5,
  "partySize": 4,
  "details": [
    {
      "productId": 10,
      "quantity": 2,
      "selectedOptionIds": [15, 23],
      "instructions": "Sin cebolla"
    },
    {
      "productId": 12,
      "quantity": 1,
      "selectedOptionIds": [18]
    }
  ]
}
```

**Proceso interno:**
1. Validar que la mesa esté disponible
2. Marcar mesa como `OCCUPIED`
3. Validar productos y opciones
4. Enforce `SINGLE_CHOICE` max-1 por grupo
5. Calcular precio: `basePrice + Σ extra_price` (ADD_ONs)
6. Validar stock disponible
7. Guardar orden
8. Deducir inventario automáticamente

### 4.2 Cambiar Estado

```bash
# Marcar en preparación
PUT /api/v1/orders/prepare
{ "orderId": 1 }

# Marcar lista
PUT /api/v1/orders/1/ready

# Entregar
PUT /api/v1/orders/1/deliver

# Cancelar
PUT /api/v1/orders/1/cancel
```

---

## 5. Tipos de Productos

### 5.1 Standard
- Producto normal con precio fijo
- Puede tener opciones (ADD_ON, SINGLE_CHOICE, etc.)
- Precio = `basePrice + Σ extra_price`

### 5.2 Special Selection (Combos)
- Productos configurables (ej: "Almuerzo Ejecutivo")
- Tienen grupos de selección (proteína, guarnición, etc.)
- Tienen adiciones (extras)
- Tienen preguntas (clarificaciones)
- Precio calculado dinámicamente

---

## 6. Opciones de Producto

### Tipos de Selección

| Tipo | Descripción | Pricing |
|------|-------------|---------|
| `SINGLE_CHOICE` | Elegir 1 de N | Sin cargo extra |
| `MULTI_CHOICE` | Elegir varios | Sin cargo extra |
| `ADD_ON` | Extra pague más | `extra_price` se suma |
| `REMOVAL` | Quitar ingrediente | Sin cargo (reduce costo) |

### Ejemplo de Pricing

```
Hamburguesa Base: $25,000
├── SINGLE_CHOICE: Tamaño (Personal/Grande) → +$0
├── ADD_ON: Tocineta → +$3,000
├── ADD_ON: Queso extra → +$2,000
└── REMOVAL: Sin cebolla → +$0

Precio Final = $25,000 + $3,000 + $2,000 = $30,000
```

---

## 7. Reglas de Negocio

### 7.1 Validaciones
- La mesa debe estar `AVAILABLE` para tomar orden
- Solo 1 opción por grupo `SINGLE_CHOICE`
- Stock debe ser suficiente antes de confirmar
- Si falla la deducción de inventario, se revierte la orden

### 7.2 Inventario
- Al tomar orden se deduce automáticamente
- Si no hay stock → `InsufficientStockException`
- La deducción usa el receta del producto + opciones

### 7.3 Métricas
- `recordOrderCreated(true/false)` - Éxito/fallo
- `recordInventoryDeduction(true/false)` - Dedución OK/falla
- `recordInsufficientStock()` - Falta de stock

---

## 8. Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/orders` | Tomar nueva orden |
| PUT | `/api/v1/orders/{id}` | Actualizar orden |
| PUT | `/api/v1/orders/{id}/cancel` | Cancelar orden |
| PUT | `/api/v1/orders/prepare` | Marcar en preparación |
| PUT | `/api/v1/orders/{id}/ready` | Marcar lista |
| PUT | `/api/v1/orders/{id}/deliver` | Entregar orden |
| GET | `/api/v1/orders` | Listar órdenes (paginado, filtros) |

### Filtros GET /api/v1/orders
- `page`, `size` - Paginación
- `status` - Filtrar por estado
- `statuses` - Filtrar por múltiples estados (comma-separated)
- `from`, `to` - Rango de fechas
- `search` - Buscar por nombre de producto/opción

---

## 9. Notas para Frontend

### UI Flow
```
1. Seleccionar mesa → POST /orders
2. Cocinero ve órdenes en QUEUE → PUT /orders/prepare
3. Cuando está lista → PUT /orders/{id}/ready
4. Mesero entrega → PUT /orders/{id}/deliver
5. Mesa se libera automáticamente
```

### WebSocket (futuro)
- Actualizaciones en tiempo real para cocina
- Notificación cuando orden está lista
