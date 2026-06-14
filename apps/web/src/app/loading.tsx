import { Skeleton } from "@/shared/ui/skeleton";

export default function Loading() {
  return (
    <section
      className="container-page page-section grid gap-5"
      aria-busy="true"
    >
      <Skeleton className="h-5 w-32" />
      <Skeleton className="h-11 w-full max-w-xl" />
      <Skeleton className="h-28 w-full" />
      <Skeleton className="h-28 w-full" />
      <Skeleton className="h-28 w-full" />
    </section>
  );
}
