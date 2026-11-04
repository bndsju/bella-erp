import { useCallback, useEffect, useState } from "react";
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
import { CategoriaFluxoCaixaFormDialog } from "@/components/categoriasfluxocaixa/CategoriaFluxoCaixaFormDialog";
import { categoriaFluxoCaixaApi } from "@/lib/categoriaFluxoCaixaApi";
import type { CategoriaFluxoCaixaResponse, StatusCategoriaFluxoCaixa } from "@/types/categoriaFluxoCaixa";
import { CircleCheck, Pencil, Power } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

export function CategoriasFluxoCaixaPage() {
  const [categorias, setCategorias] = useState<CategoriaFluxoCaixaResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [status, setStatus] = useState<StatusCategoriaFluxoCaixa | "TODOS">("TODOS");
  const [dialogAberto, setDialogAberto] = useState(false);
  const [categoriaEmEdicao, setCategoriaEmEdicao] = useState<CategoriaFluxoCaixaResponse | null>(null);

  const carregar = useCallback(() => {
    setCarregando(true);
    categoriaFluxoCaixaApi
      .listar(status === "TODOS" ? undefined : status)
      .then(setCategorias)
      .catch(() => toast.error("Não foi possível carregar as categorias de fluxo de caixa"))
      .finally(() => setCarregando(false));
  }, [status]);

  useEffect(carregar, [carregar]);

  async function alternarStatus(categoria: CategoriaFluxoCaixaResponse) {
    const novoStatus = categoria.status === "ATIVO" ? "INATIVO" : "ATIVO";
    try {
      await categoriaFluxoCaixaApi.alterarStatus(categoria.id, novoStatus);
      toast.success(novoStatus === "ATIVO" ? "Categoria de fluxo de caixa ativada" : "Categoria de fluxo de caixa inativada");
      carregar();
    } catch {
      toast.error("Não foi possível alterar o status da categoria de fluxo de caixa");
    }
  }

  return (
    <>
      <CabecalhoPagina
        titulo="Categorias de fluxo de caixa"
        acao={
          <>
          <Button
            onClick={() => {
              setCategoriaEmEdicao(null);
              setDialogAberto(true);
            }}
          >
            Nova categoria de fluxo de caixa
          </Button>
          </>
        }
      />

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusCategoriaFluxoCaixa | "TODOS")}>
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
            ) : categorias.length === 0 ? (
              <TableRow>
                <TableCell colSpan={3}>
                  <div className="vazio">
                    <h3>Nenhuma categoria de fluxo de caixa encontrada</h3>
                    <p>Ajuste os filtros ou cadastre um novo registro.</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              categorias.map((categoria) => (
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

      <CategoriaFluxoCaixaFormDialog
        open={dialogAberto}
        onOpenChange={setDialogAberto}
        categoria={categoriaEmEdicao}
        onSalvo={carregar}
      />
    </>
  );
}
