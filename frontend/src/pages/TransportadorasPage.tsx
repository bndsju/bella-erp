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
import { TransportadoraFormDialog } from "@/components/transportadoras/TransportadoraFormDialog";
import { transportadoraApi } from "@/lib/transportadoraApi";
import type { StatusTransportadora, TransportadoraResponse } from "@/types/transportadora";
import { CircleCheck, Pencil, Power } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

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
    <>
      <CabecalhoPagina
        titulo="Transportadoras"
        acao={
          <>
          <Button
            onClick={() => {
              setTransportadoraEmEdicao(null);
              setDialogAberto(true);
            }}
          >
            Nova transportadora
          </Button>
          </>
        }
      />

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusTransportadora | "TODOS")}>
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
                <TableCell colSpan={4}>
                  <div className="vazio">
                    <h3>Nenhuma transportadora encontrada</h3>
                    <p>Ajuste os filtros ou cadastre um novo registro.</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              transportadoras.map((transportadora) => (
                <TableRow key={transportadora.id}>
                  <TableCell className="tabela__forte">{transportadora.nome}</TableCell>
                  <TableCell>{transportadora.telefone ?? "—"}</TableCell>
                  <TableCell>
                    <Badge variant={transportadora.status === "ATIVO" ? "default" : "outline"}>
                      {transportadora.status === "ATIVO" ? "Ativo" : "Inativo"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes
                      itens={[
                        {
                          rotulo: "Editar",
                          icone: Pencil,
                          aoClicar: () => {
                            setTransportadoraEmEdicao(transportadora);
                            setDialogAberto(true);
                          },
                        },
                        {
                          rotulo: transportadora.status === "ATIVO" ? "Inativar" : "Ativar",
                          icone: transportadora.status === "ATIVO" ? Power : CircleCheck,
                          perigo: transportadora.status === "ATIVO",
                          aoClicar: () => alternarStatus(transportadora),
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

      <TransportadoraFormDialog
        open={dialogAberto}
        onOpenChange={setDialogAberto}
        transportadora={transportadoraEmEdicao}
        onSalvo={carregar}
      />
    </>
  );
}
