/* (C) 2026 */

package aros.services.rms.seed;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Deterministic CRC-32 primitives shared by the generator, the verifier and the tests.
 *
 * <p>The string keys mirror the reference implementation in {@code docs/sprint-0/seed-spec.md};
 * they must never change without a new seed version.
 */
final class SeedMath {

  private static final int[] TABLE_CAPACITIES = {
    2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 6, 6, 6, 8
  };

  private SeedMath() {}

  static long crc(String value) {
    CRC32 crc = new CRC32();
    crc.update(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    return crc.getValue();
  }

  static int modulo(long value, int divisor) {
    return (int) (value % divisor);
  }

  /** {@code crc(seed + ":day:" + d) mod 13 - 6}. */
  static int dayJitter(int d) {
    return modulo(crc(SeedConstants.SEED_STRING + ":day:" + d), 13) - 6;
  }

  static int hash(String tag, int d, int s) {
    return (int) (crc(SeedConstants.SEED_STRING + ":" + tag + ":" + d + ":" + s) & 0x7fffffffL);
  }

  static int hash100(String tag, int d, int s) {
    return modulo(crc(SeedConstants.SEED_STRING + ":" + tag + ":" + d + ":" + s), 100);
  }

  static int productHash(int d, int s, int k) {
    return modulo(crc(SeedConstants.SEED_STRING + ":i:" + d + ":" + s + ":" + k), 100);
  }

  static int tableHash(int d, int s) {
    return (int) (crc(SeedConstants.SEED_STRING + ":t:" + d + ":" + s) & 0x7fffffffL);
  }

  static int comboHash(int d, int s) {
    return modulo(crc(SeedConstants.SEED_STRING + ":combo:" + d + ":" + s), 3);
  }

  static int liveHash(int index, String tag) {
    return modulo(crc(SeedConstants.SEED_STRING + ":live:" + index + ":" + tag), 1_000_000);
  }

  /** Daily order count, including the closure, low-day and event overrides. */
  static int dailyVolume(LocalDate anchor, int d) {
    LocalDate date = anchor.minusDays(SeedConstants.HISTORY_DAYS).plusDays(d);
    int base = SeedConstants.baseForDayOfWeek(date.getDayOfWeek().getValue());
    int season = SeedConstants.seasonForMonth(date.getMonthValue());
    final int volume = Math.max(0, base * season / 100 + dayJitter(d));
    if (d >= 60 && d <= 62) {
      return 0;
    }
    if (d == 90) {
      return 18;
    }
    if (d == 135) {
      return 220;
    }
    return volume;
  }

  /** 0 = lunch, 1 = dinner, 2 = other. */
  static int part(int d, int s) {
    int r = hash100("o", d, s);
    if (r < 38) {
      return 0;
    }
    return r < 93 ? 1 : 2;
  }

  static int partySize(int d, int s) {
    int y = hash100("p", d, s);
    if (y <= 14) {
      return 1;
    }
    if (y <= 54) {
      return 2;
    }
    if (y <= 74) {
      return 3;
    }
    if (y <= 89) {
      return 4;
    }
    if (y <= 95) {
      return 5;
    }
    if (y <= 98) {
      return 6;
    }
    return 7;
  }

  static int durationMinutes(int d, int s, int part) {
    long jitter = crc(SeedConstants.SEED_STRING + ":dur:" + d + ":" + s);
    return switch (part) {
      case 0 -> 40 + (int) (jitter % 16);
      case 1 -> 55 + (int) (jitter % 36);
      default -> 30 + (int) (jitter % 16);
    };
  }

  static int lineCount(int d, int s) {
    return 2 + modulo(crc(SeedConstants.SEED_STRING + ":n:" + d + ":" + s), 4);
  }

  static boolean isComboOrder(int d, int s) {
    return hash100("c", d, s) < 8;
  }

  static int comboProduct(int d, int s) {
    return SeedConstants.COMBO_BASE_ID + comboHash(d, s);
  }

  static int weightedProduct(int d, int s, int k) {
    int z = productHash(d, s, k);
    int cumulative = 0;
    for (SeedConstants.ProductSpec product : SeedConstants.WEIGHTED_PRODUCTS) {
      cumulative += product.weight();
      if (z < cumulative) {
        return product.id();
      }
    }
    return SeedConstants.WEIGHTED_PRODUCTS.getLast().id();
  }

  static int tableNumber(int d, int s, int partySize) {
    List<Integer> eligible = eligibleTables(partySize);
    return eligible.get(tableHash(d, s) % eligible.size());
  }

  static List<Integer> eligibleTables(int partySize) {
    int max = Math.max(1, partySize);
    return java.util.stream.IntStream.rangeClosed(1, 20)
        .filter(number -> TABLE_CAPACITIES[number - 1] >= max)
        .boxed()
        .toList();
  }

  static String timestamp(java.time.LocalDateTime value) {
    return value.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  static boolean isWeekend(DayOfWeek dayOfWeek) {
    return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
  }
}
