import { useState } from "react";
import { Copy } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { ApiError } from "@/lib/api";
import { usuarioApi } from "@/lib/usuarioApi";
import type { UsuarioResponse } from "@/types/usuario";

interface Props {
  usuario: UsuarioResponse | null;
  onOpenChange: (open: boolean) => void;
  /** Chamado ao fechar depois de gerar, para a lista refletir que o pedido foi atendido. */
  onGerada: () => void;
}

/** Gera a senha provisória e a mostra uma única vez: depois que o diálogo fecha, ela não pode ser vista de novo. */
export function SenhaProvisoriaDialog({ usuario, onOpenChange, onGerada }: Props) {
  const [senha, setSenha] = useState<string | null>(null);
  const [gerando, setGerando] = useState(false);

  async function gerar() {
    if (!usuario) return;
    setGerando(true);
    try {
      const resposta = await usuarioApi.gerarSenhaProvisoria(usuario.id);
      setSenha(resposta.senhaProvisoria);
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Não foi possível gerar a senha provisória");
    } finally {
      setGerando(false);
    }
  }

  async function copiar() {
    if (!senha) return;
    try {
      await navigator.clipboard.writeText(senha);
      toast.success("Senha copiada");
    } catch {
      toast.error("Não foi possível copiar; selecione e copie manualmente");
    }
  }

  function fechar() {
    const gerou = senha !== null;
    setSenha(null);
    onOpenChange(false);
    if (gerou) onGerada();
  }

  return (
    <Dialog open={usuario !== null} onOpenChange={(aberto) => !aberto && fechar()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Senha provisória</DialogTitle>
        </DialogHeader>

        {senha === null ? (
          <>
            <p>
              Gerar uma senha provisória para <strong>{usuario?.nome}</strong> ({usuario?.login})? A senha atual
              deixa de valer e quem estiver com a sessão aberta é desconectado.
            </p>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={fechar}>
                Cancelar
              </Button>
              <Button type="button" onClick={gerar} disabled={gerando}>
                {gerando ? "Gerando..." : "Gerar senha provisória"}
              </Button>
            </DialogFooter>
          </>
        ) : (
          <>
            <p>
              Senha de <strong>{usuario?.nome}</strong>. Ela aparece só agora: passe para a pessoa por um canal seguro.
              No primeiro acesso ela será obrigada a escolher uma senha própria.
            </p>
            <div className="flex items-center gap-2">
              <code
                className="flex-1 rounded-xl border px-3 py-2 text-lg font-bold tracking-wider select-all"
                aria-label="Senha provisória"
              >
                {senha}
              </code>
              <Button type="button" variant="secondary" onClick={copiar} aria-label="Copiar senha">
                <Copy size={16} aria-hidden="true" />
                Copiar
              </Button>
            </div>
            <DialogFooter>
              <Button type="button" onClick={fechar}>
                Fechar
              </Button>
            </DialogFooter>
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}
