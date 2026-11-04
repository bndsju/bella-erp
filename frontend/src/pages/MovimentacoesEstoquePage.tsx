import { useCallback, useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
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
import { MovimentacaoEstoqueDialog } from "@/components/estoque/MovimentacaoEstoqueDialog";
import { TipoMovimentacaoBadge } from "@/components/estoque/TipoMovimentacaoBadge";
import { estoqueApi } from "@/lib/estoqueApi";
import { fimDoDia, inicioDoDia, quantidadeComSinal, ROTULO_ORIGEM, ROTULO_TIPO } from "@/lib/estoqueFormatos";
import { produtoApi } from "@/lib/produtoApi";
import type { MovimentacaoEstoqueResponse, TipoMovimentacaoEstoque } from "@/types/estoque";
import type { ProdutoResponse } from "@/types/produto";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

const TIPOS = Object.keys(ROTULO_TIPO) as TipoMovimentacaoEstoque[];

export function MovimentacoesEstoquePage() {
  const [parametros] = useSearchParams();
  const [movimentacoes, setMovimentacoes] = useState<MovimentacaoEstoqueResponse[]>([]);
  const [produtos, setProdutos] = useState<ProdutoResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [produtoId, setProdutoId] = useState<string>(parametros.get("produtoId") ?? "TODOS");
  const [tipo, setTipo] = useState<TipoMovimentacaoEstoque | "TODOS">("TODOS");
  const [dataInicio, setDataInicio] = useState("");
  const [dataFim, setDataFim] = useState("");
  const [dialogAberto, setDialogAberto] = useState(false);

  useEffect(() => {
    produtoApi.listar().then(setProdutos).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    estoqueApi
      .listarMovimentacoes({
        produtoId: produtoId === "TODOS" ? undefined : produtoId,
        tipo: tipo === "TODOS" ? undefined : tipo,
        dataInicio: dataInicio ? inicioDoDia(dataInicio) : undefined,
        dataFim: dataFim ? fimDoDia(dataFim) : undefined,
      })
      .then(setMovimentacoes)
      .catch(() => toast.error("Não foi possível carregar as movimentações"))
      .finally(() => setCarregando(false));
  }, [produtoId, tipo, dataInicio, dataFim]);

  useEffect(carregar, [carregar]);

  return (
    <>
      <CabecalhoPagina
        titulo="Histórico de movimentações"
        acao={
          <>
          <div className="flex gap-2">
            <Button variant="outline" asChild>
              <Link to="/estoque">Voltar ao estoque</Link>
            </Button>
            <Button onClick={() => setDialogAberto(true)}>Registrar movimentação</Button>
          </div>
          </>
        }
      />

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-produto">Produto</label>
            <Select value={produtoId} onValueChange={setProdutoId}>
            <SelectTrigger id="filtro-produto" className="w-full">
            <SelectValue placeholder="Produto" />
            </SelectTrigger>
            <SelectContent>
            <SelectItem value="TODOS">Todos os produtos</SelectItem>
            {produtos.map((produto) => (
            <SelectItem key={produto.id} value={produto.id}>
            {produto.codigoInterno} — {produto.nome}
            </SelectItem>
            ))}
            </SelectContent>
            </Select>
          </div>
          <div className="campo">
            <label htmlFor="filtro-tipo">Tipo</label>
            <Select value={tipo} onValueChange={(v) => setTipo(v as TipoMovimentacaoEstoque | "TODOS")}>
            <SelectTrigger id="filtro-tipo" className="w-full">
            <SelectValue />
            </SelectTrigger>
            <SelectContent>
            <SelectItem value="TODOS">Todos os tipos</SelectItem>
            {TIPOS.map((item) => (
            <SelectItem key={item} value={item}>
            {ROTULO_TIPO[item]}
            </SelectItem>
            ))}
            </SelectContent>
            </Select>
          </div>
        <div className="campo">
          <label htmlFor="filtro-de">De</label>
          <Input id="filtro-de" type="date" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
        </div>
        <div className="campo">
          <label htmlFor="filtro-ate">Até</label>
          <Input id="filtro-ate" type="date" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
        </div>
        </div>
      </Card>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Data/hora</TableHead>
              <TableHead>Produto</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead className="text-right">Quantidade</TableHead>
              <TableHead>Motivo</TableHead>
              <TableHead>Origem</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              Array.from({ length: 4 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell colSpan={6}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))
            ) : movimentacoes.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6}>
                  <div className="vazio">
                    <h3>Nenhuma movimentação encontrada</h3>
                    <p>Ajuste os filtros ou cadastre um novo registro.</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              movimentacoes.map((movimentacao) => (
                <TableRow key={movimentacao.id}>
                  <TableCell>{new Date(movimentacao.dataHora).toLocaleString("pt-BR")}</TableCell>
                  <TableCell className="tabela__forte">
                    {movimentacao.produtoCodigoInterno} — {movimentacao.produtoNome}
                  </TableCell>
                  <TableCell>
                    <TipoMovimentacaoBadge tipo={movimentacao.tipo} />
                  </TableCell>
                  <TableCell className="text-right">
                    {quantidadeComSinal(movimentacao.tipo, movimentacao.quantidade)}
                  </TableCell>
                  <TableCell>
                    {movimentacao.motivo}
                    {movimentacao.observacao && (
                      <p className="text-xs text-muted-foreground">{movimentacao.observacao}</p>
                    )}
                  </TableCell>
                  <TableCell>{ROTULO_ORIGEM[movimentacao.origem]}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </Card>

      <MovimentacaoEstoqueDialog
        open={dialogAberto}
        onOpenChange={setDialogAberto}
        produtoIdInicial={produtoId === "TODOS" ? undefined : produtoId}
        onSalvo={carregar}
      />
    </>
  );
}
