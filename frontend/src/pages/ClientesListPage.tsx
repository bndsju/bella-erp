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
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { StatusBadge } from "@/components/clientes/StatusBadge";
import { clienteApi } from "@/lib/clienteApi";
import { formatarCnpj, formatarCpf } from "@/lib/documento";
import type { ClienteResponse, StatusCliente } from "@/types/cliente";
import { CircleCheck, Pencil, Power } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

function nomeExibicao(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF" ? (cliente.nomeCompleto ?? "") : (cliente.razaoSocial ?? "");
}

function documentoExibicao(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF"
    ? formatarCpf(cliente.cpf ?? "")
    : formatarCnpj(cliente.cnpj ?? "");
}

export function ClientesListPage() {
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [busca, setBusca] = useState("");
  const [status, setStatus] = useState<StatusCliente | "TODOS">("TODOS");
  const [clienteParaAlterarStatus, setClienteParaAlterarStatus] = useState<ClienteResponse | null>(null);
  const [alterandoStatus, setAlterandoStatus] = useState(false);

  const carregar = useCallback(() => {
    setCarregando(true);
    clienteApi
      .listar({ busca: busca || undefined, status: status === "TODOS" ? undefined : status })
      .then(setClientes)
      .catch(() => toast.error("Não foi possível carregar os clientes"))
      .finally(() => setCarregando(false));
  }, [busca, status]);

  useEffect(() => {
    const timeout = setTimeout(carregar, 300);
    return () => clearTimeout(timeout);
  }, [carregar]);

  async function confirmarAlterarStatus() {
    if (!clienteParaAlterarStatus) return;

    const novoStatus: StatusCliente =
      clienteParaAlterarStatus.status === "ATIVO" ? "INATIVO" : "ATIVO";

    setAlterandoStatus(true);
    try {
      await clienteApi.alterarStatus(clienteParaAlterarStatus.id, novoStatus);
      toast.success(novoStatus === "ATIVO" ? "Cliente ativado" : "Cliente inativado");
      setClienteParaAlterarStatus(null);
      carregar();
    } catch {
      toast.error("Não foi possível alterar o status do cliente");
    } finally {
      setAlterandoStatus(false);
    }
  }

  return (
    <div className="space-y-4">
      <CabecalhoPagina
        titulo="Clientes"
        acao={
          <>
          <Button asChild>
            <Link to="/clientes/novo">Novo cliente</Link>
          </Button>
          </>
        }
      />

      <div className="cartao flex flex-wrap items-end gap-3">
        <Input
          placeholder="Buscar por nome ou razão social"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          className="max-w-sm"
        />
        <Select value={status} onValueChange={(v) => setStatus(v as StatusCliente | "TODOS")}>
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

      <div className="rounded-cartao bg-card p-2">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nome / Razão social</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead>Documento</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Celular</TableHead>
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
            ) : clientes.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-8 text-center text-muted-foreground">
                  Nenhum cliente encontrado.
                </TableCell>
              </TableRow>
            ) : (
              clientes.map((cliente) => (
                <TableRow key={cliente.id}>
                  <TableCell className="font-medium">{nomeExibicao(cliente)}</TableCell>
                  <TableCell>{cliente.tipoPessoa === "PF" ? "Física" : "Jurídica"}</TableCell>
                  <TableCell>{documentoExibicao(cliente)}</TableCell>
                  <TableCell>{cliente.email}</TableCell>
                  <TableCell>{cliente.celular}</TableCell>
                  <TableCell>
                    <StatusBadge status={cliente.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes
                      itens={[
                        { rotulo: "Editar", icone: Pencil, para: `/clientes/${cliente.id}/editar` },
                        {
                          rotulo: cliente.status === "ATIVO" ? "Inativar" : "Ativar",
                          icone: cliente.status === "ATIVO" ? Power : CircleCheck,
                          perigo: cliente.status === "ATIVO",
                          aoClicar: () => setClienteParaAlterarStatus(cliente),
                        },
                      ]}
                    />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <Dialog
        open={clienteParaAlterarStatus !== null}
        onOpenChange={(open) => !open && setClienteParaAlterarStatus(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {clienteParaAlterarStatus?.status === "ATIVO" ? "Inativar cliente" : "Ativar cliente"}
            </DialogTitle>
            <DialogDescription>
              {clienteParaAlterarStatus?.status === "ATIVO"
                ? `${nomeExibicao(clienteParaAlterarStatus)} deixará de aparecer em seleções de orçamentos e pedidos. Você pode reativar quando quiser.`
                : `${clienteParaAlterarStatus ? nomeExibicao(clienteParaAlterarStatus) : ""} voltará a aparecer em seleções de orçamentos e pedidos.`}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setClienteParaAlterarStatus(null)}>
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
