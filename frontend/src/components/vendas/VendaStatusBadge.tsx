import { Badge } from "@/components/ui/badge";
import type { StatusVenda } from "@/types/venda";

const CONFIG: Record<StatusVenda, { label: string; variant: "default" | "destructive" }> = {
  CONCLUIDA: { label: "Concluída", variant: "default" },
  CANCELADA: { label: "Cancelada", variant: "destructive" },
};

export function VendaStatusBadge({ status }: { status: StatusVenda }) {
  const config = CONFIG[status];
  return <Badge variant={config.variant}>{config.label}</Badge>;
}
