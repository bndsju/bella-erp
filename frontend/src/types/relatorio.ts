import type { FormaPagamento } from "@/types/venda";
import type { StatusPedido } from "@/types/pedido";
import type { StatusOrcamento } from "@/types/orcamento";
import type {
  OrigemMovimentacaoCaixa,
  SituacaoMovimentacaoCaixa,
  TipoMovimentacaoCaixa,
} from "@/types/fluxoCaixa";
import type { OrigemMovimentacaoEstoque, TipoMovimentacaoEstoque } from "@/types/estoque";

export type GranularidadeRelatorio = "DIA" | "MES";

export interface Pagina<T> {
  itens: T[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
}

// ---- Comerciais ----

export interface RelatorioVendas {
  granularidade: GranularidadeRelatorio;
  resumo: {
    quantidade: number;
    faturamento: number;
    ticketMedio: number;
    descontoTotal: number;
    freteTotal: number;
  };
  serie: { data: string; quantidade: number; valor: number }[];
  vendas: Pagina<{
    id: string;
    data: string;
    clienteId: string;
    clienteNome: string;
    formaPagamento: FormaPagamento;
    valorDesconto: number;
    valorFrete: number;
    valorTotal: number;
  }>;
}

export interface RelatorioPedidosPorStatus {
  porStatus: { status: StatusPedido; quantidade: number; valorTotal: number }[];
  quantidadeValidos: number;
  valorValidos: number;
}

export interface RelatorioOrcamentos {
  porStatus: { status: StatusOrcamento; quantidade: number; valorTotal: number }[];
  emitidos: number;
  valorEmitidos: number;
  aprovados: number;
  recusados: number;
  taxaAprovacao: number | null;
  taxaConversaoPedidos: number | null;
  taxaConversaoVendas: number | null;
}

export interface RelatorioProdutosMaisVendidos {
  produtos: {
    produtoId: string;
    codigoInterno: string;
    nome: string;
    categoriaNome: string | null;
    quantidade: number;
    valor: number;
    quantidadeVendas: number;
  }[];
}

export interface RelatorioClientesMaiorVolume {
  faturamentoTotal: number;
  clientes: {
    clienteId: string;
    nome: string;
    quantidadeVendas: number;
    valor: number;
    ticketMedio: number;
    participacaoPercentual: number;
  }[];
}

// ---- Financeiros ----

export interface TotaisContas {
  quantidadeContas: number;
  valorTotal: number;
  valorLiquidado: number;
  saldoPendente: number;
  saldoVencido: number;
  saldoAVencer: number;
}

export interface RelatorioContas {
  totais: TotaisContas;
  contas: Pagina<{
    id: string;
    descricao: string;
    parceiroNome: string;
    categoriaNome: string | null;
    valorTotal: number;
    valorLiquidado: number;
    saldoPendente: number;
    proximoVencimento: string | null;
    status: string;
    vencida: boolean;
  }>;
}

export type TipoTitulo = "RECEBER" | "PAGAR";

export interface RelatorioVencimentos {
  referencia: string;
  totais: { vencidoReceber: number; aVencerReceber: number; vencidoPagar: number; aVencerPagar: number };
  faixas: { faixa: string; receber: number; pagar: number }[];
  titulos: Pagina<{
    tipo: TipoTitulo;
    contaId: string;
    descricao: string;
    parceiroNome: string;
    parcela: number;
    vencimento: string;
    saldoPendente: number;
    diasEmAtraso: number;
  }>;
}

export interface ResumoFluxo {
  entradasPrevistas: number;
  saidasPrevistas: number;
  entradasRealizadas: number;
  saidasRealizadas: number;
  saldoRealizado: number;
  saldoProjetado: number;
}

export interface RelatorioFluxoCaixa {
  granularidade: GranularidadeRelatorio;
  resumo: ResumoFluxo;
  saldoAcumuladoAteFim: number;
  serie: { data: string; entradas: number; saidas: number }[];
  porCategoria: { categoriaId: string | null; categoriaNome: string | null; resumo: ResumoFluxo }[];
  movimentacoes: Pagina<{
    id: string;
    data: string;
    tipo: TipoMovimentacaoCaixa;
    descricao: string;
    categoriaNome: string | null;
    origem: OrigemMovimentacaoCaixa;
    situacao: SituacaoMovimentacaoCaixa;
    valor: number;
  }>;
}

// ---- Estoque ----

export interface RelatorioMovimentacoesEstoque {
  totaisPorTipo: { tipo: TipoMovimentacaoEstoque; quantidadeMovimentacoes: number; quantidadeTotal: number }[];
  movimentacoes: Pagina<{
    id: string;
    dataHora: string;
    produtoId: string;
    produtoNome: string;
    tipo: TipoMovimentacaoEstoque;
    quantidade: number;
    origem: OrigemMovimentacaoEstoque;
    motivo: string | null;
    usuarioResponsavel: string | null;
  }>;
}

export interface RelatorioPosicaoEstoque {
  totalProdutos: number;
  produtosAbaixoDoMinimo: number;
  produtos: Pagina<{
    produtoId: string;
    codigoInterno: string;
    nome: string;
    categoriaNome: string | null;
    estoqueFisico: number;
    quantidadeReservada: number;
    quantidadeDisponivel: number;
    estoqueMinimo: number;
    abaixoDoMinimo: boolean;
  }>;
}
