/* (C) 2026 */
package aros.services.rms.core.user.application.usecases;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GenerateSecurePassword {

  private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String DIGITS = "0123456789";
  private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
  private static final int LENGTH = 16;

  public static String execute() {
    String allChars = UPPERCASE + LOWERCASE + DIGITS + SYMBOLS;
    SecureRandom random = new SecureRandom();

    Set<Character> password = new HashSet<>();
    password.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
    password.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
    password.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
    password.add(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));

    while (password.size() < LENGTH) {
      password.add(allChars.charAt(random.nextInt(allChars.length())));
    }

    return password.stream().map(String::valueOf).collect(Collectors.joining());
  }
}
