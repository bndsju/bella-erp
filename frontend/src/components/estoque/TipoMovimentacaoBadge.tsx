import { Badge } from "@/components/ui/badge";
import { ROTULO_TIPO } from "@/lib/estoqueFormatos";
import type { TipoMovimentacaoEstoque } from "@/types/estoque";

const VARIANTE: Record<TipoMovimentacaoEstoque, "default" | "secondary" | "destructive" | "outline"> = {
  ENTRADA: "default",
  SAIDA: "destructive",
  AJUSTE_ENTRADA: "secondary",
  AJUSTE_SAIDA: "secondary",
  RESERVA: "outline",
  LIBERACAO_RESERVA: "outline",
};

export function TipoMovimentacaoBadge({ tipo }: { tipo: TipoMovimentacaoEstoque }) {
  return <Badge variant={VARIANTE[tipo]}>{ROTULO_TIPO[tipo]}</Badge>;
}
