export type SearchParams = Record<string, string | string[] | undefined>;

export function firstParam(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

export function pageParam(value: string | string[] | undefined) {
  const parsed = Number(firstParam(value) ?? "1");
  return Number.isInteger(parsed) && parsed > 0 ? parsed : 1;
}

export function withSearchParams(
  pathname: string,
  current: SearchParams,
  updates: Record<string, string | number | undefined>,
) {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(current)) {
    const normalized = firstParam(value);
    if (normalized) params.set(key, normalized);
  }
  for (const [key, value] of Object.entries(updates)) {
    if (value === undefined || value === "") params.delete(key);
    else params.set(key, String(value));
  }
  const query = params.toString();
  return query ? `${pathname}?${query}` : pathname;
}
