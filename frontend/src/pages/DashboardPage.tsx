import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { GraficoBarras } from "@/components/dashboard/GraficoBarras";
import { dashboardApi } from "@/lib/dashboardApi";
import { formatarMoeda } from "@/lib/formatos";
import type { DashboardResponse, GranularidadeDashboard, PosicaoContas } from "@/types/dashboard";

function paraIso(data: Date): string {
  const mes = String(data.getMonth() + 1).padStart(2, "0");
  const dia = String(data.getDate()).padStart(2, "0");
  return `${data.getFullYear()}-${mes}-${dia}`;
}

interface PeriodoPredefinido {
  rotulo: string;
  intervalo: () => [string, string];
}

const PERIODOS: PeriodoPredefinido[] = [
  {
    rotulo: "Este mês",
    intervalo: () => {
      const hoje = new Date();
      return [
        paraIso(new Date(hoje.getFullYear(), hoje.getMonth(), 1)),
        paraIso(new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0)),
      ];
    },
  },
  {
    rotulo: "Mês passado",
    intervalo: () => {
      const hoje = new Date();
      return [
        paraIso(new Date(hoje.getFullYear(), hoje.getMonth() - 1, 1)),
        paraIso(new Date(hoje.getFullYear(), hoje.getMonth(), 0)),
      ];
    },
  },
  {
    rotulo: "Últimos 30 dias",
    intervalo: () => {
      const hoje = new Date();
      return [paraIso(new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() - 29)), paraIso(hoje)];
    },
  },
  {
    rotulo: "Este ano",
    intervalo: () => {
      const ano = new Date().getFullYear();
      return [`${ano}-01-01`, `${ano}-12-31`];
    },
  },
];

function rotuloDoPonto(data: string, granularidade: GranularidadeDashboard): string {
  const [ano, mes, dia] = data.split("-");
  return granularidade === "MES" ? `${mes}/${ano.slice(2)}` : `${dia}/${mes}`;
}

function formatarPercentual(valor: number | null): string {
  return valor === null ? "—" : `${valor.toLocaleString("pt-BR", { minimumFractionDigits: 1 })}%`;
}

function formatarQuantidade(valor: number): string {
  return valor.toLocaleString("pt-BR", { maximumFractionDigits: 3 });
}

function Indicador({
  titulo,
  valor,
  detalhe,
  classeValor,
}: {
  titulo: string;
  valor: string;
  detalhe?: string;
  classeValor?: string;
}) {
  return (
    <Card>
      <CardContent className="pt-6">
        <p className="text-xs text-muted-foreground">{titulo}</p>
        <p className={`text-lg font-medium ${classeValor ?? ""}`}>{valor}</p>
        {detalhe && <p className="text-xs text-muted-foreground">{detalhe}</p>}
      </CardContent>
    </Card>
  );
}

function CartaoContas({ titulo, posicao, rota }: { titulo: string; posicao: PosicaoContas; rota: string }) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>{titulo}</CardTitle>
        <Button variant="ghost" size="sm" asChild>
          <Link to={rota}>Ver contas</Link>
        </Button>
      </CardHeader>
      <CardContent className="grid grid-cols-3 gap-3 text-sm">
        <div>
          <p className="text-xs text-muted-foreground">Em aberto</p>
          <p className="font-medium">{formatarMoeda(posicao.emAberto)}</p>
          <p className="text-xs text-muted-foreground">{posicao.quantidadeContasEmAberto} conta(s)</p>
        </div>
        <div>
          <p className="text-xs text-muted-foreground">Vencido</p>
          <p className="font-medium text-destructive">{formatarMoeda(posicao.vencido)}</p>
          <p className="text-xs text-muted-foreground">{posicao.quantidadeContasVencidas} conta(s)</p>
        </div>
        <div>
          <p className="text-xs text-muted-foreground">A vencer</p>
          <p className="font-medium">{formatarMoeda(posicao.aVencer)}</p>
          <p className="text-xs text-muted-foreground">No período: {formatarMoeda(posicao.vencendoNoPeriodo)}</p>
        </div>
      </CardContent>
    </Card>
  );
}

