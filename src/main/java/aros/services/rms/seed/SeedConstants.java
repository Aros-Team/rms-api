/* (C) 2026 */

package aros.services.rms.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

  /**
   * Numeric-only document for a seeded user.
   *
   * <p>The worker create/update forms and the API validate {@code document} as digits only, so the
   * seed must not use readable prefixes such as {@code SEED-1007}. The value is deterministic and
   * unique per user id: {@code 1000001011} for worker 1011.
   */
  static String seedDocument(long userId) {
    return String.format(Locale.ROOT, "1%09d", userId);
  }

  static final long HISTORY_ORDER_ID_BASE = 100_000L;
  static final long OPERATIONAL_ORDER_ID_BASE = 900_000L;
  static final long HISTORY_DETAIL_ID_BASE = 200_000L;
  static final long OPERATIONAL_DETAIL_ID_BASE = 920_000L;

  /** Days of closed cash shifts / payments behind the anchor day (P3-BE-PAY-08). */
  static final int PAYMENT_HISTORY_DAYS = 30;

  /** Minimum orders written for the anchor day, regardless of seed time. */
  static final int MIN_TODAY_ORDERS = 80;

  /** Drawer float used by every seeded cash shift. */
  static final BigDecimal OPENING_FLOAT = new BigDecimal("200000.00");

  static final long CASHIER_ONE = 1012L;

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

  /** Workers with their area, pay and weekly shift pattern. */
  static final List<WorkerSpec> WORKERS =
      List.of(
          new WorkerSpec(
              1011,
              "Camila Navarro",
              "Jefa de cocina",
              KITCHEN_AREA,
              3_800_000,
              200_000,
              50_000,
              shifts(tuesdayToFriday(8, 8), shift(DayOfWeek.SATURDAY, 8, 8))),
          new WorkerSpec(
              1012,
              "Bruno Castillo",
              "Cajero",
              CASH_AREA,
              2_400_000,
              0,
              50_000,
              shifts(tuesdayToFriday(14, 8), shift(DayOfWeek.SATURDAY, 9, 8))),
          new WorkerSpec(
              1013,
              "Renata Flores",
              "Mesera",
              SERVICE_AREA,
              2_300_000,
              0,
              50_000,
              shifts(
                  wednesdayToFriday(12, 8),
                  shift(DayOfWeek.SATURDAY, 9, 8),
                  shift(DayOfWeek.SUNDAY, 9, 8))),
          new WorkerSpec(
              1014,
              "Diego Salas",
              "Mesero",
              SERVICE_AREA,
              1_800_000,
              0,
              50_000,
              shifts(tuesdayToFriday(10, 6), shift(DayOfWeek.SATURDAY, 11, 6))),
          new WorkerSpec(
              1015,
              "Mateo Rivas",
              "Cocinero",
              KITCHEN_AREA,
              1_750_000,
              0,
              50_000,
              shifts(tuesdayToFriday(8, 6), shift(DayOfWeek.SATURDAY, 8, 6))),
          new WorkerSpec(
              1016,
              "Tomás Méndez",
              "Cocinero",
              KITCHEN_AREA,
              1_750_000,
              0,
              50_000,
              shifts(tuesdayToFriday(16, 6), shift(DayOfWeek.SATURDAY, 11, 6))),
          new WorkerSpec(
              1017,
              "Valentina Soto",
              "Auxiliar de cocina",
              KITCHEN_AREA,
              1_300_000,
              0,
              50_000,
              List.of(
                  shift(DayOfWeek.FRIDAY, 8, 6),
                  shift(DayOfWeek.SATURDAY, 8, 6),
                  shift(DayOfWeek.SUNDAY, 8, 6))));

  /** Gross monthly labor (base + fixed bonus) derived from the roster. */
  static final BigDecimal FULL_MONTH_LABOR = grossLabor(null);

  /** Front-of-house gross labor: service + cash. */
  static final BigDecimal FOH_LABOR = grossLabor(List.of(SERVICE_AREA, CASH_AREA));

  /** Back-of-house gross labor: kitchen + bar. */
  static final BigDecimal BOH_LABOR = grossLabor(List.of(KITCHEN_AREA, BAR_AREA));

  static final List<Long> PAYROLL_ADMINS = List.of(1007L, 1008L);

  /** Purchase demo depth (P4-INV-06/07): 12 suppliers, 60 orders over the 180-day window. */
  static final int PURCHASE_ORDER_COUNT = 60;

  static final int RECEIVED_PURCHASE_ORDERS = 59;
  static final int CONFIRMED_PURCHASE_ORDERS = 1;

  /**
   * Legacy purchase status tokens. V61 promotes the status to a typed column; the seed still writes
   * these prefixes to {@code notes} so older clients and the status-token checks keep working.
   */
  static final String PURCHASE_NOTES_RECEIVED = "RECIBIDA";

  static final String PURCHASE_NOTES_CONFIRMED = "CONFIRMADA";

  /** Typed purchase status values persisted in {@code purchase_orders.status} (V61). */
  static final String PURCHASE_ORDER_STATUS_RECEIVED = "RECEIVED";

  static final String PURCHASE_ORDER_STATUS_CONFIRMED = "CONFIRMED";

  /**
   * Deterministic supplier master. {@code categoryIds} and {@code supplyIds} describe what each
   * supplier sells; {@code excludedSupplyIds} removes a supply that belongs to another vendor (the
   * seafood vendor owns the shrimp that also sits in the protein category). One supplier is
   * inactive so the UI can exercise the inactive state.
   */
  static final List<SupplierSpec> SUPPLIERS =
      List.of(
          new SupplierSpec(
              1,
              "Distribuidora El Trigal",
              "Cra. 19 #22-14, Armenia, Quindío · +57 606 745 1234 · pedidos@eltrigal.co",
              true,
              List.of(5, 6, 9, 13, 14),
              List.of(),
              List.of()),
          new SupplierSpec(
              2,
              "Cárnicos La Estancia",
              "Calle 8 #14-52, Armenia, Quindío · +57 606 741 8820 · ventas@laestancia.co",
              true,
              List.of(1, 7),
              List.of(),
              List.of(6L)),
          new SupplierSpec(
              3,
              "Frutas del Quindío",
              "Km 3 vía Armenia–Calarcá, Quindío · +57 315 482 7710 · pedidos@frutasquindio.co",
              true,
              List.of(2, 8),
              List.of(),
              List.of()),
          new SupplierSpec(
              4,
              "Lácteos del Valle",
              "Cra. 5 #31-08, Cali, Valle del Cauca · +57 602 485 3311 · comercial@lacteosvalle.co",
              true,
              List.of(3),
              List.of(),
              List.of()),
          new SupplierSpec(
              5,
              "Bebidas Andinas",
              "Calle 26 #68-41, Bogotá D.C. · +57 601 412 9080 · distribucion@bebidasandinas.co",
              true,
              List.of(15),
              List.of(),
              List.of()),
          new SupplierSpec(
              6,
              "Licores del Eje",
              "Cra. 23 #18-77, Pereira, Risaralda · +57 606 344 2100 · ventas@licoreseje.co",
              true,
              List.of(16),
              List.of(),
              List.of()),
          new SupplierSpec(
              7,
              "Empaques Cartón S.A.",
              "Zona Industrial Km 2, Dosquebradas, Risaralda · +57 606 366 4410 · "
                  + "servicio@empaquescarton.co",
              true,
              List.of(10),
              List.of(),
              List.of()),
          new SupplierSpec(
              8,
              "Mariscos del Pacífico",
              "Muelle Buenaventura, Bodega 7, Valle del Cauca · +57 602 241 5566 · "
                  + "pedidos@mariscospacifico.co",
              true,
              List.of(),
              List.of(6L),
              List.of()),
          new SupplierSpec(
              9,
              "Panadería La Espiga",
              "Calle 20 #12-45, Armenia, Quindío · +57 606 746 0192 · hola@laespiga.co",
              true,
              List.of(4),
              List.of(),
              List.of()),
          new SupplierSpec(
              10,
              "Abarrotes del Sur",
              "Cra. 8 #16-30, Ibagué, Tolima · +57 608 263 7744 · pedidos@abarrotessur.co",
              true,
              List.of(12),
              List.of(),
              List.of()),
          new SupplierSpec(
              11,
              "Congelados del Norte",
              "Autopista Norte Km 12, Medellín, Antioquia · +57 604 481 2299 · "
                  + "ventas@congeladosnorte.co",
              true,
              List.of(11),
              List.of(),
              List.of()),
          new SupplierSpec(
              12,
              "Distribuidora Andina de Insumos",
              "Cra. 14 #9-21, Manizales, Caldas · +57 606 884 1177 · contacto@andina-insumos.co",
              false,
              List.of(),
              List.of(),
              List.of()));

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

  /** A payroll worker and the recurring weekly shifts it works. */
  record WorkerSpec(
      long id,
      String name,
      String jobTitle,
      int areaId,
      int salary,
      int bonus,
      int deduction,
      List<ShiftSpec> shifts) {

    int weeklyHours() {
      return shifts.stream().mapToInt(ShiftSpec::hours).sum();
    }

    int expectedHoursPerMonth() {
      return (int) Math.round(weeklyHours() * 52.0 / 12.0);
    }

    ShiftSpec shiftOn(DayOfWeek day) {
      return shifts.stream().filter(shift -> shift.day() == day).findFirst().orElse(null);
    }
  }

  /** One recurring weekly shift. */
  record ShiftSpec(DayOfWeek day, LocalTime start, int hours) {}

  private static ShiftSpec shift(DayOfWeek day, int startHour, int hours) {
    return new ShiftSpec(day, LocalTime.of(startHour, 0), hours);
  }

  private static List<ShiftSpec> tuesdayToFriday(int startHour, int hours) {
    return List.of(
        shift(DayOfWeek.TUESDAY, startHour, hours),
        shift(DayOfWeek.WEDNESDAY, startHour, hours),
        shift(DayOfWeek.THURSDAY, startHour, hours),
        shift(DayOfWeek.FRIDAY, startHour, hours));
  }

  private static List<ShiftSpec> wednesdayToFriday(int startHour, int hours) {
    return List.of(
        shift(DayOfWeek.WEDNESDAY, startHour, hours),
        shift(DayOfWeek.THURSDAY, startHour, hours),
        shift(DayOfWeek.FRIDAY, startHour, hours));
  }

  private static List<ShiftSpec> shifts(List<ShiftSpec> base, ShiftSpec... extra) {
    List<ShiftSpec> all = new ArrayList<>(base);
    all.addAll(List.of(extra));
    return List.copyOf(all);
  }

  private static BigDecimal grossLabor(List<Integer> areaIds) {
    BigDecimal total = BigDecimal.ZERO;
    for (WorkerSpec worker : WORKERS) {
      if (areaIds == null || areaIds.contains(worker.areaId())) {
        total = total.add(BigDecimal.valueOf(worker.salary() + (long) worker.bonus()));
      }
    }
    return total.setScale(2, RoundingMode.HALF_UP);
  }

  /** A supplier and the catalogue slice it delivers. */
  record SupplierSpec(
      long id,
      String name,
      String contact,
      boolean active,
      List<Integer> categoryIds,
      List<Long> supplyIds,
      List<Long> excludedSupplyIds) {}

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
