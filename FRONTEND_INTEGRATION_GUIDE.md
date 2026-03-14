# 📖 GUÍA DE INTEGRACIÓN: Cambios en Lógica de Órdenes

## Para Desarrolladores Frontend

Los cambios realizados en la API impactan **directamente la validación de órdenes**. Por favor, sigue estas instrucciones al consumir el endpoint `POST /api/v1/orders`.

---

## ✅ CAMBIOS EN LA API

### Nuevo Comportamiento de Validación

**Antes**: Se aceptaban opciones de cualquier producto  
**Ahora**: Solo se aceptan opciones que pertenezcan al producto específico

---

## 📋 GUÍA DE CONSUMO DEL ENDPOINT

### Endpoint: `POST /api/v1/orders`

#### Estructura Correcta del Request

```json
{
  "tableId": 1,
  "details": [
    {
      "productId": 5,
      "instructions": "Sin cebolla",
      "selectedOptionIds": [10, 12]
    }
  ]
}
```

#### Validaciones Requeridas

| Campo | Validación | Ejemplo |
|-------|-----------|---------|
| `tableId` | Tabla debe existir y estar AVAILABLE | `1` |
| `productId` | Producto debe existir y estar activo | `5` |
| `selectedOptionIds` | Opciones DEBEN pertenecer al producto | `[10, 12]` |
| `instructions` | Opcional, máximo 500 caracteres | `"Sin cebolla"` |

---

## 🔴 NUEVOS CÓDIGOS DE ERROR

### 400 Bad Request - Opción no válida para el producto

**Cuando ocurre:**
```json
{
  "tableId": 1,
  "details": [{
    "productId": 5,  // Hamburguesa
    "selectedOptionIds": [99]  // Opción que pertenece a Bebidas
  }]
}
```

**Respuesta:**
```json
{
  "code": 400,
  "message": "Option 99 is not valid for product 5"
}
```

**Qué hacer**: Verificar que todas las opciones del request pertenezcan al producto seleccionado.

---

## 🛠️ RECOMENDACIONES DE IMPLEMENTACIÓN

### 1. Cargar Opciones Válidas por Producto

```javascript
// Obtener producto
GET /api/v1/products/5

// Respuesta incluye:
{
  "id": 5,
  "name": "Hamburguesa",
  "hasOptions": true,
  "options": [
    { "id": 10, "name": "Extra Queso", "productId": 5 },
    { "id": 12, "name": "Bacon", "productId": 5 }
  ]
}
```

### 2. Validar en el Frontend

```javascript
function validateOrderDetails(details, products) {
  for (const detail of details) {
    const product = products.find(p => p.id === detail.productId);
    
    if (!product) {
      throw new Error(`Producto ${detail.productId} no encontrado`);
    }
    
    if (product.hasOptions && (!detail.selectedOptionIds || detail.selectedOptionIds.length === 0)) {
      throw new Error(`${product.name} requiere opciones`);
    }
    
    if (!product.hasOptions && detail.selectedOptionIds && detail.selectedOptionIds.length > 0) {
      throw new Error(`${product.name} no soporta opciones`);
    }
    
    // ✨ NUEVA VALIDACIÓN
    if (detail.selectedOptionIds) {
      const validOptionIds = product.options.map(o => o.id);
      for (const optionId of detail.selectedOptionIds) {
        if (!validOptionIds.includes(optionId)) {
          throw new Error(`Opción ${optionId} no válida para ${product.name}`);
        }
      }
    }
  }
}
```

### 3. Flujo Completo Recomendado

```javascript
async function createOrder(tableId, items) {
  try {
    // 1. Validar tabla
    const table = await getTable(tableId);
    if (table.status !== 'AVAILABLE') {
      throw new Error('Mesa no disponible');
    }
    
    // 2. Cargar datos de productos
    const products = await getProducts();
    
    // 3. Preparar detalles de orden
    const details = items.map(item => ({
      productId: item.product.id,
      instructions: item.instructions,
      selectedOptionIds: item.options.map(o => o.id)
    }));
    
    // 4. Validar localmente (IMPORTANTE)
    validateOrderDetails(details, products);
    
    // 5. Enviar a API
    const response = await fetch('/api/v1/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        tableId,
        details
      })
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message); // "Option X is not valid for product Y"
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error al crear orden:', error.message);
    // Mostrar error al usuario
  }
}
```

---

## 📊 MATRIZ DE ESCENARIOS

| Escenario | hasOptions | selectedOptionIds | Resultado |
|-----------|-----------|-------------------|-----------|
| Bebida sin opciones | `false` | `null` o `[]` | ✅ Orden creada |
| Bebida con opciones | `false` | `[1, 2]` | ❌ Error 400 |
| Hamburguesa sin opciones | `true` | `null` o `[]` | ❌ Error 400 |
| Hamburguesa con opciones válidas | `true` | `[1, 2]` | ✅ Orden creada |
| Hamburguesa con opción inválida | `true` | `[99]` | ❌ Error 400 |
| Múltiples productos mixtos | Variado | Variado | ✅ o ❌ (según validación) |

---

## 🐛 TROUBLESHOOTING

### "Option X is not valid for product Y"

**Causa**: La opción X no pertenece al producto Y

**Solución**:
1. Verificar que `selectedOptionIds` solo contiene opciones del producto
2. Recargar datos de productos desde la API
3. Validar en el frontend antes de enviar

### "Product 'X' requires options to be selected"

**Causa**: Producto requiere opciones pero no se proporcionaron

**Solución**:
1. Verificar que `product.hasOptions = true`
2. Asegurar que `selectedOptionIds` no esté vacío
3. Seleccionar al menos una opción

### "Product 'X' does not support options"

**Causa**: Producto no soporta opciones pero se intentó enviar

**Solución**:
1. Verificar que `product.hasOptions = false`
2. No enviar `selectedOptionIds` o enviar `[]`

---

## 📚 REFERENCIAS ÚTILES

- **Endpoint GET Products**: `/api/v1/products?active=true`
- **Endpoint GET Tables**: `/api/v1/tables`
- **Endpoint POST Orders**: `/api/v1/orders`

---

## ❓ PREGUNTAS FRECUENTES

### ¿Qué pasa si envío null en selectedOptionIds?
Si el producto tiene opciones, será un error 400. Si no tiene opciones, se acepta.

### ¿Puedo enviar un array vacío en selectedOptionIds?
Sí, es equivalente a null. Si el producto tiene opciones, será error 400.

### ¿Cuál es el máximo de opciones por producto?
No hay límite definido, pero se valida que todas pertenezcan al producto.

### ¿La validación es caso-sensible?
La validación es por ID numérico, no por nombre.

---

## 📞 SOPORTE

Si encuentras algún problema:
1. Verifica los logs de la API para más detalles
2. Valida el JSON con el schema definido
3. Prueba con ejemplos simples primero

**Status API**: ✅ Producción Ready


