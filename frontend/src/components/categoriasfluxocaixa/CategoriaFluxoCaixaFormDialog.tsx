import { useEffect } from "react";
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
import { categoriaFluxoCaixaApi } from "@/lib/categoriaFluxoCaixaApi";
import { ApiError } from "@/lib/api";
import { categoriaSchema, type CategoriaFormValues } from "@/lib/produtoSchema";
import type { CategoriaFluxoCaixaResponse } from "@/types/categoriaFluxoCaixa";

interface CategoriaFluxoCaixaFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  categoria: CategoriaFluxoCaixaResponse | null;
  onSalvo: () => void;
}

export function CategoriaFluxoCaixaFormDialog({ open, onOpenChange, categoria, onSalvo }: CategoriaFluxoCaixaFormDialogProps) {
  const form = useForm<CategoriaFormValues>({
    resolver: zodResolver(categoriaSchema),
    defaultValues: { nome: "" },
  });

  useEffect(() => {
    if (open) {
      form.reset({ nome: categoria?.nome ?? "" });
    }
  }, [open, categoria, form]);

  async function onSubmit(dados: CategoriaFormValues) {
    try {
      if (categoria) {
        await categoriaFluxoCaixaApi.editar(categoria.id, dados);
        toast.success("Categoria de fluxo de caixa atualizada");
      } else {
        await categoriaFluxoCaixaApi.cadastrar(dados);
        toast.success("Categoria de fluxo de caixa cadastrada");
      }
      onOpenChange(false);
      onSalvo();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao salvar categoria de fluxo de caixa");
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{categoria ? "Editar categoria de fluxo de caixa" : "Nova categoria de fluxo de caixa"}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="nome"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Nome</FormLabel>
                  <FormControl>
                    <Input placeholder="Vendas, Aluguel, Impostos..." {...field} />
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
