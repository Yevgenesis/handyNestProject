import { describe, expect, it } from "vitest";

import { isPasswordValid } from "@/features/auth/password-policy";

describe("password policy", () => {
  it.each([
    "ValidPass1!",
    "ValidPass1?",
    "ValidPass1_",
    "ValidPass1-",
    "Пароль1!A",
    "O‘zbekiston1!",
  ])("accepts a valid password: %s", (password) => {
    expect(isPasswordValid(password)).toBe(true);
  });

  it.each([
    "Short1!",
    "lowercase1!",
    "UPPERCASE1!",
    "NoDigits!",
    "NoSpecial1",
    "Has space1!",
    "ThisPasswordIsTooLong1!",
  ])("rejects an invalid password: %s", (password) => {
    expect(isPasswordValid(password)).toBe(false);
  });
});