export function DashboardPage() {
  const [inicioPadrao, fimPadrao] = PERIODOS[0].intervalo();
  const [inicio, setInicio] = useState(inicioPadrao);
  const [fim, setFim] = useState(fimPadrao);
  const [dados, setDados] = useState<DashboardResponse | null>(null);
  const [carregando, setCarregando] = useState(true);

  const carregar = useCallback(() => {
    if (!inicio || !fim) return;
    if (fim < inicio) {
      toast.error("A data final não pode ser anterior à data inicial");
      return;
    }
    setCarregando(true);
    dashboardApi
      .consultar(inicio, fim)
      .then(setDados)
      .catch(() => toast.error("Não foi possível carregar o painel"))
      .finally(() => setCarregando(false));
  }, [inicio, fim]);

  useEffect(carregar, [carregar]);

  const granularidade = dados?.periodo.granularidade ?? "DIA";

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <h1 className="text-xl font-medium">Início</h1>
        <div className="flex flex-wrap items-end gap-2">
          {PERIODOS.map((periodo) => (
            <Button
              key={periodo.rotulo}
              variant="outline"
              size="sm"
              onClick={() => {
                const [de, ate] = periodo.intervalo();
                setInicio(de);
                setFim(ate);
              }}
            >
              {periodo.rotulo}
            </Button>
          ))}
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">De</label>
            <Input type="date" className="w-40" value={inicio} onChange={(e) => setInicio(e.target.value)} />
          </div>
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Até</label>
            <Input type="date" className="w-40" value={fim} onChange={(e) => setFim(e.target.value)} />
          </div>
        </div>
      </div>

      {carregando && !dados ? (
        <div className="space-y-3">
          <Skeleton className="h-24 w-full" />
          <Skeleton className="h-48 w-full" />
          <Skeleton className="h-24 w-full" />
        </div>
      ) : dados ? (
        <div className={carregando ? "space-y-8 opacity-60 transition-opacity" : "space-y-8"}>
          <section className="space-y-3">
            <h2 className="text-base font-medium">Comercial</h2>
            <div className="grid grid-cols-2 gap-3 lg:grid-cols-5">
              <Indicador
                titulo="Faturamento"
                valor={formatarMoeda(dados.comercial.faturamento)}
                detalhe={`Ticket médio: ${formatarMoeda(dados.comercial.ticketMedio)}`}
              />
              <Indicador
                titulo="Vendas realizadas"
                valor={String(dados.comercial.quantidadeVendas)}
                detalhe="Concluídas no período"
              />
              <Indicador
                titulo="Pedidos"
                valor={String(dados.comercial.quantidadePedidos)}
                detalhe={`${dados.comercial.quantidadePedidosCancelados} cancelado(s)`}
              />
              <Indicador
                titulo="Orçamentos emitidos"
                valor={String(dados.comercial.orcamentosEmitidos)}
                detalhe={formatarMoeda(dados.comercial.valorOrcamentosEmitidos)}
              />
              <Indicador
                titulo="Conversão de orçamentos"
                valor={formatarPercentual(dados.comercial.taxaConversaoPedidos)}
                detalhe={`Em venda: ${formatarPercentual(dados.comercial.taxaConversaoVendas)}`}
              />
            </div>
            <Card>
              <CardHeader>
                <CardTitle>Vendas por {granularidade === "MES" ? "mês" : "dia"}</CardTitle>
              </CardHeader>
              <CardContent>
                <GraficoBarras
                  series={[{ nome: "Vendas", classe: "fill-primary" }]}
                  pontos={dados.comercial.vendasPorPeriodo.map((ponto) => ({
                    rotulo: rotuloDoPonto(ponto.data, granularidade),
                    valores: [ponto.valor],
                  }))}
                  vazio="Nenhuma venda no período."
                />
              </CardContent>
            </Card>
          </section>

          <section className="space-y-3">
            <h2 className="text-base font-medium">Financeiro</h2>
            <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
              <Indicador titulo="Saldo atual do caixa" valor={formatarMoeda(dados.financeiro.saldoAtual)} />
              <Indicador
                titulo="Entradas no período"
                valor={formatarMoeda(dados.financeiro.entradasPeriodo)}
                classeValor="text-primary"
                detalhe={`Previstas: ${formatarMoeda(dados.financeiro.entradasPrevistasPeriodo)}`}
              />
              <Indicador
                titulo="Saídas no período"
                valor={formatarMoeda(dados.financeiro.saidasPeriodo)}
                classeValor="text-destructive"
                detalhe={`Previstas: ${formatarMoeda(dados.financeiro.saidasPrevistasPeriodo)}`}
              />
              <Indicador titulo="Saldo do período" valor={formatarMoeda(dados.financeiro.saldoPeriodo)} />
            </div>
            <div className="grid grid-cols-1 gap-3 lg:grid-cols-2">
              <CartaoContas titulo="Contas a receber" posicao={dados.financeiro.contasReceber} rota="/contas-receber" />
              <CartaoContas titulo="Contas a pagar" posicao={dados.financeiro.contasPagar} rota="/contas-pagar" />
            </div>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle>Entradas e saídas por {granularidade === "MES" ? "mês" : "dia"}</CardTitle>
                <Button variant="ghost" size="sm" asChild>
                  <Link to="/fluxo-caixa">Ver fluxo de caixa</Link>
                </Button>
              </CardHeader>
              <CardContent>
                <GraficoBarras
                  series={[
                    { nome: "Entradas", classe: "fill-primary" },
                    { nome: "Saídas", classe: "fill-destructive" },
                  ]}
                  pontos={dados.financeiro.fluxoPorPeriodo.map((ponto) => ({
                    rotulo: rotuloDoPonto(ponto.data, granularidade),
                    valores: [ponto.entradas, ponto.saidas],
                  }))}
                  vazio="Nenhuma movimentação realizada no período."
                />
              </CardContent>
            </Card>
          </section>

          <section className="space-y-3">
            <h2 className="text-base font-medium">Destaques</h2>
            <div className="grid grid-cols-1 gap-3 lg:grid-cols-2">
              <Card>
                <CardHeader>
                  <CardTitle>Produtos mais vendidos</CardTitle>
                </CardHeader>
                <CardContent>
                  {dados.produtosMaisVendidos.length === 0 ? (
                    <p className="text-sm text-muted-foreground">Nenhuma venda no período.</p>
                  ) : (
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Produto</TableHead>
                          <TableHead className="text-right">Quantidade</TableHead>
                          <TableHead className="text-right">Valor</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {dados.produtosMaisVendidos.map((produto) => (
                          <TableRow key={produto.produtoId}>
                            <TableCell className="font-medium">{produto.nome}</TableCell>
                            <TableCell className="text-right">{formatarQuantidade(produto.quantidade)}</TableCell>
                            <TableCell className="text-right">{formatarMoeda(produto.valor)}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  )}
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle>Principais clientes</CardTitle>
                </CardHeader>
                <CardContent>
                  {dados.principaisClientes.length === 0 ? (
                    <p className="text-sm text-muted-foreground">Nenhuma venda no período.</p>
                  ) : (
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Cliente</TableHead>
                          <TableHead className="text-right">Vendas</TableHead>
                          <TableHead className="text-right">Valor</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {dados.principaisClientes.map((cliente) => (
                          <TableRow key={cliente.clienteId}>
                            <TableCell className="font-medium">{cliente.nome}</TableCell>
                            <TableCell className="text-right">{cliente.quantidadeVendas}</TableCell>
                            <TableCell className="text-right">{formatarMoeda(cliente.valor)}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  )}
                </CardContent>
              </Card>
            </div>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle>Estoque abaixo do mínimo ({dados.totalProdutosAbaixoDoMinimo})</CardTitle>
                <Button variant="ghost" size="sm" asChild>
                  <Link to="/estoque">Ver estoque</Link>
                </Button>
              </CardHeader>
              <CardContent>
                {dados.produtosAbaixoDoMinimo.length === 0 ? (
                  <p className="text-sm text-muted-foreground">Nenhum produto abaixo do estoque mínimo.</p>
                ) : (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Produto</TableHead>
                        <TableHead className="text-right">Disponível</TableHead>
                        <TableHead className="text-right">Mínimo</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {dados.produtosAbaixoDoMinimo.map((produto) => (
                        <TableRow key={produto.produtoId}>
                          <TableCell className="font-medium">{produto.nome}</TableCell>
                          <TableCell className="text-right text-destructive">
                            {formatarQuantidade(produto.quantidadeDisponivel)}
                          </TableCell>
                          <TableCell className="text-right">{formatarQuantidade(produto.estoqueMinimo)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
          </section>
        </div>
      ) : null}
    </div>
  );
}
