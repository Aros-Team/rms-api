# Inventory - Flujo de Negocio

> Sistema de inventario y movimientos de insumos.

## 1. Resumen

El sistema de inventario gestiona:
- **Stock** de insumos por ubicación (bodega, cocina)
- **Movimientos** entre ubicaciones y por ventas
- **Deducciones** automáticas al vender productos
- **Transferencias** entre áreas del restaurante

---

## 2. Conceptos Clave

### 2.1 Supply (Insumo)
Materia prima del restaurante:
- Arroz, pollo, tomate, etc.

### 2.2 Supply Variant (Variante)
Presentación del insumo:
- Pollo entero (1.5 kg)
- Pollo troceado (1 kg)
- Tomate maduro (1 lb)

### 2.3 Storage Location (Ubicación)
Dónde se almacena:
- **Bodega** - Almacén principal
- **Cocina** - Área de preparación
- **Bar** - Área de bebidas

---

## 3. Tipos de Movimiento

```
┌─────────────────────────────────────────────────────────────┐
│                     MOVIMIENTOS                             │
├─────────────┬─────────────┬─────────────────────────────────┤
│   ENTRY     │  TRANSFER   │         DEDUCTION               │
├─────────────┼─────────────┼─────────────────────────────────┤
│ Ingreso por │ Traslado    │ Consumo por venta               │
│ compra      │ entre áreas │ (automático al vender)          │
├─────────────┼─────────────┼─────────────────────────────────┤
│ Bodega ←    │ Bodega →    │ Cocina → (consumido)            │
│ Proveedor   │ Cocina      │                                  │
└─────────────┴─────────────┴─────────────────────────────────┘
```

| Tipo | Descripción | Origen |
|------|-------------|--------|
| `ENTRY` | Ingreso por compra | `POST /purchases` |
| `TRANSFER` | Traslado entre ubicaciones | `POST /inventory/transfer` |
| `DEDUCTION` | Consumo por venta | Automático al tomar orden |

---

## 4. Flujo de Inventario

### 4.1 Flujo Normal

```
                    ┌─────────────┐
                    │  PROVEEDOR  │
                    └──────┬──────┘
                           │ ENTRY
                           ▼
                    ┌─────────────┐
                    │   BODEGA    │
                    └──────┬──────┘
                           │ TRANSFER
                           ▼
                    ┌─────────────┐
                    │   COCINA    │
                    └──────┬──────┘
                           │ DEDUCTION
                           ▼
                    ┌─────────────┐
                    │  CONSUMIDO  │
                    └─────────────┘
```

### 4.2 Compra (Entry)

```bash
# 1. Crear orden de compra
POST /api/v1/purchases
{
  "supplierId": 1,
  "items": [
    { "supplyVariantId": 10, "quantity": 50, "unitCost": 5000 },
    { "supplyVariantId": 15, "quantity": 30, "unitCost": 3200 }
  ],
  "notes": "Compra semanal"
}

# 2. Automáticamente genera ENTRY en bodega
# inventory_movements: type=ENTRY, quantity=+50, location=BODEGA
```

### 4.3 Transferencia (Transfer)

```bash
# Transferir 20 kg de arroz de Bodega a Cocina
POST /api/v1/inventory/transfer
{
  "supplyVariantId": 10,
  "fromStorageLocationId": 1,  # Bodega
  "toStorageLocationId": 2,    # Cocina
  "quantity": 20
}

# Movimiento generado:
# - Bodega: -20 kg
# + Cocina: +20 kg
```

### 4.4 Deducción por Venta (Automática)

```bash
# Al tomar orden con Pollo a la Brasa
POST /api/v1/orders
{
  "tableId": 5,
  "details": [
    { "productId": 10, "quantity": 2 }
  ]
}

# Internamente:
# 1. Buscar receta del producto 10
#    - Pollo troceado: 0.5 kg
#    - Arroz: 0.3 kg
#    - Ají: 0.1 kg
# 2. Deducir de Cocina
#    - Pollo: -1.0 kg (2 × 0.5)
#    - Arroz: -0.6 kg (2 × 0.3)
#    - Ají: -0.2 kg (2 × 0.1)
```

---

## 5. Estructura de Datos

### Product Recipe (Receta)

```
ProductRecipe
├── productId → Product
├── supplyVariantId → SupplyVariant
└── requiredQuantity (BigDecimal)  ← Cantidad por unidad
```

Ejemplo:
```sql
-- Receta: Pollo a la Brasa (1 unidad)
INSERT INTO product_recipes (product_id, supply_variant_id, required_quantity)
VALUES
  (10, 15, 0.5),   -- Pollo troceado: 0.5 kg
  (10, 10, 0.3),   -- Arroz: 0.3 kg
  (10, 22, 0.1);   -- Ají: 0.1 kg
```

### Inventory Stock (Stock por Ubicación)

```
InventoryStock
├── supplyVariantId → SupplyVariant
├── storageLocationId → StorageLocation
└── quantity (BigDecimal)  ← Stock actual
```

---

## 6. Validaciones

### 6.1 Al Transferir
- Origen y destino deben ser diferentes
- Stock suficiente en origen
- Insumo y ubicación deben existir

### 6.2 Al Vender
- Stock suficiente en Cocina
- Si no hay stock → `InsufficientStockException`
- La venta se revierte si falla la deducción

### 6.3 Al Comprar
- Proveedor debe existir
- Variantes de insumo deben existir
- Cantidades > 0

---

## 7. Endpoints

### Inventario
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/inventory/transfer` | Transferir entre ubicaciones |

### Insumos
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/supplies` | Listar insumos |
| POST | `/api/v1/supplies` | Crear insumo |
| GET | `/api/v1/supplies/categories` | Categorías de insumos |
| POST | `/api/v1/supplies/categories` | Crear categoría |
| GET | `/api/v1/supplies/units` | Unidades de medida |
| GET | `/api/v1/supplies/variants` | Listar variantes (paginado, ?search=) |
| POST | `/api/v1/supplies/variants` | Crear variante |

### Compras
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/purchases` | Crear compra (genera ENTRY) |
| GET | `/api/v1/purchases` | Listar compras (?search=, ?from=, ?to=) |
| GET | `/api/v1/purchases/{id}` | Detalle de compra |

---

## 8. Tablas Principales

```sql
-- Insumos
supplies (id, name, category_id, unit)

-- Variantes
supply_variants (id, supply_id, name, unit_cost, current_stock)

-- Ubicaciones
storage_locations (id, name, type)

-- Stock
inventory_stock (supply_variant_id, storage_location_id, quantity)

-- Movimientos
inventory_movements (
  id,
  supply_variant_id,
  from_storage_location_id,
  to_storage_location_id,
  quantity,
  movement_type,  -- ENTRY, TRANSFER, DEDUCTION
  reference_order_id,
  reference_purchase_order_id,
  created_at
)

-- Recetas
product_recipes (product_id, supply_variant_id, required_quantity)
```

---

## 9. Notas para Frontend

### Dashboard de Inventario
- Stock actual por ubicación
- Alertas de stock bajo
- Últimos movimientos

### Al Crear Producto
- Asociar receta (insumos necesarios)
- Cada insumo = 1 línea en `product_recipes`

### Al Ver Producto
- Mostrar costo de receta = Σ(required_quantity × unit_cost)
- Indicador de disponibilidad
