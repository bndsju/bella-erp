import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import { GraficoBarras } from "@/components/dashboard/GraficoBarras";
import { dashboardApi } from "@/lib/dashboardApi";
import { dataDeHoje, formatarMoeda } from "@/lib/formatos";
import { saudacao, useUsuarioAtual } from "@/lib/usuario";
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

function Indicador({ rotulo, valor, nota }: { rotulo: string; valor: string; nota?: string }) {
  return (
    <Card className="indicador">
      <div className="indicador__rotulo">{rotulo}</div>
      <div className="indicador__valor">{valor}</div>
      {nota && <div className="indicador__nota">{nota}</div>}
    </Card>
  );
}

function Linha({ rotulo, valor, destaque }: { rotulo: string; valor: string; destaque?: "erro" }) {
  return (
    <div className="linha">
      <span>{rotulo}</span>
      <span className={destaque === "erro" ? "font-extrabold text-erro" : "font-extrabold"}>{valor}</span>
    </div>
  );
}

function CartaoContas({ titulo, posicao, rota }: { titulo: string; posicao: PosicaoContas; rota: string }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{titulo}</CardTitle>
        <Button variant="secondary" size="sm" asChild>
          <Link to={rota}>Ver contas</Link>
        </Button>
      </CardHeader>
      <Linha rotulo={`Em aberto (${posicao.quantidadeContasEmAberto})`} valor={formatarMoeda(posicao.emAberto)} />
      <Linha
        rotulo={`Vencido (${posicao.quantidadeContasVencidas})`}
        valor={formatarMoeda(posicao.vencido)}
        destaque={posicao.vencido > 0 ? "erro" : undefined}
      />
      <Linha rotulo="A vencer" valor={formatarMoeda(posicao.aVencer)} />
      <Linha rotulo="Vencendo no período" valor={formatarMoeda(posicao.vencendoNoPeriodo)} />
    </Card>
  );
}

