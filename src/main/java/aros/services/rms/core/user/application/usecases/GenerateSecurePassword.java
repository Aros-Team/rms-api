/* (C) 2026 */
package aros.services.rms.core.user.application.usecases;

import java.security.SecureRandom;

public class GenerateSecurePassword {

  private static final String[] WORDS = {
    "Admin", "Root", "User", "Chef", "Chef", "Boss", "Super", "Mega", "Admin", "Root",
    "Master", "Super", "Chief", "Lead", "Head", "Chef", "Boss", "Super", "Admin", "Root"
  };

  private static final String SYMBOLS = "!@#+*$";
  private static final SecureRandom random = new SecureRandom();

  public static String execute() {
    String word = WORDS[random.nextInt(WORDS.length)];
    int number = 100 + random.nextInt(900);
    char symbol = SYMBOLS.charAt(random.nextInt(SYMBOLS.length()));

    return word + number + symbol;
  }
}
