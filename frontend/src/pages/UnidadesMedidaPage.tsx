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
import { UnidadeMedidaFormDialog } from "@/components/unidadesmedida/UnidadeMedidaFormDialog";
import { unidadeMedidaApi } from "@/lib/unidadeMedidaApi";
import type { StatusUnidadeMedida, UnidadeMedidaResponse } from "@/types/unidadeMedida";

export function UnidadesMedidaPage() {
  const [unidades, setUnidades] = useState<UnidadeMedidaResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [status, setStatus] = useState<StatusUnidadeMedida | "TODOS">("TODOS");
  const [dialogAberto, setDialogAberto] = useState(false);
  const [unidadeEmEdicao, setUnidadeEmEdicao] = useState<UnidadeMedidaResponse | null>(null);

  const carregar = useCallback(() => {
    setCarregando(true);
    unidadeMedidaApi
      .listar(status === "TODOS" ? undefined : status)
      .then(setUnidades)
      .catch(() => toast.error("Não foi possível carregar as unidades de medida"))
      .finally(() => setCarregando(false));
  }, [status]);

  useEffect(carregar, [carregar]);

  async function alternarStatus(unidade: UnidadeMedidaResponse) {
    const novoStatus = unidade.status === "ATIVO" ? "INATIVO" : "ATIVO";
    try {
      await unidadeMedidaApi.alterarStatus(unidade.id, novoStatus);
      toast.success(novoStatus === "ATIVO" ? "Unidade ativada" : "Unidade inativada");
      carregar();
    } catch {
      toast.error("Não foi possível alterar o status da unidade de medida");
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Unidades de medida</h1>
        <Button
          onClick={() => {
            setUnidadeEmEdicao(null);
            setDialogAberto(true);
          }}
        >
          Nova unidade de medida
        </Button>
      </div>

      <Select value={status} onValueChange={(v) => setStatus(v as StatusUnidadeMedida | "TODOS")}>
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
              <TableHead>Sigla</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              <TableRow>
                <TableCell colSpan={4}>
                  <Skeleton className="h-6 w-full" />
                </TableCell>
              </TableRow>
            ) : unidades.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="py-8 text-center text-muted-foreground">
                  Nenhuma unidade de medida encontrada.
                </TableCell>
              </TableRow>
            ) : (
              unidades.map((unidade) => (
                <TableRow key={unidade.id}>
                  <TableCell className="font-medium">{unidade.nome}</TableCell>
                  <TableCell>{unidade.sigla}</TableCell>
                  <TableCell>
                    <Badge variant={unidade.status === "ATIVO" ? "default" : "outline"}>
                      {unidade.status === "ATIVO" ? "Ativo" : "Inativo"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => {
                          setUnidadeEmEdicao(unidade);
                          setDialogAberto(true);
                        }}
                      >
                        Editar
                      </Button>
                      <Button variant="ghost" size="sm" onClick={() => alternarStatus(unidade)}>
                        {unidade.status === "ATIVO" ? "Inativar" : "Ativar"}
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <UnidadeMedidaFormDialog
        open={dialogAberto}
        onOpenChange={setDialogAberto}
        unidadeMedida={unidadeEmEdicao}
        onSalvo={carregar}
      />
    </div>
  );
}
