/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Service for generating secure random passwords. */
public class GenerateSecurePasswordService {

  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String DIGITS = "0123456789";
  private static final String SYMBOLS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
  private static final String ALL = LOWERCASE + UPPERCASE + DIGITS + SYMBOLS;

  private static final int MIN_LENGTH = 14;
  private static final int MAX_LENGTH = 16;

  private static final SecureRandom random = new SecureRandom();

  private GenerateSecurePasswordService() {}

  /**
   * Generates a secure random password meeting all complexity requirements.
   *
   * @return a random password with upper, lower, digit, and symbol characters
   */
  public static String execute() {
    int length = MIN_LENGTH + random.nextInt(MAX_LENGTH - MIN_LENGTH + 1);
    List<Character> chars = new ArrayList<>(length);

    chars.add(pick(LOWERCASE));
    chars.add(pick(UPPERCASE));
    chars.add(pick(DIGITS));
    chars.add(pick(SYMBOLS));

    for (int i = 4; i < length; i++) {
      chars.add(pick(ALL));
    }

    Collections.shuffle(chars, random);

    StringBuilder sb = new StringBuilder(length);
    for (char c : chars) {
      sb.append(c);
    }
    return sb.toString();
  }

  private static char pick(String pool) {
    return pool.charAt(random.nextInt(pool.length()));
  }
}
