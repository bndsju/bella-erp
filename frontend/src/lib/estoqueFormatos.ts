import type { OrigemMovimentacaoEstoque, TipoMovimentacaoEstoque } from "@/types/estoque";

const formatoQuantidade = new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 3 });

export function formatarQuantidade(valor: number): string {
  return formatoQuantidade.format(valor);
}

export const ROTULO_TIPO: Record<TipoMovimentacaoEstoque, string> = {
  ENTRADA: "Entrada",
  SAIDA: "Saída",
  AJUSTE_ENTRADA: "Ajuste de entrada",
  AJUSTE_SAIDA: "Ajuste de saída",
  RESERVA: "Reserva",
  LIBERACAO_RESERVA: "Liberação de reserva",
};

export const ROTULO_ORIGEM: Record<OrigemMovimentacaoEstoque, string> = {
  MANUAL: "Manual",
  PEDIDO: "Pedido",
  COMPRA: "Compra",
};

const TIPOS_QUE_AUMENTAM = new Set<TipoMovimentacaoEstoque>(["ENTRADA", "AJUSTE_ENTRADA"]);
const TIPOS_QUE_DIMINUEM = new Set<TipoMovimentacaoEstoque>(["SAIDA", "AJUSTE_SAIDA"]);

export function quantidadeComSinal(tipo: TipoMovimentacaoEstoque, quantidade: number): string {
  if (TIPOS_QUE_AUMENTAM.has(tipo)) return `+${formatarQuantidade(quantidade)}`;
  if (TIPOS_QUE_DIMINUEM.has(tipo)) return `-${formatarQuantidade(quantidade)}`;
  return formatarQuantidade(quantidade);
}

export function inicioDoDia(data: string): string {
  return new Date(`${data}T00:00:00`).toISOString();
}

export function fimDoDia(data: string): string {
  return new Date(`${data}T23:59:59.999`).toISOString();
}
