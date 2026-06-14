function marketLocale(locale: string) {
  return locale === "uz" ? "uz-UZ" : "ru-UZ";
}

export function formatMoney(value: number, currency: string, locale: string) {
  return new Intl.NumberFormat(marketLocale(locale), {
    style: "currency",
    currency,
    maximumFractionDigits: 0,
  }).format(value);
}

export function formatDate(
  value: string | null | undefined,
  locale: string,
  fallback: string,
) {
  if (!value) return fallback;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return fallback;
  return new Intl.DateTimeFormat(marketLocale(locale), {
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(date);
}

export function formatDateTime(
  value: string | null | undefined,
  locale: string,
  fallback: string,
) {
  if (!value) return fallback;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return fallback;
  return new Intl.DateTimeFormat(marketLocale(locale), {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

export function formatTaskBudget(
  task: {
    fixedPrice?: number;
    budgetMin?: number;
    budgetMax?: number;
    currency?: string;
  },
  locale: string,
  labels: {
    negotiable: string;
    from: (price: string) => string;
    to: (price: string) => string;
  },
) {
  const currency = task.currency ?? "UZS";
  if (task.fixedPrice != null)
    return formatMoney(task.fixedPrice, currency, locale);
  if (task.budgetMin != null && task.budgetMax != null) {
    return `${formatMoney(task.budgetMin, currency, locale)} – ${formatMoney(task.budgetMax, currency, locale)}`;
  }
  if (task.budgetMin != null)
    return labels.from(formatMoney(task.budgetMin, currency, locale));
  if (task.budgetMax != null)
    return labels.to(formatMoney(task.budgetMax, currency, locale));
  return labels.negotiable;
}
