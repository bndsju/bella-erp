import { Badge } from "@/components/ui/badge";
import type { StatusCompra } from "@/types/compra";

const CONFIG: Record<StatusCompra, { label: string; variant: "default" | "secondary" | "destructive" | "outline" }> = {
  RASCUNHO: { label: "Rascunho", variant: "outline" },
  CONFIRMADA: { label: "Confirmada", variant: "secondary" },
  RECEBIDA: { label: "Recebida", variant: "default" },
  CANCELADA: { label: "Cancelada", variant: "destructive" },
};

export function CompraStatusBadge({ status }: { status: StatusCompra }) {
  const config = CONFIG[status];
  return <Badge variant={config.variant}>{config.label}</Badge>;
}
