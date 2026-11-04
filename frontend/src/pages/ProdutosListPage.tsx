import { useCallback, useEffect, useMemo, useState } from "react";
import { SelecaoPesquisavel } from "@/components/ui/selecao-pesquisavel";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
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
import { CircleCheck, Pencil, Power } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

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

  const opcoesCategorias = useMemo(
    () => categorias.map((categoria) => ({ valor: categoria.id, rotulo: categoria.nome })),
    [categorias],
  );

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

  const temFiltro = busca !== "" || status !== "TODOS" || categoriaId !== "TODAS";
  const limpar = () => {
    setBusca("");
    setStatus("TODOS");
    setCategoriaId("TODAS");
  };

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
    <>
      <CabecalhoPagina
        titulo="Produtos"
        acao={
          <>
          <Button asChild>
            <Link to="/produtos/novo">Novo produto</Link>
          </Button>
          </>
        }
      />

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-busca">Buscar</label>
            <input
              id="filtro-busca"
              placeholder="Nome ou código interno"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
            />
          </div>
          <div className="campo">
            <label htmlFor="filtro-categoria">Categoria</label>
            <SelecaoPesquisavel
              id="filtro-categoria"
              value={categoriaId}
              onChange={setCategoriaId}
              opcoes={opcoesCategorias}
              valorTodos="TODAS"
              rotuloTodos="Todas as categorias"
              placeholderBusca="Buscar categoria"
              textoVazio="Nenhuma categoria encontrada"
            />
          </div>
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusProduto | "TODOS")}>
              <SelectTrigger id="filtro-status" className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos os status</SelectItem>
                <SelectItem value="ATIVO">Ativo</SelectItem>
                <SelectItem value="INATIVO">Inativo</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <Button variant="secondary" disabled={!temFiltro} onClick={limpar}>
            Limpar filtros
          </Button>
        </div>
      </Card>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Nome</TableHead>
              <TableHead>Categoria</TableHead>
              <TableHead>Unidade</TableHead>
              <TableHead className="text-right">Preço de venda</TableHead>
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
                <TableCell colSpan={7}>
                  <div className="vazio">
                    <h3>{temFiltro ? "Nenhum produto com esses filtros" : "Nenhum produto ainda"}</h3>
                    <p>{temFiltro ? "Ajuste ou limpe os filtros para ver mais." : "Seu primeiro produto aparece aqui."}</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              produtos.map((produto) => (
                <TableRow key={produto.id}>
                  <TableCell>{produto.codigoInterno}</TableCell>
                  <TableCell className="tabela__forte">{produto.nome}</TableCell>
                  <TableCell>{produto.categoria.nome}</TableCell>
                  <TableCell>{produto.unidadeMedida.sigla}</TableCell>
                  <TableCell className="text-right">{formatoMoeda.format(produto.precoVenda)}</TableCell>
                  <TableCell>
                    <Badge variant={produto.status === "ATIVO" ? "default" : "outline"}>
                      {produto.status === "ATIVO" ? "Ativo" : "Inativo"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes
                      itens={[
                        { rotulo: "Editar", icone: Pencil, para: `/produtos/${produto.id}/editar` },
                        {
                          rotulo: produto.status === "ATIVO" ? "Inativar" : "Ativar",
                          icone: produto.status === "ATIVO" ? Power : CircleCheck,
                          perigo: produto.status === "ATIVO",
                          aoClicar: () => setProdutoParaAlterarStatus(produto),
                        },
                      ]}
                    />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </Card>

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
            <Button variant="secondary" onClick={() => setProdutoParaAlterarStatus(null)}>
              Cancelar
            </Button>
            <Button onClick={confirmarAlterarStatus} disabled={alterandoStatus}>
              Confirmar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
