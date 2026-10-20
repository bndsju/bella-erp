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
import { estoqueApi } from "@/lib/estoqueApi";
import { produtoApi } from "@/lib/produtoApi";
import { ApiError } from "@/lib/api";
import { ROTULO_TIPO } from "@/lib/estoqueFormatos";
import {
  movimentacaoEstoqueSchema,
  type MovimentacaoEstoqueFormValues,
} from "@/lib/estoqueSchema";
import type { ProdutoResponse } from "@/types/produto";
import type { TipoMovimentacaoManual } from "@/types/estoque";

const TIPOS: TipoMovimentacaoManual[] = ["ENTRADA", "SAIDA", "AJUSTE_ENTRADA", "AJUSTE_SAIDA"];

interface MovimentacaoEstoqueDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  produtoIdInicial?: string;
  onSalvo: () => void;
}

export function MovimentacaoEstoqueDialog({
  open,
  onOpenChange,
  produtoIdInicial,
  onSalvo,
}: MovimentacaoEstoqueDialogProps) {
  const [produtos, setProdutos] = useState<ProdutoResponse[]>([]);

  const form = useForm<MovimentacaoEstoqueFormValues>({
    resolver: zodResolver(movimentacaoEstoqueSchema),
    defaultValues: { tipo: "ENTRADA", produtoId: "", quantidade: 1, motivo: "", observacao: "" },
  });

  useEffect(() => {
    if (!open) return;
    form.reset({
      tipo: "ENTRADA",
      produtoId: produtoIdInicial ?? "",
      quantidade: 1,
      motivo: "",
      observacao: "",
    });
    produtoApi.listar({ status: "ATIVO" }).then(setProdutos).catch(() => {});
  }, [open, produtoIdInicial, form]);

  async function onSubmit(dados: MovimentacaoEstoqueFormValues) {
    try {
      await estoqueApi.registrarMovimentacao(dados.tipo, {
        produtoId: dados.produtoId,
        quantidade: dados.quantidade,
        motivo: dados.motivo,
        observacao: dados.observacao || undefined,
      });
      toast.success("Movimentação registrada");
      onOpenChange(false);
      onSalvo();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao registrar a movimentação", {
        description: erro instanceof ApiError ? erro.erros?.join(", ") : undefined,
      });
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Registrar movimentação</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
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
                      {TIPOS.map((tipo) => (
                        <SelectItem key={tipo} value={tipo}>
                          {ROTULO_TIPO[tipo]}
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
              name="produtoId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Produto</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="Selecione o produto" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {produtos.map((produto) => (
                        <SelectItem key={produto.id} value={produto.id}>
                          {produto.codigoInterno} — {produto.nome}
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
              name="quantidade"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Quantidade</FormLabel>
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
            <FormField
              control={form.control}
              name="motivo"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Motivo</FormLabel>
                  <FormControl>
                    <Input placeholder="Compra de fornecedor, avaria, inventário..." {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="observacao"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Observação</FormLabel>
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
                {form.formState.isSubmitting ? "Salvando..." : "Registrar"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
