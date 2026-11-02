import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
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
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Contas a pagar</h1>
        <Button asChild>
          <Link to="/contas-pagar/novo">Nova conta</Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">A pagar (saldo pendente)</p>
            <p className="text-lg font-medium">{formatarMoeda(aPagar)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Vencido</p>
            <p className="text-lg font-medium text-destructive">{formatarMoeda(vencido)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Já pago</p>
            <p className="text-lg font-medium text-primary">{formatarMoeda(pago)}</p>
          </CardContent>
        </Card>
      </div>

      <div className="flex flex-wrap items-end gap-3">
        <Select value={categoriaId} onValueChange={setCategoriaId}>
          <SelectTrigger className="w-56">
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
        <Select value={fornecedorId} onValueChange={setFornecedorId}>
          <SelectTrigger className="w-56">
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
        <Select value={situacao} onValueChange={(v) => setSituacao(v as SituacaoContaPagar | "TODAS")}>
          <SelectTrigger className="w-44">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODAS">Todas as situações</SelectItem>
            <SelectItem value="EM_ABERTO">Em aberto</SelectItem>
            <SelectItem value="VENCIDAS">Vencidas</SelectItem>
            <SelectItem value="PAGAS">Pagas</SelectItem>
          </SelectContent>
        </Select>
        <Select value={status} onValueChange={(v) => setStatus(v as StatusContaPagar | "TODOS")}>
          <SelectTrigger className="w-56">
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
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground">Vencimento de</label>
          <Input type="date" className="w-40" value={vencimentoInicio} onChange={(e) => setVencimentoInicio(e.target.value)} />
        </div>
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground">até</label>
          <Input type="date" className="w-40" value={vencimentoFim} onChange={(e) => setVencimentoFim(e.target.value)} />
        </div>
      </div>

      <div className="rounded-lg border border-border">
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
                <TableCell colSpan={8} className="py-8 text-center text-muted-foreground">
                  Nenhuma conta a pagar encontrada.
                </TableCell>
              </TableRow>
            ) : (
              contas.map((conta) => (
                <TableRow key={conta.id}>
                  <TableCell className="font-medium">{conta.descricao}</TableCell>
                  <TableCell>{conta.fornecedorNome ?? "—"}</TableCell>
                  <TableCell>{conta.categoriaDespesaNome}</TableCell>
                  <TableCell>{formatarData(proximoVencimento(conta))}</TableCell>
                  <TableCell className="text-right">{formatarMoeda(conta.valorTotal)}</TableCell>
                  <TableCell className="text-right">{formatarMoeda(conta.saldoPendente)}</TableCell>
                  <TableCell>
                    <StatusContaPagarBadge status={conta.status} vencida={conta.vencida} />
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" asChild>
                      <Link to={`/contas-pagar/${conta.id}`}>Abrir</Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
