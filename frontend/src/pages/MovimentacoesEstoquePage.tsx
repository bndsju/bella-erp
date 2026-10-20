import { useCallback, useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
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
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Histórico de movimentações</h1>
        <div className="flex gap-2">
          <Button variant="outline" asChild>
            <Link to="/estoque">Voltar ao estoque</Link>
          </Button>
          <Button onClick={() => setDialogAberto(true)}>Registrar movimentação</Button>
        </div>
      </div>

      <div className="flex flex-wrap items-end gap-3">
        <Select value={produtoId} onValueChange={setProdutoId}>
          <SelectTrigger className="w-64">
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
        <Select value={tipo} onValueChange={(v) => setTipo(v as TipoMovimentacaoEstoque | "TODOS")}>
          <SelectTrigger className="w-52">
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
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground">De</label>
          <Input type="date" className="w-40" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
        </div>
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground">Até</label>
          <Input type="date" className="w-40" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
        </div>
      </div>

      <div className="rounded-lg border border-border">
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
                <TableCell colSpan={6} className="py-8 text-center text-muted-foreground">
                  Nenhuma movimentação encontrada.
                </TableCell>
              </TableRow>
            ) : (
              movimentacoes.map((movimentacao) => (
                <TableRow key={movimentacao.id}>
                  <TableCell>{new Date(movimentacao.dataHora).toLocaleString("pt-BR")}</TableCell>
                  <TableCell className="font-medium">
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
      </div>

      <MovimentacaoEstoqueDialog
        open={dialogAberto}
        onOpenChange={setDialogAberto}
        produtoIdInicial={produtoId === "TODOS" ? undefined : produtoId}
        onSalvo={carregar}
      />
    </div>
  );
}
