import path from "node:path";

import { expect, test, type APIRequestContext } from "@playwright/test";

const password = "Test121314#";

type ConsentRequirement = {
  type: string;
  documentVersion: string;
  requiredAtRegistration: boolean;
};

async function login(page: import("@playwright/test").Page, email: string) {
  await page.goto("/login");
  await page.getByLabel("Электронная почта").fill(email);
  await page.getByLabel("Пароль").fill(password);
  await page.getByRole("button", { name: "Войти" }).click();
  await expect(page).toHaveURL(/\/dashboard$/);
}

async function logout(page: import("@playwright/test").Page) {
  await page.getByRole("button", { name: "Выйти" }).click();
  await expect(
    page.getByRole("link", { name: "Войти", exact: true }),
  ).toBeVisible();
}

async function registerAccount(
  request: APIRequestContext,
  prefix: string,
  firstName: string,
  lastName: string,
) {
  const suffix = Date.now();
  const email = `${prefix}-${suffix}@handynest.dev`;
  const requirementsResponse = await request.get(
    "/api/v1/legal/consent-requirements",
  );
  expect(requirementsResponse.ok()).toBeTruthy();
  const requirements =
    (await requirementsResponse.json()) as ConsentRequirement[];
  const registration = await request.post("/api/v1/auth/register", {
    data: {
      firstName,
      lastName,
      email,
      password,
      passwordConfirmation: password,
      consents: requirements
        .filter((item) => item.requiredAtRegistration)
        .map((item) => ({
          type: item.type,
          documentVersion: item.documentVersion,
        })),
    },
  });
  expect(registration.status()).toBe(201);
  const body = (await registration.json()) as { accessToken?: string };
  expect(body.accessToken).toBeTruthy();
  return { email, accessToken: body.accessToken!, requirements };
}

async function onboardPerformer(page: import("@playwright/test").Page) {
  const account = await registerAccount(
    page.request,
    "e2e-performer",
    "E2E",
    "Исполнитель",
  );
  const suffix = Date.now();
  const email = account.email;
  const displayName = `E2E Мастер ${suffix}`;

  await page.goto("/performer/profile");
  await page.getByLabel("Отображаемое имя").fill(displayName);
  await page
    .getByLabel("О себе")
    .fill("Аккуратно выполняю бытовые и дизайнерские работы.");
  await page
    .getByLabel("Навыки и подход к работе")
    .fill("Монтаж, дизайн и консультации.");
  await page.getByLabel("Основной город").selectOption({ index: 1 });
  await page.getByLabel("Категория").selectOption({ label: "Сантехник" });
  await page.getByLabel("Опыт, лет").fill("4");
  await page.getByLabel("Цена от, сум").fill("5000");
  await page.getByLabel("Цена до, сум").fill("50000");
  const rules = page.getByRole("checkbox", {
    name: /актуальные правила исполнителя/i,
  });
  if (await rules.isVisible()) await rules.check();
  await page.getByRole("button", { name: "Создать профиль" }).click();
  await expect(page.getByText("Профиль исполнителя создан")).toBeVisible();
  await logout(page);
  return { email, displayName };
}

async function apiLogin(request: APIRequestContext, email: string) {
  const response = await request.post("/api/v1/auth/login", {
    data: { email, password },
  });
  expect(response.ok()).toBeTruthy();
  const body = (await response.json()) as { accessToken?: string };
  expect(body.accessToken).toBeTruthy();
  return body.accessToken!;
}

