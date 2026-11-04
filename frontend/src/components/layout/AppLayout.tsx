import { useCallback, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { DropdownMenu } from "radix-ui";
import { ChevronDown, Ellipsis, Lightbulb, Settings, X } from "lucide-react";
import { cn } from "@/lib/utils";
import { EncaixesContexto, type EncaixesLayout } from "./CabecalhoPagina";
import { atalhosCelular, estaAtivo, grupoAtivo, grupos, inicio, type ItemMenu } from "./navegacao";

const PILULA = "inline-flex h-11 items-center gap-2 rounded-full px-4 text-sm font-bold whitespace-nowrap transition-colors outline-none";
const PILULA_ATIVA = "bg-marca text-marca-texto";
const PILULA_INATIVA = "text-sobre-escuro/85 hover:bg-white/10 hover:text-sobre-escuro focus-visible:bg-white/10";

function Marca() {
  return (
    <Link to="/" className="flex items-center gap-2.5 rounded-full outline-none focus-visible:ring-2 focus-visible:ring-marca" aria-label="Bella, ir para o início">
      <Lightbulb className="size-7 text-marca" strokeWidth={2.2} aria-hidden="true" />
      <span className="text-2xl font-extrabold tracking-tight text-sobre-escuro">Bella</span>
    </Link>
  );
}

function ItemInicio() {
  return (
    <NavLink to={inicio.to} end className={({ isActive }) => cn(PILULA, isActive ? PILULA_ATIVA : PILULA_INATIVA)}>
      {inicio.label}
    </NavLink>
  );
}

function MenuDoGrupo({ titulo, itens, ativo }: { titulo: string; itens: ItemMenu[]; ativo: boolean }) {
  return (
    <DropdownMenu.Root>
      <DropdownMenu.Trigger className={cn(PILULA, ativo ? PILULA_ATIVA : PILULA_INATIVA, "data-[state=open]:bg-white/10 data-[state=open]:text-sobre-escuro", ativo && "data-[state=open]:bg-marca data-[state=open]:text-marca-texto")}>
        {titulo}
        <ChevronDown className="size-4" aria-hidden="true" />
      </DropdownMenu.Trigger>
      <DropdownMenu.Portal>
        <DropdownMenu.Content
          align="start"
          sideOffset={8}
          className="z-50 min-w-60 rounded-2xl border border-borda bg-popover p-1.5 text-popover-foreground"
        >
          {itens.map((item) => {
            const Icone = item.icon;
            return (
              <DropdownMenu.Item key={item.to} asChild>
                <NavLink
                  to={item.to}
                  className={({ isActive }) =>
                    cn(
                      "flex cursor-pointer items-center gap-3 rounded-full px-3.5 py-2.5 text-sm font-semibold outline-none transition-colors",
                      isActive
                        ? "bg-marca text-marca-texto"
                        : "data-[highlighted]:bg-marca-hover data-[highlighted]:text-sobre-escuro",
                    )
                  }
                >
                  <Icone className="size-4 shrink-0" aria-hidden="true" />
                  {item.label}
                </NavLink>
              </DropdownMenu.Item>
            );
          })}
        </DropdownMenu.Content>
      </DropdownMenu.Portal>
    </DropdownMenu.Root>
  );
}

function BarraInferior({ aoAbrirMais, caminho }: { aoAbrirMais: () => void; caminho: string }) {
  const maisAtivo = !atalhosCelular.some((item) => estaAtivo(item, caminho));
  return (
    <nav
      aria-label="Menu principal"
      className="fixed inset-x-0 bottom-0 z-40 grid grid-cols-5 border-t border-white/10 bg-barra px-2 pt-2 pb-[max(0.5rem,env(safe-area-inset-bottom))] lg:hidden"
    >
      {atalhosCelular.map((item) => {
        const Icone = item.icon;
        return (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/"}
            className={({ isActive }) =>
              cn(
                "flex flex-col items-center gap-1 rounded-2xl px-1 py-1.5 text-[13px] font-bold outline-none",
                isActive ? "text-marca" : "text-sobre-escuro/80",
              )
            }
          >
            <Icone className="size-5" aria-hidden="true" />
            {item.label}
          </NavLink>
        );
      })}
      <button
        type="button"
        onClick={aoAbrirMais}
        className={cn(
          "flex flex-col items-center gap-1 rounded-2xl px-1 py-1.5 text-[13px] font-bold outline-none",
          maisAtivo ? "text-marca" : "text-sobre-escuro/80",
        )}
      >
        <Ellipsis className="size-5" aria-hidden="true" />
        Mais
      </button>
    </nav>
  );
}

function FolhaMais({ aberta, aoFechar }: { aberta: boolean; aoFechar: () => void }) {
  if (!aberta) return null;
  return (
    <div className="fixed inset-0 z-50 lg:hidden" role="dialog" aria-modal="true" aria-label="Todos os módulos">
      <div className="absolute inset-0 bg-black/50" onClick={aoFechar} aria-hidden="true" />
      <div className="absolute inset-x-0 bottom-0 max-h-[85vh] overflow-y-auto rounded-t-cartao bg-cartao p-5 pb-8">
        <div className="mb-3 flex items-center justify-between">
          <p className="text-lg font-extrabold text-titulo">Todos os módulos</p>
          <button
            type="button"
            onClick={aoFechar}
            className="rounded-full p-2 text-texto hover:bg-muted"
            aria-label="Fechar"
          >
            <X className="size-5" />
          </button>
        </div>
        <NavLink
          to={inicio.to}
          end
          onClick={aoFechar}
          className={({ isActive }) =>
            cn("mb-3 flex h-11 items-center gap-3 rounded-full px-4 text-sm font-bold", isActive ? "bg-marca text-marca-texto" : "hover:bg-muted")
          }
        >
          <inicio.icon className="size-4" aria-hidden="true" />
          {inicio.label}
        </NavLink>
        {grupos.map((grupo) => (
          <div key={grupo.titulo} className="mb-3">
            <p className="px-4 pb-1 text-[13px] font-semibold text-auxiliar">{grupo.titulo}</p>
            {grupo.itens.map((item) => {
              const Icone = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={aoFechar}
                  className={({ isActive }) =>
                    cn(
                      "flex h-11 items-center gap-3 rounded-full px-4 text-sm font-bold",
                      isActive ? "bg-marca text-marca-texto" : "hover:bg-muted",
                    )
                  }
                >
                  <Icone className="size-4" aria-hidden="true" />
                  {item.label}
                </NavLink>
              );
            })}
          </div>
        ))}
        <Link
          to="/ajustes"
          onClick={aoFechar}
          className="flex h-11 items-center gap-3 rounded-full px-4 text-sm font-bold hover:bg-muted"
        >
          <Settings className="size-4" aria-hidden="true" />
          Ajustes
        </Link>
      </div>
    </div>
  );
}

