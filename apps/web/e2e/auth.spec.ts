import { expect, test } from "@playwright/test";

test("login, restore session after reload and logout", async ({ page }) => {
  await page.goto("/login");
  await page.getByLabel("Электронная почта").fill("customer@handynest.dev");
  await page.getByLabel("Пароль").fill("Test121314#");
  await page.getByRole("button", { name: "Войти" }).click();

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(
    page.getByRole("heading", { name: /Здравствуйте/ }),
  ).toBeVisible();

  await page.reload();
  await expect(
    page.getByRole("heading", { name: /Здравствуйте/ }),
  ).toBeVisible();

  const logout = page.getByRole("button", { name: "Выйти" });
  if (await logout.isVisible()) await logout.click();
  else {
    await page.getByRole("button", { name: "Открыть меню" }).click();
    await page.getByRole("button", { name: "Выйти" }).click();
  }
  await page.goto("/dashboard");
  await expect(page).toHaveURL(/\/login$/);
});
