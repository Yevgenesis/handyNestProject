# HandyNest Frontend ТЗ для Codex

Версия: 1.0

Этот документ дополняет основное ТЗ:

```text
docs/HandyNest_TZ_for_Codex_ru_v6.md
```

Основное backend/API ТЗ остаётся главным источником бизнес-логики. Этот документ фиксирует frontend-стек, структуру приложения, UX-подход, экраны и правила интеграции с backend.

---

## 1. Почему frontend вынесен отдельно

HandyNest проектируется как backend-first REST API, потому что платформа должна поддерживать:

- веб-приложение;
- мобильное приложение в будущем;
- админ-панель;
- интеграции с платежами;
- уведомления;
- внешние сервисы проверки документов.

Frontend должен разрабатываться как отдельное приложение, которое использует backend REST API `/api/v1`.

---

## 2. Рекомендуемый frontend stack

Использовать:

```text
TypeScript
React
Next.js App Router
Tailwind CSS
shadcn/ui
TanStack Query
React Hook Form
Zod
next-intl или аналог для i18n
Playwright
Vitest
ESLint
Prettier
```

### 2.1. Почему Next.js

Next.js выбран как основной frontend framework, потому что это React framework для full-stack web applications, который даёт routing, rendering, оптимизации и project structure. Использовать App Router, так как он является новым роутером Next.js и поддерживает React Server Components.

### 2.2. Почему TypeScript

Frontend должен быть строго типизированным.

Правила:

- `strict: true` в `tsconfig.json`;
- не использовать `any` без крайней необходимости;
- API DTO типизировать;
- формы валидировать через Zod;
- generated API types предпочтительны после стабилизации OpenAPI.

### 2.3. Почему Tailwind CSS + shadcn/ui

Tailwind CSS использовать для быстрого, единообразного и responsive styling.

shadcn/ui использовать как основу UI-компонентов, потому что это не закрытая библиотека компонентов, а open-code подход: компоненты копируются в проект и могут адаптироваться под дизайн HandyNest.

### 2.4. Почему TanStack Query

TanStack Query использовать для server state:

- загрузка списков;
- кеширование;
- pagination;
- retry;
- mutations;
- invalidation после действий;
- optimistic updates там, где безопасно.

Не хранить server state в Zustand/Redux.

### 2.5. Почему React Hook Form + Zod

React Hook Form использовать для форм.

Zod использовать для client-side validation и согласования frontend-схем с backend DTO.

---

## 3. Архитектура frontend

Рекомендуемая структура:

```text
apps/web

  src
    app
      (public)
        page.tsx
        tasks
        performers
        categories

      (auth)
        login
        register
        forgot-password

      (customer)
        dashboard
        tasks
        deals
        chats
        favorites
        profile

      (performer)
        dashboard
        profile
        offers
        deals
        chats
        verification

      (admin)
        dashboard
        users
        categories
        verification
        moderation
        disputes
        audit-log
        settings

    shared
      api
      config
      constants
      hooks
      lib
      ui
      types
      validation

    entities
      user
      task
      offer
      deal
      chat
      category
      performer
      feedback
      notification
      geo

    features
      auth
      create-task
      search-tasks
      create-offer
      accept-offer
      deal-chat
      submit-work
      accept-work
      request-revision
      open-dispute
      verification
      favorite-performer
      rebooking

    widgets
      header
      footer
      sidebar
      task-card
      performer-card
      offer-list
      chat-window
      deal-status-panel
      category-grid
      filters-panel
      notification-bell

    processes
      customer-order-flow
      performer-work-flow
      verification-flow
      dispute-flow
```

Подход можно назвать feature-oriented architecture. Не обязательно строго FSD, но структура должна быть понятной и модульной.

---

## 4. Главные frontend-принципы

Frontend должен быть:

- mobile-first;
- простой;
- не перегруженный;
- быстрый;
- понятный обычному пользователю;
- с крупными CTA-кнопками;
- с короткими формами;
- с progressive disclosure: сложные настройки показывать только когда они нужны.

Не показывать пользователю backend enum напрямую.

Например, вместо:

```text
WORK_SUBMITTED
```

Показывать:

```text
Работа отправлена на проверку
```

---

## 5. Роли и layout

### 5.1. Public layout

Для гостей:

- главная;
- поиск услуг;
- категории;
- публичные задачи;
- публичные профили исполнителей;
- login/register.

### 5.2. Customer layout

Для заказчика:

- dashboard;
- мои заказы;
- создать заказ;
- отклики;
- сделки;
- чаты;
- избранные исполнители;
- профиль.

### 5.3. Performer layout

Для исполнителя:

- dashboard;
- профиль исполнителя;
- мои отклики;
- доступные заказы;
- сделки;
- чаты;
- верификация;
- статистика.

### 5.4. Admin layout

Для администратора:

- dashboard;
- пользователи;
- категории;
- модерация заказов;
- верификация исполнителей;
- жалобы;
- споры;
- audit log;
- настройки платформы.

---

## 6. Основные экраны MVP

### 6.1. Главная страница

Цель:

- объяснить ценность платформы;
- дать быстрый поиск;
- показать категории;
- показать CTA для заказчика и исполнителя.

