# 📋 NOTAS IMPORTANTES: Migración de Datos

## ⚠️ CONSIDERACIONES IMPORTANTES

### 1. Migración V3 - Nueva Columna product_id

La migración V3 agrega la columna `product_id` a la tabla `product_options`, pero **no la rellena automáticamente**.

```sql
ALTER TABLE product_options 
ADD COLUMN product_id BIGINT,
ADD FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
```

### 2. Datos Existentes

Si tienes datos existentes en `product_options`, necesitarás:

#### Opción A: Rellenar datos existentes manualmente (Recomendado)

Crea una nueva migración `V4__populate_product_options_product_id.sql`:

```sql
-- V4__populate_product_options_product_id.sql
-- Associate existing product options with their products

-- Ejemplo para opciones de tamaño (Bebidas)
UPDATE product_options SET product_id = 2 WHERE name = 'Small' AND option_category_id = 1;
UPDATE product_options SET product_id = 2 WHERE name = 'Medium' AND option_category_id = 1;
UPDATE product_options SET product_id = 2 WHERE name = 'Large' AND option_category_id = 1;

-- Ejemplo para extras (Hamburguesas)
UPDATE product_options SET product_id = 1 WHERE name = 'Extra Cheese' AND option_category_id = 2;
UPDATE product_options SET product_id = 1 WHERE name = 'Bacon' AND option_category_id = 2;

-- Verifica que todas las opciones tengan product_id asignado
-- SELECT * FROM product_options WHERE product_id IS NULL;
```

#### Opción B: Usar null como opciones globales

Si deseas que algunas opciones sean reutilizables por múltiples productos, mantén `product_id = null`:

```sql
-- Las opciones con product_id = null pueden ser usadas por cualquier producto
-- Pero se recomienda hacer la validación en el código
```

### 3. Script de Limpieza (Si es necesario)

```sql
-- Identificar opciones huérfanas (sin producto)
SELECT id, name, product_id FROM product_options WHERE product_id IS NULL;

-- Si tienes opciones globales, verifica que estén siendo usadas
SELECT po.id, po.name, COUNT(odo.option_id) as usage_count
FROM product_options po
LEFT JOIN order_detail_options odo ON po.id = odo.option_id
WHERE po.product_id IS NULL
GROUP BY po.id, po.name
HAVING usage_count = 0;
```

---

## 🔄 PASOS DE DESPLIEGUE

1. **Respaldar Base de Datos**
   ```bash
   # Antes de cualquier cambio, respalda tu BD
   mysqldump -u root -p rmsdb > backup_$(date +%Y%m%d).sql
   ```

2. **Aplicar Migración V3**
   ```bash
   ./gradlew flywayMigrate
   # O simplemente ejecuta la aplicación, Flyway se ejecuta automáticamente
   ```

3. **Verificar Columna Creada**
   ```sql
   DESCRIBE product_options;
   -- Debe mostrar: product_id BIGINT
   ```

4. **Rellenar Datos (Si es necesario)**
   ```sql
   -- Ejecutar script de población manual o migración V4
   ```

5. **Verificar Integridad Referencial**
   ```sql
   SELECT po.id, po.name, po.product_id, p.name as product_name
   FROM product_options po
   LEFT JOIN products p ON po.product_id = p.id
   WHERE po.product_id IS NOT NULL
   LIMIT 10;
   ```

---

## 🚨 ROLLBACK (Si es necesario)

Si necesitas revertir los cambios:

```sql
-- OPCIÓN 1: Usar Flyway para revertir a V2
-- (Requiere usar versiones de Flyway que soportan undo)
./gradlew flywayUndo

-- OPCIÓN 2: Manual (si Flyway undo no está disponible)
ALTER TABLE product_options DROP FOREIGN KEY product_options_ibfk_3;
ALTER TABLE product_options DROP COLUMN product_id;

-- Luego revertir cambios en código Java
```

---

## 📊 VERIFICACIÓN POST-MIGRACIÓN

Ejecuta estos queries para verificar que todo está bien:

```sql
-- 1. Verificar columna producto_id existe
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'product_options' AND COLUMN_NAME = 'product_id';

-- 2. Contar opciones por producto
SELECT 
    p.id,
    p.name,
    COUNT(po.id) as option_count
FROM products p
LEFT JOIN product_options po ON p.id = po.product_id
WHERE p.active = TRUE
GROUP BY p.id, p.name
ORDER BY p.id;

-- 3. Identificar opciones huérfanas
SELECT id, name FROM product_options WHERE product_id IS NULL;

-- 4. Verificar foreign keys
SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'product_options' AND COLUMN_NAME = 'product_id';
```

---

## 🔐 CONSIDERACIONES DE SEGURIDAD

1. **Validación Dual**: La validación en el código previene opciones inválidas incluso si no hay `product_id`
2. **Foreign Key**: La BD también refuerza la integridad referencial
3. **Transacciones**: Los cambios en órdenes son atómicos

---

## 💡 RECOMENDACIONES

1. ✅ **Respalda la BD** antes de cualquier migración
2. ✅ **Rellena datos existentes** inmediatamente después de V3
3. ✅ **Valida integridad referencial** post-migración
4. ✅ **Prueba en ambiente de staging** antes de producción
5. ✅ **Monitorea logs** de Flyway durante startup

---

## 📚 REFERENCIAS

- Archivo de migración: `src/main/resources/db/migration/V3__add_product_id_to_product_options.sql`
- Documentación Flyway: https://flywaydb.org/
- MySQL Foreign Keys: https://dev.mysql.com/doc/refman/8.0/en/foreign-keys.html


