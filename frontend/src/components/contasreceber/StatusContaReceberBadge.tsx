import { Badge } from "@/components/ui/badge";
import type { StatusContaReceber } from "@/types/contaReceber";

const CONFIG: Record<StatusContaReceber, { label: string; variant: "default" | "secondary" | "destructive" | "outline" }> = {
  PENDENTE: { label: "Pendente", variant: "outline" },
  PARCIALMENTE_RECEBIDA: { label: "Parcialmente recebida", variant: "secondary" },
  RECEBIDA: { label: "Recebida", variant: "default" },
  CANCELADA: { label: "Cancelada", variant: "destructive" },
};

export function StatusContaReceberBadge({ status, vencida }: { status: StatusContaReceber; vencida?: boolean }) {
  const config = CONFIG[status];
  return (
    <span className="inline-flex gap-1">
      <Badge variant={config.variant}>{config.label}</Badge>
      {vencida && <Badge variant="destructive">Vencida</Badge>}
    </span>
  );
}
