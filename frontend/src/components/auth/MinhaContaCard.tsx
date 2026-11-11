import { Card, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/lib/authContexto";
import { ROTULO_PERFIL } from "@/lib/permissoes";
import { TrocarSenhaForm } from "./TrocarSenhaForm";

/** Dados de quem está logado e troca da própria senha. */
export function MinhaContaCard() {
  const { usuario } = useAuth();
  if (!usuario) return null;

  return (
    <Card>
      <CardTitle>Minha conta</CardTitle>
      <p className="mb-3">
        <strong>{usuario.nome}</strong> · login {usuario.login} · {ROTULO_PERFIL[usuario.perfil]}
      </p>
      <TrocarSenhaForm />
    </Card>
  );
}
