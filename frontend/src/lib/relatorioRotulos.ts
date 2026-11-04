import type { StatusPedido } from "@/types/pedido";
import type { StatusOrcamento } from "@/types/orcamento";

export const ROTULO_STATUS_PEDIDO: Record<StatusPedido, string> = {
  CRIADO: "Criado",
  CONFIRMADO: "Confirmado",
  EM_SEPARACAO: "Em separação",
  PRONTO_PARA_ENTREGA: "Pronto para entrega",
  ENTREGUE: "Entregue",
  CANCELADO: "Cancelado",
};

export const ROTULO_STATUS_ORCAMENTO: Record<StatusOrcamento, string> = {
  RASCUNHO: "Rascunho",
  ENVIADO: "Enviado",
  APROVADO: "Aprovado",
  RECUSADO: "Recusado",
  EXPIRADO: "Expirado",
  CANCELADO: "Cancelado",
};

export const ROTULO_STATUS_CONTA: Record<string, string> = {
  PENDENTE: "Pendente",
  PARCIALMENTE_RECEBIDA: "Parcialmente recebida",
  RECEBIDA: "Recebida",
  PARCIALMENTE_PAGA: "Parcialmente paga",
  PAGA: "Paga",
  CANCELADA: "Cancelada",
};

export function formatarPercentual(valor: number | null): string {
  return valor === null
    ? "—"
    : `${valor.toLocaleString("pt-BR", { minimumFractionDigits: 1, maximumFractionDigits: 1 })}%`;
}

export function rotuloDoPonto(data: string, granularidade: "DIA" | "MES"): string {
  const [ano, mes, dia] = data.split("-");
  return granularidade === "MES" ? `${mes}/${ano.slice(2)}` : `${dia}/${mes}`;
}

export function formatarDataHora(instante: string): string {
  return new Date(instante).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}
