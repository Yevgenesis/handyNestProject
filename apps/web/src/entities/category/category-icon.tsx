import {
  Car,
  Hammer,
  Home,
  Laptop,
  Paintbrush,
  Scissors,
  Sparkles,
  Truck,
  Wrench,
} from "lucide-react";

const iconMap = [
  Car,
  Home,
  Hammer,
  Wrench,
  Sparkles,
  Laptop,
  Paintbrush,
  Scissors,
  Truck,
];

export function CategoryIcon({
  slug,
  className = "size-5",
}: {
  slug?: string;
  className?: string;
}) {
  const hash = [...(slug ?? "category")].reduce(
    (sum, letter) => sum + letter.charCodeAt(0),
    0,
  );
  const Icon = iconMap[hash % iconMap.length];
  return <Icon className={className} aria-hidden />;
}
