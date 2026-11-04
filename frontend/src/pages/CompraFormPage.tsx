import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardTitle } from "@/components/ui/card";
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
import { CompraItensField } from "@/components/compras/CompraItensField";
import { CompraResumoValores } from "@/components/compras/CompraResumoValores";
import { CompraStatusBadge } from "@/components/compras/CompraStatusBadge";
import { CancelarCompraDialog } from "@/components/compras/CancelarCompraDialog";
import { ReceberCompraDialog } from "@/components/compras/ReceberCompraDialog";
import { compraApi } from "@/lib/compraApi";
import { fornecedorApi } from "@/lib/fornecedorApi";
import { produtoApi } from "@/lib/produtoApi";
import { ApiError } from "@/lib/api";
import { compraSchema, type CompraFormValues } from "@/lib/compraSchema";
import type { CompraResponse } from "@/types/compra";
import type { FornecedorResponse } from "@/types/fornecedor";
import type { ProdutoResponse } from "@/types/produto";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function formatarData(data: string | null): string {
  return data ? new Date(`${data}T00:00:00`).toLocaleDateString("pt-BR") : "—";
}

export function CompraFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const modo = id ? "editar" : "novo";

  const [carregando, setCarregando] = useState(modo === "editar");
  const [fornecedores, setFornecedores] = useState<FornecedorResponse[]>([]);
  const [produtos, setProdutos] = useState<ProdutoResponse[]>([]);
  const [compra, setCompra] = useState<CompraResponse | null>(null);
  const [processandoAcao, setProcessandoAcao] = useState(false);
  const [dialogCancelarAberto, setDialogCancelarAberto] = useState(false);
  const [dialogReceberAberto, setDialogReceberAberto] = useState(false);

  const form = useForm<CompraFormValues>({
    resolver: zodResolver(compraSchema),
    defaultValues: {
      fornecedorId: "",
      previsaoRecebimento: "",
      itens: [],
      descontoGeral: 0,
      frete: 0,
      outrasDespesas: 0,
      condicaoPagamento: "",
      observacoes: "",
    },
  });

  function preencherFormulario(dados: CompraResponse) {
    form.reset({
      fornecedorId: dados.fornecedorId,
      previsaoRecebimento: dados.previsaoRecebimento ?? "",
      itens: dados.itens.map((item) => ({
        produtoId: item.produtoId,
        quantidade: item.quantidade,
        custoUnitario: item.custoUnitario,
        desconto: item.desconto,
      })),
      descontoGeral: dados.descontoGeral,
      frete: dados.frete,
      outrasDespesas: dados.outrasDespesas,
      condicaoPagamento: dados.condicaoPagamento,
      observacoes: dados.observacoes ?? "",
    });
  }

  useEffect(() => {
    fornecedorApi.listar("ATIVO").then(setFornecedores).catch(() => {});
    produtoApi.listar({ status: "ATIVO" }).then(setProdutos).catch(() => {});
  }, []);

  useEffect(() => {
    if (!id) return;

    compraApi
      .buscarPorId(id)
      .then((dados) => {
        setCompra(dados);
        preencherFormulario(dados);
      })
      .catch(() => {
        toast.error("Não foi possível carregar a compra");
        navigate("/compras");
      })
      .finally(() => setCarregando(false));
  }, [id, navigate, form]);

  const itensForm = form.watch("itens") ?? [];
  const descontoGeral = form.watch("descontoGeral") || 0;
  const frete = form.watch("frete") || 0;
  const outrasDespesas = form.watch("outrasDespesas") || 0;
  const valorProdutos = itensForm.reduce((t, item) => t + (item.quantidade || 0) * (item.custoUnitario || 0), 0);
  const descontoItens = itensForm.reduce((t, item) => t + (item.desconto || 0), 0);
  const descontoTotal = descontoItens + descontoGeral;
  const valorTotal = valorProdutos - descontoTotal + frete + outrasDespesas;

  async function onSubmit(dados: CompraFormValues) {
    const payload = {
      ...dados,
      previsaoRecebimento: dados.previsaoRecebimento || undefined,
      observacoes: dados.observacoes || undefined,
    };
    try {
      if (id) {
        const atualizada = await compraApi.editar(id, payload);
        setCompra(atualizada);
        preencherFormulario(atualizada);
        toast.success("Compra atualizada");
      } else {
        const criada = await compraApi.cadastrar(payload);
        toast.success("Compra cadastrada");
        navigate(`/compras/${criada.id}`);
      }
    } catch (erro) {
      if (erro instanceof ApiError) {
        toast.error(erro.message, { description: erro.erros?.join(", ") });
      } else {
        toast.error("Erro inesperado ao salvar a compra");
      }
    }
  }

  async function executarAcao(acao: () => Promise<CompraResponse>, mensagemSucesso: string) {
    setProcessandoAcao(true);
    try {
      const atualizada = await acao();
      setCompra(atualizada);
      preencherFormulario(atualizada);
      toast.success(mensagemSucesso);
      setDialogCancelarAberto(false);
      setDialogReceberAberto(false);
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Não foi possível concluir a ação");
    } finally {
      setProcessandoAcao(false);
    }
  }

  function confirmarCompra() {
    if (!id) return;
    if (form.formState.isDirty) {
      toast.error("Salve as alterações antes de confirmar a compra");
      return;
    }
    executarAcao(() => compraApi.confirmar(id), "Compra confirmada");
  }

  if (carregando) {
    return (
      <Card>
        <CardContent className="space-y-4">
          <Skeleton className="h-11 w-full" />
          <Skeleton className="h-11 w-full" />
          <Skeleton className="h-11 w-full" />
        </CardContent>
      </Card>
    );
  }

  const editavel = modo === "novo" || compra?.status === "RASCUNHO";

  return (
    <>
      <CabecalhoPagina
        titulo={modo === "editar" ? "Compra" : "Nova compra"}
        acao={
          <>
          {compra && <CompraStatusBadge status={compra.status} />}
          </>
        }
      />

      {editavel ? (
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="contents">
            <Card>
              <CardTitle>Fornecedor e recebimento</CardTitle>
              <div className="grade grade--2">
                  <FormField
                    control={form.control}
                    name="fornecedorId"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Fornecedor</FormLabel>
                        <Select value={field.value} onValueChange={field.onChange}>
                          <FormControl>
                            <SelectTrigger className="w-full">
                              <SelectValue placeholder="Selecione o fornecedor" />
                            </SelectTrigger>
                          </FormControl>
                          <SelectContent>
                            {fornecedores.map((fornecedor) => (
                              <SelectItem key={fornecedor.id} value={fornecedor.id}>
                                {fornecedor.nome}
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
                    name="previsaoRecebimento"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Previsão de recebimento</FormLabel>
                        <FormControl>
                          <Input type="date" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
              </div>
            </Card>

            <Card>
              <CardTitle>Itens</CardTitle>
              <CompraItensField produtos={produtos} />
            </Card>

            <Card>
              <CardTitle>Condições</CardTitle>
              <div className="grade grade--4">
                  <FormField
                    control={form.control}
                    name="descontoGeral"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Desconto geral (R$)</FormLabel>
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
                    name="frete"
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
                    name="outrasDespesas"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Outras despesas</FormLabel>
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

                <CompraResumoValores
                  valorProdutos={valorProdutos}
                  descontoTotal={descontoTotal}
                  frete={frete}
                  outrasDespesas={outrasDespesas}
                  valorTotal={valorTotal}
                />

                <div className="acoes">
                  {modo === "editar" && (
                    <Button
                      type="button"
                      variant="secondary"
                      disabled={processandoAcao}
                      onClick={() => setDialogCancelarAberto(true)}
                    >
                      Cancelar compra
                    </Button>
                  )}
                  <Button type="submit" disabled={form.formState.isSubmitting}>
                    {form.formState.isSubmitting ? "Salvando..." : "Salvar"}
                  </Button>
                  {modo === "editar" && (
                    <Button type="button" disabled={processandoAcao} onClick={confirmarCompra}>
                      Confirmar compra
                    </Button>
                  )}
                </div>
          </form>
        </Form>
      ) : (
        compra && (
          <>
            <Card>
              <CardTitle>{compra.fornecedorNome}</CardTitle>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Produto</TableHead>
                    <TableHead>Quantidade</TableHead>
                    <TableHead>Custo unitário</TableHead>
                    <TableHead>Desconto</TableHead>
                    <TableHead className="text-right">Total</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {compra.itens.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className="tabela__forte">
                        {item.produtoCodigoInterno} — {item.produtoNome}
                      </TableCell>
                      <TableCell>{item.quantidade}</TableCell>
                      <TableCell>{formatoMoeda.format(item.custoUnitario)}</TableCell>
                      <TableCell>{formatoMoeda.format(item.desconto)}</TableCell>
                      <TableCell className="text-right">{formatoMoeda.format(item.valorTotal)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Card>

            <CompraResumoValores
              valorProdutos={compra.valorProdutos}
              descontoTotal={compra.descontoTotal}
              frete={compra.frete}
              outrasDespesas={compra.outrasDespesas}
              valorTotal={compra.valorTotal}
            />

            <Card>
              <CardTitle>Datas e condições</CardTitle>
              <div className="linha">
                <span>Data da compra</span>
                <span>{formatarData(compra.dataCompra)}</span>
              </div>
              <div className="linha">
                <span>Previsão de recebimento</span>
                <span>{formatarData(compra.previsaoRecebimento)}</span>
              </div>
              <div className="linha">
                <span>Data do recebimento</span>
                <span>{formatarData(compra.dataRecebimento)}</span>
              </div>
              <div className="linha">
                <span>Condição de pagamento</span>
                <span>{compra.condicaoPagamento}</span>
              </div>
              {compra.observacoes && (
                <div className="linha">
                  <span>Observações</span>
                  <span>{compra.observacoes}</span>
                </div>
              )}
              {compra.status === "CANCELADA" && (
                <div className="linha">
                  <span>Motivo do cancelamento</span>
                  <span>{compra.motivoCancelamento}</span>
                </div>
              )}
            </Card>

            {compra.status === "CONFIRMADA" && (
              <div className="acoes">
                <Button
                  type="button"
                  variant="secondary"
                  disabled={processandoAcao}
                  onClick={() => setDialogCancelarAberto(true)}
                >
                  Cancelar compra
                </Button>
                <Button type="button" disabled={processandoAcao} onClick={() => setDialogReceberAberto(true)}>
                  Receber compra
                </Button>
              </div>
            )}
          </>
        )
      )}

      <CancelarCompraDialog
        open={dialogCancelarAberto}
        onOpenChange={setDialogCancelarAberto}
        processando={processandoAcao}
        onConfirmar={(motivo) => id && executarAcao(() => compraApi.cancelar(id, motivo), "Compra cancelada")}
      />
      <ReceberCompraDialog
        open={dialogReceberAberto}
        onOpenChange={setDialogReceberAberto}
        processando={processandoAcao}
        onConfirmar={(data) => id && executarAcao(() => compraApi.receber(id, data), "Compra recebida")}
      />
    </>
  );
}
