import * as React from "react";

import { cn } from "@/shared/lib/cn";

export const Input = React.forwardRef<
  HTMLInputElement,
  React.InputHTMLAttributes<HTMLInputElement>
>(({ className, ...props }, ref) => (
  <input
    ref={ref}
    className={cn(
      "h-11 w-full rounded-[6px] border border-[var(--border)] bg-white px-3 text-sm text-[var(--foreground)] placeholder:text-[var(--muted)] focus:border-[var(--primary)] focus:outline-2 focus:outline-[var(--primary-soft)]",
      className,
    )}
    {...props}
  />
));
Input.displayName = "Input";