Блоки:

- hero;
- поиск услуги;
- популярные категории;
- как это работает;
- безопасность и проверка исполнителей;
- CTA “Создать заказ”;
- CTA “Стать исполнителем”.

### 6.2. Регистрация и вход

Экраны:

- login;
- register;
- phone OTP;
- email verification, если включена;
- forgot password позже.

Регистрация должна позволять пользователю начать как заказчик. Стать исполнителем можно позже через создание performer profile.

### 6.3. Создание заказа

Пошаговая форма:

1. Что нужно сделать?
2. Категория.
3. Где выполнить?
4. Когда выполнить?
5. Бюджет.
6. Фото/файлы.
7. Проверка и публикация.

Не показывать все поля сразу.

### 6.4. Список заказов

Для исполнителей:

- список доступных заказов;
- фильтры;
- сортировка;
- карточки;
- быстрый отклик.

Фильтры:

```text
categoryId
countryId
regionId
cityId
districtId
serviceMode
priceMin
priceMax
remote
onsite
sort
page
size
```

### 6.5. Детали заказа

Показывать:

- название;
- описание;
- категорию;
- город/район;
- бюджет;
- срок;
- вложения;
- статус;
- заказчика без приватных контактов;
- CTA для отклика.

### 6.6. Отклики

Для заказчика:

- список откликов;
- карточка исполнителя;
- рейтинг;
- бейджи;
- цена;
- срок;
- сообщение;
- кнопка “Выбрать”.

Для исполнителя:

- мои отклики;
- статус отклика;
- возможность отменить отклик, если он ещё `PENDING`.

### 6.7. Чат сделки

Чат — главный экран сделки.

Должен показывать:

- сообщения;
- вложения;
- системные события;
- статус сделки;
- карточку задачи сверху;
- действия по текущему статусу.

Для исполнителя:

```text
[Отправить работу на проверку]
```

Для заказчика после отправки работы:

```text
[Принять работу]
[Попросить доработку]
[Открыть спор]
```

### 6.8. Верификация исполнителя

Показывать уровни:

```text
Basic
Phone Verified
ID Verified
Payment Verified
Business Verified
```

В MVP:

- phone verification;
- загрузка документов;
- статус проверки;
- причина отказа;
- повторная отправка.

### 6.9. Споры

Экран спора должен показывать:

- причину;
- статус;
- сообщения;
- доказательства;
- решение администратора;
- сроки SLA.

### 6.10. Отзывы

После завершения сделки:

- оценка 1–5;
- текст;
- отзыв можно оставить только один раз;
- отзыв доступен только после `COMPLETED`.

---

## 7. Admin UI

Admin UI должен быть отдельной зоной приложения:

```text
/admin
```

Требования:

- только для admin/moderator;
- проверка прав на frontend;
- backend всё равно остаётся главным источником безопасности;
- таблицы с pagination;
- фильтры;
- audit-friendly действия;
- подтверждение опасных действий.

Основные разделы:

```text
Users
Categories
Verification
Moderation
Complaints
Disputes
Audit Log
Platform Settings
```

---

## 8. Design system

### 8.1. Общий стиль

Стиль:

- clean SaaS;
- светлый интерфейс;
- адаптивный mobile-first;
- минимум визуального шума;
- много воздуха;
- ясные статусы;
- понятные CTA.

### 8.2. Цвета

Точные цвета можно определить позже, но базово:

```text
primary: green/emerald
secondary: neutral gray
danger: red
warning: amber
success: green
info: blue
```

### 8.3. Компоненты

Базовые компоненты:

- Button;
- Input;
- Textarea;
- Select;
- Checkbox;
- Radio;
- Badge;
- Card;
- Dialog;
- Drawer;
- Sheet;
- Tabs;
- Table;
- Pagination;
- Skeleton;
- Alert;
- Toast;
- Avatar;
- Dropdown;
- Date picker;
- File uploader;
- Empty state.

---

## 9. API integration

### 9.1. API client

Создать единый API client:

```text
src/shared/api
```

Требования:

- base URL из env;
- автоматическая подстановка access token;
- refresh flow;
- обработка 401;
- обработка validation errors;
- typed responses;
- единый error mapper.

ENV:

