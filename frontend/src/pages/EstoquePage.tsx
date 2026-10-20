import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
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
import { estoqueApi } from "@/lib/estoqueApi";
import { formatarQuantidade } from "@/lib/estoqueFormatos";
import type { SaldoEstoqueResponse } from "@/types/estoque";

type VisaoEstoque = "TODOS" | "ABAIXO_DO_MINIMO";

export function EstoquePage() {
  const [saldos, setSaldos] = useState<SaldoEstoqueResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [visao, setVisao] = useState<VisaoEstoque>("TODOS");
  const [busca, setBusca] = useState("");
  const [dialogAberto, setDialogAberto] = useState(false);
  const [produtoSelecionado, setProdutoSelecionado] = useState<string | undefined>();

  const carregar = useCallback(() => {
    setCarregando(true);
    const requisicao =
      visao === "ABAIXO_DO_MINIMO" ? estoqueApi.listarAbaixoDoMinimo() : estoqueApi.listarSaldos();
    requisicao
      .then(setSaldos)
      .catch(() => toast.error("Não foi possível carregar o estoque"))
      .finally(() => setCarregando(false));
  }, [visao]);

  useEffect(carregar, [carregar]);

  const termo = busca.trim().toLowerCase();
  const saldosFiltrados = termo
    ? saldos.filter(
        (saldo) =>
          saldo.produtoNome.toLowerCase().includes(termo) ||
          saldo.produtoCodigoInterno.toLowerCase().includes(termo),
      )
    : saldos;

  function abrirMovimentacao(produtoId?: string) {
    setProdutoSelecionado(produtoId);
    setDialogAberto(true);
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Estoque</h1>
        <div className="flex gap-2">
          <Button variant="outline" asChild>
            <Link to="/estoque/movimentacoes">Histórico de movimentações</Link>
          </Button>
          <Button onClick={() => abrirMovimentacao()}>Registrar movimentação</Button>
        </div>
      </div>

      <div className="flex gap-3">
        <Input
          className="w-72"
          placeholder="Buscar por nome ou código"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
        />
        <Select value={visao} onValueChange={(v) => setVisao(v as VisaoEstoque)}>
          <SelectTrigger className="w-56">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos os produtos</SelectItem>
            <SelectItem value="ABAIXO_DO_MINIMO">Abaixo do estoque mínimo</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-lg border border-border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Produto</TableHead>
              <TableHead className="text-right">Físico</TableHead>
              <TableHead className="text-right">Reservado</TableHead>
              <TableHead className="text-right">Disponível</TableHead>
              <TableHead className="text-right">Mínimo</TableHead>
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
            ) : saldosFiltrados.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-8 text-center text-muted-foreground">
                  Nenhum produto encontrado.
                </TableCell>
              </TableRow>
            ) : (
              saldosFiltrados.map((saldo) => {
                const abaixoDoMinimo = saldo.quantidadeDisponivel <= saldo.estoqueMinimo;
                return (
                  <TableRow key={saldo.produtoId}>
                    <TableCell className="font-medium">
                      {saldo.produtoCodigoInterno} — {saldo.produtoNome}
                    </TableCell>
                    <TableCell className="text-right">{formatarQuantidade(saldo.estoqueFisico)}</TableCell>
                    <TableCell className="text-right">
                      {formatarQuantidade(saldo.quantidadeReservada)}
                    </TableCell>
                    <TableCell className="text-right">
                      {formatarQuantidade(saldo.quantidadeDisponivel)}
                    </TableCell>
                    <TableCell className="text-right">{formatarQuantidade(saldo.estoqueMinimo)}</TableCell>
                    <TableCell>
                      <Badge variant={abaixoDoMinimo ? "destructive" : "default"}>
                        {abaixoDoMinimo ? "Abaixo do mínimo" : "Normal"}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button variant="ghost" size="sm" onClick={() => abrirMovimentacao(saldo.produtoId)}>
                          Movimentar
                        </Button>
                        <Button variant="ghost" size="sm" asChild>
                          <Link to={`/estoque/movimentacoes?produtoId=${saldo.produtoId}`}>Histórico</Link>
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>

      <MovimentacaoEstoqueDialog
        open={dialogAberto}
        onOpenChange={setDialogAberto}
        produtoIdInicial={produtoSelecionado}
        onSalvo={carregar}
      />
    </div>
  );
}
