import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardTitle } from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
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
import { OrcamentoItensField } from "@/components/orcamentos/OrcamentoItensField";
import { OrcamentoResumoValores } from "@/components/orcamentos/OrcamentoResumoValores";
import { OrcamentoStatusBadge } from "@/components/orcamentos/OrcamentoStatusBadge";
import { orcamentoApi } from "@/lib/orcamentoApi";
import { clienteApi } from "@/lib/clienteApi";
import { produtoApi } from "@/lib/produtoApi";
import { ApiError } from "@/lib/api";
import { orcamentoSchema, type OrcamentoFormValues } from "@/lib/orcamentoSchema";
import type { ClienteResponse } from "@/types/cliente";
import type { ProdutoResponse } from "@/types/produto";
import type { OrcamentoResponse } from "@/types/orcamento";
import { HistoricoAlteracoes } from "@/components/auditoria/HistoricoAlteracoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function nomeExibicaoCliente(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF" ? (cliente.nomeCompleto ?? "") : (cliente.razaoSocial ?? "");
}

export function OrcamentoFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const modo = id ? "editar" : "novo";

  const [carregando, setCarregando] = useState(modo === "editar");
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [produtos, setProdutos] = useState<ProdutoResponse[]>([]);
  const [orcamento, setOrcamento] = useState<OrcamentoResponse | null>(null);
  const [processandoAcao, setProcessandoAcao] = useState(false);

  const form = useForm<OrcamentoFormValues>({
    resolver: zodResolver(orcamentoSchema),
    defaultValues: {
      clienteId: "",
      itens: [],
      percentualDesconto: 0,
      valorFrete: 0,
      prazoValidade: "",
      condicaoPagamento: "",
      observacoes: "",
    },
  });

  useEffect(() => {
    clienteApi.listar({ status: "ATIVO" }).then(setClientes).catch(() => {});
    produtoApi.listar({ status: "ATIVO" }).then(setProdutos).catch(() => {});
  }, []);

  useEffect(() => {
    if (!id) return;

    orcamentoApi
      .buscarPorId(id)
      .then((dados) => {
        setOrcamento(dados);
        form.reset({
          clienteId: dados.clienteId,
          itens: dados.itens.map((item) => ({
            produtoId: item.produtoId,
            quantidade: item.quantidade,
            valorUnitario: item.valorUnitario,
          })),
          percentualDesconto: dados.percentualDesconto,
          valorFrete: dados.valorFrete,
          prazoValidade: dados.prazoValidade,
          condicaoPagamento: dados.condicaoPagamento,
          observacoes: dados.observacoes ?? "",
        });
      })
      .catch(() => {
        toast.error("Não foi possível carregar o orçamento");
        navigate("/orcamentos");
      })
      .finally(() => setCarregando(false));
  }, [id, navigate, form]);

  const itensForm = form.watch("itens") ?? [];
  const percentualDesconto = form.watch("percentualDesconto") ?? 0;
  const valorFreteForm = form.watch("valorFrete") ?? 0;
  const subtotal = itensForm.reduce(
    (total, item) => total + (item.quantidade || 0) * (item.valorUnitario || 0),
    0,
  );
  const valorDesconto = subtotal * ((percentualDesconto || 0) / 100);
  const valorTotal = subtotal - valorDesconto + (valorFreteForm || 0);

  async function onSubmit(dados: OrcamentoFormValues) {
    try {
      if (id) {
        const atualizado = await orcamentoApi.editar(id, dados);
        setOrcamento(atualizado);
        toast.success("Orçamento atualizado");
      } else {
        const criado = await orcamentoApi.cadastrar(dados);
        toast.success("Orçamento cadastrado");
        navigate(`/orcamentos/${criado.id}/editar`);
      }
    } catch (erro) {
      if (erro instanceof ApiError) {
        toast.error(erro.message, { description: erro.erros?.join(", ") });
      } else {
        toast.error("Erro inesperado ao salvar o orçamento");
      }
    }
  }

  async function executarAcao(acao: () => Promise<OrcamentoResponse>, mensagemSucesso: string) {
    setProcessandoAcao(true);
    try {
      const atualizado = await acao();
      setOrcamento(atualizado);
      toast.success(mensagemSucesso);
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Não foi possível concluir a ação");
    } finally {
      setProcessandoAcao(false);
    }
  }

  if (carregando) {
    return (
      <>
        <CabecalhoPagina titulo="Orçamento" />
        <Card>
          <div className="space-y-4">
            <Skeleton className="h-11 w-full" />
            <Skeleton className="h-11 w-full" />
            <Skeleton className="h-11 w-full" />
          </div>
        </Card>
      </>
    );
  }

  const editavel = modo === "novo" || orcamento?.status === "RASCUNHO";

  return (
    <>
      <CabecalhoPagina
        titulo={modo === "editar" ? "Orçamento" : "Novo orçamento"}
        acao={orcamento && <OrcamentoStatusBadge status={orcamento.status} />}
      />

      {editavel ? (
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="contents">
            <Card>
              <CardTitle>Cliente</CardTitle>
              <FormField
                control={form.control}
                name="clienteId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Cliente</FormLabel>
                    <Select value={field.value} onValueChange={field.onChange}>
                      <FormControl>
                        <SelectTrigger className="w-full">
                          <SelectValue placeholder="Selecione o cliente" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {clientes.map((cliente) => (
                          <SelectItem key={cliente.id} value={cliente.id}>
                            {nomeExibicaoCliente(cliente)}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </Card>

            <Card>
              <CardTitle>Itens</CardTitle>
              <OrcamentoItensField produtos={produtos} />
            </Card>

            <Card>
              <CardTitle>Condições</CardTitle>
              <div className="grade grade--4">
                <FormField
                  control={form.control}
                  name="percentualDesconto"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Desconto (%)</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="0.01"
                          min="0"
                          max="100"
                          {...field}
                          onChange={(e) => field.onChange(e.target.valueAsNumber)}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="valorFrete"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Frete</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="0.01"
                          min="0"
                          {...field}
                          onChange={(e) => field.onChange(e.target.valueAsNumber)}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="prazoValidade"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Válido até</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="condicaoPagamento"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Condição de pagamento</FormLabel>
                      <FormControl>
                        <Input placeholder="À vista, 30/60/90 dias..." {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
            </Card>

            <Card>
              <FormField
                control={form.control}
                name="observacoes"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Observações</FormLabel>
                    <FormControl>
                      <Textarea rows={3} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </Card>

            <OrcamentoResumoValores
              subtotal={subtotal}
              valorDesconto={valorDesconto}
              valorFrete={valorFreteForm || 0}
              valorTotal={valorTotal}
            />

            <div className="acoes">
              {modo === "editar" && orcamento && (
                <Button
                  type="button"
                  variant="secondary"
                  disabled={processandoAcao}
                  onClick={() => executarAcao(() => orcamentoApi.cancelar(id!), "Orçamento cancelado")}
                >
                  Cancelar orçamento
                </Button>
              )}
              <Button type="submit" variant={modo === "editar" ? "secondary" : "default"} disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting ? "Salvando..." : "Salvar"}
              </Button>
              {modo === "editar" && orcamento && (
                <Button
                  type="button"
                  disabled={processandoAcao}
                  onClick={() => executarAcao(() => orcamentoApi.enviar(id!), "Orçamento enviado")}
                >
                  Enviar orçamento
                </Button>
              )}
            </div>
          </form>
        </Form>
      ) : (
        orcamento && (
          <>
            <Card>
              <CardTitle>{orcamento.clienteNome}</CardTitle>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Produto</TableHead>
                    <TableHead>Quantidade</TableHead>
                    <TableHead>Valor unitário</TableHead>
                    <TableHead className="text-right">Subtotal</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {orcamento.itens.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className="tabela__forte">
                        {item.produtoCodigoInterno} — {item.produtoNome}
                      </TableCell>
                      <TableCell>{item.quantidade}</TableCell>
                      <TableCell>{formatoMoeda.format(item.valorUnitario)}</TableCell>
                      <TableCell className="text-right">{formatoMoeda.format(item.subtotal)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Card>

            <OrcamentoResumoValores
              subtotal={orcamento.subtotal}
              valorDesconto={orcamento.valorDesconto}
              valorFrete={orcamento.valorFrete}
              valorTotal={orcamento.valorTotal}
            />

            <Card>
              <CardTitle>Condições</CardTitle>
              <div className="linha">
                <span>Válido até</span>
                <span>{new Date(orcamento.prazoValidade).toLocaleDateString("pt-BR")}</span>
              </div>
              <div className="linha">
                <span>Condição de pagamento</span>
                <span>{orcamento.condicaoPagamento}</span>
              </div>
              {orcamento.observacoes && (
                <div className="linha">
                  <span>Observações</span>
                  <span>{orcamento.observacoes}</span>
                </div>
              )}
            </Card>

            {orcamento.status === "ENVIADO" && (
              <div className="acoes">
                <Button
                  type="button"
                  variant="secondary"
                  disabled={processandoAcao}
                  onClick={() => executarAcao(() => orcamentoApi.cancelar(id!), "Orçamento cancelado")}
                >
                  Cancelar
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  disabled={processandoAcao}
                  onClick={() => executarAcao(() => orcamentoApi.recusar(id!), "Orçamento recusado")}
                >
                  Recusar
                </Button>
                <Button
                  type="button"
                  disabled={processandoAcao}
                  onClick={() => executarAcao(() => orcamentoApi.aprovar(id!), "Orçamento aprovado")}
                >
                  Aprovar
                </Button>
              </div>
            )}
          </>
        )
      )}

      {modo === "editar" && id && (
        <HistoricoAlteracoes modulo="ORCAMENTO" entidadeId={id} atualizarCom={orcamento?.status} />
      )}
    </>
  );
}
