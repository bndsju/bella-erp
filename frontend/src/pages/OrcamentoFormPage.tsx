import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
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
      <Card>
        <CardContent className="space-y-4 pt-6">
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
        </CardContent>
      </Card>
    );
  }

  const editavel = modo === "novo" || orcamento?.status === "RASCUNHO";

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">{modo === "editar" ? "Orçamento" : "Novo orçamento"}</h1>
        {orcamento && <OrcamentoStatusBadge status={orcamento.status} />}
      </div>

      {editavel ? (
        <Card>
          <CardContent className="pt-6">
            <Form {...form}>
              <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                <div className="grid grid-cols-2 gap-4">
                  <FormField
                    control={form.control}
                    name="clienteId"
                    render={({ field }) => (
                      <FormItem className="col-span-2">
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
                </div>

                <Separator />

                <OrcamentoItensField produtos={produtos} />

                <Separator />

                <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
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

                <OrcamentoResumoValores
                  subtotal={subtotal}
                  valorDesconto={valorDesconto}
                  valorFrete={valorFreteForm || 0}
                  valorTotal={valorTotal}
                />

                <div className="flex justify-end gap-2 pt-2">
                  {modo === "editar" && orcamento && (
                    <Button
                      type="button"
                      variant="outline"
                      disabled={processandoAcao}
                      onClick={() => executarAcao(() => orcamentoApi.cancelar(id!), "Orçamento cancelado")}
                    >
                      Cancelar orçamento
                    </Button>
                  )}
                  <Button type="submit" disabled={form.formState.isSubmitting}>
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
          </CardContent>
        </Card>
      ) : (
        orcamento && (
          <Card>
            <CardHeader>
              <CardTitle>{orcamento.clienteNome}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="rounded-lg border border-border">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-border text-left text-muted-foreground">
                      <th className="p-3 font-normal">Produto</th>
                      <th className="p-3 font-normal">Quantidade</th>
                      <th className="p-3 font-normal">Valor unitário</th>
                      <th className="p-3 text-right font-normal">Subtotal</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orcamento.itens.map((item) => (
                      <tr key={item.id} className="border-b border-border last:border-0">
                        <td className="p-3">
                          {item.produtoCodigoInterno} — {item.produtoNome}
                        </td>
                        <td className="p-3">{item.quantidade}</td>
                        <td className="p-3">{formatoMoeda.format(item.valorUnitario)}</td>
                        <td className="p-3 text-right">{formatoMoeda.format(item.subtotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <OrcamentoResumoValores
                subtotal={orcamento.subtotal}
                valorDesconto={orcamento.valorDesconto}
                valorFrete={orcamento.valorFrete}
                valorTotal={orcamento.valorTotal}
              />

              <div className="grid grid-cols-2 gap-4 text-sm sm:grid-cols-3">
                <div>
                  <p className="text-muted-foreground">Válido até</p>
                  <p className="font-medium">{new Date(orcamento.prazoValidade).toLocaleDateString("pt-BR")}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Condição de pagamento</p>
                  <p className="font-medium">{orcamento.condicaoPagamento}</p>
                </div>
                {orcamento.observacoes && (
                  <div className="col-span-2 sm:col-span-3">
                    <p className="text-muted-foreground">Observações</p>
                    <p className="font-medium">{orcamento.observacoes}</p>
                  </div>
                )}
              </div>

              {orcamento.status === "ENVIADO" && (
                <div className="flex justify-end gap-2 pt-2">
                  <Button
                    type="button"
                    variant="outline"
                    disabled={processandoAcao}
                    onClick={() => executarAcao(() => orcamentoApi.cancelar(id!), "Orçamento cancelado")}
                  >
                    Cancelar
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
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
            </CardContent>
          </Card>
        )
      )}
    </div>
  );
}
