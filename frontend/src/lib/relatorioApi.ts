import { api } from "./api";
import type {
  RelatorioClientesMaiorVolume,
  RelatorioContas,
  RelatorioFluxoCaixa,
  RelatorioMovimentacoesEstoque,
  RelatorioOrcamentos,
  RelatorioPedidosPorStatus,
  RelatorioPosicaoEstoque,
  RelatorioProdutosMaisVendidos,
  RelatorioVencimentos,
  RelatorioVendas,
} from "@/types/relatorio";

export type ParametrosRelatorio = Record<string, string | number | boolean | null | undefined>;

/** Monta "?a=1&b=2" ignorando vazios e falsos; o servidor trata o que não vier como "sem filtro". */
function consulta(params: ParametrosRelatorio): string {
  const query = new URLSearchParams();
  for (const [chave, valor] of Object.entries(params)) {
    if (valor === undefined || valor === null || valor === "" || valor === false) continue;
    query.set(chave, String(valor));
  }
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const relatorioApi = {
  vendas: (p: ParametrosRelatorio) => api.get<RelatorioVendas>(`/relatorios/vendas${consulta(p)}`),
  pedidosPorStatus: (p: ParametrosRelatorio) =>
    api.get<RelatorioPedidosPorStatus>(`/relatorios/pedidos-por-status${consulta(p)}`),
  orcamentos: (p: ParametrosRelatorio) => api.get<RelatorioOrcamentos>(`/relatorios/orcamentos${consulta(p)}`),
  produtosMaisVendidos: (p: ParametrosRelatorio) =>
    api.get<RelatorioProdutosMaisVendidos>(`/relatorios/produtos-mais-vendidos${consulta(p)}`),
  clientesMaiorVolume: (p: ParametrosRelatorio) =>
    api.get<RelatorioClientesMaiorVolume>(`/relatorios/clientes-maior-volume${consulta(p)}`),

  contasReceber: (p: ParametrosRelatorio) => api.get<RelatorioContas>(`/relatorios/contas-receber${consulta(p)}`),
  contasPagar: (p: ParametrosRelatorio) => api.get<RelatorioContas>(`/relatorios/contas-pagar${consulta(p)}`),
  vencimentos: (p: ParametrosRelatorio) => api.get<RelatorioVencimentos>(`/relatorios/vencimentos${consulta(p)}`),
  fluxoCaixa: (p: ParametrosRelatorio) => api.get<RelatorioFluxoCaixa>(`/relatorios/fluxo-caixa${consulta(p)}`),

  estoqueMovimentacoes: (p: ParametrosRelatorio) =>
    api.get<RelatorioMovimentacoesEstoque>(`/relatorios/estoque/movimentacoes${consulta(p)}`),
  estoquePosicao: (p: ParametrosRelatorio) =>
    api.get<RelatorioPosicaoEstoque>(`/relatorios/estoque/posicao${consulta(p)}`),
};
