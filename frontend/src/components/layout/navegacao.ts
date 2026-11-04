import {
  ArrowDownToLine,
  ArrowUpFromLine,
  Boxes,
  Building2,
  ClipboardList,
  FileText,
  Landmark,
  Layers,
  LayoutDashboard,
  House,
  Package,
  Settings,
  Receipt,
  Ruler,
  ShoppingCart,
  Tags,
  Truck,
  Users,
  Wallet,
  type LucideIcon,
} from "lucide-react";

export interface ItemMenu {
  to: string;
  label: string;
  icon: LucideIcon;
}

export interface GrupoMenu {
  titulo: string;
  itens: ItemMenu[];
}

/** Início fica solto no menu; os demais módulos seguem agrupados, como já eram. Nenhum módulo fica de fora. */
export const inicio: ItemMenu = { to: "/", label: "Início", icon: LayoutDashboard };

export const grupos: GrupoMenu[] = [
  {
    titulo: "Cadastros",
    itens: [
      { to: "/clientes", label: "Clientes", icon: Users },
      { to: "/produtos", label: "Produtos", icon: Package },
      { to: "/categorias", label: "Categorias de produto", icon: Tags },
      { to: "/unidades-medida", label: "Unidades de medida", icon: Ruler },
      { to: "/fornecedores", label: "Fornecedores", icon: Building2 },
      { to: "/transportadoras", label: "Transportadoras", icon: Truck },
    ],
  },
  {
    titulo: "Comercial",
    itens: [
      { to: "/orcamentos", label: "Orçamentos", icon: FileText },
      { to: "/pedidos", label: "Pedidos", icon: ClipboardList },
      { to: "/vendas", label: "Vendas", icon: Receipt },
    ],
  },
  {
    titulo: "Suprimentos",
    itens: [
      { to: "/compras", label: "Compras", icon: ShoppingCart },
      { to: "/estoque", label: "Estoque", icon: Boxes },
    ],
  },
  {
    titulo: "Financeiro",
    itens: [
      { to: "/contas-receber", label: "Contas a receber", icon: ArrowDownToLine },
      { to: "/contas-pagar", label: "Contas a pagar", icon: ArrowUpFromLine },
      { to: "/fluxo-caixa", label: "Fluxo de caixa", icon: Wallet },
      { to: "/categorias-despesa", label: "Categorias de despesa", icon: Layers },
      { to: "/categorias-fluxo-caixa", label: "Categorias de caixa", icon: Landmark },
    ],
  },
];

/** Menu "Mais" do topo: o que não cabe nos grupos (hoje, só os ajustes). */
export const mais: GrupoMenu = {
  titulo: "Mais",
  itens: [{ to: "/ajustes", label: "Ajustes", icon: Settings }],
};

/** Itens fixos da barra inferior no celular (como no arquivo-mestre); o resto fica em "Mais". */
export const atalhosCelular: ItemMenu[] = [
  { to: "/", label: "Início", icon: House },
  { to: "/vendas", label: "Vendas", icon: Receipt },
  { to: "/estoque", label: "Estoque", icon: Boxes },
  { to: "/clientes", label: "Clientes", icon: Users },
];

export function estaAtivo(item: ItemMenu, caminho: string): boolean {
  return item.to === "/" ? caminho === "/" : caminho === item.to || caminho.startsWith(`${item.to}/`);
}

export function grupoAtivo(grupo: GrupoMenu, caminho: string): boolean {
  return grupo.itens.some((item) => estaAtivo(item, caminho));
}
