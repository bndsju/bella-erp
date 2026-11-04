import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { CircleCheck, Pencil, Power } from "lucide-react";
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
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import { StatusBadge } from "@/components/clientes/StatusBadge";
import { clienteApi } from "@/lib/clienteApi";
import { apenasDigitos, formatarCnpj, formatarCpf } from "@/lib/documento";
import type { ClienteResponse, StatusCliente } from "@/types/cliente";

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
  const [documento, setDocumento] = useState("");
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

  // O servidor busca por nome; o documento (CPF ou CNPJ) é comparado aqui, só pelos dígitos digitados.
  const digitosDocumento = apenasDigitos(documento);
  const visiveis = useMemo(
    () =>
      digitosDocumento === ""
        ? clientes
        : clientes.filter((cliente) =>
            apenasDigitos(cliente.tipoPessoa === "PF" ? (cliente.cpf ?? "") : (cliente.cnpj ?? "")).includes(
              digitosDocumento,
            ),
          ),
    [clientes, digitosDocumento],
  );

  const temFiltro = busca !== "" || documento !== "" || status !== "TODOS";

  function limparFiltros() {
    setBusca("");
    setDocumento("");
    setStatus("TODOS");
  }

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
    <>
      <CabecalhoPagina
        titulo="Clientes"
        acao={
          <Button asChild>
            <Link to="/clientes/novo">Novo cliente</Link>
          </Button>
        }
      />

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-busca">Buscar</label>
            <input
              id="filtro-busca"
              placeholder="Nome ou razão social"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
            />
          </div>
          <div className="campo">
            <label htmlFor="filtro-documento">CPF ou CNPJ</label>
            <input
              id="filtro-documento"
              inputMode="numeric"
              placeholder="Somente números"
              value={documento}
              onChange={(e) => setDocumento(e.target.value)}
            />
          </div>
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusCliente | "TODOS")}>
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
          <Button variant="secondary" onClick={limparFiltros} disabled={!temFiltro}>
            Limpar filtros
          </Button>
        </div>
      </Card>

      <Card>
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
            ) : visiveis.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7}>
                  <div className="vazio">
                    <h3>{temFiltro ? "Nenhum cliente com esses filtros" : "Nenhum cliente ainda"}</h3>
                    <p>
                      {temFiltro
                        ? "Ajuste ou limpe os filtros para ver mais."
                        : "Seu primeiro cliente aparece aqui."}
                    </p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              visiveis.map((cliente) => (
                <TableRow key={cliente.id}>
                  <TableCell className="tabela__forte">{nomeExibicao(cliente)}</TableCell>
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
      </Card>

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
            <Button variant="secondary" onClick={() => setClienteParaAlterarStatus(null)}>
              Cancelar
            </Button>
            <Button
              variant={clienteParaAlterarStatus?.status === "ATIVO" ? "destructive" : "default"}
              onClick={confirmarAlterarStatus}
              disabled={alterandoStatus}
            >
              {clienteParaAlterarStatus?.status === "ATIVO" ? "Inativar" : "Ativar"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
