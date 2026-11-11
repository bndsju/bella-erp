import { Navigate, Outlet, useLocation } from "react-router-dom";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/lib/authContexto";
import { podeAcessar } from "@/lib/permissoes";
import { SemAcesso } from "./SemAcesso";

/** Só deixa passar quem está logado; senão manda para o login e, depois dele, devolve para onde a pessoa queria ir. */
export function RotaProtegida() {
  const { usuario, carregando } = useAuth();
  const local = useLocation();

  if (carregando) {
    return (
      <div className="mx-auto mt-24 w-full max-w-sm space-y-3 px-4" aria-busy="true" aria-label="Carregando">
        <Skeleton className="h-8 w-40" />
        <Skeleton className="h-24 w-full" />
      </div>
    );
  }
  if (!usuario) {
    return <Navigate to="/login" replace state={{ de: `${local.pathname}${local.search}` }} />;
  }
  // senha provisória: só a tela de troca abre, até a pessoa definir uma senha própria
  if (usuario.senhaProvisoria && local.pathname !== "/trocar-senha") {
    return <Navigate to="/trocar-senha" replace />;
  }
  return <Outlet />;
}

/** Dentro do layout: se o perfil não pode abrir a área, mostra o aviso em vez da tela. */
export function RotaPorPerfil() {
  const { usuario } = useAuth();
  const { pathname } = useLocation();
  if (usuario && !podeAcessar(usuario.perfil, pathname)) {
    return <SemAcesso />;
  }
  return <Outlet />;
}
