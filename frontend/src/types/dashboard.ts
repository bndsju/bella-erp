export type GranularidadeDashboard = "DIA" | "MES";

export interface PontoVendas {
  data: string;
  valor: number;
  quantidade: number;
}

export interface PontoFluxo {
  data: string;
  entradas: number;
  saidas: number;
}

export interface PosicaoContas {
  emAberto: number;
  vencido: number;
  aVencer: number;
  vencendoNoPeriodo: number;
  quantidadeContasEmAberto: number;
  quantidadeContasVencidas: number;
}

export interface DashboardResponse {
  periodo: { inicio: string; fim: string; granularidade: GranularidadeDashboard };
  comercial: {
    faturamento: number;
    quantidadeVendas: number;
    ticketMedio: number;
    quantidadePedidos: number;
    quantidadePedidosCancelados: number;
    orcamentosEmitidos: number;
    valorOrcamentosEmitidos: number;
    taxaConversaoPedidos: number | null;
    taxaConversaoVendas: number | null;
    vendasPorPeriodo: PontoVendas[];
  };
  financeiro: {
    contasReceber: PosicaoContas;
    contasPagar: PosicaoContas;
    saldoAtual: number;
    entradasPeriodo: number;
    saidasPeriodo: number;
    saldoPeriodo: number;
    entradasPrevistasPeriodo: number;
    saidasPrevistasPeriodo: number;
    fluxoPorPeriodo: PontoFluxo[];
  };
  produtosAbaixoDoMinimo: {
    produtoId: string;
    nome: string;
    quantidadeDisponivel: number;
    estoqueMinimo: number;
  }[];
  totalProdutosAbaixoDoMinimo: number;
  produtosMaisVendidos: { produtoId: string; nome: string; quantidade: number; valor: number }[];
  principaisClientes: { clienteId: string; nome: string; quantidadeVendas: number; valor: number }[];
}
