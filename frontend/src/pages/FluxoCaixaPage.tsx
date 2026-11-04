import { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { CancelarMovimentacaoCaixaDialog } from "@/components/fluxocaixa/CancelarMovimentacaoCaixaDialog";
import { LancamentoCaixaFormDialog } from "@/components/fluxocaixa/LancamentoCaixaFormDialog";
import { RealizarMovimentacaoCaixaDialog } from "@/components/fluxocaixa/RealizarMovimentacaoCaixaDialog";
import { ApiError } from "@/lib/api";
import { categoriaFluxoCaixaApi } from "@/lib/categoriaFluxoCaixaApi";
import { fluxoCaixaApi } from "@/lib/fluxoCaixaApi";
import { ROTULO_ORIGEM } from "@/lib/fluxoCaixaRotulos";
import { formatarData, formatarMoeda } from "@/lib/formatos";
import type { CategoriaFluxoCaixaResponse } from "@/types/categoriaFluxoCaixa";
import { Ban, CircleCheck, Pencil } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import type {
  MovimentacaoCaixaResponse,
  OrigemMovimentacaoCaixa,
  ResumoCaixaResponse,
  ResumoCategoriaCaixaResponse,
  SituacaoMovimentacaoCaixa,
  TipoMovimentacaoCaixa,
} from "@/types/fluxoCaixa";

function paraIso(data: Date): string {
  const mes = String(data.getMonth() + 1).padStart(2, "0");
  const dia = String(data.getDate()).padStart(2, "0");
  return `${data.getFullYear()}-${mes}-${dia}`;
}

function inicioDoMes(): string {
  const hoje = new Date();
  return paraIso(new Date(hoje.getFullYear(), hoje.getMonth(), 1));
}

function fimDoMes(): string {
  const hoje = new Date();
  return paraIso(new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0));
}

function dataDaMovimentacao(movimentacao: MovimentacaoCaixaResponse): string {
  return movimentacao.situacao === "REALIZADO" && movimentacao.dataEfetiva
    ? movimentacao.dataEfetiva
    : movimentacao.dataPrevista;
}

