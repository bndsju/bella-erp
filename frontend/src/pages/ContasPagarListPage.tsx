import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
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
import { StatusContaPagarBadge } from "@/components/contaspagar/StatusContaPagarBadge";
import { fornecedorApi } from "@/lib/fornecedorApi";
import { categoriaDespesaApi } from "@/lib/categoriaDespesaApi";
import type { CategoriaDespesaResponse } from "@/types/categoriaDespesa";
import { contaPagarApi } from "@/lib/contaPagarApi";
import { formatarData, formatarMoeda } from "@/lib/formatos";
import type { FornecedorResponse } from "@/types/fornecedor";
import type { ContaPagarResponse, SituacaoContaPagar, StatusContaPagar } from "@/types/contaPagar";
import { ExternalLink } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

function nomeExibicaoFornecedor(fornecedor: FornecedorResponse): string {
  return fornecedor.nome;
}

function proximoVencimento(conta: ContaPagarResponse): string | null {
  const emAberto = conta.parcelas
    .filter((parcela) => parcela.status === "PENDENTE" || parcela.status === "PARCIALMENTE_PAGA")
    .map((parcela) => parcela.vencimento)
    .sort();
  return emAberto[0] ?? null;
}

export function ContasPagarListPage() {
  const navigate = useNavigate();
  const [contas, setContas] = useState<ContaPagarResponse[]>([]);
  const [fornecedores, setFornecedores] = useState<FornecedorResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [fornecedorId, setFornecedorId] = useState("TODOS");
  const [categorias, setCategorias] = useState<CategoriaDespesaResponse[]>([]);
  const [categoriaId, setCategoriaId] = useState("TODAS");
  const [status, setStatus] = useState<StatusContaPagar | "TODOS">("TODOS");
  const [situacao, setSituacao] = useState<SituacaoContaPagar | "TODAS">("TODAS");
  const [vencimentoInicio, setVencimentoInicio] = useState("");
  const [vencimentoFim, setVencimentoFim] = useState("");

  useEffect(() => {
    fornecedorApi.listar().then(setFornecedores).catch(() => {});
    categoriaDespesaApi.listar().then(setCategorias).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    contaPagarApi
      .listar({
        fornecedorId: fornecedorId === "TODOS" ? undefined : fornecedorId,
        categoriaDespesaId: categoriaId === "TODAS" ? undefined : categoriaId,
        status: status === "TODOS" ? undefined : status,
        situacao: situacao === "TODAS" ? undefined : situacao,
        vencimentoInicio: vencimentoInicio || undefined,
        vencimentoFim: vencimentoFim || undefined,
      })
      .then(setContas)
      .catch(() => toast.error("Não foi possível carregar as contas a pagar"))
      .finally(() => setCarregando(false));
  }, [fornecedorId, categoriaId, status, situacao, vencimentoInicio, vencimentoFim]);

  useEffect(carregar, [carregar]);

  const aPagar = contas.reduce((total, conta) => total + conta.saldoPendente, 0);
  const vencido = contas.reduce(
    (total, conta) =>
      total + conta.parcelas.filter((parcela) => parcela.vencida).reduce((soma, parcela) => soma + parcela.saldoPendente, 0),
    0,
  );
  const pago = contas.reduce((total, conta) => total + conta.valorPago, 0);

  return (
    <>
      <CabecalhoPagina
        titulo="Contas a pagar"
        acao={
          <>
          <Button asChild>
            <Link to="/contas-pagar/novo">Nova conta</Link>
          </Button>
          </>
        }
      />

      <div className="grade grade--3">
        <Card className="indicador">
          <div className="indicador__rotulo">A pagar (saldo pendente)</div>
          <div className="indicador__valor">{formatarMoeda(aPagar)}</div>
        </Card>
        <Card className="indicador">
          <div className="indicador__rotulo">Vencido</div>
          <div className="indicador__valor">{formatarMoeda(vencido)}</div>
        </Card>
        <Card className="indicador">
          <div className="indicador__rotulo">Já pago</div>
          <div className="indicador__valor">{formatarMoeda(pago)}</div>
        </Card>
      </div>

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-categoria">Categoria</label>
            <Select value={categoriaId} onValueChange={setCategoriaId}>
            <SelectTrigger id="filtro-categoria" className="w-full">
            <SelectValue placeholder="Categoria" />
            </SelectTrigger>
            <SelectContent>
            <SelectItem value="TODAS">Todas as categorias</SelectItem>
            {categorias.map((categoria) => (
            <SelectItem key={categoria.id} value={categoria.id}>
            {categoria.nome}
            </SelectItem>
            ))}
            </SelectContent>
            </Select>
          </div>
          <div className="campo">
            <label htmlFor="filtro-fornecedor">Fornecedor</label>
            <Select value={fornecedorId} onValueChange={setFornecedorId}>
            <SelectTrigger id="filtro-fornecedor" className="w-full">
            <SelectValue placeholder="Fornecedor" />
            </SelectTrigger>
            <SelectContent>
            <SelectItem value="TODOS">Todos os fornecedores</SelectItem>
            {fornecedores.map((fornecedor) => (
            <SelectItem key={fornecedor.id} value={fornecedor.id}>
            {nomeExibicaoFornecedor(fornecedor)}
            </SelectItem>
            ))}
            </SelectContent>
            </Select>
          </div>
          <div className="campo">
            <label htmlFor="filtro-situacao">Situação</label>
            <Select value={situacao} onValueChange={(v) => setSituacao(v as SituacaoContaPagar | "TODAS")}>
            <SelectTrigger id="filtro-situacao" className="w-full">
            <SelectValue />
            </SelectTrigger>
            <SelectContent>
            <SelectItem value="TODAS">Todas as situações</SelectItem>
            <SelectItem value="EM_ABERTO">Em aberto</SelectItem>
            <SelectItem value="VENCIDAS">Vencidas</SelectItem>
            <SelectItem value="PAGAS">Pagas</SelectItem>
            </SelectContent>
            </Select>
          </div>
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusContaPagar | "TODOS")}>
            <SelectTrigger id="filtro-status" className="w-full">
            <SelectValue />
            </SelectTrigger>
            <SelectContent>
            <SelectItem value="TODOS">Todos os status</SelectItem>
            <SelectItem value="PENDENTE">Pendente</SelectItem>
            <SelectItem value="PARCIALMENTE_PAGA">Parcialmente paga</SelectItem>
            <SelectItem value="PAGA">Paga</SelectItem>
            <SelectItem value="CANCELADA">Cancelada</SelectItem>
            </SelectContent>
            </Select>
          </div>
        <div className="campo">
          <label htmlFor="filtro-vencimento-de">Vencimento de</label>
          <Input id="filtro-vencimento-de" type="date" value={vencimentoInicio} onChange={(e) => setVencimentoInicio(e.target.value)} />
        </div>
        <div className="campo">
          <label htmlFor="filtro-ate">Até</label>
          <Input id="filtro-ate" type="date" value={vencimentoFim} onChange={(e) => setVencimentoFim(e.target.value)} />
        </div>
        </div>
      </Card>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Descrição</TableHead>
              <TableHead>Fornecedor</TableHead>
              <TableHead>Categoria</TableHead>
              <TableHead>Próx. vencimento</TableHead>
              <TableHead className="text-right">Total</TableHead>
              <TableHead className="text-right">Saldo</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              Array.from({ length: 4 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell colSpan={8}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))
            ) : contas.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8}>
                  <div className="vazio">
                    <h3>Nenhuma conta a pagar encontrada</h3>
                    <p>Ajuste os filtros ou cadastre um novo registro.</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              contas.map((conta) => (
                <TableRow key={conta.id} className="cursor-pointer" onClick={() => navigate(`/contas-pagar/${conta.id}`)}>
                  <TableCell className="tabela__forte">{conta.descricao}</TableCell>
                  <TableCell>{conta.fornecedorNome ?? "—"}</TableCell>
                  <TableCell>{conta.categoriaDespesaNome}</TableCell>
                  <TableCell>{formatarData(proximoVencimento(conta))}</TableCell>
                  <TableCell className="text-right">{formatarMoeda(conta.valorTotal)}</TableCell>
                  <TableCell className="text-right">{formatarMoeda(conta.saldoPendente)}</TableCell>
                  <TableCell>
                    <StatusContaPagarBadge status={conta.status} vencida={conta.vencida} />
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes itens={[{ rotulo: "Abrir", icone: ExternalLink, para: `/contas-pagar/${conta.id}` }]} />
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