```text
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

### 9.2. Auth storage

Для MVP предпочтительно:

- access token хранить в memory или безопасном frontend state;
- refresh token лучше хранить в HttpOnly cookie, если backend это поддерживает;
- если backend возвращает refresh token в JSON, нужно отдельно зафиксировать риски и перейти на HttpOnly cookie позже.

Не хранить sensitive данные в localStorage без необходимости.

### 9.3. Error handling

Backend error format должен маппиться в UI:

- field errors в формы;
- global error в toast/alert;
- 401 -> logout/refresh;
- 403 -> access denied page;
- 404 -> not found page;
- 409 -> conflict message;
- 429 -> rate limit message.

---

## 10. State management

### 10.1. Server state

Использовать TanStack Query.

Примеры:

- tasks list;
- performer list;
- offers;
- chat messages;
- notifications;
- admin tables.

### 10.2. Local UI state

Для простого UI state использовать React state.

Zustand можно добавить только при реальной необходимости, например:

- auth session state;
- global UI state;
- chat draft state.

Не использовать Redux в MVP.

---

## 11. Forms

Использовать:

```text
React Hook Form
Zod
```

Правила:

- client validation через Zod;
- backend validation остаётся обязательной;
- показывать field errors рядом с полями;
- длинные формы разбивать на шаги;
- сохранять draft заказа, если пользователь случайно ушёл со страницы.

---

## 12. i18n

MVP UI:

```text
ru
```

Подготовка:

```text
kk
en
```

Все пользовательские строки должны быть через словари.

Не хардкодить текст внутри бизнес-компонентов.

---

## 13. Realtime / polling

В MVP не обязательно использовать WebSocket.

Чат можно реализовать через polling:

```text
GET /api/v1/chats/{chatId}/messages
```

Рекомендуемый polling interval:

```text
3–5 секунд для активного чата
```

В будущем заменить на WebSocket/SSE.

---

## 14. Frontend security

Требования:

- не полагаться только на frontend guards;
- скрывать UI-кнопки по роли, но backend всё равно проверяет права;
- не показывать приватные контакты до разрешённого события;
- не логировать токены;
- не вставлять HTML из user input без sanitization;
- использовать CSP в production;
- обрабатывать 401/403 корректно;
- не хранить документы в публичных URL.

---

## 15. Testing

Использовать:

```text
Vitest
Testing Library
Playwright
```

Тесты:

- unit tests для utils/hooks;
- component tests для важных компонентов;
- e2e tests для главных flow.

E2E MVP:

```text
register/login
create task
performer creates offer
customer accepts offer
deal/chat opens
performer submits work
customer accepts work
feedback
admin verification approve/reject
```

---

## 16. Frontend phases

### Phase F1 — Setup

- создать Next.js app;
- TypeScript strict;
- Tailwind;
- shadcn/ui;
- ESLint/Prettier;
- env config;
- base layout.

### Phase F2 — Auth UI

- login;
- register;
- token handling;
- protected routes;
- role-based navigation.

### Phase F3 — Public marketplace

- home;
- categories;
- task list;
- performer list;
- task details.

### Phase F4 — Customer flow

- create task wizard;
- my tasks;
- offers list;
- accept offer;
- deal page.

### Phase F5 — Performer flow

- performer profile;
- verification;
- available tasks;
- create offer;
- my offers;
- my deals.

### Phase F6 — Chat and deal actions

- chat window;
- attachments;
- submit work;
- accept work;
- request revision;
- open dispute.

### Phase F7 — Feedback and notifications

- feedback form;
- rating display;
- notifications list;
- notification bell.

### Phase F8 — Admin UI

- users;
- categories;
- verification;
- moderation;
- disputes;
- audit log;
- settings.

### Phase F9 — Polish and tests

- responsive polish;
- accessibility pass;
- e2e tests;
- performance pass;
- error states;
- empty states;
- loading skeletons.

---

## 17. Frontend acceptance criteria

Frontend MVP считается готовым, если:

1. Пользователь может зарегистрироваться и войти.
2. Пользователь видит публичные категории и заказы.
3. Заказчик может создать заказ через wizard.
4. Исполнитель может создать профиль.
5. Исполнитель может отправить отклик.
6. Заказчик может принять отклик.
7. Открывается deal/chat.
8. В чате можно отправлять сообщения.
9. Исполнитель может отправить работу на проверку.
10. Заказчик может принять работу, попросить доработку или открыть спор.
11. После завершения можно оставить отзыв.
12. Админ может открыть разделы верификации, категорий, модерации и споров.
13. UI не показывает приватные контакты до разрешённого события.
14. Основные страницы адаптированы под мобильный экран.
15. Ошибки backend корректно отображаются пользователю.
16. E2E тест главного flow проходит.

---

## 18. Что не делать во frontend MVP

Не делать:

- React Native;
- мобильное приложение;
- Redux;
- сложную дизайн-систему с нуля;
- WebSocket как обязательное условие;
- SSR для всех приватных страниц без необходимости;
- сложную карту с real-time ETA;
- offline-first;
- PWA как обязательное условие;
- сложный WYSIWYG-редактор;
- video calls;
- встроенные звонки.

---

## 19. Рекомендуемый первый frontend prompt для Codex

```markdown
Read:
1. AGENTS.md
2. docs/HandyNest_TZ_for_Codex_ru_v6.md
3. docs/HandyNest_Frontend_TZ_for_Codex_ru.md

Goal: implement Frontend Phase F1 only.

Tasks:
1. Create Next.js app in apps/web.
2. Use TypeScript strict mode.
3. Configure Tailwind CSS.
4. Configure shadcn/ui.
5. Add ESLint/Prettier.
6. Add base app layout.
7. Add env example with NEXT_PUBLIC_API_BASE_URL.
8. Add shared API client skeleton.
9. Add placeholder pages: home, login, register, tasks, performers.
10. Do not implement business flows yet.

After changes:
- run npm/pnpm install if possible;
- run lint/build if possible;
- summarize files changed and commands run.
```