export function DashboardPage() {
  const { nome } = useUsuarioAtual();
  const [inicioPadrao, fimPadrao] = PERIODOS[0].intervalo();
  const [inicio, setInicio] = useState(inicioPadrao);
  const [fim, setFim] = useState(fimPadrao);
  const [dados, setDados] = useState<DashboardResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [falhou, setFalhou] = useState(false);

  const carregar = useCallback(() => {
    if (!inicio || !fim) return;
    if (fim < inicio) {
      toast.error("A data final não pode ser anterior à data inicial");
      return;
    }
    setCarregando(true);
    setFalhou(false);
    dashboardApi
      .consultar(inicio, fim)
      .then(setDados)
      .catch(() => {
        setFalhou(true);
        toast.error("Não foi possível carregar o painel");
      })
      .finally(() => setCarregando(false));
  }, [inicio, fim]);

  useEffect(carregar, [carregar]);

  const granularidade = dados?.periodo.granularidade ?? "DIA";
  const hoje = dataDeHoje();
  const frase = dados ? `Neste período você vendeu ${formatarMoeda(dados.comercial.faturamento)}.` : undefined;

  return (
    <>
      <CabecalhoPagina titulo={saudacao(nome)} frase={frase} faixaAlta />

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="periodo-de">De</label>
            <input id="periodo-de" type="date" value={inicio} onChange={(e) => setInicio(e.target.value)} />
          </div>
          <div className="campo">
            <label htmlFor="periodo-ate">Até</label>
            <input id="periodo-ate" type="date" value={fim} onChange={(e) => setFim(e.target.value)} />
          </div>
          {PERIODOS.map((periodo) => (
            <Button
              key={periodo.rotulo}
              variant="secondary"
              onClick={() => {
                const [de, ate] = periodo.intervalo();
                setInicio(de);
                setFim(ate);
              }}
            >
              {periodo.rotulo}
            </Button>
          ))}
        </div>
      </Card>

      {falhou && !dados ? (
        <Card>
          <div className="vazio">
            <h3>Não consegui carregar o painel</h3>
            <p>Confira se o sistema está no ar e tente de novo.</p>
            <Button onClick={carregar}>Tentar de novo</Button>
          </div>
        </Card>
      ) : carregando && !dados ? (
        <>
          <Skeleton className="h-28 w-full rounded-[20px]" />
          <Skeleton className="h-64 w-full rounded-[20px]" />
        </>
      ) : dados ? (
        <div className={carregando ? "contents opacity-60" : "contents"}>
          <div className="grade grade--4">
            <Indicador
              rotulo="Faturamento"
              valor={formatarMoeda(dados.comercial.faturamento)}
              nota={`Ticket médio: ${formatarMoeda(dados.comercial.ticketMedio)}`}
            />
            <Indicador
              rotulo="Saldo do caixa"
              valor={formatarMoeda(dados.financeiro.saldoAtual)}
              nota={`Projetado no período: ${formatarMoeda(dados.financeiro.saldoPeriodo)}`}
            />
            <Indicador
              rotulo="A receber"
              valor={formatarMoeda(dados.financeiro.contasReceber.emAberto)}
              nota={`${formatarMoeda(dados.financeiro.contasReceber.vencido)} vencidos`}
            />
            <Indicador
              rotulo="A pagar"
              valor={formatarMoeda(dados.financeiro.contasPagar.emAberto)}
              nota={`${formatarMoeda(dados.financeiro.contasPagar.vencido)} vencidos`}
            />
          </div>

          <div className="grade grade--principal">
            <Card variante="destaque">
              <CardTitle>Vendas por {granularidade === "MES" ? "mês" : "dia"}</CardTitle>
              <GraficoBarras
                sobreDestaque
                series={[{ nome: "Vendas" }]}
                pontos={dados.comercial.vendasPorPeriodo.map((ponto) => ({
                  rotulo: rotuloDoPonto(ponto.data, granularidade),
                  valores: [ponto.valor],
                  atual: ponto.data === hoje,
                }))}
                vazio={{ titulo: "Nenhuma venda no período", texto: "Sua primeira venda aparece aqui." }}
              />
            </Card>
            <Card variante="suave">
              <CardHeader>
                <CardTitle>Repor em breve</CardTitle>
                <Button variant="faixa" size="sm" asChild>
                  <Link to="/estoque">Ver estoque</Link>
                </Button>
              </CardHeader>
              {dados.produtosAbaixoDoMinimo.length === 0 ? (
                <p>Nenhum produto abaixo do mínimo.</p>
              ) : (
                dados.produtosAbaixoDoMinimo.map((produto) => (
                  <div key={produto.produtoId} className="linha">
                    <span>{produto.nome}</span>
                    <span>
                      {formatarQuantidade(produto.quantidadeDisponivel)} de {formatarQuantidade(produto.estoqueMinimo)}
                    </span>
                  </div>
                ))
              )}
              {dados.totalProdutosAbaixoDoMinimo > dados.produtosAbaixoDoMinimo.length && (
                <p className="mt-3 text-[13px] font-semibold">
                  E mais {dados.totalProdutosAbaixoDoMinimo - dados.produtosAbaixoDoMinimo.length} produto(s).
                </p>
              )}
            </Card>
          </div>

          <div className="grade grade--2">
            <Card>
              <CardTitle>Comercial</CardTitle>
              <Linha rotulo="Vendas realizadas" valor={String(dados.comercial.quantidadeVendas)} />
              <Linha
                rotulo="Pedidos"
                valor={`${dados.comercial.quantidadePedidos} (${dados.comercial.quantidadePedidosCancelados} cancelados)`}
              />
              <Linha
                rotulo="Orçamentos emitidos"
                valor={`${dados.comercial.orcamentosEmitidos} · ${formatarMoeda(dados.comercial.valorOrcamentosEmitidos)}`}
              />
              <Linha rotulo="Conversão em pedido" valor={formatarPercentual(dados.comercial.taxaConversaoPedidos)} />
              <Linha rotulo="Conversão em venda" valor={formatarPercentual(dados.comercial.taxaConversaoVendas)} />
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>Caixa no período</CardTitle>
                <Button variant="secondary" size="sm" asChild>
                  <Link to="/fluxo-caixa">Ver fluxo de caixa</Link>
                </Button>
              </CardHeader>
              <Linha rotulo="Entradas realizadas" valor={formatarMoeda(dados.financeiro.entradasPeriodo)} />
              <Linha rotulo="Entradas previstas" valor={formatarMoeda(dados.financeiro.entradasPrevistasPeriodo)} />
              <Linha rotulo="Saídas realizadas" valor={formatarMoeda(dados.financeiro.saidasPeriodo)} />
              <Linha rotulo="Saídas previstas" valor={formatarMoeda(dados.financeiro.saidasPrevistasPeriodo)} />
              <Linha rotulo="Saldo do período" valor={formatarMoeda(dados.financeiro.saldoPeriodo)} />
            </Card>
          </div>

          <div className="grade grade--2">
            <CartaoContas titulo="Contas a receber" posicao={dados.financeiro.contasReceber} rota="/contas-receber" />
            <CartaoContas titulo="Contas a pagar" posicao={dados.financeiro.contasPagar} rota="/contas-pagar" />
          </div>

          <Card>
            <CardTitle>Entradas e saídas por {granularidade === "MES" ? "mês" : "dia"}</CardTitle>
            <GraficoBarras
              series={[
                { nome: "Entradas", cor: "var(--grafico-2)" },
                { nome: "Saídas", cor: "var(--grafico-4)" },
              ]}
              pontos={dados.financeiro.fluxoPorPeriodo.map((ponto) => ({
                rotulo: rotuloDoPonto(ponto.data, granularidade),
                valores: [ponto.entradas, ponto.saidas],
              }))}
              vazio={{ titulo: "Nenhuma movimentação no período", texto: "Entradas e saídas realizadas aparecem aqui." }}
            />
          </Card>

          <div className="grade grade--2">
            <Card>
              <CardTitle>Produtos mais vendidos</CardTitle>
              {dados.produtosMaisVendidos.length === 0 ? (
                <div className="vazio">
                  <h3>Nenhuma venda no período</h3>
                  <p>Os produtos mais vendidos aparecem aqui.</p>
                </div>
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
                        <TableCell className="tabela__forte">{produto.nome}</TableCell>
                        <TableCell className="text-right">{formatarQuantidade(produto.quantidade)}</TableCell>
                        <TableCell className="text-right">{formatarMoeda(produto.valor)}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </Card>
            <Card>
              <CardTitle>Principais clientes</CardTitle>
              {dados.principaisClientes.length === 0 ? (
                <div className="vazio">
                  <h3>Nenhuma venda no período</h3>
                  <p>Quem mais compra de você aparece aqui.</p>
                </div>
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
                        <TableCell className="tabela__forte">{cliente.nome}</TableCell>
                        <TableCell className="text-right">{cliente.quantidadeVendas}</TableCell>
                        <TableCell className="text-right">{formatarMoeda(cliente.valor)}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </Card>
          </div>
        </div>
      ) : null}
    </>
  );
}
