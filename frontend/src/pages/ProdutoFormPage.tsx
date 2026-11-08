import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardTitle } from "@/components/ui/card";
import { HistoricoAlteracoes } from "@/components/auditoria/HistoricoAlteracoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
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
import { produtoApi } from "@/lib/produtoApi";
import { categoriaApi } from "@/lib/categoriaApi";
import { unidadeMedidaApi } from "@/lib/unidadeMedidaApi";
import { ApiError } from "@/lib/api";
import { produtoCadastroSchema, type ProdutoCadastroFormValues } from "@/lib/produtoSchema";
import type { CategoriaResponse } from "@/types/categoria";
import type { UnidadeMedidaResponse } from "@/types/unidadeMedida";

export function ProdutoFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const modo = id ? "editar" : "novo";

  const [carregando, setCarregando] = useState(modo === "editar");
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [unidades, setUnidades] = useState<UnidadeMedidaResponse[]>([]);

  const form = useForm<ProdutoCadastroFormValues>({
    resolver: zodResolver(produtoCadastroSchema),
    defaultValues: {
      codigoInterno: "",
      codigoBarras: "",
      nome: "",
      descricao: "",
      categoriaId: "",
      unidadeMedidaId: "",
      precoCusto: 0,
      precoVenda: 0,
      estoqueMinimo: 0,
    },
  });

  useEffect(() => {
    categoriaApi.listar("ATIVO").then(setCategorias).catch(() => {});
    unidadeMedidaApi.listar("ATIVO").then(setUnidades).catch(() => {});
  }, []);

  useEffect(() => {
    if (!id) return;

    produtoApi
      .buscarPorId(id)
      .then((produto) => {
        form.reset({
          codigoInterno: produto.codigoInterno,
          codigoBarras: produto.codigoBarras ?? "",
          nome: produto.nome,
          descricao: produto.descricao ?? "",
          categoriaId: produto.categoria.id,
          unidadeMedidaId: produto.unidadeMedida.id,
          precoCusto: produto.precoCusto,
          precoVenda: produto.precoVenda,
          estoqueMinimo: produto.estoqueMinimo,
        });
      })
      .catch(() => {
        toast.error("Não foi possível carregar o produto");
        navigate("/produtos");
      })
      .finally(() => setCarregando(false));
  }, [id, navigate, form]);

  async function onSubmit(dados: ProdutoCadastroFormValues) {
    try {
      if (id) {
        await produtoApi.editar(id, dados);
        toast.success("Produto atualizado");
      } else {
        await produtoApi.cadastrar(dados);
        toast.success("Produto cadastrado");
      }
      navigate("/produtos");
    } catch (erro) {
      if (erro instanceof ApiError) {
        toast.error(erro.message, { description: erro.erros?.join(", ") });
      } else {
        toast.error("Erro inesperado ao salvar o produto");
      }
    }
  }

  return (
    <>
      <CabecalhoPagina
        titulo={modo === "editar" ? "Editar produto" : "Novo produto"}
        frase={modo === "editar" ? "Atualize os dados do produto." : "Preencha os dados para cadastrar."}
      />
      {carregando ? (
        <Card>
          <div className="space-y-4">
            <Skeleton className="h-11 w-full" />
            <Skeleton className="h-11 w-full" />
            <Skeleton className="h-11 w-full" />
          </div>
        </Card>
      ) : (
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="contents">
              <Card>
              <CardTitle>Identificação</CardTitle>
              <div className="grade grade--2">
                <FormField
                  control={form.control}
                  name="codigoInterno"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Código interno</FormLabel>
                      <FormControl>
                        <Input placeholder="SKU-001" disabled={modo === "editar"} {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="codigoBarras"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Código de barras</FormLabel>
                      <FormControl>
                        <Input placeholder="7891234567890" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="nome"
                  render={({ field }) => (
                    <FormItem className="col-span-2">
                      <FormLabel>Nome</FormLabel>
                      <FormControl>
                        <Input placeholder="Arroz Branco" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="categoriaId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Categoria</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger className="w-full">
                            <SelectValue placeholder="Selecione a categoria" />
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
                  name="unidadeMedidaId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Unidade de medida</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger className="w-full">
                            <SelectValue placeholder="Selecione a unidade" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {unidades.map((unidade) => (
                            <SelectItem key={unidade.id} value={unidade.id}>
                              {unidade.nome} ({unidade.sigla})
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
              <CardTitle>Preços e estoque</CardTitle>
              <div className="grade grade--3">
                <FormField
                  control={form.control}
                  name="precoCusto"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Preço de custo</FormLabel>
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
                  name="precoVenda"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Preço de venda</FormLabel>
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
                  name="estoqueMinimo"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Estoque mínimo</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="0.001"
                          min="0"
                          {...field}
                          onChange={(e) => field.onChange(e.target.valueAsNumber)}
                        />
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
                name="descricao"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Descrição</FormLabel>
                    <FormControl>
                      <Textarea rows={3} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              </Card>

              <div className="acoes">
                <Button variant="secondary" asChild>
                  <Link to="/produtos">Cancelar</Link>
                </Button>
                <Button type="submit" disabled={form.formState.isSubmitting}>
                  {form.formState.isSubmitting ? "Salvando..." : "Salvar produto"}
                </Button>
              </div>
            </form>
          </Form>
      )}

      {modo === "editar" && id && (
        <HistoricoAlteracoes modulo="PRODUTO" entidadeId={id} />
      )}
    </>
  );
}
