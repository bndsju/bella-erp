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
import { categoriaDespesaApi } from "@/lib/categoriaDespesaApi";
import { ApiError } from "@/lib/api";
import { categoriaSchema, type CategoriaFormValues } from "@/lib/produtoSchema";
import type { CategoriaDespesaResponse } from "@/types/categoriaDespesa";

interface CategoriaDespesaFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  categoria: CategoriaDespesaResponse | null;
  onSalvo: () => void;
}

export function CategoriaDespesaFormDialog({ open, onOpenChange, categoria, onSalvo }: CategoriaDespesaFormDialogProps) {
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
        await categoriaDespesaApi.editar(categoria.id, dados);
        toast.success("Categoria de despesa atualizada");
      } else {
        await categoriaDespesaApi.cadastrar(dados);
        toast.success("Categoria de despesa cadastrada");
      }
      onOpenChange(false);
      onSalvo();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao salvar categoria de despesa");
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{categoria ? "Editar categoria de despesa" : "Nova categoria de despesa"}</DialogTitle>
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
                    <Input placeholder="Aluguel, Energia, Internet..." {...field} />
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