export function FluxoCaixaPage() {
  const [movimentacoes, setMovimentacoes] = useState<MovimentacaoCaixaResponse[]>([]);
  const [resumo, setResumo] = useState<ResumoCaixaResponse | null>(null);
  const [saldoAcumulado, setSaldoAcumulado] = useState<ResumoCaixaResponse | null>(null);
  const [porCategoria, setPorCategoria] = useState<ResumoCategoriaCaixaResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaFluxoCaixaResponse[]>([]);
  const [carregando, setCarregando] = useState(true);

  const [dataInicio, setDataInicio] = useState(inicioDoMes());
  const [dataFim, setDataFim] = useState(fimDoMes());
  const [tipo, setTipo] = useState<TipoMovimentacaoCaixa | "TODOS">("TODOS");
  const [categoriaId, setCategoriaId] = useState("TODAS");
  const [origem, setOrigem] = useState<OrigemMovimentacaoCaixa | "TODAS">("TODAS");
  const [situacao, setSituacao] = useState<SituacaoMovimentacaoCaixa | "TODAS">("TODAS");
  const [incluirCanceladas, setIncluirCanceladas] = useState(false);

  const [dialogLancamentoAberto, setDialogLancamentoAberto] = useState(false);
  const [emEdicao, setEmEdicao] = useState<MovimentacaoCaixaResponse | null>(null);
  const [emRealizacao, setEmRealizacao] = useState<MovimentacaoCaixaResponse | null>(null);
  const [emCancelamento, setEmCancelamento] = useState<MovimentacaoCaixaResponse | null>(null);
  const [processando, setProcessando] = useState(false);

  useEffect(() => {
    categoriaFluxoCaixaApi.listar().then(setCategorias).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    Promise.all([
      fluxoCaixaApi.listar({
        tipo: tipo === "TODOS" ? undefined : tipo,
        categoriaId: categoriaId === "TODAS" ? undefined : categoriaId,
        origem: origem === "TODAS" ? undefined : origem,
        situacao: situacao === "TODAS" ? undefined : situacao,
        dataInicio: dataInicio || undefined,
        dataFim: dataFim || undefined,
        incluirCanceladas,
      }),
      fluxoCaixaApi.resumo(dataInicio || undefined, dataFim || undefined),
      fluxoCaixaApi.saldoAcumulado(),
      fluxoCaixaApi.resumoPorCategoria(dataInicio || undefined, dataFim || undefined),
    ])
      .then(([lista, resumoPeriodo, acumulado, categoriasResumo]) => {
        setMovimentacoes(lista);
        setResumo(resumoPeriodo);
        setSaldoAcumulado(acumulado);
        setPorCategoria(categoriasResumo);
      })
      .catch(() => toast.error("Não foi possível carregar o fluxo de caixa"))
      .finally(() => setCarregando(false));
  }, [tipo, categoriaId, origem, situacao, dataInicio, dataFim, incluirCanceladas]);

  useEffect(carregar, [carregar]);

  async function executar(acao: () => Promise<unknown>, sucesso: string, erro: string, aoConcluir: () => void) {
    setProcessando(true);
    try {
      await acao();
      toast.success(sucesso);
      aoConcluir();
      carregar();
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : erro);
    } finally {
      setProcessando(false);
    }
  }

  function realizar(dataEfetiva: string) {
    if (!emRealizacao) return;
    executar(
      () => fluxoCaixaApi.realizar(emRealizacao.id, dataEfetiva),
      "Lançamento realizado",
      "Não foi possível realizar o lançamento",
      () => setEmRealizacao(null),
    );
  }

  function cancelar(motivo: string) {
    if (!emCancelamento) return;
    executar(
      () => fluxoCaixaApi.cancelar(emCancelamento.id, motivo),
      "Lançamento cancelado",
      "Não foi possível cancelar o lançamento",
      () => setEmCancelamento(null),
    );
  }

  return (
    <div className="space-y-4">
      <CabecalhoPagina
        titulo="Fluxo de caixa"
        acao={
          <>
          <Button
            onClick={() => {
              setEmEdicao(null);
              setDialogLancamentoAberto(true);
            }}
          >
            Novo lançamento
          </Button>
          </>
        }
      />

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Saldo acumulado até hoje</p>
            <p className="text-lg font-medium">{formatarMoeda(saldoAcumulado?.saldoRealizado ?? 0)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Entradas realizadas</p>
            <p className="text-lg font-medium text-primary">{formatarMoeda(resumo?.entradasRealizadas ?? 0)}</p>
            <p className="text-xs text-muted-foreground">
              Previstas: {formatarMoeda(resumo?.entradasPrevistas ?? 0)}
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Saídas realizadas</p>
            <p className="text-lg font-medium text-destructive">{formatarMoeda(resumo?.saidasRealizadas ?? 0)}</p>
            <p className="text-xs text-muted-foreground">
              Previstas: {formatarMoeda(resumo?.saidasPrevistas ?? 0)}
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Saldo do período</p>
            <p className="text-lg font-medium">{formatarMoeda(resumo?.saldoRealizado ?? 0)}</p>
            <p className="text-xs text-muted-foreground">
              Projetado: {formatarMoeda(resumo?.saldoProjetado ?? 0)}
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="cartao flex flex-wrap items-end gap-3">
        <div className="space-y-1">
          <label className="block text-xs text-muted-foreground">De</label>
          <Input type="date" className="w-40" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
        </div>
        <div className="space-y-1">
          <label className="block text-xs text-muted-foreground">Até</label>
          <Input type="date" className="w-40" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
        </div>
        <Select value={tipo} onValueChange={(v) => setTipo(v as TipoMovimentacaoCaixa | "TODOS")}>
          <SelectTrigger className="w-40">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Entradas e saídas</SelectItem>
            <SelectItem value="ENTRADA">Entradas</SelectItem>
            <SelectItem value="SAIDA">Saídas</SelectItem>
          </SelectContent>
        </Select>
        <Select value={situacao} onValueChange={(v) => setSituacao(v as SituacaoMovimentacaoCaixa | "TODAS")}>
          <SelectTrigger className="w-44">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODAS">Previstos e realizados</SelectItem>
            <SelectItem value="PREVISTO">Previstos</SelectItem>
            <SelectItem value="REALIZADO">Realizados</SelectItem>
          </SelectContent>
        </Select>
        <Select value={origem} onValueChange={(v) => setOrigem(v as OrigemMovimentacaoCaixa | "TODAS")}>
          <SelectTrigger className="w-48">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODAS">Todas as origens</SelectItem>
            {(Object.keys(ROTULO_ORIGEM) as OrigemMovimentacaoCaixa[]).map((chave) => (
              <SelectItem key={chave} value={chave}>
                {ROTULO_ORIGEM[chave]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select value={categoriaId} onValueChange={setCategoriaId}>
          <SelectTrigger className="w-48">
            <SelectValue placeholder="Categoria" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODAS">Todas as categorias</SelectItem>
            {categorias.map((categoria) => (
              <SelectItem key={categoria.id} value={categoria.id}>
                {categoria.nome}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <label className="flex items-center gap-2 pb-2 text-sm">
          <input
            type="checkbox"
            checked={incluirCanceladas}
            onChange={(e) => setIncluirCanceladas(e.target.checked)}
          />
          Mostrar canceladas
        </label>
      </div>

      <div className="rounded-cartao bg-card p-2">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Data</TableHead>
              <TableHead>Descrição</TableHead>
              <TableHead>Categoria</TableHead>
              <TableHead>Origem</TableHead>
              <TableHead className="text-right">Valor</TableHead>
              <TableHead>Situação</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              Array.from({ length: 4 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell colSpan={7}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))
            ) : movimentacoes.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-8 text-center text-muted-foreground">
                  Nenhuma movimentação no período.
                </TableCell>
              </TableRow>
            ) : (
              movimentacoes.map((movimentacao) => {
                const manual = movimentacao.origem === "LANCAMENTO_MANUAL";
                const editavel = manual && !movimentacao.cancelada;
                return (
                  <TableRow key={movimentacao.id} className={movimentacao.cancelada ? "opacity-60" : undefined}>
                    <TableCell>{formatarData(dataDaMovimentacao(movimentacao))}</TableCell>
                    <TableCell className="font-medium">
                      {movimentacao.descricao}
                      {movimentacao.cancelada && movimentacao.motivoCancelamento && (
                        <p className="text-xs font-normal text-muted-foreground">
                          Cancelada: {movimentacao.motivoCancelamento}
                        </p>
                      )}
                    </TableCell>
                    <TableCell>{movimentacao.categoriaNome ?? "—"}</TableCell>
                    <TableCell>{ROTULO_ORIGEM[movimentacao.origem]}</TableCell>
                    <TableCell
                      className={`text-right ${movimentacao.tipo === "ENTRADA" ? "text-primary" : "text-destructive"}`}
                    >
                      {movimentacao.tipo === "ENTRADA" ? "+" : "−"} {formatarMoeda(movimentacao.valor)}
                    </TableCell>
                    <TableCell>
                      {movimentacao.cancelada ? (
                        <Badge variant="outline">Cancelada</Badge>
                      ) : (
                        <Badge variant={movimentacao.situacao === "REALIZADO" ? "default" : "outline"}>
                          {movimentacao.situacao === "REALIZADO" ? "Realizado" : "Previsto"}
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <MenuAcoes
                        itens={[
                          {
                            rotulo: "Realizar",
                            icone: CircleCheck,
                            oculto: !(editavel && movimentacao.situacao === "PREVISTO"),
                            aoClicar: () => setEmRealizacao(movimentacao),
                          },
                          {
                            rotulo: "Editar",
                            icone: Pencil,
                            oculto: !(editavel && movimentacao.situacao === "PREVISTO"),
                            aoClicar: () => {
                              setEmEdicao(movimentacao);
                              setDialogLancamentoAberto(true);
                            },
                          },
                          {
                            rotulo: "Cancelar lançamento",
                            icone: Ban,
                            perigo: true,
                            oculto: !editavel,
                            aoClicar: () => setEmCancelamento(movimentacao),
                          },
                        ]}
                      />
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>

      {porCategoria.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Resumo por categoria</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="rounded-xl border border-border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Categoria</TableHead>
                    <TableHead className="text-right">Entradas realizadas</TableHead>
                    <TableHead className="text-right">Saídas realizadas</TableHead>
                    <TableHead className="text-right">Saldo realizado</TableHead>
                    <TableHead className="text-right">Saldo projetado</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {porCategoria.map((item) => (
                    <TableRow key={item.categoriaId ?? "sem-categoria"}>
                      <TableCell className="font-medium">{item.categoriaNome ?? "Sem categoria"}</TableCell>
                      <TableCell className="text-right">{formatarMoeda(item.resumo.entradasRealizadas)}</TableCell>
                      <TableCell className="text-right">{formatarMoeda(item.resumo.saidasRealizadas)}</TableCell>
                      <TableCell className="text-right">{formatarMoeda(item.resumo.saldoRealizado)}</TableCell>
                      <TableCell className="text-right">{formatarMoeda(item.resumo.saldoProjetado)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>
      )}

      <LancamentoCaixaFormDialog
        open={dialogLancamentoAberto}
        onOpenChange={setDialogLancamentoAberto}
        movimentacao={emEdicao}
        onSalvo={carregar}
      />
      <RealizarMovimentacaoCaixaDialog
        open={emRealizacao !== null}
        onOpenChange={(aberto) => !aberto && setEmRealizacao(null)}
        movimentacao={emRealizacao}
        processando={processando}
        onConfirmar={realizar}
      />
      <CancelarMovimentacaoCaixaDialog
        open={emCancelamento !== null}
        onOpenChange={(aberto) => !aberto && setEmCancelamento(null)}
        processando={processando}
        onConfirmar={cancelar}
      />
    </div>
  );
}
