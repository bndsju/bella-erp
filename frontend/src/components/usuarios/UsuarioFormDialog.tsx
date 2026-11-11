import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ApiError } from "@/lib/api";
import { DESCRICAO_PERFIL, PERFIS, ROTULO_PERFIL } from "@/lib/permissoes";
import { usuarioApi } from "@/lib/usuarioApi";
import {
  TAMANHO_MINIMO_SENHA,
  usuarioCadastroSchema,
  usuarioEdicaoSchema,
  type UsuarioCadastroValues,
} from "@/lib/usuarioSchema";
import type { UsuarioResponse } from "@/types/usuario";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Nulo = cadastrar um novo usuário. */
  usuario: UsuarioResponse | null;
  onSalvo: () => void;
}

const VAZIO: UsuarioCadastroValues = { nome: "", login: "", senha: "", perfil: "VENDEDOR" };

/** Cadastro e edição. Na edição o login não muda (ele aparece na auditoria) e a senha tem ação própria. */
export function UsuarioFormDialog({ open, onOpenChange, usuario, onSalvo }: Props) {
  const edicao = usuario !== null;
  const form = useForm<UsuarioCadastroValues>({
    // na edição login e senha não existem no formulário; o schema de edição não os valida
    resolver: (valores, contexto, opcoes) =>
      zodResolver(edicao ? (usuarioEdicaoSchema as unknown as typeof usuarioCadastroSchema) : usuarioCadastroSchema)(
        valores,
        contexto,
        opcoes,
      ),
    defaultValues: VAZIO,
  });

  useEffect(() => {
    if (open) {
      form.reset(usuario ? { ...VAZIO, nome: usuario.nome, login: usuario.login, perfil: usuario.perfil } : VAZIO);
    }
  }, [open, usuario, form]);

  async function onSubmit(dados: UsuarioCadastroValues) {
    try {
      if (usuario) {
        await usuarioApi.editar(usuario.id, { nome: dados.nome.trim(), perfil: dados.perfil });
        toast.success("Usuário atualizado");
      } else {
        await usuarioApi.cadastrar({ ...dados, nome: dados.nome.trim(), login: dados.login.trim() });
        toast.success("Usuário cadastrado");
      }
      onOpenChange(false);
      onSalvo();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao salvar usuário");
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{edicao ? "Editar usuário" : "Novo usuário"}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <FormField
              control={form.control}
              name="nome"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Nome</FormLabel>
                  <FormControl>
                    <Input autoComplete="off" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="login"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Login</FormLabel>
                  <FormControl>
                    <Input autoComplete="off" autoCapitalize="none" disabled={edicao} {...field} />
                  </FormControl>
                  {!edicao && <FormDescription>Com ele a pessoa entra no sistema. Não pode ser trocado depois.</FormDescription>}
                  <FormMessage />
                </FormItem>
              )}
            />
            {!edicao && (
              <FormField
                control={form.control}
                name="senha"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Senha inicial</FormLabel>
                    <FormControl>
                      <Input type="password" autoComplete="new-password" {...field} />
                    </FormControl>
                    <FormDescription>Mínimo de {TAMANHO_MINIMO_SENHA} caracteres. A pessoa pode trocá-la em "Minha conta".</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}
            <FormField
              control={form.control}
              name="perfil"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Perfil</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {PERFIS.map((perfil) => (
                        <SelectItem key={perfil} value={perfil}>
                          {ROTULO_PERFIL[perfil]}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormDescription>{DESCRICAO_PERFIL[field.value]}</FormDescription>
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
