import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ExternalLink } from "lucide-react";
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
import { OrcamentoStatusBadge } from "@/components/orcamentos/OrcamentoStatusBadge";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import { orcamentoApi } from "@/lib/orcamentoApi";
import { clienteApi } from "@/lib/clienteApi";
import { dataDoInstante, formatarData, formatarMoeda } from "@/lib/formatos";
import type { OrcamentoResponse, StatusOrcamento } from "@/types/orcamento";
import type { ClienteResponse } from "@/types/cliente";

function nomeExibicaoCliente(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF" ? (cliente.nomeCompleto ?? "") : (cliente.razaoSocial ?? "");
}

export function OrcamentosListPage() {
  const navigate = useNavigate();
  const [orcamentos, setOrcamentos] = useState<OrcamentoResponse[]>([]);
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [clienteId, setClienteId] = useState<string>("TODOS");
  const [status, setStatus] = useState<StatusOrcamento | "TODOS">("TODOS");
  const [emitidoDe, setEmitidoDe] = useState("");
  const [emitidoAte, setEmitidoAte] = useState("");

  useEffect(() => {
    clienteApi.listar().then(setClientes).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    orcamentoApi
      .listar({
        clienteId: clienteId === "TODOS" ? undefined : clienteId,
        status: status === "TODOS" ? undefined : status,
      })
      .then(setOrcamentos)
      .catch(() => toast.error("Não foi possível carregar os orçamentos"))
      .finally(() => setCarregando(false));
  }, [clienteId, status]);

  useEffect(carregar, [carregar]);

  const periodoInvalido = emitidoDe !== "" && emitidoAte !== "" && emitidoAte < emitidoDe;

  // A data de emissão é a de criação do orçamento; o filtro é inclusivo nas duas pontas.
  const visiveis = useMemo(
    () =>
      periodoInvalido
        ? []
        : orcamentos.filter((orcamento) => {
            const emitido = dataDoInstante(orcamento.criadoEm);
            return (!emitidoDe || emitido >= emitidoDe) && (!emitidoAte || emitido <= emitidoAte);
          }),
    [orcamentos, emitidoDe, emitidoAte, periodoInvalido],
  );

  const temFiltro = clienteId !== "TODOS" || status !== "TODOS" || emitidoDe !== "" || emitidoAte !== "";

  function limparFiltros() {
    setClienteId("TODOS");
    setStatus("TODOS");
    setEmitidoDe("");
    setEmitidoAte("");
  }

  return (
    <>
      <CabecalhoPagina
        titulo="Orçamentos"
        acao={
          <Button asChild>
            <Link to="/orcamentos/novo">Novo orçamento</Link>
          </Button>
        }
      />

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-cliente">Cliente</label>
            <Select value={clienteId} onValueChange={setClienteId}>
              <SelectTrigger id="filtro-cliente" className="w-full">
                <SelectValue placeholder="Cliente" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos os clientes</SelectItem>
                {clientes.map((cliente) => (
                  <SelectItem key={cliente.id} value={cliente.id}>
                    {nomeExibicaoCliente(cliente)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusOrcamento | "TODOS")}>
              <SelectTrigger id="filtro-status" className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos os status</SelectItem>
                <SelectItem value="RASCUNHO">Rascunho</SelectItem>
                <SelectItem value="ENVIADO">Enviado</SelectItem>
                <SelectItem value="APROVADO">Aprovado</SelectItem>
                <SelectItem value="RECUSADO">Recusado</SelectItem>
                <SelectItem value="EXPIRADO">Expirado</SelectItem>
                <SelectItem value="CANCELADO">Cancelado</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className={periodoInvalido ? "campo campo--erro" : "campo"}>
            <label htmlFor="filtro-emitido-de">Emitido de</label>
            <input
              id="filtro-emitido-de"
              type="date"
              value={emitidoDe}
              max={emitidoAte || undefined}
              aria-invalid={periodoInvalido}
              onChange={(e) => setEmitidoDe(e.target.value)}
            />
          </div>
          <div className={periodoInvalido ? "campo campo--erro" : "campo"}>
            <label htmlFor="filtro-emitido-ate">Emitido até</label>
            <input
              id="filtro-emitido-ate"
              type="date"
              value={emitidoAte}
              min={emitidoDe || undefined}
              aria-invalid={periodoInvalido}
              onChange={(e) => setEmitidoAte(e.target.value)}
            />
            {periodoInvalido && <small>A data final precisa ser igual ou depois da inicial.</small>}
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
              <TableHead>Cliente</TableHead>
              <TableHead>Emitido em</TableHead>
              <TableHead>Válido até</TableHead>
              <TableHead className="text-right">Total</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              Array.from({ length: 4 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell colSpan={6}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))
            ) : visiveis.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6}>
                  <div className="vazio">
                    <h3>{temFiltro ? "Nenhum orçamento com esses filtros" : "Nenhum orçamento ainda"}</h3>
                    <p>{temFiltro ? "Ajuste ou limpe os filtros para ver mais." : "Seu primeiro orçamento aparece aqui."}</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              visiveis.map((orcamento) => (
                <TableRow
                  key={orcamento.id}
                  onClick={() => navigate(`/orcamentos/${orcamento.id}/editar`)}
                >
                  <TableCell className="tabela__forte">{orcamento.clienteNome}</TableCell>
                  <TableCell>{formatarData(dataDoInstante(orcamento.criadoEm))}</TableCell>
                  <TableCell>{formatarData(orcamento.prazoValidade)}</TableCell>
                  <TableCell className="text-right">{formatarMoeda(orcamento.valorTotal)}</TableCell>
                  <TableCell>
                    <OrcamentoStatusBadge status={orcamento.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes
                      itens={[{ rotulo: "Abrir", icone: ExternalLink, para: `/orcamentos/${orcamento.id}/editar` }]}
                    />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </Card>
    </>
  );
}
