import { Navigate } from "react-router-dom";
import { TrocarSenhaForm } from "@/components/auth/TrocarSenhaForm";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/lib/authContexto";

/** Tela exclusiva de quem entrou com senha provisória: nada do sistema abre antes de definir uma senha própria. */
export function TrocarSenhaPage() {
  const { usuario, sair } = useAuth();
  if (usuario && !usuario.senhaProvisoria) {
    return <Navigate to="/" replace />;
  }

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-md flex-col justify-center px-4 py-10">
      <div className="mb-6 flex flex-col items-center gap-2 text-center">
        <img src="/logo-bella.png" alt="" className="h-16 w-auto" />
        <h1 className="!mb-0">Defina sua nova senha</h1>
        <p>
          Você entrou com uma senha provisória. Escolha uma senha só sua para continuar; depois é só entrar de novo.
        </p>
      </div>

      <div className="cartao">
        <TrocarSenhaForm rotuloSenhaAtual="Senha provisória" />
        <Button type="button" variant="outline" className="mt-4" onClick={sair}>
          Sair
        </Button>
      </div>
    </main>
  );
}