async function createActiveDeal(
  request: APIRequestContext,
  customerEmail: string,
  performerEmail: string,
) {
  const customerToken = await apiLogin(request, customerEmail);
  const performerToken = await apiLogin(request, performerEmail);
  const auth = (token: string) => ({ Authorization: `Bearer ${token}` });
  const performerResponse = await request.get("/api/v1/performers/me", {
    headers: auth(performerToken),
  });
  expect(performerResponse.ok()).toBeTruthy();
  const performer = (await performerResponse.json()) as {
    cityId?: string;
    categories?: Array<{ categoryId?: string }>;
  };
  const categoryId = performer.categories?.[0]?.categoryId;
  expect(categoryId).toBeTruthy();
  expect(performer.cityId).toBeTruthy();

  const suffix = Date.now();
  const taskResponse = await request.post("/api/v1/tasks", {
    headers: {
      ...auth(customerToken),
      "Idempotency-Key": `e2e-dispute-task-${suffix}`,
    },
    data: {
      title: `E2E спор ${suffix}`,
      description: "Тестовая сделка для проверки открытия спора из чата.",
      categoryId,
      serviceMode: "ONSITE",
      priceType: "FIXED",
      fixedPrice: 15000,
      currency: "UZS",
      countryCode: "UZ",
      cityId: performer.cityId,
      addressText: "Ташкент",
      expiresAt: new Date(Date.now() + 7 * 86400000).toISOString(),
    },
  });
  expect(taskResponse.status()).toBe(201);
  const task = (await taskResponse.json()) as { publicId?: string };
  expect(task.publicId).toBeTruthy();

  const offerResponse = await request.post(
    `/api/v1/tasks/${task.publicId}/offers`,
    {
      headers: {
        ...auth(performerToken),
        "Idempotency-Key": `e2e-dispute-offer-${suffix}`,
      },
      data: {
        message: "Готов выполнить тестовую работу.",
        proposedPrice: 14000,
        currency: "UZS",
        estimatedDuration: "Один день",
        includesMaterials: false,
      },
    },
  );
  expect(offerResponse.status()).toBe(201);
  const offer = (await offerResponse.json()) as { publicId?: string };
  expect(offer.publicId).toBeTruthy();

  const dealResponse = await request.post(
    `/api/v1/tasks/${task.publicId}/offers/${offer.publicId}/accept`,
    {
      headers: {
        ...auth(customerToken),
        "Idempotency-Key": `e2e-dispute-accept-${suffix}`,
      },
    },
  );
  expect(dealResponse.ok()).toBeTruthy();
  const deal = (await dealResponse.json()) as { chatId?: string };
  expect(deal.chatId).toBeTruthy();
  const submissionResponse = await request.post(
    `/api/v1/chats/${deal.chatId}/submit-work`,
    {
      headers: {
        ...auth(performerToken),
        "Idempotency-Key": `e2e-dispute-submit-${suffix}`,
      },
      data: { message: "Работа отправлена для проверки dispute flow." },
    },
  );
  expect(submissionResponse.ok()).toBeTruthy();
  return deal.chatId!;
}

let sharedActors: { customerEmail: string; performerEmail: string } | undefined;

