import type { FormaPagamento } from "@/types/venda";

export const FORMAS_PAGAMENTO: { value: FormaPagamento; label: string }[] = [
  { value: "DINHEIRO", label: "Dinheiro" },
  { value: "CARTAO_CREDITO", label: "Cartão de crédito" },
  { value: "CARTAO_DEBITO", label: "Cartão de débito" },
  { value: "PIX", label: "Pix" },
  { value: "BOLETO", label: "Boleto" },
  { value: "TRANSFERENCIA", label: "Transferência" },
];

export function labelFormaPagamento(formaPagamento: FormaPagamento): string {
  return FORMAS_PAGAMENTO.find((forma) => forma.value === formaPagamento)?.label ?? formaPagamento;
}
