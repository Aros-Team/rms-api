/* (C) 2026 */
package aros.services.rms.core.user.application.usecases;

import java.security.SecureRandom;

public class GenerateSecurePassword {

  private static final String[] WORDS = {
    "Table", "Table", "Table", "Table", "Table", "Chair", "Chair", "Chair", "Chair", "Plate",
    "Plate", "Plate", "Glass", "Glass", "Glass", "Fork", "Knife", "Spoon", "Menu", "Menu", "Menu",
    "Order", "Order", "Order", "Check", "Check", "Bill", "Toast", "Toast", "Salad", "Soup", "Soup",
    "Bread", "Rice", "Pasta", "Beans", "Steak", "Steak", "Grill", "Roast", "Steam", "Bake", "Slice",
    "Dice", "Mix", "Pour", "Fill", "Pour", "Seat", "Serve", "Cater", "Cook", "Prep", "Brew", "Dish",
    "Dish", "Chef", "Menu", "Order", "Table", "Guest", "Visit", "Dine", "Lunch", "Dinner", "Brunch",
    "Taste", "Flavor", "Aroma", "Spice", "Herb", "Sauce", "Sauté", "Broil", "Roast", "Slice",
    "Chop", "Blend", "Whisk", "Fold", "Knead", "Rise", "Proof", "Heat", "Cool", "Chill", "Freeze",
    "Mince", "Grind", "Crush"
  };

  private static final String SYMBOLS = "!@#+*$%&";
  private static final SecureRandom random = new SecureRandom();

  public static String execute() {
    String word = WORDS[random.nextInt(WORDS.length)];
    int number = 100 + random.nextInt(900);
    char symbol = SYMBOLS.charAt(random.nextInt(SYMBOLS.length()));

    return word + number + symbol;
  }
}