test.describe.serial("F6 chat lifecycle", () => {
  test("customer and performer complete work through chat with one revision", async ({
    browser,
    page,
    isMobile,
  }) => {
    test.setTimeout(90_000);
    test.skip(
      isMobile,
      "Полный изменяющий данные flow выполняется один раз; mobile проверяется browser QA.",
    );

    const taskTitle = `Установить светильник ${Date.now()}`;
    const performer = await onboardPerformer(page);
    const customer = await registerAccount(
      page.request,
      "e2e-customer",
      "E2E",
      "Заказчик",
    );
    sharedActors = {
      customerEmail: customer.email,
      performerEmail: performer.email,
    };
    await page.goto("/tasks/new");

    await page.getByLabel("Название задачи").fill(taskTitle);
    await page
      .getByLabel("Подробное описание")
      .fill(
        "Нужно аккуратно установить потолочный светильник и проверить подключение.",
      );
    await page.getByLabel("Категория").selectOption({ label: "Сантехник" });
    await page.getByRole("button", { name: "Продолжить" }).click();

    await page.getByLabel("Город").selectOption({ index: 1 });
    await page
      .getByLabel("Ориентир или адрес без номера квартиры")
      .fill("Ташкент, центр");
    await page.getByRole("button", { name: "Продолжить" }).click();
    await page.getByRole("button", { name: "Продолжить" }).click();

    await page.getByLabel("Стоимость, сум").fill("32000");
    await page.getByRole("button", { name: "Продолжить" }).click();
    await page
      .locator('input[type="file"]')
      .setInputFiles(path.resolve("public/images/handynest-hero.png"));
    await expect(page.getByText("Выбрано файлов: 1")).toBeVisible();
    await page.getByRole("button", { name: "Продолжить" }).click();

    const customerRules = page.getByRole("checkbox", {
      name: /правила заказчика/i,
    });
    if (await customerRules.isVisible()) await customerRules.check();
    await page.getByRole("button", { name: "Опубликовать заказ" }).click();
    await expect(page).toHaveURL(/\/my\/tasks\/[^/]+$/);
    const taskId = page.url().split("/").pop()!;
    await expect(page.getByRole("heading", { name: taskTitle })).toBeVisible();
    await expect(page.locator('img[alt="handynest-hero.png"]')).toBeVisible();

    await logout(page);
    await login(page, performer.email);
    await page.goto(`/tasks/${taskId}`);
    await expect(
      page.getByRole("heading", { name: "Предложить свои услуги" }),
    ).toBeVisible();
    await page.getByLabel("Ваша цена, сум").fill("300000");
    await page.getByLabel("Срок выполнения").fill("Один день");
    await page
      .getByLabel("Сообщение заказчику")
      .fill(
        "Готов выполнить работу завтра, инструмент и расходники привезу с собой.",
      );
    const offerResponse = page.waitForResponse(
      (response) =>
        response.url().includes(`/api/v1/tasks/${taskId}/offers`) &&
        response.request().method() === "POST",
    );
    await page.getByRole("button", { name: "Отправить отклик" }).click();
    await expect((await offerResponse).status()).toBe(201);
    await expect(page.getByText("Отклик отправлен")).toBeVisible();

    await logout(page);
    await login(page, customer.email);
    await page.goto(`/my/tasks/${taskId}`);
    await expect(page.getByText(performer.displayName)).toBeVisible();
    await page.getByRole("button", { name: "Выбрать исполнителя" }).click();
    const dialog = page.getByRole("dialog", { name: "Принять отклик?" });
    await expect(dialog).toBeVisible();
    await dialog.getByRole("button", { name: "Выбрать исполнителя" }).click();

    await expect(page).toHaveURL(/\/deals\/[^/]+$/);
    await expect(page.getByRole("heading", { name: taskTitle })).toBeVisible();
    await expect(page.getByText(performer.displayName)).toBeVisible();

    await page.getByRole("link", { name: "Открыть чат" }).click();
    await expect(page).toHaveURL(/\/chats\/[^/]+$/);
    const chatPath = new URL(page.url()).pathname;

    const performerContext = await browser.newContext({
      baseURL: process.env.PLAYWRIGHT_BASE_URL ?? "http://127.0.0.1:3000",
    });
    const performerPage = await performerContext.newPage();
    await login(performerPage, performer.email);
    await performerPage.goto(chatPath);

    await page
      .getByRole("textbox", { name: "Сообщение", exact: true })
      .fill("Подтвердите, пожалуйста, что видите детали заказа.");
    await page.getByRole("button", { name: "Отправить сообщение" }).click();
    await performerPage.bringToFront();
    await expect(
      performerPage.getByText(
        "Подтвердите, пожалуйста, что видите детали заказа.",
      ),
    ).toBeVisible({ timeout: 8000 });

    await performerPage
      .getByRole("textbox", { name: "Сообщение", exact: true })
      .fill("Вижу задачу, приступаю к работе.");
    await performerPage
      .getByRole("button", { name: "Отправить сообщение" })
      .click();
    await page.bringToFront();
    await expect(
      page.getByText("Вижу задачу, приступаю к работе."),
    ).toBeVisible({
      timeout: 8000,
    });

    await performerPage
      .getByRole("button", { name: "Отправить работу" })
      .click();
    await performerPage
      .getByLabel("Комментарий к результату")
      .fill("Первый вариант готов, файл приложен.");
    await performerPage
      .getByLabel("Файлы с результатом")
      .setInputFiles(path.resolve("public/images/handynest-hero.png"));
    await performerPage
      .getByRole("button", { name: "Подтвердить действие" })
      .click();
    await expect(
      performerPage.getByText("Статус сделки обновлён"),
    ).toBeVisible();

    await page.reload();
    await page.getByRole("button", { name: "Попросить доработку" }).click();
    await page
      .getByLabel("Что нужно исправить")
      .fill("Добавьте итоговое описание выполненной работы.");
    await page.getByRole("button", { name: "Подтвердить действие" }).click();
    await expect(page.getByText("Статус сделки обновлён")).toBeVisible();

    await performerPage.reload();
    await performerPage
      .getByRole("button", { name: "Отправить работу" })
      .click();
    await performerPage
      .getByLabel("Комментарий к результату")
      .fill("Описание добавлено, отправляю повторно.");
    await performerPage
      .getByRole("button", { name: "Подтвердить действие" })
      .click();
    await expect(
      performerPage.getByText("Статус сделки обновлён"),
    ).toBeVisible();

    await page.reload();
    await page.getByRole("button", { name: "Принять работу" }).click();
    await page
      .getByLabel("Комментарий исполнителю")
      .fill("Работа принята, спасибо.");
    await page.getByRole("button", { name: "Подтвердить действие" }).click();
    await expect(
      page.getByText("Сделка завершена. Чат доступен только для чтения."),
    ).toBeVisible();
    await expect(
      page.getByRole("textbox", { name: "Сообщение", exact: true }),
    ).toHaveCount(0);
    await performerContext.close();
  });

  test("customer opens a dispute from an active deal chat", async ({
    page,
    isMobile,
  }) => {
    test.skip(
      isMobile,
      "Изменяющий данные dispute flow выполняется на desktop; mobile проверяется browser QA.",
    );
    expect(sharedActors).toBeTruthy();
    const chatId = await createActiveDeal(
      page.request,
      sharedActors!.customerEmail,
      sharedActors!.performerEmail,
    );
    await login(page, sharedActors!.customerEmail);
    await page.goto(`/chats/${chatId}`);
    await page.getByRole("button", { name: "Открыть спор" }).click();
    await page.getByLabel("Краткая причина спора").fill("Работа не начата");
    await page
      .getByLabel("Подробное описание ситуации")
      .fill("Исполнитель не приступил к работе в согласованный срок.");
    await page.getByRole("button", { name: "Подтвердить действие" }).click();
    await expect(page.getByText("По сделке открыт спор")).toBeVisible();
    await expect(
      page.getByText("Обычная приёмка работы приостановлена"),
    ).toBeVisible();
  });
});
