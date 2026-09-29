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
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { PedidoItensField } from "@/components/pedidos/PedidoItensField";
import { PedidoResumoValores } from "@/components/pedidos/PedidoResumoValores";
import { PedidoStatusBadge } from "@/components/pedidos/PedidoStatusBadge";
import { ConcluirVendaDialog } from "@/components/vendas/ConcluirVendaDialog";
import { pedidoApi } from "@/lib/pedidoApi";
import { clienteApi } from "@/lib/clienteApi";
import { produtoApi } from "@/lib/produtoApi";
import { transportadoraApi } from "@/lib/transportadoraApi";
import { ApiError } from "@/lib/api";
import { pedidoSchema, type PedidoFormValues } from "@/lib/pedidoSchema";
import type { ClienteResponse } from "@/types/cliente";
import type { ProdutoResponse } from "@/types/produto";
import type { PedidoResponse, StatusPedido } from "@/types/pedido";
import type { TransportadoraResponse } from "@/types/transportadora";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const SEM_TRANSPORTADORA = "SEM_TRANSPORTADORA";

function nomeExibicaoCliente(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF" ? (cliente.nomeCompleto ?? "") : (cliente.razaoSocial ?? "");
}

const PROXIMA_ACAO: Partial<Record<StatusPedido, { label: string; executar: (id: string) => Promise<PedidoResponse> }>> = {
  CRIADO: { label: "Confirmar pedido", executar: pedidoApi.confirmar },
  CONFIRMADO: { label: "Iniciar separação", executar: pedidoApi.iniciarSeparacao },
  EM_SEPARACAO: { label: "Marcar pronto para entrega", executar: pedidoApi.marcarProntoParaEntrega },
  PRONTO_PARA_ENTREGA: { label: "Marcar como entregue", executar: pedidoApi.entregar },
};

