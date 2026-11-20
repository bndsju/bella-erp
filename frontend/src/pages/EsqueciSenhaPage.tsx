import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ApiError } from "@/lib/api";
import { authApi } from "@/lib/authApi";

/** A pessoa só informa o login; o administrador vê o pedido em Usuários e passa uma senha provisória. */
export function EsqueciSenhaPage() {
  const [login, setLogin] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [mensagem, setMensagem] = useState<string | null>(null);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (!login.trim()) {
      setErro("Informe o seu login");
      return;
    }
    setErro(null);
    setEnviando(true);
    try {
      const resposta = await authApi.esqueciSenha(login.trim());
      setMensagem(resposta.mensagem);
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível conectar ao servidor. Tente de novo.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-md flex-col justify-center px-4 py-10">
      <div className="mb-6 flex flex-col items-center gap-2 text-center">
        <img src="/logo-bella.png" alt="" className="h-16 w-auto" />
        <h1 className="!mb-0">Esqueci a senha</h1>
        <p>Informe o seu login. O administrador do sistema recebe o pedido e passa uma senha provisória para você.</p>
      </div>

      <div className="cartao space-y-4">
        {mensagem ? (
          <p role="status" className="font-semibold">
            {mensagem}
          </p>
        ) : (
          <form className="space-y-4" onSubmit={enviar} noValidate>
            <div className="campo">
              <label htmlFor="login">Login</label>
              <Input
                id="login"
                autoComplete="username"
                autoCapitalize="none"
                autoFocus
                value={login}
                onChange={(e) => setLogin(e.target.value)}
              />
            </div>
            {erro && (
              <p role="alert" className="text-sm font-semibold text-erro">
                {erro}
              </p>
            )}
            <Button type="submit" className="w-full" disabled={enviando}>
              {enviando ? "Enviando..." : "Pedir nova senha"}
            </Button>
          </form>
        )}
        <p className="text-center">
          <Link to="/login">Voltar para o login</Link>
        </p>
      </div>
    </main>
  );
}
