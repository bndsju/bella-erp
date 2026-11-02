import { Badge } from "@/components/ui/badge";
import type { StatusContaPagar } from "@/types/contaPagar";

const CONFIG: Record<StatusContaPagar, { label: string; variant: "default" | "secondary" | "destructive" | "outline" }> = {
  PENDENTE: { label: "Pendente", variant: "outline" },
  PARCIALMENTE_PAGA: { label: "Parcialmente paga", variant: "secondary" },
  PAGA: { label: "Paga", variant: "default" },
  CANCELADA: { label: "Cancelada", variant: "destructive" },
};

export function StatusContaPagarBadge({ status, vencida }: { status: StatusContaPagar; vencida?: boolean }) {
  const config = CONFIG[status];
  return (
    <span className="inline-flex gap-1">
      <Badge variant={config.variant}>{config.label}</Badge>
      {vencida && <Badge variant="destructive">Vencida</Badge>}
    </span>
  );
}
