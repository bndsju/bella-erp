import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { zodResolver } from "@hookform/resolvers/zod";
import { useFieldArray, useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
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
import { fornecedorApi } from "@/lib/fornecedorApi";
import { categoriaDespesaApi } from "@/lib/categoriaDespesaApi";
import type { CategoriaDespesaResponse } from "@/types/categoriaDespesa";
import { contaPagarApi } from "@/lib/contaPagarApi";
import { contaPagarSchema, type ContaPagarFormValues } from "@/lib/contaPagarSchema";
import { ApiError } from "@/lib/api";
import { adicionarMeses, dataDeHoje, dividirEmParcelas, formatarMoeda } from "@/lib/formatos";
import type { FornecedorResponse } from "@/types/fornecedor";
import type { ContaPagarResponse } from "@/types/contaPagar";

const SEM_FORNECEDOR = "SEM_FORNECEDOR";

function nomeExibicaoFornecedor(fornecedor: FornecedorResponse): string {
  return fornecedor.nome;
}

export function ContaPagarFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const modoTela = id ? "editar" : "novo";

  const [carregando, setCarregando] = useState(modoTela === "editar");
  const [fornecedores, setFornecedores] = useState<FornecedorResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaDespesaResponse[]>([]);
  const [quantidadeParcelas, setQuantidadeParcelas] = useState(3);
  const [primeiroVencimento, setPrimeiroVencimento] = useState(dataDeHoje());

  const form = useForm<ContaPagarFormValues>({
    resolver: zodResolver(contaPagarSchema),
    defaultValues: {
      descricao: "",
      fornecedorId: SEM_FORNECEDOR,
      categoriaDespesaId: "",
      valorTotal: 0,
      modo: "UNICO",
      vencimento: "",
      parcelas: [],
      observacoes: "",
    },
  });
  const { fields, append, remove, replace } = useFieldArray({ control: form.control, name: "parcelas" });

  useEffect(() => {
    fornecedorApi.listar("ATIVO").then(setFornecedores).catch(() => {});
    categoriaDespesaApi.listar("ATIVO").then(setCategorias).catch(() => {});
  }, []);

  useEffect(() => {
    if (!id) return;

    contaPagarApi
      .buscarPorId(id)
      .then((conta: ContaPagarResponse) => {
        const temPagamentos = conta.parcelas.some((parcela) => parcela.pagamentos.length > 0);
        if (conta.status === "CANCELADA" || temPagamentos) {
          toast.error("Esta conta não pode mais ter valor ou parcelas alterados");
          navigate(`/contas-pagar/${id}`);
          return;
        }
        const unica = conta.parcelas.length === 1;
        form.reset({
          descricao: conta.descricao,
          fornecedorId: conta.fornecedorId ?? SEM_FORNECEDOR,
          categoriaDespesaId: conta.categoriaDespesaId,
          valorTotal: conta.valorTotal,
          modo: unica ? "UNICO" : "PARCELADO",
          vencimento: unica ? conta.parcelas[0].vencimento : "",
          parcelas: unica ? [] : conta.parcelas.map((p) => ({ valor: p.valor, vencimento: p.vencimento })),
          observacoes: conta.observacoes ?? "",
        });
      })
      .catch(() => {
        toast.error("Não foi possível carregar a conta");
        navigate("/contas-pagar");
      })
      .finally(() => setCarregando(false));
  }, [id, navigate, form]);

  const modo = form.watch("modo");
  const valorTotal = form.watch("valorTotal") || 0;
  const parcelas = form.watch("parcelas") ?? [];
  const somaParcelas = parcelas.reduce((total, parcela) => total + (parcela.valor || 0), 0);
  const somaConfere = Math.round(somaParcelas * 100) === Math.round(valorTotal * 100);

  function gerarParcelas() {
    if (!(valorTotal > 0)) {
      toast.error("Informe o valor da conta antes de gerar as parcelas");
      return;
    }
    if (!primeiroVencimento || quantidadeParcelas < 1) {
      toast.error("Informe a quantidade de parcelas e o primeiro vencimento");
      return;
    }
    const valores = dividirEmParcelas(valorTotal, quantidadeParcelas);
    replace(valores.map((valor, indice) => ({ valor, vencimento: adicionarMeses(primeiroVencimento, indice) })));
    form.clearErrors("parcelas");
  }

  async function onSubmit(dados: ContaPagarFormValues) {
    const base = {
      descricao: dados.descricao,
      fornecedorId: !dados.fornecedorId || dados.fornecedorId === SEM_FORNECEDOR ? undefined : dados.fornecedorId,
      categoriaDespesaId: dados.categoriaDespesaId,
      valorTotal: dados.valorTotal,
      observacoes: dados.observacoes || undefined,
    };
    const payload =
      dados.modo === "UNICO"
        ? { ...base, vencimento: dados.vencimento }
        : { ...base, parcelas: dados.parcelas };

    try {
      const conta = id ? await contaPagarApi.editar(id, payload) : await contaPagarApi.cadastrar(payload);
      toast.success(id ? "Conta atualizada" : "Conta cadastrada");
      navigate(`/contas-pagar/${conta.id}`);
    } catch (erro) {
      if (erro instanceof ApiError) {
        toast.error(erro.message, { description: erro.erros?.join(", ") });
      } else {
        toast.error("Erro inesperado ao salvar a conta");
      }
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

  const erroParcelas = form.formState.errors.parcelas?.message ?? form.formState.errors.parcelas?.root?.message;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-medium">{modoTela === "editar" ? "Editar conta a pagar" : "Nova conta a pagar"}</h1>

      <Card>
        <CardContent className="pt-6">
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="descricao"
                  render={({ field }) => (
                    <FormItem className="col-span-2">
                      <FormLabel>Descrição</FormLabel>
                      <FormControl>
                        <Input placeholder="Aluguel de março, conta de energia..." {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="categoriaDespesaId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Categoria de despesa</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger className="w-full">
                            <SelectValue placeholder="Selecione" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {categorias.map((categoria) => (
                            <SelectItem key={categoria.id} value={categoria.id}>
                              {categoria.nome}
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
                  name="fornecedorId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Fornecedor (opcional)</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger className="w-full">
                            <SelectValue />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value={SEM_FORNECEDOR}>Sem fornecedor</SelectItem>
                          {fornecedores.map((fornecedor) => (
                            <SelectItem key={fornecedor.id} value={fornecedor.id}>
                              {nomeExibicaoFornecedor(fornecedor)}
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
                  name="valorTotal"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Valor total</FormLabel>
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
                  name="modo"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Forma de pagamento</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger className="w-full">
                            <SelectValue />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="UNICO">Pagamento único</SelectItem>
                          <SelectItem value="PARCELADO">Parcelado</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                {modo === "UNICO" && (
                  <FormField
                    control={form.control}
                    name="vencimento"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Vencimento</FormLabel>
                        <FormControl>
                          <Input type="date" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                )}
              </div>

              {modo === "PARCELADO" && (
                <div className="space-y-4 rounded-lg border border-border p-4">
                  <div className="flex flex-wrap items-end gap-3">
                    <div className="space-y-1">
                      <label className="text-xs text-muted-foreground">Quantidade de parcelas</label>
                      <Input
                        type="number"
                        min="1"
                        className="w-32"
                        value={quantidadeParcelas}
                        onChange={(e) => setQuantidadeParcelas(Math.max(1, Math.floor(e.target.valueAsNumber) || 1))}
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="text-xs text-muted-foreground">Primeiro vencimento</label>
                      <Input
                        type="date"
                        className="w-44"
                        value={primeiroVencimento}
                        onChange={(e) => setPrimeiroVencimento(e.target.value)}
                      />
                    </div>
                    <Button type="button" variant="outline" onClick={gerarParcelas}>
                      Gerar parcelas
                    </Button>
                  </div>

                  {fields.length > 0 && (
                    <div className="space-y-2">
                      {fields.map((campo, indice) => (
                        <div key={campo.id} className="grid grid-cols-[3rem_1fr_1fr_auto] items-start gap-3">
                          <span className="pt-2 text-sm text-muted-foreground">{indice + 1}ª</span>
                          <FormField
                            control={form.control}
                            name={`parcelas.${indice}.valor`}
                            render={({ field }) => (
                              <FormItem>
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
                            name={`parcelas.${indice}.vencimento`}
                            render={({ field }) => (
                              <FormItem>
                                <FormControl>
                                  <Input type="date" {...field} />
                                </FormControl>
                                <FormMessage />
                              </FormItem>
                            )}
                          />
                          <Button type="button" variant="ghost" size="sm" onClick={() => remove(indice)}>
                            Remover
                          </Button>
                        </div>
                      ))}
                    </div>
                  )}

                  <div className="flex items-center justify-between">
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => append({ valor: 0, vencimento: "" })}
                    >
                      Adicionar parcela
                    </Button>
                    <p className={somaConfere ? "text-sm text-muted-foreground" : "text-sm font-medium text-destructive"}>
                      Soma das parcelas: {formatarMoeda(somaParcelas)} / {formatarMoeda(valorTotal)}
                    </p>
                  </div>
                  {erroParcelas && <p className="text-sm font-medium text-destructive">{erroParcelas}</p>}
                </div>
              )}

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

              <div className="flex justify-end gap-2 pt-2">
                <Button type="button" variant="outline" asChild>
                  <Link to={id ? `/contas-pagar/${id}` : "/contas-pagar"}>Cancelar</Link>
                </Button>
                <Button type="submit" disabled={form.formState.isSubmitting}>
                  {form.formState.isSubmitting ? "Salvando..." : "Salvar"}
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
}
