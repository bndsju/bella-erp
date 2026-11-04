import { useCallback, useEffect, useMemo, useState } from "react";
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
import { CategoriaFormDialog } from "@/components/categorias/CategoriaFormDialog";
import { categoriaApi } from "@/lib/categoriaApi";
import type { CategoriaResponse, StatusCategoria } from "@/types/categoria";
import { CircleCheck, Pencil, Power } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

export function CategoriasPage() {
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [busca, setBusca] = useState("");
  const [status, setStatus] = useState<StatusCategoria | "TODOS">("TODOS");
  const [dialogAberto, setDialogAberto] = useState(false);
  const [categoriaEmEdicao, setCategoriaEmEdicao] = useState<CategoriaResponse | null>(null);

  const carregar = useCallback(() => {
    setCarregando(true);
    categoriaApi
      .listar(status === "TODOS" ? undefined : status)
      .then(setCategorias)
      .catch(() => toast.error("Não foi possível carregar as categorias"))
      .finally(() => setCarregando(false));
  }, [status]);

  useEffect(carregar, [carregar]);

  // Compara sem acento e sem diferenciar maiúsculas: "bebida" encontra "Bebidas".
  const visiveis = useMemo(() => {
    const normalizar = (t: string) => t.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().trim();
    const termo = normalizar(busca);
    return termo === "" ? categorias : categorias.filter((c) => normalizar(c.nome).includes(termo));
  }, [categorias, busca]);

  const temFiltro = busca !== "" || status !== "TODOS";

  async function alternarStatus(categoria: CategoriaResponse) {
    const novoStatus = categoria.status === "ATIVO" ? "INATIVO" : "ATIVO";
    try {
      await categoriaApi.alterarStatus(categoria.id, novoStatus);
      toast.success(novoStatus === "ATIVO" ? "Categoria ativada" : "Categoria inativada");
      carregar();
    } catch {
      toast.error("Não foi possível alterar o status da categoria");
    }
  }

  return (
    <>
      <CabecalhoPagina
        titulo="Categorias"
        acao={
          <>
          <Button
            onClick={() => {
              setCategoriaEmEdicao(null);
              setDialogAberto(true);
            }}
          >
            Nova categoria
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
              placeholder="Nome da categoria"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
            />
          </div>
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusCategoria | "TODOS")}>
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
          <Button
            variant="secondary"
            disabled={!temFiltro}
            onClick={() => {
              setBusca("");
              setStatus("TODOS");
            }}
          >
            Limpar filtros
          </Button>
        </div>
      </Card>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nome</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              <TableRow>
                <TableCell colSpan={3}>
                  <Skeleton className="h-6 w-full" />
                </TableCell>
              </TableRow>
            ) : visiveis.length === 0 ? (
              <TableRow>
                <TableCell colSpan={3}>
                  <div className="vazio">
                    <h3>{temFiltro ? "Nenhuma categoria com esses filtros" : "Nenhuma categoria ainda"}</h3>
                    <p>{temFiltro ? "Ajuste ou limpe os filtros para ver mais." : "Sua primeira categoria aparece aqui."}</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              visiveis.map((categoria) => (
                <TableRow key={categoria.id}>
                  <TableCell className="tabela__forte">{categoria.nome}</TableCell>
                  <TableCell>
                    <Badge variant={categoria.status === "ATIVO" ? "default" : "outline"}>
                      {categoria.status === "ATIVO" ? "Ativo" : "Inativo"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes
                      itens={[
                        {
                          rotulo: "Editar",
                          icone: Pencil,
                          aoClicar: () => {
                            setCategoriaEmEdicao(categoria);
                            setDialogAberto(true);
                          },
                        },
                        {
                          rotulo: categoria.status === "ATIVO" ? "Inativar" : "Ativar",
                          icone: categoria.status === "ATIVO" ? Power : CircleCheck,
                          perigo: categoria.status === "ATIVO",
                          aoClicar: () => alternarStatus(categoria),
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

      <CategoriaFormDialog
        open={dialogAberto}
        onOpenChange={setDialogAberto}
        categoria={categoriaEmEdicao}
        onSalvo={carregar}
      />
    </>
  );
}
