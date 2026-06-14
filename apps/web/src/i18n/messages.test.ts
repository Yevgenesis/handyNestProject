import ru from "@/messages/ru.json";
import uz from "@/messages/uz.json";
import { describe, expect, it } from "vitest";

function collectLeafKeys(value: unknown, prefix = ""): string[] {
  if (value === null || typeof value !== "object") {
    return [prefix];
  }

  return Object.entries(value).flatMap(([key, child]) =>
    collectLeafKeys(child, prefix ? `${prefix}.${key}` : key),
  );
}

describe("active locale dictionaries", () => {
  it("keeps Russian and Uzbek dictionaries in key parity", () => {
    expect(collectLeafKeys(uz).sort()).toEqual(collectLeafKeys(ru).sort());
  });

  it("does not leave Russian Cyrillic copy in the Uzbek Latin dictionary", () => {
    expect(JSON.stringify(uz)).not.toMatch(/[А-Яа-яЁё]/);
  });
});
