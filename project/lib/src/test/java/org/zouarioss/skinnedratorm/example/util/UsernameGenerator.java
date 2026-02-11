package org.zouarioss.skinnedratorm.example.util;

public class UsernameGenerator {

  public static String generate(String email) {
    if (email == null || email.isBlank()) {
      return "user_" + System.currentTimeMillis();
    }

    String prefix = email.split("@")[0];
    String sanitized = prefix.replaceAll("[^a-zA-Z0-9]", "_");

    return sanitized + "_" + System.currentTimeMillis();
  }
}
