import { expect, test } from "@playwright/test";

test("public marketplace pages render", async ({ page }) => {
  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "Найдите мастера в своём городе" }),
  ).toBeVisible();
  await expect(
    page.getByRole("heading", { name: "Выберите категорию" }),
  ).toBeVisible();

  await page.goto("/categories");
  await expect(
    page.getByRole("heading", { name: "Категории услуг" }),
  ).toBeVisible();

  await page.goto("/tasks");
  await expect(
    page.getByRole("heading", { name: "Открытые задачи" }),
  ).toBeVisible();

  await page.goto("/performers");
  await expect(
    page.getByRole("heading", { name: "Исполнители HandyNest" }),
  ).toBeVisible();
});

test("registration exposes current required consents", async ({ page }) => {
  await page.goto("/register");
  await expect(
    page.getByRole("heading", { name: "Создать аккаунт" }),
  ).toBeVisible();
  await expect(page.getByText(/Условия использования/)).toBeVisible();
  await expect(page.getByText(/Политика конфиденциальности/)).toBeVisible();
});
