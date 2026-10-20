import { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
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
import { CategoriaDespesaFormDialog } from "@/components/categoriasdespesa/CategoriaDespesaFormDialog";
import { categoriaDespesaApi } from "@/lib/categoriaDespesaApi";
import type { CategoriaDespesaResponse, StatusCategoriaDespesa } from "@/types/categoriaDespesa";

export function CategoriasDespesaPage() {
  const [categorias, setCategorias] = useState<CategoriaDespesaResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [status, setStatus] = useState<StatusCategoriaDespesa | "TODOS">("TODOS");
  const [dialogAberto, setDialogAberto] = useState(false);
  const [categoriaEmEdicao, setCategoriaEmEdicao] = useState<CategoriaDespesaResponse | null>(null);

  const carregar = useCallback(() => {
    setCarregando(true);
    categoriaDespesaApi
      .listar(status === "TODOS" ? undefined : status)
      .then(setCategorias)
      .catch(() => toast.error("Não foi possível carregar as categorias de despesa"))
      .finally(() => setCarregando(false));
  }, [status]);

  useEffect(carregar, [carregar]);

  async function alternarStatus(categoria: CategoriaDespesaResponse) {
    const novoStatus = categoria.status === "ATIVO" ? "INATIVO" : "ATIVO";
    try {
      await categoriaDespesaApi.alterarStatus(categoria.id, novoStatus);
      toast.success(novoStatus === "ATIVO" ? "Categoria de despesa ativada" : "Categoria de despesa inativada");
      carregar();
    } catch {
      toast.error("Não foi possível alterar o status da categoria de despesa");
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Categorias de despesa</h1>
        <Button
          onClick={() => {
            setCategoriaEmEdicao(null);
            setDialogAberto(true);
          }}
        >
          Nova categoria de despesa
        </Button>
      </div>

      <Select value={status} onValueChange={(v) => setStatus(v as StatusCategoriaDespesa | "TODOS")}>
        <SelectTrigger className="w-40">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="TODOS">Todos os status</SelectItem>
          <SelectItem value="ATIVO">Ativo</SelectItem>
          <SelectItem value="INATIVO">Inativo</SelectItem>
        </SelectContent>
      </Select>

      <div className="rounded-lg border border-border">
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
                <TableCell colSpan={3} className="py-8 text-center text-muted-foreground">
                  Nenhuma categoria de despesa encontrada.
                </TableCell>
              </TableRow>
            ) : (
              categorias.map((categoria) => (
                <TableRow key={categoria.id}>
                  <TableCell className="font-medium">{categoria.nome}</TableCell>
                  <TableCell>
                    <Badge variant={categoria.status === "ATIVO" ? "default" : "outline"}>
                      {categoria.status === "ATIVO" ? "Ativo" : "Inativo"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => {
                          setCategoriaEmEdicao(categoria);
                          setDialogAberto(true);
                        }}
                      >
                        Editar
                      </Button>
                      <Button variant="ghost" size="sm" onClick={() => alternarStatus(categoria)}>
                        {categoria.status === "ATIVO" ? "Inativar" : "Ativar"}
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <CategoriaDespesaFormDialog
        open={dialogAberto}
        onOpenChange={setDialogAberto}
        categoria={categoriaEmEdicao}
        onSalvo={carregar}
      />
    </div>
  );
}