export function AppLayout() {
  const { pathname } = useLocation();
  const [encaixes, setEncaixes] = useState<EncaixesLayout>({ titulo: null, frase: null, acao: null });
  const [maisAberto, setMaisAberto] = useState(false);

  // Funções estáveis: um ref recriado a cada render provocaria um laço de atualizações.
  const refAcao = useCallback((no: HTMLElement | null) => setEncaixes((atual) => ({ ...atual, acao: no })), []);
  const refTitulo = useCallback((no: HTMLElement | null) => setEncaixes((atual) => ({ ...atual, titulo: no })), []);
  const refFrase = useCallback((no: HTMLElement | null) => setEncaixes((atual) => ({ ...atual, frase: no })), []);

  return (
    <EncaixesContexto.Provider value={encaixes}>
      <div className="min-h-screen bg-pagina text-texto">
        <header className="sticky top-0 z-40 flex items-center gap-4 bg-barra px-4 py-3 sm:px-10">
          <Marca />
          <nav aria-label="Menu principal" className="mx-auto hidden items-center gap-1 lg:flex">
            <ItemInicio />
            {grupos.map((grupo) => (
              <MenuDoGrupo key={grupo.titulo} titulo={grupo.titulo} itens={grupo.itens} ativo={grupoAtivo(grupo, pathname)} />
            ))}
          </nav>
          <div className="ml-auto flex items-center gap-2 lg:ml-0">
            {/* ação principal do módulo (botão branco com texto preto), projetada pela página */}
            <div
              ref={refAcao}
              className="flex items-center gap-2 [&_a]:bg-sobre-escuro [&_a]:text-marca-texto [&_a:hover]:bg-marca-hover [&_a:hover]:text-sobre-escuro [&_button]:bg-sobre-escuro [&_button]:text-marca-texto [&_button:hover]:bg-marca-hover [&_button:hover]:text-sobre-escuro"
            />
            <Link
              to="/ajustes"
              aria-label="Ajustes"
              className="hidden size-11 items-center justify-center rounded-full text-sobre-escuro/85 outline-none hover:bg-white/10 hover:text-sobre-escuro focus-visible:bg-white/10 lg:inline-flex"
            >
              <Settings className="size-5" aria-hidden="true" />
            </Link>
          </div>
        </header>

        <section className="bg-marca px-4 pt-10 pb-28 text-marca-texto sm:px-10">
          <div className="mx-auto max-w-[1240px]">
            <h1
              ref={refTitulo}
              className="text-3xl font-extrabold tracking-tight"
            />
            <p
              ref={refFrase}
              className="mt-1 text-lg font-medium empty:hidden"
            />
          </div>
        </section>

        <main className="relative mx-auto -mt-[84px] w-full max-w-[1240px] px-4 pb-28 sm:px-10 lg:pb-12">
          <Outlet />
        </main>

        <BarraInferior aoAbrirMais={() => setMaisAberto(true)} caminho={pathname} />
        <FolhaMais aberta={maisAberto} aoFechar={() => setMaisAberto(false)} />
      </div>
    </EncaixesContexto.Provider>
  );
}
