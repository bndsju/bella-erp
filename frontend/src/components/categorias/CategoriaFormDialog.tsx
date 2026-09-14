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
import { categoriaApi } from "@/lib/categoriaApi";
import { ApiError } from "@/lib/api";
import { categoriaSchema, type CategoriaFormValues } from "@/lib/produtoSchema";
import type { CategoriaResponse } from "@/types/categoria";

interface CategoriaFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  categoria: CategoriaResponse | null;
  onSalvo: () => void;
}

export function CategoriaFormDialog({ open, onOpenChange, categoria, onSalvo }: CategoriaFormDialogProps) {
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
        await categoriaApi.editar(categoria.id, dados);
        toast.success("Categoria atualizada");
      } else {
        await categoriaApi.cadastrar(dados);
        toast.success("Categoria cadastrada");
      }
      onOpenChange(false);
      onSalvo();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao salvar categoria");
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{categoria ? "Editar categoria" : "Nova categoria"}</DialogTitle>
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
                    <Input placeholder="Bebidas" {...field} />
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
