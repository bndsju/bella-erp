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
import { TransportadoraFormDialog } from "@/components/transportadoras/TransportadoraFormDialog";
import { transportadoraApi } from "@/lib/transportadoraApi";
import type { StatusTransportadora, TransportadoraResponse } from "@/types/transportadora";

export function TransportadorasPage() {
  const [transportadoras, setTransportadoras] = useState<TransportadoraResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [status, setStatus] = useState<StatusTransportadora | "TODOS">("TODOS");
  const [dialogAberto, setDialogAberto] = useState(false);
  const [transportadoraEmEdicao, setTransportadoraEmEdicao] = useState<TransportadoraResponse | null>(null);

  const carregar = useCallback(() => {
    setCarregando(true);
    transportadoraApi
      .listar(status === "TODOS" ? undefined : status)
      .then(setTransportadoras)
      .catch(() => toast.error("Não foi possível carregar as transportadoras"))
      .finally(() => setCarregando(false));
  }, [status]);

  useEffect(carregar, [carregar]);

  async function alternarStatus(transportadora: TransportadoraResponse) {
    const novoStatus = transportadora.status === "ATIVO" ? "INATIVO" : "ATIVO";
    try {
      await transportadoraApi.alterarStatus(transportadora.id, novoStatus);
      toast.success(novoStatus === "ATIVO" ? "Transportadora ativada" : "Transportadora inativada");
      carregar();
    } catch {
      toast.error("Não foi possível alterar o status da transportadora");
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Transportadoras</h1>
        <Button
          onClick={() => {
            setTransportadoraEmEdicao(null);
            setDialogAberto(true);
          }}
        >
          Nova transportadora
        </Button>
      </div>

      <Select value={status} onValueChange={(v) => setStatus(v as StatusTransportadora | "TODOS")}>
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
              <TableHead>Telefone</TableHead>
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
            ) : transportadoras.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="py-8 text-center text-muted-foreground">
                  Nenhuma transportadora encontrada.
                </TableCell>
              </TableRow>
            ) : (
              transportadoras.map((transportadora) => (
                <TableRow key={transportadora.id}>
                  <TableCell className="font-medium">{transportadora.nome}</TableCell>
                  <TableCell>{transportadora.telefone ?? "—"}</TableCell>
                  <TableCell>
                    <Badge variant={transportadora.status === "ATIVO" ? "default" : "outline"}>
                      {transportadora.status === "ATIVO" ? "Ativo" : "Inativo"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => {
                          setTransportadoraEmEdicao(transportadora);
                          setDialogAberto(true);
                        }}
                      >
                        Editar
                      </Button>
                      <Button variant="ghost" size="sm" onClick={() => alternarStatus(transportadora)}>
                        {transportadora.status === "ATIVO" ? "Inativar" : "Ativar"}
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <TransportadoraFormDialog
        open={dialogAberto}
        onOpenChange={setDialogAberto}
        transportadora={transportadoraEmEdicao}
        onSalvo={carregar}
      />
    </div>
  );
}
