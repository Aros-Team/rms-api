/* (C) 2026 */

package aros.services.rms.seed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable constants of the deterministic seed v1 dataset.
 *
 * <p>Every number here comes from {@code docs/sprint-0/seed-spec.md}. The generator never reads
 * random values; all variability is derived from {@link #SEED} and the anchor date.
 */
final class SeedConstants {

  static final long SEED = 20261121L;
  static final String SEED_STRING = Long.toString(SEED);
  static final String VERSION = "seed-v1";
  static final String DATASET = "dev";
  static final int DAYS = 180;
  static final int HISTORY_DAYS = DAYS - 1;

  static final int KITCHEN_AREA = 1;
  static final int SERVICE_AREA = 2;
  static final int BAR_AREA = 3;
  static final int CASH_AREA = 4;

  static final int BODEGA_LOCATION = 1;
  static final int COCINA_LOCATION = 2;
  static final int CUARTO_FRIO_LOCATION = 3;
  static final int BAR_LOCATION = 4;

  static final long SEED_USER_FIRST = 1001L;
  static final long SEED_USER_LAST = 1017L;
  static final long ADMIN_ID = 1007L;

  static final long HISTORY_ORDER_ID_BASE = 100_000L;
  static final long OPERATIONAL_ORDER_ID_BASE = 900_000L;
  static final long HISTORY_DETAIL_ID_BASE = 200_000L;
  static final long OPERATIONAL_DETAIL_ID_BASE = 920_000L;

  static final BigDecimal FULL_MONTH_LABOR = new BigDecimal("33500000.00");
  static final BigDecimal FOH_LABOR = new BigDecimal("14400000.00");
  static final BigDecimal BOH_LABOR = new BigDecimal("19100000.00");

  private static final int[] BASE_BY_DOW = {78, 74, 82, 92, 118, 132, 106};
  private static final int[] SEASON_BY_MONTH = {
    90, 92, 96, 100, 105, 102, 98, 95, 97, 104, 110, 115
  };

  /** Product catalogue: price, weight (sum 100 over the 24 non-combo SKUs) and recipe cost. */
  static final List<ProductSpec> PRODUCTS = buildProducts();

  /** Fast lookup by product id. */
  static final Map<Integer, ProductSpec> PRODUCT_BY_ID = indexProducts();

  /** Products sold by weight, i.e. every non-combo SKU. */
  static final List<ProductSpec> WEIGHTED_PRODUCTS =
      PRODUCTS.stream().filter(p -> p.weight() > 0).toList();

  static final int COMBO_BASE_ID = 22;

  /** Workers: id, role, area, salary, bonus, deduction. */
  static final List<WorkerSpec> WORKERS =
      List.of(
          new WorkerSpec(1001, "Mesero 1", SERVICE_AREA, 2_500_000, 0, 50_000),
          new WorkerSpec(1002, "Mesero 2", SERVICE_AREA, 2_500_000, 0, 50_000),
          new WorkerSpec(1003, "Cocinero 1", KITCHEN_AREA, 2_800_000, 200_000, 50_000),
          new WorkerSpec(1004, "Cocinero 2", KITCHEN_AREA, 2_800_000, 200_000, 50_000),
          new WorkerSpec(1005, "Cocinero 3", KITCHEN_AREA, 2_800_000, 0, 50_000),
          new WorkerSpec(1006, "Cocinero 4", KITCHEN_AREA, 2_800_000, 0, 50_000),
          new WorkerSpec(1011, "Mesero 3", SERVICE_AREA, 2_500_000, 0, 50_000),
          new WorkerSpec(1012, "Mesero 4", SERVICE_AREA, 2_500_000, 0, 50_000),
          new WorkerSpec(1013, "Cajero 1", CASH_AREA, 2_200_000, 0, 50_000),
          new WorkerSpec(1014, "Cajero 2", CASH_AREA, 2_200_000, 0, 50_000),
          new WorkerSpec(1015, "Cocinero 5", KITCHEN_AREA, 2_800_000, 0, 50_000),
          new WorkerSpec(1016, "Cocinero 6", KITCHEN_AREA, 2_800_000, 0, 50_000),
          new WorkerSpec(1017, "Auxiliar de cocina", KITCHEN_AREA, 1_900_000, 0, 50_000));

  static final List<Long> PAYROLL_ADMINS = List.of(1007L, 1008L);
  static final List<Long> QA_USERS = List.of(1009L, 1010L);

  static final List<String> SUPPLIER_NAMES =
      List.of(
          "Distribuidora El Trigal",
          "Cárnicos La Estancia",
          "Frutas del Quindío",
          "Lácteos del Valle",
          "Bebidas Andinas",
          "Licores del Eje",
          "Empaques Cartón S.A.",
          "Mariscos del Pacífico");

  private SeedConstants() {}

  static int baseForDayOfWeek(int dowMondayOne) {
    return BASE_BY_DOW[dowMondayOne - 1];
  }

  static int seasonForMonth(int month) {
    return SEASON_BY_MONTH[month - 1];
  }

  /** Cost breakdown in COP for one unit. */
  record Cost(BigDecimal food, BigDecimal beverage, BigDecimal alcohol, BigDecimal other) {
    BigDecimal total() {
      return food.add(beverage).add(alcohol).add(other);
    }

    Cost plus(Cost otherCost) {
      return new Cost(
          food.add(otherCost.food),
          beverage.add(otherCost.beverage),
          alcohol.add(otherCost.alcohol),
          other.add(otherCost.other));
    }
  }

  /** A catalogue product. */
  record ProductSpec(
      int id, String name, int price, int weight, int recipeLines, Cost cost, boolean combo) {}

  /** A payroll worker. */
  record WorkerSpec(long id, String name, int areaId, int salary, int bonus, int deduction) {}

  private static List<ProductSpec> buildProducts() {
    List<ProductSpec> list = new ArrayList<>();
    list.add(product(1, "Hamburguesa Double Bacon Cheese", 28_000, 8, 8, "13330", "0", "0", "0"));
    list.add(product(2, "Pizza Especial Familiar", 38_000, 4, 7, "10685", "0", "0", "0"));
    list.add(product(3, "Pasta de la Casa", 22_000, 5, 5, "6575", "0", "0", "0"));
    list.add(product(4, "Corte de Res Premium", 55_000, 3, 5, "16212.5", "0", "0", "0"));
    list.add(product(5, "Limonada / Jugo Natural", 8_000, 10, 5, "680", "187.5", "0", "950"));
    list.add(product(6, "Hamburguesa Clásica", 18_000, 7, 4, "7980", "0", "0", "0"));
    list.add(product(7, "Hamburguesa BBQ", 32_000, 5, 8, "13192.5", "0", "0", "0"));
    list.add(product(8, "Hamburguesa Veggie", 22_000, 3, 7, "8570", "0", "0", "0"));
    list.add(product(9, "Bandeja Paisa", 25_000, 6, 8, "6791", "0", "0", "0"));
    list.add(product(10, "Sancocho de Gallina", 22_000, 3, 8, "6971", "0", "0", "0"));
    list.add(product(11, "Ajiaco Santafereño", 20_000, 4, 8, "9516", "0", "0", "0"));
    list.add(product(12, "Pizza Personal", 22_000, 5, 6, "3295", "0", "0", "0"));
    list.add(product(13, "Pizza Vegetariana", 32_000, 3, 9, "4790", "0", "0", "0"));
    list.add(product(14, "Pizza Hawaiana", 30_000, 4, 7, "5160", "0", "0", "0"));
    list.add(product(15, "Lasagna Clásica", 25_000, 4, 5, "9225", "0", "0", "0"));
    list.add(product(16, "Spaghetti Carbonara", 24_000, 5, 5, "4365", "0", "0", "0"));
    list.add(product(17, "Costillas BBQ", 42_000, 2, 6, "12137.5", "0", "0", "0"));
    list.add(product(18, "Lomo de Cerdo", 38_000, 3, 5, "8652.5", "0", "0", "0"));
    list.add(product(19, "Malteada", 12_000, 4, 5, "1180", "124.88", "0", "950"));
    list.add(product(20, "Agua de Coco", 6_000, 3, 4, "540", "75", "0", "950"));
    list.add(product(21, "Té Helado", 7_000, 3, 5, "680", "187.5", "0", "2100"));
    list.add(product(22, "Almuerzo Ejecutivo", 29_900, 0, 0, "8200", "400", "0", "600", true));
    list.add(product(23, "Combo Hamburguesa", 34_900, 0, 0, "7600", "300", "0", "800", true));
    list.add(product(24, "Combo Familiar", 89_900, 0, 0, "30000", "2500", "0", "2000", true));
    list.add(product(25, "Cerveza Artesanal", 12_000, 2, 3, "0", "0", "3600", "0"));
    list.add(product(26, "Copa de Vino", 18_000, 2, 3, "0", "0", "5400", "0"));
    list.add(product(27, "Cóctel de la Casa", 26_000, 2, 3, "0", "0", "6500", "0"));
    return list;
  }

  private static ProductSpec product(
      int id,
      String name,
      int price,
      int weight,
      int lines,
      String f,
      String b,
      String a,
      String o) {
    return new ProductSpec(
        id,
        name,
        price,
        weight,
        lines,
        new Cost(new BigDecimal(f), new BigDecimal(b), new BigDecimal(a), new BigDecimal(o)),
        false);
  }

  private static ProductSpec product(
      int id,
      String name,
      int price,
      int weight,
      int lines,
      String f,
      String b,
      String a,
      String o,
      boolean combo) {
    return new ProductSpec(
        id,
        name,
        price,
        weight,
        lines,
        new Cost(new BigDecimal(f), new BigDecimal(b), new BigDecimal(a), new BigDecimal(o)),
        combo);
  }

  private static Map<Integer, ProductSpec> indexProducts() {
    Map<Integer, ProductSpec> map = new LinkedHashMap<>();
    for (ProductSpec spec : PRODUCTS) {
      map.put(spec.id(), spec);
    }
    return Map.copyOf(map);
  }
}
