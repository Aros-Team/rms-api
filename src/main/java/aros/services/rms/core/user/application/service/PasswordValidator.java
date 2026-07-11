/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.user.application.exception.InvalidPasswordException;

/** Centralized password validation using character-type checks. */
public class PasswordValidator {

  private static final int MIN_LENGTH = 8;
  private static final int MAX_LENGTH = 64;

  private PasswordValidator() {}

  /**
   * Validates password and throws on failure.
   *
   * @param password the password to validate
   * @throws InvalidPasswordException if password does not meet requirements
   */
  public static void validate(String password) {
    if (!isValid(password)) {
      throw new InvalidPasswordException(
          "La contraseña debe tener entre 8 y 64 caracteres, incluir al menos"
              + " una mayúscula, una minúscula, un número y un símbolo");
    }
  }

  /**
   * Checks if password meets all requirements: length 8-64, at least one lowercase, one uppercase,
   * one digit, and one special character.
   */
  public static boolean isValid(String password) {
    if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
      return false;
    }
    boolean hasLower = false;
    boolean hasUpper = false;
    boolean hasDigit = false;
    boolean hasSymbol = false;
    for (int i = 0; i < password.length(); i++) {
      char c = password.charAt(i);
      if (Character.isLowerCase(c)) {
        hasLower = true;
      } else if (Character.isUpperCase(c)) {
        hasUpper = true;
      } else if (Character.isDigit(c)) {
        hasDigit = true;
      } else {
        hasSymbol = true;
      }
    }
    return hasLower && hasUpper && hasDigit && hasSymbol;
  }
}
