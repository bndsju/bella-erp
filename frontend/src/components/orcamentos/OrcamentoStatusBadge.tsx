import { Badge } from "@/components/ui/badge";
import type { StatusOrcamento } from "@/types/orcamento";

const CONFIG: Record<StatusOrcamento, { label: string; variant: "default" | "secondary" | "destructive" | "outline" }> = {
  RASCUNHO: { label: "Rascunho", variant: "outline" },
  ENVIADO: { label: "Enviado", variant: "secondary" },
  APROVADO: { label: "Aprovado", variant: "default" },
  RECUSADO: { label: "Recusado", variant: "destructive" },
  EXPIRADO: { label: "Expirado", variant: "outline" },
  CANCELADO: { label: "Cancelado", variant: "destructive" },
};

export function OrcamentoStatusBadge({ status }: { status: StatusOrcamento }) {
  const config = CONFIG[status];
  return <Badge variant={config.variant}>{config.label}</Badge>;
}
