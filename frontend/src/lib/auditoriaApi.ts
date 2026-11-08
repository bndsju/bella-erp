import { api } from "./api";
import type { ModuloAuditoria, OperacaoAuditoria, RegistroAuditoria } from "@/types/auditoria";
import type { Pagina } from "@/types/relatorio";

export interface ListarAuditoriaParams {
  usuario?: string;
  modulo?: ModuloAuditoria;
  operacao?: OperacaoAuditoria;
  inicio?: string;
  fim?: string;
  pagina?: number;
  tamanho?: number;
}

function consulta(params: Record<string, string | number | undefined>): string {
  const query = new URLSearchParams();
  for (const [chave, valor] of Object.entries(params)) {
    if (valor !== undefined && valor !== "") query.set(chave, String(valor));
  }
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

/** Só leitura: o sistema grava a auditoria sozinho e não há como criar, editar ou apagar registros. */
export const auditoriaApi = {
  listar: (params: ListarAuditoriaParams = {}) =>
    api.get<Pagina<RegistroAuditoria>>(`/auditoria${consulta({ ...params })}`),

  historico: (modulo: ModuloAuditoria, entidadeId: string, pagina = 0, tamanho = 10) =>
    api.get<Pagina<RegistroAuditoria>>(`/auditoria/historico/${modulo}/${entidadeId}${consulta({ pagina, tamanho })}`),
};
