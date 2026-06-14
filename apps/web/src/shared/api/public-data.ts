import type { operations } from "@/shared/api/generated/schema";
import { createServerApiClient, toServerResult } from "@/shared/api/server";
import type {
  Category,
  MarketplaceTaskPage,
  PerformerPage,
} from "@/shared/api/types";

type TaskSearchQuery = NonNullable<
  operations["searchMarketplaceTasks"]["parameters"]["query"]
>;
type PerformerSearchQuery = NonNullable<
  operations["search"]["parameters"]["query"]
>;

export function getCategories(locale: string) {
  return toServerResult(
    createServerApiClient(locale, 300).GET("/api/v1/categories", {
      params: { query: { locale } },
    }),
  );
}

export function getMarketConfig(locale: string) {
  return toServerResult(
    createServerApiClient(locale, 3600).GET("/api/v1/market/config", {}),
  );
}

export function getMarketCities(locale: string) {
  return toServerResult(
    createServerApiClient(locale, 3600).GET("/api/v1/geo/cities", {
      params: { query: { locale } },
    }),
  );
}

export function getPublicTasks(locale: string, query: TaskSearchQuery) {
  return toServerResult<MarketplaceTaskPage>(
    createServerApiClient(locale).GET("/api/v1/tasks", { params: { query } }),
  );
}

export function getPublicTask(locale: string, taskId: string) {
  return toServerResult(
    createServerApiClient(locale).GET("/api/v1/tasks/{taskId}", {
      params: { path: { taskId } },
    }),
  );
}

export function getPublicPerformers(
  locale: string,
  query: PerformerSearchQuery,
) {
  return toServerResult<PerformerPage>(
    createServerApiClient(locale).GET("/api/v1/performers", {
      params: { query: { ...query, locale } },
    }),
  );
}

export function getPublicPerformer(locale: string, performerId: string) {
  return toServerResult(
    createServerApiClient(locale).GET("/api/v1/performers/{performerId}", {
      params: { path: { performerId }, query: { locale } },
    }),
  );
}

export function getPublicPerformerFeedbacks(
  locale: string,
  performerId: string,
) {
  return toServerResult(
    createServerApiClient(locale).GET(
      "/api/v1/performers/{performerId}/feedbacks",
      { params: { path: { performerId } } },
    ),
  );
}

export function flattenCategories(categories: Category[]): Category[] {
  return categories.flatMap((category) => [
    category,
    ...flattenCategories(category.children ?? []),
  ]);
}
