import { cn } from "@/shared/lib/cn";

export function Badge({
  children,
  tone = "neutral",
  className,
}: {
  children: React.ReactNode;
  tone?: "neutral" | "green" | "blue" | "amber";
  className?: string;
}) {
  const tones = {
    neutral: "bg-[var(--surface-muted)] text-[var(--muted)]",
    green: "bg-[var(--primary-soft)] text-[var(--primary-strong)]",
    blue: "bg-blue-50 text-blue-700",
    amber: "bg-amber-50 text-amber-800",
  };
  return (
    <span
      className={cn(
        "inline-flex rounded-full px-2.5 py-1 text-xs font-semibold",
        tones[tone],
        className,
      )}
    >
      {children}
    </span>
  );
}