export function PedidoFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const modo = id ? "editar" : "novo";

  const [carregando, setCarregando] = useState(modo === "editar");
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [produtos, setProdutos] = useState<ProdutoResponse[]>([]);
  const [transportadoras, setTransportadoras] = useState<TransportadoraResponse[]>([]);
  const [pedido, setPedido] = useState<PedidoResponse | null>(null);
  const [processandoAcao, setProcessandoAcao] = useState(false);
  const [dialogVendaAberto, setDialogVendaAberto] = useState(false);

  const form = useForm<PedidoFormValues>({
    resolver: zodResolver(pedidoSchema),
    defaultValues: {
      clienteId: "",
      transportadoraId: SEM_TRANSPORTADORA,
      itens: [],
      percentualDesconto: 0,
      valorFrete: 0,
      condicaoPagamento: "",
    },
  });

  useEffect(() => {
    clienteApi.listar({ status: "ATIVO" }).then(setClientes).catch(() => {});
    produtoApi.listar({ status: "ATIVO" }).then(setProdutos).catch(() => {});
    transportadoraApi.listar("ATIVO").then(setTransportadoras).catch(() => {});
  }, []);

  useEffect(() => {
    if (!id) return;

    pedidoApi
      .buscarPorId(id)
      .then((dados) => {
        setPedido(dados);
        form.reset({
          clienteId: dados.clienteId,
          transportadoraId: dados.transportadoraId ?? SEM_TRANSPORTADORA,
          itens: dados.itens.map((item) => ({
            produtoId: item.produtoId,
            quantidade: item.quantidade,
            valorUnitario: item.valorUnitario,
          })),
          percentualDesconto: dados.percentualDesconto,
          valorFrete: dados.valorFrete,
          condicaoPagamento: dados.condicaoPagamento,
        });
      })
      .catch(() => {
        toast.error("Não foi possível carregar o pedido");
        navigate("/pedidos");
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

  async function onSubmit(dados: PedidoFormValues) {
    const payload = {
      ...dados,
      transportadoraId: dados.transportadoraId === SEM_TRANSPORTADORA ? undefined : dados.transportadoraId,
    };
    try {
      if (id) {
        const atualizado = await pedidoApi.editar(id, payload);
        setPedido(atualizado);
        toast.success("Pedido atualizado");
      } else {
        const criado = await pedidoApi.cadastrar(payload);
        toast.success("Pedido cadastrado");
        navigate(`/pedidos/${criado.id}/editar`);
      }
    } catch (erro) {
      if (erro instanceof ApiError) {
        toast.error(erro.message, { description: erro.erros?.join(", ") });
      } else {
        toast.error("Erro inesperado ao salvar o pedido");
      }
    }
  }

  async function executarAcao(acao: (id: string) => Promise<PedidoResponse>, mensagemSucesso: string) {
    if (!id) return;
    setProcessandoAcao(true);
    try {
      const atualizado = await acao(id);
      setPedido(atualizado);
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

  const editavel = modo === "novo" || (pedido?.status !== "ENTREGUE" && pedido?.status !== "CANCELADO");
  const proximaAcao = pedido ? PROXIMA_ACAO[pedido.status] : undefined;
  const podeCancelar = modo === "editar" && pedido && pedido.status !== "ENTREGUE" && pedido.status !== "CANCELADO";

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">{modo === "editar" ? "Pedido" : "Novo pedido"}</h1>
        {pedido && <PedidoStatusBadge status={pedido.status} />}
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
                  <FormField
                    control={form.control}
                    name="transportadoraId"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Transportadora</FormLabel>
                        <Select value={field.value} onValueChange={field.onChange}>
                          <FormControl>
                            <SelectTrigger className="w-full">
                              <SelectValue />
                            </SelectTrigger>
                          </FormControl>
                          <SelectContent>
                            <SelectItem value={SEM_TRANSPORTADORA}>Sem transportadora</SelectItem>
                            {transportadoras.map((transportadora) => (
                              <SelectItem key={transportadora.id} value={transportadora.id}>
                                {transportadora.nome}
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

                <PedidoItensField produtos={produtos} />

                <Separator />

                <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
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

                <PedidoResumoValores
                  subtotal={subtotal}
                  valorDesconto={valorDesconto}
                  valorFrete={valorFreteForm || 0}
                  valorTotal={valorTotal}
                />

                <div className="flex justify-end gap-2 pt-2">
                  {podeCancelar && (
                    <Button
                      type="button"
                      variant="outline"
                      disabled={processandoAcao}
                      onClick={() => executarAcao(pedidoApi.cancelar, "Pedido cancelado")}
                    >
                      Cancelar pedido
                    </Button>
                  )}
                  <Button type="submit" disabled={form.formState.isSubmitting}>
                    {form.formState.isSubmitting ? "Salvando..." : "Salvar"}
                  </Button>
                  {modo === "editar" && proximaAcao && (
                    <Button
                      type="button"
                      disabled={processandoAcao}
                      onClick={() => executarAcao(proximaAcao.executar, "Pedido atualizado")}
                    >
                      {proximaAcao.label}
                    </Button>
                  )}
                </div>
              </form>
            </Form>
          </CardContent>
        </Card>
      ) : (
        pedido && (
          <Card>
            <CardHeader>
              <CardTitle>{pedido.clienteNome}</CardTitle>
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
                    {pedido.itens.map((item) => (
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

              <PedidoResumoValores
                subtotal={pedido.subtotal}
                valorDesconto={pedido.valorDesconto}
                valorFrete={pedido.valorFrete}
                valorTotal={pedido.valorTotal}
              />

              <div className="grid grid-cols-2 gap-4 text-sm sm:grid-cols-3">
                <div>
                  <p className="text-muted-foreground">Transportadora</p>
                  <p className="font-medium">{pedido.transportadoraNome ?? "—"}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Condição de pagamento</p>
                  <p className="font-medium">{pedido.condicaoPagamento}</p>
                </div>
              </div>

              {pedido.status === "ENTREGUE" && (
                <div className="flex justify-end gap-2 pt-2">
                  <Button type="button" onClick={() => setDialogVendaAberto(true)}>
                    Concluir venda
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        )
      )}

      {pedido && (
        <ConcluirVendaDialog open={dialogVendaAberto} onOpenChange={setDialogVendaAberto} pedidoId={pedido.id} />
      )}
    </div>
  );
}
