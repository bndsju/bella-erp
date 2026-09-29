import { Badge } from "@/components/ui/badge";
import type { StatusPedido } from "@/types/pedido";

const CONFIG: Record<StatusPedido, { label: string; variant: "default" | "secondary" | "destructive" | "outline" }> = {
  CRIADO: { label: "Criado", variant: "outline" },
  CONFIRMADO: { label: "Confirmado", variant: "secondary" },
  EM_SEPARACAO: { label: "Em separação", variant: "secondary" },
  PRONTO_PARA_ENTREGA: { label: "Pronto para entrega", variant: "secondary" },
  ENTREGUE: { label: "Entregue", variant: "default" },
  CANCELADO: { label: "Cancelado", variant: "destructive" },
};

export function PedidoStatusBadge({ status }: { status: StatusPedido }) {
  const config = CONFIG[status];
  return <Badge variant={config.variant}>{config.label}</Badge>;
}
