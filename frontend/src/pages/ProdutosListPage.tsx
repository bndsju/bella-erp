import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
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
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { produtoApi } from "@/lib/produtoApi";
import { categoriaApi } from "@/lib/categoriaApi";
import type { ProdutoResponse, StatusProduto } from "@/types/produto";
import type { CategoriaResponse } from "@/types/categoria";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

export function ProdutosListPage() {
  const [produtos, setProdutos] = useState<ProdutoResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [busca, setBusca] = useState("");
  const [status, setStatus] = useState<StatusProduto | "TODOS">("TODOS");
  const [categoriaId, setCategoriaId] = useState<string>("TODAS");
  const [produtoParaAlterarStatus, setProdutoParaAlterarStatus] = useState<ProdutoResponse | null>(null);
  const [alterandoStatus, setAlterandoStatus] = useState(false);

  useEffect(() => {
    categoriaApi.listar().then(setCategorias).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    produtoApi
      .listar({
        busca: busca || undefined,
        status: status === "TODOS" ? undefined : status,
        categoriaId: categoriaId === "TODAS" ? undefined : categoriaId,
      })
      .then(setProdutos)
      .catch(() => toast.error("Não foi possível carregar os produtos"))
      .finally(() => setCarregando(false));
  }, [busca, status, categoriaId]);

  useEffect(() => {
    const timeout = setTimeout(carregar, 300);
    return () => clearTimeout(timeout);
  }, [carregar]);

  async function confirmarAlterarStatus() {
    if (!produtoParaAlterarStatus) return;

    const novoStatus: StatusProduto = produtoParaAlterarStatus.status === "ATIVO" ? "INATIVO" : "ATIVO";

    setAlterandoStatus(true);
    try {
      await produtoApi.alterarStatus(produtoParaAlterarStatus.id, novoStatus);
      toast.success(novoStatus === "ATIVO" ? "Produto ativado" : "Produto inativado");
      setProdutoParaAlterarStatus(null);
      carregar();
    } catch {
      toast.error("Não foi possível alterar o status do produto");
    } finally {
      setAlterandoStatus(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Produtos</h1>
        <Button asChild>
          <Link to="/produtos/novo">Novo produto</Link>
        </Button>
      </div>

      <div className="flex gap-3">
        <Input
          placeholder="Buscar por nome ou código interno"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          className="max-w-sm"
        />
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
        <Select value={status} onValueChange={(v) => setStatus(v as StatusProduto | "TODOS")}>
          <SelectTrigger className="w-40">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos os status</SelectItem>
            <SelectItem value="ATIVO">Ativo</SelectItem>
            <SelectItem value="INATIVO">Inativo</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-lg border border-border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Nome</TableHead>
              <TableHead>Categoria</TableHead>
              <TableHead>Unidade</TableHead>
              <TableHead>Preço de venda</TableHead>
              <TableHead>Status</TableHead>
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
            ) : produtos.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-8 text-center text-muted-foreground">
                  Nenhum produto encontrado.
                </TableCell>
              </TableRow>
            ) : (
              produtos.map((produto) => (
                <TableRow key={produto.id}>
                  <TableCell className="font-mono text-sm">{produto.codigoInterno}</TableCell>
                  <TableCell className="font-medium">{produto.nome}</TableCell>
                  <TableCell>{produto.categoria.nome}</TableCell>
                  <TableCell>{produto.unidadeMedida.sigla}</TableCell>
                  <TableCell>{formatoMoeda.format(produto.precoVenda)}</TableCell>
                  <TableCell>
                    <Badge variant={produto.status === "ATIVO" ? "default" : "outline"}>
                      {produto.status === "ATIVO" ? "Ativo" : "Inativo"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="ghost" size="sm" asChild>
                        <Link to={`/produtos/${produto.id}/editar`}>Editar</Link>
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setProdutoParaAlterarStatus(produto)}
                      >
                        {produto.status === "ATIVO" ? "Inativar" : "Ativar"}
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <Dialog
        open={produtoParaAlterarStatus !== null}
        onOpenChange={(open) => !open && setProdutoParaAlterarStatus(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {produtoParaAlterarStatus?.status === "ATIVO" ? "Inativar produto" : "Ativar produto"}
            </DialogTitle>
            <DialogDescription>
              {produtoParaAlterarStatus?.status === "ATIVO"
                ? `${produtoParaAlterarStatus?.nome} deixará de aparecer em seleções de orçamentos e pedidos. Você pode reativar quando quiser.`
                : `${produtoParaAlterarStatus?.nome} voltará a aparecer em seleções de orçamentos e pedidos.`}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setProdutoParaAlterarStatus(null)}>
              Cancelar
            </Button>
            <Button onClick={confirmarAlterarStatus} disabled={alterandoStatus}>
              Confirmar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
