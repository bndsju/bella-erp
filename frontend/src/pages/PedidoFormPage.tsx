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
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

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
      <>
        <CabecalhoPagina titulo="Pedido" />
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

  const editavel = modo === "novo" || (pedido?.status !== "ENTREGUE" && pedido?.status !== "CANCELADO");
  const proximaAcao = pedido ? PROXIMA_ACAO[pedido.status] : undefined;
  const podeCancelar = modo === "editar" && pedido && pedido.status !== "ENTREGUE" && pedido.status !== "CANCELADO";

  return (
    <>
      <CabecalhoPagina
        titulo={modo === "editar" ? "Pedido" : "Novo pedido"}
        acao={pedido && <PedidoStatusBadge status={pedido.status} />}
      />

      {editavel ? (
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="contents">
            <Card>
              <CardTitle>Cliente e entrega</CardTitle>
              <div className="grade grade--2">
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
            </Card>

            <Card>
              <CardTitle>Itens</CardTitle>
              <PedidoItensField produtos={produtos} />
            </Card>

            <Card>
              <CardTitle>Condições</CardTitle>
              <div className="grade grade--3">
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
            </Card>

            <PedidoResumoValores
              subtotal={subtotal}
              valorDesconto={valorDesconto}
              valorFrete={valorFreteForm || 0}
              valorTotal={valorTotal}
            />

            <div className="acoes">
              {podeCancelar && (
                <Button
                  type="button"
                  variant="secondary"
                  disabled={processandoAcao}
                  onClick={() => executarAcao(pedidoApi.cancelar, "Pedido cancelado")}
                >
                  Cancelar pedido
                </Button>
              )}
              <Button type="submit" variant={modo === "editar" ? "secondary" : "default"} disabled={form.formState.isSubmitting}>
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
      ) : (
        pedido && (
          <>
            <Card>
              <CardTitle>{pedido.clienteNome}</CardTitle>
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
                  {pedido.itens.map((item) => (
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

            <PedidoResumoValores
              subtotal={pedido.subtotal}
              valorDesconto={pedido.valorDesconto}
              valorFrete={pedido.valorFrete}
              valorTotal={pedido.valorTotal}
            />

            <Card>
              <CardTitle>Entrega e pagamento</CardTitle>
              <div className="linha">
                <span>Transportadora</span>
                <span>{pedido.transportadoraNome ?? "—"}</span>
              </div>
              <div className="linha">
                <span>Condição de pagamento</span>
                <span>{pedido.condicaoPagamento}</span>
              </div>
            </Card>

            {pedido.status === "ENTREGUE" && (
              <div className="acoes">
                <Button type="button" onClick={() => setDialogVendaAberto(true)}>
                  Concluir venda
                </Button>
              </div>
            )}
          </>
        )
      )}

      {pedido && (
        <ConcluirVendaDialog open={dialogVendaAberto} onOpenChange={setDialogVendaAberto} pedidoId={pedido.id} />
      )}
    </>
  );
}
