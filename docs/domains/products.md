# Products - Flujo de Negocio

> Catálogo de productos del menú.

## 1. Resumen

El sistema de productos gestiona:
- **Catálogo** de platos y bebidas
- **Precios** y costos
- **Opciones** (ingredientes extra, quitar, etc.)
- **Recetas** para cálculo de costos
- **Imágenes** del producto

---

## 2. Tipos de Producto

### 2.1 Standard
```
Producto normal:
- Precio fijo (basePrice)
- Opciones opcionales (ADD_ON, SINGLE_CHOICE, etc.)
- Receta para cálculo de costo
```

### 2.2 Special Selection (Combo)
```
Producto configurable:
- Grupos de selección (proteína, guarnición)
- Adiciones (extras)
- Preguntas (clarificaciones)
- Precio dinámico
- Horario de disponibilidad
```

---

## 3. Estructura de un Producto

```
Product
├── id
├── name                    ← "Hamburguesa Clásica"
├── description             ← "Carne 200g, lechuga, tomate..."
├── basePrice (Money)       ← $25,000
├── active (boolean)        ← true/false
├── category (Category)     ← "Hamburguesas"
├── preparationAreaId       ← 2 (Cocina Principal)
├── estimatedPrepMinutes    ← 15
├── selectionType           ← STANDARD | SPECIAL_SELECTION
├── baseRecipeEnabled       ← false ( Special Selection)
├── schedulingRequired      ← false (Special Selection)
│
├── recipe[]                ← Ingredientes para cálculo costo
│   └── ProductRecipe
│       ├── supplyVariantId ← Pollo troceado
│       └── requiredQuantity← 0.5 kg
│
├── optionIds[]             ← Opciones disponibles
│   └── ProductOption
│       ├── id
│       ├── name            ← "Tocineta Extra"
│       ├── extraPrice      ← $3,000
│       └── category        ← ADD_ON
│
└── optionGroupIds[]        ← Grupos de opciones
    └── OptionGroup
        ├── id
        ├── name            ← "Proteína"
        ├── selectionType   ← SINGLE_CHOICE
        └── required        ← true
```

---

## 4. Cálculo de Costos

### 4.1 Costo de Receta (Material)

```
Costo Receta = Σ(required_quantity × unit_cost)

Ejemplo:
- Pollo: 0.5 kg × $8,000/kg = $4,000
- Arroz: 0.3 kg × $3,000/kg = $900
- Ají: 0.1 kg × $5,000/kg = $500
─────────────────────────────────
Costo Material = $5,400
```

### 4.2 Costo Laboral

```
Costo Laboral = (estimatedPrepMinutes / 60) × CostoHoraÁrea

Ejemplo:
- 15 min / 60 = 0.25 horas
- $8,000/hora (Cocina)
─────────────────────────────
Costo Laboral = $2,000
```

### 4.3 Costo Efectivo (con Opciones)

```
Costo Efectivo = Costo Receta + Costo Opciones Promedio

Ejemplo (con Single Choice):
- Sin extra: $5,400
- Con tocino: $5,400 + $1,500 = $6,900
- Promedio: ($5,400 + $6,900) / 2 = $6,150
```

---

## 5. Opciones de Producto

### Tipos de Selección

| Tipo | Cantidad | Pricing | Ejemplo |
|------|----------|---------|---------|
| `SINGLE_CHOICE` | Elegir 1 | +$0 | Tamaño: Personal/Grande |
| `MULTI_CHOICE` | Elegir varios | +$0 | Ingredientes extra |
| `ADD_ON` | Elegir varios | +extra_price | Tocineta, Queso |
| `REMOVAL` | Quitar | +$0 | Sin cebolla, Sin ají |

### Pricing de Opciones

```json
{
  "optionId": 15,
  "name": "Tocineta Extra",
  "extraPrice": 3000,
  "categorySelectionType": "ADD_ON"
}
```

### Reglas
- Solo 1 opción por grupo `SINGLE_CHOICE`
- `ADD_ON` suma al precio final
- `REMOVAL` no suma pero reduce costo

---

## 6. Imágenes

### Estructura
```
Image
├── id
├── entityType (PRODUCT)
├── entityId (productId)
├── originalUrl
├── thumbnailUrl (150px)
├── mediumUrl (300px)
└── largeUrl (600px)
```

### Endpoints
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/products/{id}/images` | Subir imagen |
| GET | `/api/v1/products/{id}/images` | Listar imágenes |
| PUT | `/api/v1/products/{id}/images` | Reemplazar imágenes |

---

## 7. Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/products` | Crear producto |
| PUT | `/api/v1/products/{id}` | Actualizar producto |
| GET | `/api/v1/products` | Listar productos (paginado, ?search=) |
| GET | `/api/v1/products/top-selling` | Top ventas |
| GET | `/api/v1/products/{id}` | Detalle de producto |
| PUT | `/api/v1/products/{id}/enable` | Habilitar |
| PUT | `/api/v1/products/{id}/disable` | Deshabilitar |
| GET | `/api/v1/products/{id}/options` | Opciones por categoría |
| GET | `/api/v1/products/{id}/option-groups` | Grupos de opciones |
| GET | `/api/v1/products/{id}/cost` | Costo de producción |
| GET | `/api/v1/products/{id}/cost-breakdown` | Desglose con proyecciones |

### Product Options
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/product-options` | Asociar opción a producto |
| PUT | `/api/v1/product-options/{id}` | Actualizar asociación |

---

## 8. Cost Breakdown (Desglose)

```bash
GET /api/v1/products/10/cost-breakdown
```

```json
{
  "productId": 10,
  "productName": "Hamburguesa Clásica",
  "baseCost": {
    "materialCost": 5400,
    "laborCost": 2000,
    "totalCost": 7400
  },
  "options": [
    {
      "optionId": 15,
      "name": "Tocineta",
      "categoryName": "Extras",
      "selectionType": "ADD_ON",
      "extraPrice": 3000,
      "materialCost": 1500
    }
  ],
  "projectedOptionCost": 1500,
  "projectedEffectiveCost": 8900,
  "basePrice": 25000,
  "margin": 16100
}
```

---

## 9. Notas para Frontend

### Listado de Productos
- Filtro por categoría
- Búsqueda por nombre
- Badge de estado (activo/inactivo)
- Imagen principal

### Detalle de Producto
- Galería de imágenes
- Lista de opciones organizadas por grupo
- Precio base + opciones
- Tiempo estimado de preparación

### Crear/Editar
- Selector de categoría
- Campos de precio y costo
- Editor de receta (insumos)
- Gestor de opciones
- Subida de imágenes
