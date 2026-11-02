import { useEffect, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ApiError } from "@/lib/api";
import { categoriaFluxoCaixaApi } from "@/lib/categoriaFluxoCaixaApi";
import { dataDeHoje } from "@/lib/formatos";
import { fluxoCaixaApi } from "@/lib/fluxoCaixaApi";
import { lancamentoCaixaSchema, type LancamentoCaixaFormValues } from "@/lib/fluxoCaixaSchema";
import type { CategoriaFluxoCaixaResponse } from "@/types/categoriaFluxoCaixa";
import type { MovimentacaoCaixaResponse } from "@/types/fluxoCaixa";

interface LancamentoCaixaFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  movimentacao: MovimentacaoCaixaResponse | null;
  onSalvo: () => void;
}

function valoresIniciais(movimentacao: MovimentacaoCaixaResponse | null): LancamentoCaixaFormValues {
  if (!movimentacao) {
    return {
      tipo: "ENTRADA",
      descricao: "",
      categoriaId: "",
      valor: 0,
      situacao: "PREVISTO",
      dataPrevista: dataDeHoje(),
      dataEfetiva: "",
      observacoes: "",
    };
  }
  return {
    tipo: movimentacao.tipo,
    descricao: movimentacao.descricao,
    categoriaId: movimentacao.categoriaId ?? "",
    valor: movimentacao.valor,
    situacao: movimentacao.situacao,
    dataPrevista: movimentacao.dataPrevista,
    dataEfetiva: movimentacao.dataEfetiva ?? "",
    observacoes: movimentacao.observacoes ?? "",
  };
}

export function LancamentoCaixaFormDialog({ open, onOpenChange, movimentacao, onSalvo }: LancamentoCaixaFormDialogProps) {
  const [categorias, setCategorias] = useState<CategoriaFluxoCaixaResponse[]>([]);
  const form = useForm<LancamentoCaixaFormValues>({
    resolver: zodResolver(lancamentoCaixaSchema),
    defaultValues: valoresIniciais(null),
  });
  const situacao = form.watch("situacao");
  const edicao = movimentacao !== null;

  useEffect(() => {
    if (!open) return;
    form.reset(valoresIniciais(movimentacao));
    categoriaFluxoCaixaApi.listar("ATIVO").then(setCategorias).catch(() => {});
  }, [open, movimentacao, form]);

  async function onSubmit(dados: LancamentoCaixaFormValues) {
    const payload = {
      tipo: dados.tipo,
      descricao: dados.descricao.trim(),
      categoriaId: dados.categoriaId,
      valor: dados.valor,
      situacao: dados.situacao,
      dataPrevista: dados.dataPrevista,
      dataEfetiva: dados.situacao === "REALIZADO" ? dados.dataEfetiva : undefined,
      observacoes: dados.observacoes.trim() || undefined,
    };
    try {
      if (movimentacao) {
        await fluxoCaixaApi.editar(movimentacao.id, payload);
        toast.success("Lançamento atualizado");
      } else {
        await fluxoCaixaApi.lancar(payload);
        toast.success("Lançamento registrado");
      }
      onOpenChange(false);
      onSalvo();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao salvar o lançamento");
    }
  }

  const categoriaAtualForaDaLista =
    movimentacao?.categoriaId && !categorias.some((categoria) => categoria.id === movimentacao.categoriaId);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{edicao ? "Editar lançamento" : "Novo lançamento"}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="tipo"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Tipo</FormLabel>
                    <Select value={field.value} onValueChange={field.onChange}>
                      <FormControl>
                        <SelectTrigger className="w-full">
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="ENTRADA">Entrada</SelectItem>
                        <SelectItem value="SAIDA">Saída</SelectItem>
                      </SelectContent>
                    </Select>
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
                          <SelectValue placeholder="Selecione" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {categorias.map((categoria) => (
                          <SelectItem key={categoria.id} value={categoria.id}>
                            {categoria.nome}
                          </SelectItem>
                        ))}
                        {categoriaAtualForaDaLista && movimentacao?.categoriaId && (
                          <SelectItem value={movimentacao.categoriaId}>{movimentacao.categoriaNome}</SelectItem>
                        )}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            <FormField
              control={form.control}
              name="descricao"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Descrição</FormLabel>
                  <FormControl>
                    <Input placeholder="Aporte, retirada, taxa bancária..." {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="valor"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Valor</FormLabel>
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
                name="situacao"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Situação</FormLabel>
                    <Select value={field.value} onValueChange={field.onChange} disabled={edicao}>
                      <FormControl>
                        <SelectTrigger className="w-full">
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="PREVISTO">Previsto</SelectItem>
                        <SelectItem value="REALIZADO">Realizado</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="dataPrevista"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Data prevista</FormLabel>
                    <FormControl>
                      <Input type="date" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              {situacao === "REALIZADO" && (
                <FormField
                  control={form.control}
                  name="dataEfetiva"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Data de realização</FormLabel>
                      <FormControl>
                        <Input type="date" max={dataDeHoje()} {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              )}
            </div>
            <FormField
              control={form.control}
              name="observacoes"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Observações</FormLabel>
                  <FormControl>
                    <Textarea rows={2} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={form.formState.isSubmitting}>
                Salvar
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
