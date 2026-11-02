import type { OrigemMovimentacaoCaixa } from "@/types/fluxoCaixa";

export const ROTULO_ORIGEM: Record<OrigemMovimentacaoCaixa, string> = {
  CONTA_RECEBER: "Conta a receber",
  CONTA_PAGAR: "Conta a pagar",
  LANCAMENTO_MANUAL: "Lançamento manual",
};
