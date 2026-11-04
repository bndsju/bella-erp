import type { OpcaoSelecao } from "@/components/ui/selecao-pesquisavel";
import type { ClienteResponse } from "@/types/cliente";

/** Nome mostrado para o cliente: nome completo (pessoa física) ou razão social (empresa). */
export function nomeDoCliente(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF" ? (cliente.nomeCompleto ?? "") : (cliente.razaoSocial ?? "");
}

/** Opções para a seleção pesquisável de clientes: acha por nome, razão social ou nome fantasia. */
export function opcoesDeClientes(clientes: ClienteResponse[]): OpcaoSelecao[] {
  return clientes.map((cliente) => ({
    valor: cliente.id,
    rotulo: nomeDoCliente(cliente),
    termos: cliente.nomeFantasia ?? "",
  }));
}
