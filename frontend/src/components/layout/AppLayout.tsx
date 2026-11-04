import { useCallback, useMemo, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { DropdownMenu } from "radix-ui";
import { ChevronDown, Menu, X } from "lucide-react";
import { cn } from "@/lib/utils";
import { EncaixesContexto, type EncaixesLayout } from "./CabecalhoPagina";
import { atalhosCelular, estaAtivo, grupoAtivo, grupos, inicio, mais, type GrupoMenu } from "./navegacao";

function MenuDoGrupo({ grupo, caminho }: { grupo: GrupoMenu; caminho: string }) {
  const ativo = grupoAtivo(grupo, caminho);
  return (
    <DropdownMenu.Root>
      <DropdownMenu.Trigger className="menu__item" aria-current={ativo ? "page" : undefined}>
        {grupo.titulo}
        <ChevronDown size={16} strokeWidth={1.75} aria-hidden="true" />
      </DropdownMenu.Trigger>
      <DropdownMenu.Portal>
        <DropdownMenu.Content align="start" sideOffset={6} className="painel">
          {grupo.itens.map((item) => (
            <DropdownMenu.Item key={item.to} asChild className="painel__item">
              <NavLink to={item.to} end={item.to === "/"}>
                <item.icon size={18} strokeWidth={1.75} aria-hidden="true" />
                {item.label}
              </NavLink>
            </DropdownMenu.Item>
          ))}
        </DropdownMenu.Content>
      </DropdownMenu.Portal>
    </DropdownMenu.Root>
  );
}

function BarraInferior({ aoAbrirMais, caminho }: { aoAbrirMais: () => void; caminho: string }) {
  return (
    <nav className="barra-inferior" aria-label="Menu principal no celular">
      {atalhosCelular.map((item) => (
        <NavLink key={item.to} to={item.to} end={item.to === "/"} aria-current={estaAtivo(item, caminho) ? "page" : undefined}>
          <item.icon size={22} strokeWidth={1.75} aria-hidden="true" />
          {item.label}
        </NavLink>
      ))}
      <button type="button" onClick={aoAbrirMais}>
        <Menu size={22} strokeWidth={1.75} aria-hidden="true" />
        Mais
      </button>
    </nav>
  );
}

/** Todos os módulos no celular: folha que sobe da barra inferior, no mesmo cartão de 20 px. */
function FolhaMais({ aberta, aoFechar }: { aberta: boolean; aoFechar: () => void }) {
  if (!aberta) return null;
  const todos: GrupoMenu[] = [{ titulo: "Geral", itens: [inicio] }, ...grupos, mais];
  return (
    <div className="fixed inset-0 z-50 md:hidden" role="dialog" aria-modal="true" aria-label="Todos os módulos">
      <div className="janela-sobreposicao absolute inset-0" onClick={aoFechar} aria-hidden="true" />
      <div className="cartao absolute inset-x-0 bottom-0 max-h-[85vh] overflow-y-auto !rounded-b-none pb-24">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="!mb-0">Todos os módulos</h2>
          <button type="button" onClick={aoFechar} className="botao botao--fantasma botao--compacto w-9 px-0" aria-label="Fechar">
            <X size={20} />
          </button>
        </div>
        {todos.map((grupo) => (
          <div key={grupo.titulo} className="mb-3">
            <p className="rotulo mb-1 px-3.5 text-auxiliar">{grupo.titulo}</p>
            {grupo.itens.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === "/"}
                onClick={aoFechar}
                className={({ isActive }) =>
                  cn(
                    "flex min-h-11 items-center gap-3 rounded-full px-3.5 font-semibold",
                    isActive ? "bg-marca font-bold text-marca-texto" : "hover:bg-selecionado",
                  )
                }
              >
                <item.icon size={18} strokeWidth={1.75} aria-hidden="true" />
                {item.label}
              </NavLink>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

export function AppLayout() {
  const { pathname } = useLocation();
  const [titulo, setTitulo] = useState<HTMLElement | null>(null);
  const [frase, setFrase] = useState<HTMLElement | null>(null);
  const [acao, setAcao] = useState<HTMLElement | null>(null);
  const [faixaBaixa, setFaixaBaixa] = useState(true);
  const [maisAberto, setMaisAberto] = useState(false);

  const definirFaixaBaixa = useCallback((baixa: boolean) => setFaixaBaixa(baixa), []);
  const encaixes = useMemo<EncaixesLayout>(
    () => ({ titulo, frase, acao, definirFaixaBaixa }),
    [titulo, frase, acao, definirFaixaBaixa],
  );

  return (
    <EncaixesContexto.Provider value={encaixes}>
      <header className="topo">
        <Link className="marca" to="/" aria-label="Bella, ir para o início">
          <img src="/logo-bella.png" alt="" />
          Bella
        </Link>
        <nav className="menu" aria-label="Menu principal">
          <NavLink className="menu__item" to={inicio.to} end>
            {inicio.label}
          </NavLink>
          {grupos.map((grupo) => (
            <MenuDoGrupo key={grupo.titulo} grupo={grupo} caminho={pathname} />
          ))}
          <MenuDoGrupo grupo={mais} caminho={pathname} />
        </nav>
      </header>

      <div className={cn("faixa", faixaBaixa && "faixa--baixa")}>
        <div className="faixa__interna">
          <div>
            <h1 ref={setTitulo} />
            <p ref={setFrase} className="empty:hidden" />
          </div>
          <div ref={setAcao} className="faixa__acao empty:hidden" />
        </div>
      </div>

      <main className={cn("conteudo", faixaBaixa && "conteudo--baixo")}>
        <Outlet />
      </main>

      <BarraInferior aoAbrirMais={() => setMaisAberto(true)} caminho={pathname} />
      <FolhaMais aberta={maisAberto} aoFechar={() => setMaisAberto(false)} />
    </EncaixesContexto.Provider>
  );
}
