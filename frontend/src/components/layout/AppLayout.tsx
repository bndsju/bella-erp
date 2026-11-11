import { useCallback, useMemo, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { DropdownMenu } from "radix-ui";
import { ChevronDown, CircleUserRound, LogOut, Menu, X } from "lucide-react";
import { useAuth } from "@/lib/authContexto";
import { ROTULO_PERFIL } from "@/lib/permissoes";
import { cn } from "@/lib/utils";
import { EncaixesContexto, type EncaixesLayout } from "./CabecalhoPagina";
import { atalhosCelular, estaAtivo, grupoAtivo, inicio, menuParaPerfil, type GrupoMenu } from "./navegacao";

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
  const { usuario, sair } = useAuth();
  if (!aberta || !usuario) return null;
  const menu = menuParaPerfil(usuario.perfil);
  const todos: GrupoMenu[] = [{ titulo: "Geral", itens: [inicio] }, ...menu.grupos, menu.mais];
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
        <div className="mb-3">
          <p className="rotulo mb-1 px-3.5 text-auxiliar">
            {usuario.nome} · {ROTULO_PERFIL[usuario.perfil]}
          </p>
          <button
            type="button"
            onClick={() => {
              aoFechar();
              sair();
            }}
            className="flex min-h-11 w-full items-center gap-3 rounded-full px-3.5 font-semibold hover:bg-selecionado"
          >
            <LogOut size={18} strokeWidth={1.75} aria-hidden="true" />
            Sair
          </button>
        </div>
      </div>
    </div>
  );
}

function MenuDoUsuario() {
  const { usuario, sair } = useAuth();
  if (!usuario) return null;
  return (
    <DropdownMenu.Root>
      <DropdownMenu.Trigger className="menu__item" aria-label={`Conta de ${usuario.nome}`}>
        <CircleUserRound size={20} strokeWidth={1.75} aria-hidden="true" />
        <span className="hidden md:inline">{usuario.nome.split(" ")[0]}</span>
        <ChevronDown size={16} strokeWidth={1.75} aria-hidden="true" />
      </DropdownMenu.Trigger>
      <DropdownMenu.Portal>
        <DropdownMenu.Content align="end" sideOffset={6} className="painel">
          <p className="rotulo px-3.5 pt-2 text-auxiliar">
            {usuario.nome} · {ROTULO_PERFIL[usuario.perfil]}
          </p>
          <DropdownMenu.Item asChild className="painel__item">
            <NavLink to="/ajustes">
              <CircleUserRound size={18} strokeWidth={1.75} aria-hidden="true" />
              Minha conta
            </NavLink>
          </DropdownMenu.Item>
          <DropdownMenu.Item className="painel__item" onSelect={sair}>
            <LogOut size={18} strokeWidth={1.75} aria-hidden="true" />
            Sair
          </DropdownMenu.Item>
        </DropdownMenu.Content>
      </DropdownMenu.Portal>
    </DropdownMenu.Root>
  );
}

export function AppLayout() {
  const { usuario } = useAuth();
  const menu = menuParaPerfil(usuario?.perfil ?? "VENDEDOR");
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
          {menu.grupos.map((grupo) => (
            <MenuDoGrupo key={grupo.titulo} grupo={grupo} caminho={pathname} />
          ))}
          <MenuDoGrupo grupo={menu.mais} caminho={pathname} />
        </nav>
        <MenuDoUsuario />
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
