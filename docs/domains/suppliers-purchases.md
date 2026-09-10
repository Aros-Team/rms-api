# Suppliers & Purchases - Flujo de Negocio

> Gestión de proveedores y compras de insumos.

## 1. Resumen

- **Suppliers**: Proveedores que venden insumos al restaurante
- **Purchases**: Órdenes de compra que generan inventario

---

## 2. Suppliers (Proveedores)

### Estructura
```
Supplier
├── id
├── name                    ← "Distribuidora El Campo"
├── nit                     ← "900123456-7"
├── contactName             ← "María López"
├── phone                   ← "+57 300 1234567"
├── email                   ← "ventas@elcampo.com"
└── address                 ← "Calle 45 #12-34"
```

---

## 3. Purchases (Compras)

### Estructura
```
PurchaseOrder
├── id
├── supplierId → Supplier
├── date                    ← 2026-08-15
├── status                  ← PENDING | RECEIVED | CANCELLED
├── totalAmount             ← $250,000
├── notes                   ← "Compra semanal"
└── items[]                 ← Detalle de la compra
    └── PurchaseOrderItem
        ├── supplyVariantId → SupplyVariant
        ├── quantity        ← 50
        ├── unitCost        ← $5,000
        └── subtotal        ← $250,000
```

---

## 4. Flujo de Compra

### 4.1 Crear Compra

```bash
POST /api/v1/purchases
{
  "supplierId": 1,
  "items": [
    {
      "supplyVariantId": 10,
      "quantity": 50,
      "unitCost": 5000
    },
    {
      "supplyVariantId": 15,
      "quantity": 30,
      "unitCost": 3200
    }
  ],
  "notes": "Compra semanal - Agosto"
}
```

### 4.2 Efecto en Inventario

```
Al crear compra:
1. Se crea PurchaseOrder (status: PENDING)
2. Se generan movimientos ENTRY en inventario
   - Bodega: +50 kg Arroz
   - Bodega: +30 kg Pollo

Al recibir:
PUT /api/v1/purchases/{id}
{ "status": "RECEIVED" }
```

### 4.3 Movimiento Generado

```sql
inventory_movements:
├── supply_variant_id: 10 (Arroz)
├── from_storage_location_id: NULL (ENTRY)
├── to_storage_location_id: 1 (Bodega)
├── quantity: 50
├── movement_type: ENTRY
└── reference_purchase_order_id: 1
```

---

## 5. Endpoints

### Suppliers
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/suppliers` | Crear proveedor |
| PUT | `/api/v1/suppliers/{id}` | Actualizar proveedor |
| GET | `/api/v1/suppliers` | Listar proveedores (?search=) |

### Purchases
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/purchases` | Crear compra |
| GET | `/api/v1/purchases` | Listar compras (?search=, ?from=, ?to=) |
| GET | `/api/v1/purchases/{id}` | Detalle de compra |

---

## 6. Notas para Frontend

### Formulario de Compra
```
Proveedor: [Distribuidora El Campo ▼]

Insumo                    | Cantidad | Precio Unit. | Subtotal
─────────────────────────┼──────────┼──────────────┼─────────
Arroz (1 kg)             | 50       | $5,000       | $250,000
Pollo troceado (1 kg)    | 30       | $3,200       | $96,000
─────────────────────────┴──────────┴──────────────┴─────────
                                          Total: $346,000

Notas: [Compra semanal__________]

[Guardar Compra]
```

### Historial de Compras
- Filtro por fecha
- Búsqueda por proveedor
- Exportar a Excel (futuro)
