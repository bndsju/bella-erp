import { useState, type FormEvent } from "react";
import { Link, Navigate, useLocation } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ApiError } from "@/lib/api";
import { useAuth } from "@/lib/authContexto";

export function LoginPage() {
  const { usuario, carregando, entrar } = useAuth();
  const local = useLocation();
  const [login, setLogin] = useState("");
  const [senha, setSenha] = useState("");
  const [verSenha, setVerSenha] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const destino = (local.state as { de?: string } | null)?.de ?? "/";

  if (!carregando && usuario) {
    return <Navigate to={destino} replace />;
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (!login.trim() || !senha) {
      setErro("Informe o login e a senha");
      return;
    }
    setErro(null);
    setEnviando(true);
    try {
      await entrar(login.trim(), senha);
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : "Não foi possível conectar ao servidor. Tente de novo.");
      setSenha("");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-md flex-col justify-center px-4 py-10">
      <div className="mb-6 flex flex-col items-center gap-2 text-center">
        <img src="/logo-bella.png" alt="" className="h-16 w-auto" />
        <h1 className="!mb-0">Bella</h1>
        <p>Entre para continuar</p>
      </div>

      <form className="cartao space-y-4" onSubmit={enviar} noValidate>
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

        <div className="campo">
          <label htmlFor="senha">Senha</label>
          <div className="relative">
            <Input
              id="senha"
              type={verSenha ? "text" : "password"}
              autoComplete="current-password"
              className="pr-12"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
            <button
              type="button"
              className="absolute inset-y-0 right-0 flex w-11 items-center justify-center"
              aria-label={verSenha ? "Ocultar senha" : "Mostrar senha"}
              aria-pressed={verSenha}
              onClick={() => setVerSenha((v) => !v)}
            >
              {verSenha ? <EyeOff size={18} aria-hidden="true" /> : <Eye size={18} aria-hidden="true" />}
            </button>
          </div>
        </div>

        {erro && (
          <p role="alert" className="text-sm font-semibold text-erro">
            {erro}
          </p>
        )}

        <Button type="submit" className="w-full" disabled={enviando}>
          {enviando ? "Entrando..." : "Entrar"}
        </Button>
        <p className="text-center">
          <Link to="/esqueci-senha">Esqueci a senha</Link>
        </p>
      </form>
    </main>
  );
}
